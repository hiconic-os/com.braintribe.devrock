// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.utils.archives.zip.ZipContextEntry;
import com.braintribe.web.velocity.renderer.VelocityTemplateRendererException;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 * repolet to generate maven compatible answers  
 * 
 * @author pit
 *
 */
public class ZipBasedSwitchingRepolet implements Repolet {
	private static final String MARKER_UPDATE = "update";
	private static final String MARKER_RAVENHURST = "rest/changes";
	private static final String MARKER_RAVENHURST_OLD = "ravenhurst/changes";
	private ZipContext archive;
	private File [] archiveFiles; 
	private String root = "content";
	private ContentGenerator contentGenerator = new ContentGenerator();
	private Map<ZipContextEntry, File> entryToCacheMap = new HashMap<ZipContextEntry, File>();
	private int currentIndex = 0;
	
	
	@Configurable @Required
	public void setContent( File ... files) throws ArchivesException{
		this.archiveFiles = files;
		this.archive = Archives.zip().from(files[ currentIndex]);		
	}
	
	@Configurable
	public void setRoot(String root) {
		this.root = root;
	}
	
	public ZipBasedSwitchingRepolet() {
	}


	@Override
	public void handleRequest(HttpServerExchange exchange, String path) throws Exception {		
		if (path.length() == 0) {		
			replyWithDumpAnswer( exchange);
			return;
		}			
		String subPath = path.substring( root.length() + 2);
		//
		if (subPath.equalsIgnoreCase( MARKER_UPDATE)) {
			switchContents();
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
			exchange.getResponseSender().send( "switched to [" + archiveFiles[ currentIndex].getAbsolutePath() + "]");	
		}
		
		if (
				subPath.startsWith( MARKER_RAVENHURST) ||
				subPath.startsWith( MARKER_RAVENHURST_OLD)
			){
			// deliver update information
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/txt");			
			ZipContextEntry zipContextEntry = archive.getEntry("artifacts.lst");
			if (zipContextEntry == null) {
				exchange.getResponseSender().send( "");								
			}
			InputStream in = zipContextEntry.getPayload();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		    StringBuilder out = new StringBuilder();
		    String line;
		    while ((line = reader.readLine()) != null) {
		    	if (out.length() > 0)
		    		out.append( "\n");
		        out.append(line);
		    }
		    exchange.getResponseSender().send( out.toString());
		    return;
		}
		
		if (subPath.endsWith( "/")) {
			subPath = subPath.substring(0, subPath.length()-1);
		}
		ZipContextEntry entry = archive.getEntry( subPath);
		if (entry == null) {
			replyWithListing(exchange, subPath);			
			return;
		}
		else {
			replyWithFile(exchange, entry);
		}
		
	}

	public void switchContents() throws ArchivesException {
		if (currentIndex < archiveFiles.length-1) {
			archive.close();
			currentIndex++; 
			archive = Archives.zip().from( archiveFiles[currentIndex]);	
		}
	}

	/**
	 * just dump what's in there ... 
	 * @param exchange - the {@link HttpServerExchange} to work with
	 */
	private void replyWithDumpAnswer(HttpServerExchange exchange) {
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
	 * create a proper listing as archiva does 
	 * @param exchange - the {@link HttpServerExchange} to work with
	 * @param path - the path as {@link String}
	 */
	private void replyWithListing( HttpServerExchange exchange, String path) {
		if (path.length() == 0) {		
			return;
		}			
		int numSlashesInPath = countSlashes(path.substring(1));		
		
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
		List<ZipContextEntry> entries = archive.getEntries( path + "/.*");
	
		if (entries.size() > 0) {
			List<String> tokens = new ArrayList<String>();
			
			for (ZipContextEntry suspect : entries) {
				ZipEntry zipEntry = suspect.getZipEntry();
				String name = zipEntry.getName();			
				if (name.endsWith( "/")) {
					name = name.substring(0, name.length()-1);
				}
				int numSlashesInName = countSlashes(name);
				if (numSlashesInName > numSlashesInPath + 1) 
					continue;			
				if (name.equalsIgnoreCase( path)) 
						continue;
				
				name = name.substring( name.lastIndexOf( '/')+1);											
				if (zipEntry.isDirectory()) {				
					name += "/";
				}
				tokens.add( name);
			}
			if (tokens.size() > 0) {
				try {
					String content = contentGenerator.render( path, tokens);
					exchange.getResponseSender().send( content);
				} catch (VelocityTemplateRendererException e) {
					exchange.getResponseSender().send( e.getMessage());
				}
			} else {
				exchange.setResponseCode( 404);
				try {
					exchange.getResponseSender().send( contentGenerator.render404(path));
				} catch (VelocityTemplateRendererException e) {
					exchange.getResponseSender().send( e.getMessage());
				}
				exchange.endExchange();
			}
		}
		else {
			exchange.setResponseCode( 404);
			try {
				exchange.getResponseSender().send( contentGenerator.render404(path));
			} catch (VelocityTemplateRendererException e) {
				exchange.getResponseSender().send( e.getMessage());
			}
			exchange.endExchange();
			
			
		}
	}
	
	/**
	 * deliver a file (and cache it, as it can only be read once from the zip file) 
	 * @param exchange - the {@link HttpServerExchange}
	 * @param entry - the {@link ZipContextEntry}
	 * @throws IOException - arrgh
	 */
	private void replyWithFile( HttpServerExchange exchange, ZipContextEntry entry) throws IOException{
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
		exchange.startBlocking();
		
		File tempFile = entryToCacheMap.get( entry);
		if (tempFile == null) {					
			InputStream in = entry.getPayload();
			tempFile = File.createTempFile("ZipRepolet.", ".cached");
			tempFile.deleteOnExit();
			FileOutputStream fout = new FileOutputStream(tempFile);			
		
			try {
				pump( in, fout);
			} catch (IOException e) {
				exchange.getResponseSender().send( "cannot pump contents of [" + entry.getZipEntry().getName() + "] as " + e.getMessage());
			}
			finally {
				IOTools.closeQuietly(in);
				IOTools.closeQuietly(fout);
			}
			entryToCacheMap.put( entry, tempFile);
		}
		
		OutputStream out = exchange.getOutputStream();
		FileInputStream in = new FileInputStream(tempFile);

		try {
			pump( in, out);
		} catch (IOException e) {
			exchange.getResponseSender().send( "cannot pump contents of [" + entry.getZipEntry().getName() + "] as " + e.getMessage());
		}
		finally {
			IOTools.closeQuietly(in);		
		}
		
	}
		
	private int countSlashes(String string) {
		int num = 0;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c == '/') {
				num += 1;
			}
		}
		return num;
	}
	
	/**
	 * pump from one stream to the other 
	 * @param inputStream - the {@link InputStream} to read from 
	 * @param outputStream - the {@link OutputStream} to write to 
	 * @throws IOException - thrown if anything goes wrong
	 */
	private void pump(InputStream inputStream, OutputStream outputStream) throws IOException {
		int bufferSize = 4096;
		byte[] buffer = new byte[bufferSize];

		int count;
		while ((count = inputStream.read(buffer)) != -1) { 
			outputStream.write(buffer, 0, count);
		}
		outputStream.flush();
	}
}
