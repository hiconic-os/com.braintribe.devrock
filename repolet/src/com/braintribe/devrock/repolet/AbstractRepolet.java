// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.devrock.repolet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.string.DateCodec;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.event.EntityEventListener;
import com.braintribe.devrock.model.artifactory.FolderInfo;
import com.braintribe.devrock.model.repolet.event.instance.OnDeleteRequestEvent;
import com.braintribe.devrock.model.repolet.event.instance.OnGetRequestEvent;
import com.braintribe.devrock.model.repolet.event.instance.OnHeadRequestEvent;
import com.braintribe.devrock.model.repolet.event.instance.OnNotFoundEvent;
import com.braintribe.devrock.model.repolet.event.instance.OnOptionsRequestEvent;
import com.braintribe.devrock.model.repolet.event.instance.OnPutRequestEvent;
import com.braintribe.devrock.model.repolet.event.instance.RepoletInstanceEvent;
import com.braintribe.devrock.repolet.content.ContentGenerator;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.utils.archives.zip.ZipContextEntry;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 * an abstract implementation for the Repolet instances, covers commonality between the different ones
 * @author pit
 *
 */
public abstract class AbstractRepolet implements Repolet {
	private static Logger log = Logger.getLogger(AbstractRepolet.class);
	protected final String changesUrlHeader = "X-Artifact-Repository-Changes-Url";
	protected static final String RAVENHURST_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	protected static final String RAVENHURST_PARAMETER = "timestamp=";
	protected final String serverHeader = "Server";
	protected static final String MARKER_UPDATE = "update";
	protected static JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
	protected static GmSerializationOptions options = GmSerializationOptions.defaultOptions.derive().inferredRootType( FolderInfo.T).build();
	
	
	protected ZipContext archive;
	protected Map<ZipContextEntry, File> entryToCacheMap = new HashMap<ZipContextEntry, File>();
	protected String root = "content";
	protected ContentGenerator contentGenerator = new ContentGenerator();
	protected boolean verbose = false;
	protected String changesUrl;
	protected String restApiUrl;
	protected String serverIdentification;	
	protected TreeMap<Date, File> dateToResponseMap;
	protected DateCodec dateCodec = new DateCodec(RAVENHURST_DATE_FORMAT);
	protected int actualPort;
	protected File uploadContent;
	protected EntityEventListener<GenericEntity> listener;
	
	protected boolean ignoreNoMatchingHashes=true;
	protected Map<String, Map<String, String>> hashOverrides = new HashMap<>();
	protected Map<String,Boolean> hashesInHeader = new HashMap<>();
	protected Map<String,Integer> uploadReturnCodeOverrides = new HashMap<>();
	
	protected Integer overridingReponseCode;
	
	@Configurable 
	public void setUploadContent(File uploadContent) {
		this.uploadContent = uploadContent;
	}
	
	/**
	 * @param overridingReponseCode - the code to return for any exchange if not null
	 */
	@Configurable
	public void setOverridingReponseCode(Integer overridingReponseCode) {
		this.overridingReponseCode = overridingReponseCode;
	}
	
	protected void sendEvent( GenericEntity e) {
		if ( e instanceof RepoletInstanceEvent) {
			((RepoletInstanceEvent)e).setSendingRepoletName( root);
		}
		if (listener != null) {
			listener.onEvent(null, e);
		}
	}
	
	protected  File getUploadContent() {
		if (uploadContent == null) {
			throw new IllegalStateException("no upload content folder specified");
		}
		if (!uploadContent.exists()) {
			uploadContent.mkdirs();
		}
		return uploadContent;
	}
	
	@Configurable
	public void setChangesUrl(String changesUrl) {
		this.changesUrl = changesUrl;
	}
	@Override
	public String getChangesUrl() {
		return changesUrl;
	}
	
	@Configurable
	public void setServerIdentification(String serverIdentification) {
		this.serverIdentification = serverIdentification;
	}
	
	@Override
	public String getServerIdentification() {
		return serverIdentification;
	}
	@Configurable
	public void setRestApiUrl(String restApiUrl) {
		this.restApiUrl = restApiUrl;
	}
	@Override
	public String getRestApiUrl() {
		return restApiUrl;
	}
	
	
	

	@Configurable
	public void setRoot(String root) {
		this.root = root;
	}
	@Configurable
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	@Configurable @Required
	public void setActualPort(int actualPort) {
		this.actualPort = actualPort;
	}
	
	@Configurable
	public void setListener(EntityEventListener<GenericEntity> listener) {
		this.listener = listener;
	}
	
	@Configurable
	public void setHashOverrides(Map<String, Map<String, String>> hashOverrides) {
		this.hashOverrides = hashOverrides;
	}
	@Configurable
	public void setHashesInHeader(Map<String, Boolean> hashesInHeader) {
		this.hashesInHeader = hashesInHeader;
	}
	
	@Configurable
	public void setUploadReturnCodeOverrides(Map<String, Integer> uploadReturnCodeOverrides) {
		this.uploadReturnCodeOverrides = uploadReturnCodeOverrides;
	}
	
	protected void replyWith404(HttpServerExchange exchange) {
		OnNotFoundEvent nevent = OnNotFoundEvent.T.create();		
		nevent.setRequestPath(exchange.getRequestPath());
		nevent.setRequestMethod( exchange.getRequestMethod().toString());
		sendEvent(nevent);
		
		replyWithCode(exchange, 404);
	}
	protected void replyWith200(HttpServerExchange exchange) {
		replyWithCode(exchange, 200);
	}
	
	protected void replyWithCode( HttpServerExchange exchange, int code) {
		exchange.setResponseCode( code);
		exchange.endExchange();			
	}
	
	/**
	 * just dump what's in there ... 
	 * @param exchange - the {@link HttpServerExchange} to work with
	 */
	protected void replyWithDumpAnswer(HttpServerExchange exchange) {
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
		StringBuilder builder = new StringBuilder();
		for (String entry : archive.getHeaders()) {
			if (builder.length() > 0) {
				builder.append( "\n");
			}
			builder.append( entry);
		}
		exchange.getResponseSender().send( builder.toString());
	}
	
	/**
	 * @param bulkFile - the bulk file to read the hashes from 
	 * @return - a {@link Map} of hash-header-name and hash-value
	 * @throws IOException - if it can't be read
	 */
	protected Map<String, String> readBulkHashes(File bulkFile) throws IOException {
		Map<String,String> result = new HashMap<>();
		String bulkContents = IOTools.slurp(bulkFile, "US-ASCII");
		String [] hashes = bulkContents.split( "\n");
		for (String hash : hashes) {
			int pC = hash.indexOf(':');
			result.put( hash.substring(0, pC), hash.substring( pC+1));			
		}		
		return result;
	}
			
	public Map<Date, File> getDateToResponseMap() {
		return dateToResponseMap;
	}

	public void setDateToResponseMap(Map<Date, File> dateToResponseMap) {
		if (dateToResponseMap != null) {
			this.dateToResponseMap = new TreeMap<>(dateToResponseMap);
		}
		else {
			this.dateToResponseMap = new TreeMap<>();
		}
	}

	protected abstract void handleGetRequest(HttpServerExchange exchange, String path) throws Exception;
	protected abstract void handleHeadRequest(HttpServerExchange exchange, String path) throws Exception;
	protected abstract void handleOptionsRequest(HttpServerExchange exchange, String path) throws Exception;
	protected abstract void handlePutRequest(HttpServerExchange exchange, String path) throws Exception;
	protected abstract void handleDeleteRequest(HttpServerExchange exchange, String path) throws Exception;
	
	@Override
	public void handleRequest(HttpServerExchange exchange, String path) throws Exception {
		
		// if told to override the actual return code, just reply with it
		if (overridingReponseCode != null) {
			log.info("returning overriding code : " + overridingReponseCode);
			replyWithCode( exchange, overridingReponseCode);
			return;
		}
	
		
		String method = exchange.getRequestMethod().toString();		
		switch (method) {
			case "GET" :
				OnGetRequestEvent gevent = OnGetRequestEvent.T.create();
				gevent.setRequestPath(path);
				sendEvent(gevent);
				
				handleGetRequest( exchange, path);
				break;
			case "HEAD":
				OnHeadRequestEvent hevent = OnHeadRequestEvent.T.create();
				hevent.setRequestPath(path);
				sendEvent(hevent);
				
				handleHeadRequest( exchange, path);
				break;
			case "OPTIONS":
				OnOptionsRequestEvent oevent = OnOptionsRequestEvent.T.create();
				oevent.setRequestPath(path);
				sendEvent(oevent);
				
				handleOptionsRequest( exchange, path);
				break;
			case "PUT":
				OnPutRequestEvent pevent = OnPutRequestEvent.T.create();
				pevent.setRequestPath(path);
				sendEvent( pevent);
				
				handlePutRequest(exchange, path);
				break;							
			case "DELETE": {
				OnDeleteRequestEvent devent = OnDeleteRequestEvent.T.create();
				devent.setRequestPath(path);
				sendEvent(devent);
				
				handleDeleteRequest(exchange, path);
				break;
			}
			default :
				throw new UnsupportedOperationException("HttpMethod [" + method + "] is not supported by this version of Repolet");
		}
	}
	
	protected String compileChangesUrl( HttpServerExchange exchange) {
		if (changesUrl == null)
			return null;		
		String compiledChangesUrl = changesUrl.replace("${port}", "" + actualPort);
		return compiledChangesUrl;
	}
	
	protected String compileRestApiUrl( HttpServerExchange exchange) {
		if ( restApiUrl == null)
			return null;
		String compiledChangesUrl = restApiUrl.replace("${port}", "" + actualPort);
		return compiledChangesUrl;
	}
	
	/**
	 * generates hashes
	 * @param sourceFile
	 * @param digestTypes
	 * @return
	 */
	public static Map<String, String> generateHash(File sourceFile, List<String> digestTypes) {
		Map<String, String> result = new HashMap<>();
		List<MessageDigest> digests = digestTypes.stream().map( t -> {
			try {
				return MessageDigest.getInstance( t);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("no digest found for [" + t + "]");
			}
		}).collect( Collectors.toList());
	
		byte bytes [] = new byte[65536];
		try (FileInputStream in = new FileInputStream( sourceFile)) {
			int size = 0;
			while ((size = in.read(bytes)) != -1) {
				for (MessageDigest digest : digests)  {
					digest.update(bytes, 0, size);
				}				
			}
			for (int i = 0; i < digestTypes.size(); i++)  {
				MessageDigest digest = digests.get( i);
				byte [] digested = digest.digest();
				result.put( digestTypes.get(i), StringTools.toHex(digested));
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("can't read [" + sourceFile.getAbsolutePath() + "]", e);
		}
		return result;
	}
	
	public static void writeBlkFile( Map<String,String> hashes, File file) {
		try (FileWriter writer = new FileWriter(file)) {
			for (Map.Entry<String, String> entry : hashes.entrySet()) {
				writer.write( entry.getKey() + ":" + entry.getValue());
			}
		}
		catch( IOException e) {
			throw new UncheckedIOException("can't write to [" + file.getAbsolutePath() + "]", e);
		}
	}
	
	/**
	 * @param hashAlg - name of hashing algo
	 * @return - a matching {@link MessageDigest}
	 */
	protected MessageDigest createMessageDigest(String hashAlg) {
		try {
			return MessageDigest.getInstance( hashAlg);
		} catch (NoSuchAlgorithmException e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	/**
	 * checks hashes
	 * @param messageDigest
	 * @param hashAlgAndValuePair
	 * @param url
	 */
	protected void checkChecksum(MessageDigest messageDigest, Pair<String,String> hashAlgAndValuePair, String url) {						
		if (messageDigest != null) {
			String hash = StringTools.toHex( messageDigest.digest());
			if (!hash.equalsIgnoreCase( hashAlgAndValuePair.getSecond())) {
				String msg = "checksum [" + hashAlgAndValuePair.first() + "] mismatch for [" + url + "], expected [" + hashAlgAndValuePair.getSecond() + "], found [" + hash + "]";
				log.error( msg);
				if (!ignoreNoMatchingHashes) { 
					throw new IllegalStateException(msg);
				}
			}
			else {
				if (log.isDebugEnabled()) {
					String msg = "checksum [" + hashAlgAndValuePair.first() + "] match for [" + url + "], expected [" + hashAlgAndValuePair.getSecond() + "], found [" + hash + "]";
					log.debug(msg);
				}
			}
		}
		
	}

	
}
