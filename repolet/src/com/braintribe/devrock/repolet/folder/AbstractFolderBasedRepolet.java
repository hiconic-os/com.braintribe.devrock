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
package com.braintribe.devrock.repolet.folder;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.artifactory.FileItem;
import com.braintribe.devrock.model.artifactory.FolderInfo;
import com.braintribe.devrock.repolet.AbstractRepolet;
import com.braintribe.devrock.repolet.common.RepoletCommons;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;
import com.braintribe.web.velocity.renderer.VelocityTemplateRendererException;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

/**
 * an {@link AbstractRepolet} that is based on file systems 
 * @author pit
 *
 */
public abstract class AbstractFolderBasedRepolet extends AbstractRepolet {
	private static Logger log = Logger.getLogger(AbstractFolderBasedRepolet.class);		
	private File uploadContent;
	private boolean useExternalHashData = false;

	/**
	 * @return - the current file system to use for data 
	 */
	protected abstract File getContent();
	
	@Configurable 
	public void setUploadContent(File uploadContent) {
		this.uploadContent = uploadContent;
	}
	
	@Configurable
	public void setUseExternalHashData(boolean useExternalHashData) {
		this.useExternalHashData = useExternalHashData;
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
	/**
	 * @param exchange - handle an update request (if supported)
	 */
	protected abstract void processUpdate( HttpServerExchange exchange);
	
	protected File retrieveFile( File folder, String path) {
		File suspect = new File( folder, path);
		if (suspect.exists()) {
			return suspect;
		}
		return null;
	}
	
	protected void replyWithFile(HttpServerExchange exchange, File file) {
		Map<String, String> bulkHashes;
		
		//
		if (useExternalHashData) {
			// look for external blk file that contains all hashes to be sent
			File blkFile = new File( file.getParentFile(), file.getName() + ".blk");		
			if (blkFile.exists()) {
				try {
					bulkHashes = readBulkHashes(blkFile);
					if (bulkHashes != null) {
						for (Entry<String,String> me : bulkHashes.entrySet()) {				
							exchange.getResponseHeaders().put( new HttpString(me.getKey()), me.getValue());				
						}
					}
				} catch (IOException e1) {
					throw new IllegalStateException("cannot read file [" + blkFile.getAbsolutePath() + "] with hashes", e1);
				}
			}			
		}
		else {		
			addHashesToHeaders(exchange, file);
		}
		
		exchange.startBlocking();
		
		OutputStream out = exchange.getOutputStream();
		try (FileInputStream in = new FileInputStream(file)) {
			IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);
		} catch (IOException e) {
			exchange.getResponseSender().send( "cannot pump contents of [" + file.getAbsolutePath() + "] as " + e.getMessage());
		}	
		exchange.endExchange();
	}
	
	/**
	 * calculates hashes of the file and attaches the values to the response headers
	 * @param exchange
	 * @param file
	 */
	protected void addHashesToHeaders(HttpServerExchange exchange, File file) {	
		if (file.isFile()) {
			Map<String, String> hashes = generateHash(file, Arrays.asList("sha1", "md5", "SHA-256"));
			exchange.getResponseHeaders().put(new HttpString("X-Checksum-Sha1"), hashes.get("sha1"));
			exchange.getResponseHeaders().put(new HttpString("X-Checksum-MD5"), hashes.get("md5"));
			exchange.getResponseHeaders().put(new HttpString("X-Checksum-SHA256"), hashes.get("SHA-256"));
		}
	}
	

	protected void replyWithListing(HttpServerExchange exchange, String path, File directory) {
		
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
		
		File [] files = directory.listFiles();
		List<String> tokens = new ArrayList<>();
				
		for (File file : files) {
			String name = file.getName();
			if (file.isDirectory()) {
				name += "/";
			}
			tokens.add( name);
			
		}
		if (tokens.size() > 0) {
			try {
				String content = contentGenerator.render( root, path, tokens);
				exchange.getResponseSender().send( content);
			} catch (VelocityTemplateRendererException e) {
				exchange.getResponseSender().send( e.getMessage());
			}
		}		
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
	
		File requested = retrieveFile(getContent(), subPath);
		
		if (requested == null || !requested.exists()) {
			replyWith404(exchange);
		}
		else {
			addHashesToHeaders(exchange, requested);
			replyWith200( exchange);
		}				
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
	
		File requested = retrieveFile(getContent(), subPath);
				
		if (requested == null || !requested.exists()) {
			// no node, file doesn't exist
			replyWith404(exchange);
		}
		else if (requested.isDirectory()) {
			// node found, a directory 
			replyWithListing(exchange, path, requested);					
		}
		else {
			// standard node f 
			replyWithFile(exchange, requested);			
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
		
		Writer writer = null;

		Pair<String,String> hashAlgAndValuePair = null;
		for (Entry<String, Pair<String,String>> entry : RepoletCommons.hashAlgToHeaderKeyAndExtension.entrySet()) {
			String headerName = entry.getValue().first();
			String header = exchange.getRequestHeaders().getFirst( headerName);
			if (header != null) {
				hashAlgAndValuePair = Pair.of( entry.getKey(), header);
			}
		}
		
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
	}
	private void processRestApiQuery(HttpServerExchange exchange, String constructedUrl) {
		String compiledRestApiUrl = compileRestApiUrl(exchange);
		int p = constructedUrl.indexOf( compiledRestApiUrl);
		String path = constructedUrl.substring(p + compiledRestApiUrl.length()+1);
		File directory = new File( getContent(), path);
		FolderInfo folderInfo = extractFolderInfoFromDirectory(directory);
		try (StringWriter writer = new StringWriter()) {
			marshaller.marshall(writer, folderInfo, options);
			exchange.getResponseSender().send( writer.toString());
		}
		catch (Exception e) {
			throw Exceptions.uncheckedAndContextualize(e, "cannot marshall folderinfo from [" + directory.getAbsolutePath() + "]", RuntimeException::new);
		}		
		
	}

	/**
	 * @param exchange
	 * @param constructed
	 */
	private void processRavenhurstQuery(HttpServerExchange exchange, String constructed) {
		String queryString = exchange.getQueryString();
		int p = queryString.indexOf(RAVENHURST_PARAMETER);
		if (p >= 0) {
			String timestampAsString = queryString.substring(p+RAVENHURST_PARAMETER.length());
			try {
				timestampAsString = URLDecoder.decode(timestampAsString, "UTF-8");
			} catch (Exception e) {
				throw Exceptions.uncheckedAndContextualize(e, "cannot decode timestamp from [" + timestampAsString + "]", RuntimeException::new);
			}
			Date date = dateCodec.decode(timestampAsString);
			replyWithRavenhurstAnswer( exchange, date);
		}
		else {
			replyWithRavenhurstAnswer( exchange, null);
		}
		
	}

	/**
	 * @param exchange - the {@link HttpServerExchange}
	 * @param date - the received {@link Date} to get an answer for
	 */
	private void replyWithRavenhurstAnswer(HttpServerExchange exchange, Date date) {
		String payload = null;
		if (date == null) {			
			payload = extractArtifactsFromDirectory( getContent()).stream().collect(Collectors.joining("\n"));
			exchange.getResponseSender().send( payload);
			return;
		}
		else {
			Map<Date, File> responseMap = getDateToResponseMap();
			if (responseMap == null || responseMap.size() == 0) {
				payload = extractArtifactsFromDirectory( getContent()).stream().collect(Collectors.joining("\n"));
				exchange.getResponseSender().send( payload);
				return;
			}
			
			Date lastDate = null;
			for (Map.Entry<Date, File> entry : responseMap.entrySet()) {
				Date storedDate = entry.getKey();
				if (storedDate.after( date)) {			
					if (lastDate == null) {
						payload = extractArtifactsFromDirectory( getContent()).stream().collect(Collectors.joining("\n"));
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

	/**
	 * @param directory - the directory {@link File} to build a {@link FolderInfo} from
	 * @return - a {@link FolderInfo} representing the content (via its {@link FileItem}s)
	 */
	private FolderInfo extractFolderInfoFromDirectory( File directory) {
		FolderInfo folderInfo = FolderInfo.T.create();
		folderInfo.setPath( directory.getAbsolutePath());

		File [] files = directory.listFiles();
		if (files == null || files.length == 0) {
			return folderInfo;
		}
		for (File file : files) {
			FileItem fileItem = FileItem.T.create();
			fileItem.setUri( file.getName());
			folderInfo.getChildren().add(fileItem);
		}
		return folderInfo;
	}
	
	/**
	 * @param directory - the directory {@link File} to build a list of artifact names from
	 * @return - a {@link Collection} of fully qualified artifact names
	 */
	private Collection<? extends String> extractArtifactsFromDirectory(File directory) {
		
		File [] files = directory.listFiles();
		if (files == null || files.length == 0) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>();
		for (File file : files) {
			if (file.isDirectory()) {
				result.addAll( extractArtifactsFromDirectory(file));
			}
			else {
				if (file.getName().endsWith(".pom")) {
					List<String> tokens = new ArrayList<>();
					file = file.getParentFile();
					while (!file.getAbsolutePath().equals(getContent().getAbsolutePath())) {
						tokens.add( 0, file.getName());
						file = file.getParentFile();
					}
					
					int size = tokens.size();
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < size; i++) {
						String token = tokens.get(i);
						if (i == size - 1) {
							sb.append( "#");							
						}
						else if (i == size - 2) {
							sb.append( ":");							
						}				
						else {
							if (sb.length() > 0)
								sb.append(".");
						}
						sb.append( token);						
					}
					result.add( sb.toString());
					return result;
				}
				
			}
		}
		
		return result;
	}
	
	@Override
	protected void handleDeleteRequest(HttpServerExchange exchange, String path) throws Exception {
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
		}
	}

	
	
	
}
