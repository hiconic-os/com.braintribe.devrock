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
package com.braintribe.devrock.repolet.descriptive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.artifactory.FileItem;
import com.braintribe.devrock.model.artifactory.FolderInfo;
import com.braintribe.devrock.model.repolet.event.instance.OnDownloadEvent;
import com.braintribe.devrock.model.repolet.event.instance.OnHashFileUploadEvent;
import com.braintribe.devrock.model.repolet.event.instance.OnRavenhurstQueryRequestEvent;
import com.braintribe.devrock.model.repolet.event.instance.OnUploadEvent;
import com.braintribe.devrock.repolet.AbstractRepolet;
import com.braintribe.devrock.repolet.common.RepoletCommons;
import com.braintribe.devrock.repolet.descriptive.Navigator.NavigatorNode;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.file.FileBackedPipe;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

public abstract class AbstractDescriptionBasedRepolet extends AbstractRepolet {
	private static Logger log = Logger.getLogger(AbstractDescriptionBasedRepolet.class);
	private File uploadContent; 
	
	protected boolean dumpedContentAlreadyOnce = false;
	
	
	protected abstract Navigator getNavigator();
	protected abstract void processUpdate( HttpServerExchange exchange);

	@Override
	protected void handleGetRequest(HttpServerExchange exchange, String path) throws Exception {	
		if (path.length() == 0) {		
			replyWithDumpAnswer( exchange);
			return;
		}	
		String hostAndPort = exchange.getHostAndPort();		
		String constructedUrl = "http://" + hostAndPort  + path;
		String compiledChangesUrl = compileChangesUrl(exchange);
		String compiledRestApiUrl = compileRestApiUrl(exchange);
		
		// ravenhurst request
		if (compiledChangesUrl != null && constructedUrl.startsWith(compiledChangesUrl))  {
			processRavenhurstQuery( exchange, constructedUrl);
			return;
		}
		// artifactory rest api request 
		if (compiledRestApiUrl != null && constructedUrl.startsWith(compiledRestApiUrl)) {
			processRestApiQuery( exchange, constructedUrl);
			return;
		}		
		// examine remaining path 
		String subPath = path.substring( root.length() + 1);
		if (subPath.endsWith( "/")) {
			subPath = subPath.substring(0, subPath.length()-1);
		}
		// content switching request
		if (subPath.contains( MARKER_UPDATE)) {			
			processUpdate( exchange);
			return;
		}
		NavigatorNode node = getNavigator().getNode(subPath);		
		String digest = null;

		if (node == null) {
			// no node - doesn't exist, but may be a hash 
			digest = isHashFileRequest( subPath); 
 
			if (digest == null) { // no hash file request
				replyWith404(exchange);
				return;
			}
			else {
				// produce hash file
				replyWithHash(exchange, subPath, digest);
				return;
			}
		} 
				
		// actually serve download
		OnDownloadEvent devent = OnDownloadEvent.T.create();
		devent.setDownloadSource( subPath);
		sendEvent(devent);
		
		FileBackedPipe pipe = new FileBackedPipe("repolet", IOTools.SIZE_64K);
	
		Map<String, String> hashes = node.writeTo( subPath, pipe.acquireOutputStream());
		
		
		
		// check if hashes should be put into the headers for the current node
		Boolean noHashInHeader = hashesInHeader.get( node.name);
		
		if (noHashInHeader == null || !noHashInHeader) { 
			// check if hashes are overridden for his node
			Map<String, String> overriddenHashes = hashOverrides.get( node.name);
			if (overriddenHashes == null) {
				// no overrides, use the one from the pipe
				for (Map.Entry<String, String> entry : hashes.entrySet()) {
					exchange.getResponseHeaders().put( new HttpString(entry.getKey()), entry.getValue());
				}
			}
			else {
				for (Map.Entry<String, String> entry : overriddenHashes.entrySet()) {
					exchange.getResponseHeaders().put( new HttpString(entry.getKey()), entry.getValue());
				}
			}				
		}
		exchange.startBlocking();
		OutputStream out = exchange.getOutputStream();		
		try (InputStream in = pipe.openInputStream()) {
			IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);
		} catch (IOException e) {
			exchange.getResponseSender().send( "cannot pump contents of [" + node.value + "] as " + e.getMessage());
		}	
		exchange.endExchange();	
		pipe.close();
	}

	/**
	 * replies with a hash of the derived node (same name minus the hash extension)
	 * @param exchange 
	 * @param hashFileName - the subpath, i.e. the hash name
	 * @param digest - the digest to pick 
	 */
	private void replyWithHash(HttpServerExchange exchange, String hashFileName, String digest) {	
		OnDownloadEvent devent = OnDownloadEvent.T.create();
		devent.setDownloadSource( hashFileName);
		sendEvent(devent);
		
		String nodeName = hashFileName.substring( 0, hashFileName.lastIndexOf('.'));		
		NavigatorNode node = getNavigator().getNode( nodeName);
		
		// get the real hash of the node 
		FileBackedPipe pipe = new FileBackedPipe("repolet", IOTools.SIZE_64K);	
		Map<String, String> hashes = node.writeTo( hashFileName, pipe.acquireOutputStream());
		pipe.close();
		
		// node.writeTo returns a map with the header names as key for the hashes, we need to find it via the digest
		String hashHeaderKey = digest;
		for (Entry<String, Pair<String,String>> entry : RepoletCommons.hashAlgToHeaderKeyAndExtension.entrySet()) {
			Pair<String, String> value = entry.getValue();
			if (value.second.equals( digest)) {
				hashHeaderKey = value.first;
				break;
			}
		}
		
		// return chosed hash 
		String hash = hashes.get( hashHeaderKey);	
		exchange.getResponseSender().send( hash);	
		
	}
	/**
	 * detecs and identifies the hash file & digest, 
	 * @param subPath - the request's subpath
	 * @return - either md5, sha1, sha256 or null if none of these are matching
	 */
	private String isHashFileRequest(String subPath) {
		if (subPath.endsWith( ".md5"))
			return "md5";
		
		if (subPath.endsWith( ".sha1"))
		return "sha1";

		if (subPath.endsWith( ".sha256"))
			return "sha256";
	
		return null;
	}
	
	
	private void processRestApiQuery(HttpServerExchange exchange, String constructedUrl) {
		// TODO: add proper event 
		
		String compiledRestApiUrl = compileRestApiUrl(exchange);
		int p = constructedUrl.indexOf( compiledRestApiUrl);
		String path = constructedUrl.substring(p + compiledRestApiUrl.length()+1);
			
		FolderInfo folderInfo = extractFolderInfo( path);
		if (folderInfo == null) {
			replyWith404(exchange);
			return;
		}
		try (StringWriter writer = new StringWriter()) {
			marshaller.marshall(writer, folderInfo, options);
			exchange.getResponseSender().send( writer.toString());
		}
		catch (Exception e) {
			throw Exceptions.uncheckedAndContextualize(e, "cannot marshall folderinfo requested via [" + path + "]", RuntimeException::new);
		}	
		
	}
	
	private FolderInfo extractFolderInfo(String path) {		
		NavigatorNode node = getNavigator().getNode("/" + path);
		if (node == null) {
			return null;
		}
		FolderInfo folderInfo = FolderInfo.T.create();
		folderInfo.setPath( path);
		for (NavigatorNode child : node.children) {
			FileItem fileItem = FileItem.T.create();
			fileItem.setUri( child.name);
			folderInfo.getChildren().add(fileItem);
		}
		return folderInfo;
	}


	/**
	 * @param exchange
	 * @param constructed
	 */
	private void processRavenhurstQuery(HttpServerExchange exchange, String constructed) {
		OnRavenhurstQueryRequestEvent revent = OnRavenhurstQueryRequestEvent.T.create();
		String queryString = exchange.getQueryString();
		revent.setRequestPath( queryString);
		
		int p = queryString.indexOf(RAVENHURST_PARAMETER);
		if (p >= 0) {
			String timestampAsString = queryString.substring(p+RAVENHURST_PARAMETER.length());
			try {
				timestampAsString = URLDecoder.decode(timestampAsString, "UTF-8");
			} catch (Exception e) {
				throw Exceptions.uncheckedAndContextualize(e, "cannot decode timestamp from [" + timestampAsString + "]", RuntimeException::new);
			}
			Date date = dateCodec.decode(timestampAsString);
			revent.setReceivedDate(date);
			replyWithRavenhurstAnswer( exchange, date);
		}
		else {
			replyWithRavenhurstAnswer( exchange, null);
		}
		
		sendEvent(revent);		
	}

	/**
	 * @param exchange - the {@link HttpServerExchange}
	 * @param date - the received {@link Date} to get an answer for
	 */
	private void replyWithRavenhurstAnswer(HttpServerExchange exchange, Date date) {
		String payload = null;
		if (date == null) {	
			// dump requested -> mark as dumped at least once 	
			payload = extractArtifactsFromDescription().stream().collect(Collectors.joining("\n"));
			exchange.getResponseSender().send( payload);
			dumpedContentAlreadyOnce = true;
			return;
		}
		else {
			// date has been supplied 
			Map<Date, File> responseMap = getDateToResponseMap();
			
			if (responseMap == null || responseMap.size() == 0) {
				// no match with stored response, standard delta output expected  
				if (!dumpedContentAlreadyOnce) {
					// never dumped -> full dump
					payload = extractArtifactsFromDescription().stream().collect(Collectors.joining("\n"));
					exchange.getResponseSender().send( payload);
				}
				else {					
					// dumped at least once, no changes to report
					exchange.getResponseSender().send("");					
				}			
				return;
			}
			
			Date lastDate = null;
			for (Map.Entry<Date, File> entry : responseMap.entrySet()) {
				Date storedDate = entry.getKey();
				if (storedDate.after( date)) {			
					if (lastDate == null) {
						payload = extractArtifactsFromDescription().stream().collect(Collectors.joining("\n"));
						exchange.getResponseSender().send( payload);
						return;
					}
					else {
						File fileToRead = getDateToResponseMap().get(lastDate);
						try {
							payload = IOTools.slurp(fileToRead, "UTF-8");
							exchange.getResponseSender().send( payload);
						} catch (IOException e) {
							throw new UncheckedIOException( "cannot read RH faking file [" + fileToRead.getAbsolutePath() + "]", e);
						}
						break;
					}
				}
				else {
					lastDate = storedDate;
				}				
			}
		}		
	}

	private List<String> extractArtifactsFromDescription() {
		return getNavigator().getKnownArtifacts().stream().map( a -> a.asString()).collect(Collectors.toList());		
	}

	@Override
	protected void handleHeadRequest(HttpServerExchange exchange, String path) throws Exception {
		if (changesUrl != null) {
			String compiledChangesUrl = compileChangesUrl(exchange);
			exchange.getResponseHeaders().put( new HttpString( changesUrlHeader), compiledChangesUrl);
		}
		if (serverIdentification != null) {
			exchange.getResponseHeaders().put( new HttpString( serverHeader), serverIdentification);
		}
		// examine remaining path 
		String subPath = path.substring( root.length() + 1);
		if (subPath.endsWith( "/")) {
			subPath = subPath.substring(0, subPath.length()-1);
		}
		
		NavigatorNode node = getNavigator().getNode(subPath);
		if (node == null) {
			replyWith404(exchange);
			return;
		}
		
		FileBackedPipe pipe = new FileBackedPipe("repolet", IOTools.SIZE_64K);
		Map<String, String> hashes = node.writeTo( subPath, pipe.acquireOutputStream());
		
		// check if hashes are overridden here 
		Map<String, String> overriddenHashes = hashOverrides.get( node.name);

		if (overriddenHashes == null) {
			// no overrides, use the one from the pipe
			for (Map.Entry<String, String> entry : hashes.entrySet()) {
				exchange.getResponseHeaders().put( new HttpString(entry.getKey()), entry.getValue());
			}
		}
		else {
			for (Map.Entry<String, String> entry : overriddenHashes.entrySet()) {
				exchange.getResponseHeaders().put( new HttpString(entry.getKey()), entry.getValue());
			}
		}
 	
		exchange.startBlocking();
		// no output, just them headers 
		exchange.endExchange();
		pipe.close();
	}

	@Override
	protected void handleOptionsRequest(HttpServerExchange exchange, String path) throws Exception {
		if (changesUrl != null) {
			String compiledChangesUrl = compileChangesUrl(exchange);
			exchange.getResponseHeaders().put( new HttpString( changesUrlHeader), compiledChangesUrl);
		}
		if (serverIdentification != null) {
			exchange.getResponseHeaders().put( new HttpString( serverHeader), serverIdentification);
		}
	}

	@Override
	protected void handlePutRequest(HttpServerExchange exchange, String path) throws Exception {
		// examine remaining path 
		String subPath = path.substring( root.length() + 1);
		if (subPath.endsWith( "/")) {
			subPath = subPath.substring(0, subPath.length()-1);
		}
		BufferedReader reader = null;
		
		File target = new File( getUploadContent(), subPath);		
		target.getParentFile().mkdirs();
		
		
		OnUploadEvent uevent = OnUploadEvent.T.create();
		uevent.setUploadTarget( target.getAbsolutePath());
		
		Writer writer = null;

		Pair<String,String> hashAlgAndValuePair = null;
		for (Entry<String, Pair<String,String>> entry : RepoletCommons.hashAlgToHeaderKeyAndExtension.entrySet()) {
			String headerName = entry.getValue().first();
			String header = exchange.getRequestHeaders().getFirst( headerName);
			if (header != null) {
				hashAlgAndValuePair = Pair.of( entry.getKey(), header);
				uevent.getHashes().put( entry.getKey(), header);
				//break;
			}
		}
				
		// send event with the received hashes
		sendEvent(uevent);
		MessageDigest messageDigest = hashAlgAndValuePair != null ? createMessageDigest(hashAlgAndValuePair.getFirst()) : null;
		
		try {
		    exchange.startBlocking( );
		    InputStream inputStream = exchange.getInputStream();
		    if (messageDigest != null) {
		    	inputStream = new DigestInputStream(inputStream, messageDigest);
			}
		    
			reader = new BufferedReader( new InputStreamReader( inputStream));
		    writer = new FileWriter(target);
		    
		    IOTools.pump(reader, writer);
		    
		    checkChecksum(messageDigest, hashAlgAndValuePair, subPath);
		    
		} catch( IOException e ) {
		    e.printStackTrace( );
		} finally {
			if (writer != null) {
				try {
					writer.close();				
	        	} catch( IOException e ) {
	        		log.error( "cannot close writer to [" + target.getAbsolutePath() + "]");
	        		e.printStackTrace( );
	        	}
			}		
		    if( reader != null ) {
		        try {
		            reader.close( );
		        } catch( IOException e ) {
		        	log.error( "cannot close reader");
		            e.printStackTrace( );
		        }
		    }
		}	
		// update content with a new artifact (acquire!), and add FileResource to Part
		getNavigator().addPart( subPath, target);
		
		if (isHashFileRequest(subPath) != null) {
			OnHashFileUploadEvent hevent = OnHashFileUploadEvent.T.create();
			hevent.setHashFileName(target.getAbsolutePath());
			hevent.setContent( IOTools.slurp( target, "UTF-8"));		
			sendEvent(hevent);
		}
		
		// check overloaded return codes
		if (uploadReturnCodeOverrides != null) {
			Integer code = uploadReturnCodeOverrides.get( target.getName());
			if (code != null) {
				exchange.setResponseCode( code);			
			}
			
		}
	
	}

	@Override
	protected void handleDeleteRequest(HttpServerExchange exchange, String path) throws Exception {
		
		// TODO: delete request
		// examine remaining path 
		String subPath = path.substring( root.length() + 1);
		if (subPath.endsWith( "/")) {
			subPath = subPath.substring(0, subPath.length()-1);
		}
	
		File requested = retrieveFile(getUploadContent(), subPath);
		if (requested == null) {
			// TODO: if a node exists, it could be marked as 'deleted', and then the resolver won't return that 'deleted' node
			log.error("cannot delete file [" + subPath +"] as it's not an uploaded file. Other files are all inmutable");
			replyWithCode(exchange, 418);
		}
		else {
			requested.delete();
			getNavigator().removeNode( subPath);
		}
	}

	protected File retrieveFile( File folder, String path) {
		File suspect = new File( folder, path);
		if (suspect.exists()) {
			return suspect;
		}
		return null;
	}
	
	
	@Configurable 
	public void setUploadContent(File uploadContent) {
		this.uploadContent = uploadContent;
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
}
