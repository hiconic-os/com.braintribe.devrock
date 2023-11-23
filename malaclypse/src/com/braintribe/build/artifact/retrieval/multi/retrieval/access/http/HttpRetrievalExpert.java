// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.retrieval.access.http;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.MavenMetadataPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClient;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.listener.RepositoryAccessBroadcaster;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.listener.RepositoryAccessListener;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.maven.settings.Server;

public class HttpRetrievalExpert implements RepositoryAccessClient, RepositoryAccessListener, RepositoryAccessBroadcaster {
	private static Logger log = Logger.getLogger(HttpRetrievalExpert.class);
	private static String HREF_TOKEN_BEGIN = "href=\"";
	private static String HREF_TOKEN_END = "\"";
	private Set<RepositoryAccessListener> listeners = new HashSet<RepositoryAccessListener>();
	
	private HttpAccess httpAccess;
	
	public HttpRetrievalExpert( HttpAccess httpAccess) {
		this.httpAccess = httpAccess;		
	}
	
	
	@Override
	public void addListener(RepositoryAccessListener listener) {
		listeners.add( listener);	
	}
	@Override
	public void removeListener(RepositoryAccessListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void acknowledgeDownloadSuccess(String source, long millis) {
		for (RepositoryAccessListener listener : listeners) {
			listener.acknowledgeDownloadSuccess(source, millis);
		}
	}
	@Override
	public void acknowledgeDownloadFailure(String source, String reason) {
		for (RepositoryAccessListener listener : listeners) {
			listener.acknowledgeDownloadFailure(source, reason);
		}
	}
	@Override
	public void acknowledgeUploadSuccess(String source, String target, long millis) {
		for (RepositoryAccessListener listener : listeners) {
			listener.acknowledgeUploadSuccess(source, target, millis);
		}
	}
	@Override
	public void acknowledgeUploadFailure(String source, String target, String reason) {
		for (RepositoryAccessListener listener : listeners) {
			listener.acknowledgeUploadFailure(source, target, reason);
		}
	}
	@Override
	public void acknowledgeDeleteSuccess(String target, long millis) {
		for (RepositoryAccessListener listener : listeners) {
			listener.acknowledgeDeleteSuccess(target, millis);
		}
	}
	@Override
	public void acknowledgeDeleteFailure(String target, String reason) {
		for (RepositoryAccessListener listener : listeners) {
			listener.acknowledgeDeleteFailure(target, reason);
		}
	}
	
	

	@Override
	public boolean checkConnectivity(String location, Server server) {
		try {
			String htmlContents = httpAccess.acquire(location, server, this);
			if (htmlContents != null) {
				return true;
			}
		} catch (HttpRetrievalException e) {
			;
		}	
		return false;
	}

	@Override
	public List<String> extractFilenamesFromRepository(String location, Server server) throws RepositoryAccessException {
		try {
			String htmlContents = httpAccess.acquire(location, server, this);			
			return parseFilenamesFromHtml(htmlContents, location);
		} catch (HttpRetrievalException e) {
			String msg = "cannot extract filenames from contents of [" + location + "]";
			log.error( msg, e);
			throw new RepositoryAccessException( msg, e);
		}
	}

	@Override
	public List<String> extractVersionDirectoriesFromRepository( String location, Server server) throws RepositoryAccessException{
		try {
			String htmlContents = httpAccess.acquire(location, server,  this);			
			return parseVersionDirectoriesFromHtml(location, htmlContents);
		} catch (HttpRetrievalException e) {
			String msg = "cannot extract directories from contents of [" + location + "]";
			log.warn( msg, e);
			return new ArrayList<String>();				
		}		
	}
	

	@Override
	public File extractFileFromRepository(String source, String target, Server server) throws RepositoryAccessException {	
		try {
			return httpAccess.require(new File(target), source, server, this);
		} catch (HttpRetrievalException e) {
			throw new RepositoryAccessException(e);
		}
	}

	@Override
	public String extractFileContentsFromRepository(String source, Server server) throws RepositoryAccessException {
		try {
			return httpAccess.acquire(source, server, this);
		} catch (HttpRetrievalException e) {
			throw new RepositoryAccessException(e);
		}
	}
	
	

	@Override
	public MavenMetaData extractMavenMetaData(LockFactory lockFactory, String source, Server server) throws RepositoryAccessException {
		String metadataSource = source;
		if (!source.endsWith("maven-metadata.xml")) {
			if (source.endsWith( "/")) {
				metadataSource += "maven-metadata.xml";
			}
			else {
				metadataSource += "/maven-metadata.xml";
			}
		}
		MavenMetaData metaData;
		try {
			String contents = extractFileContentsFromRepository(metadataSource, server);
			if (contents == null) {
				log.debug( "no resource [" + source + "] found");
				return null;
			}
			metaData = MavenMetadataPersistenceExpert.decode(contents);
		} catch (Exception e) {
			throw new RepositoryAccessException(e);
		} 
		return metaData;
	}
	
	@Override
	public Integer uploadFile(Server server, File source, String target) throws RepositoryAccessException {	
		try {
			Map<File, String> sourceToTargetMap = new HashMap<File, String>(1);
			sourceToTargetMap.put( source, target);
			Map<File, Integer> result = httpAccess.upload(server, sourceToTargetMap, true,  this);
			return result.get(source);
		} catch (HttpRetrievalException e) { 
			throw new RepositoryAccessException(e);
		}
	}


	@Override
	public Map<File, Integer> uploadFile(Server server, Map<File, String> sourceToTargetMap) throws RepositoryAccessException {
		try {
			return httpAccess.upload(server, sourceToTargetMap, true, this);
		} catch (HttpRetrievalException e) {
			throw new RepositoryAccessException(e);
		}
	}
	private static String sanitizeHrefExpression( String value) {
		//
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case ':':
					break;
				default:
					buffer.append(c);
				}
		}
		return buffer.toString();
	}
	/**
	 * read "directories" from a html page - actually, by definition, hrefs are interpreted as pointing to sub-directories  
	 * @param location - the source, i.e. the url prefix 
	 * @param directoriesInHtml - the contents of the page 
	 * @return - a {@link List} of versions (as {@link String})
	 */
	private static List<String> parseVersionDirectoriesFromHtml( String location, String directoriesInHtml) {
		List<String> result = new ArrayList<String>();	
		if (directoriesInHtml == null)
			return result;
		List<String> hrefs = extractHrefs( directoriesInHtml);
		
		for (String href : hrefs) {
			if (href.endsWith( "/") == false)
				continue;
			if (href.endsWith("../"))
				continue;
			String name = href.substring( 0, href.length() - 1);
			int p = name.lastIndexOf( "/");
			if (p >= 0) {
				name = name.substring( p+1);
			}
			name = sanitizeHrefExpression(name);
			//
			// ugly fix: version should start with a number.. 
			//
			if (name.matches( ".*[0-9].*")) {				
				result.add( name); // only add the version part of the directory 
			} else {
				// otherwise try to build some kind of version representation.. 
				try {
					VersionProcessor.createFromString( name);			
					result.add( name); // only add the version part of the directory
				} catch (VersionProcessingException e) {
					log.warn("Directory [" + href + "] is not a valid version");
				}		
			}
		}
		return result;
	}
	
	/**
	 * parse file names from a html page - actually, hrefs are interpreted as pointing to files 
	 * @param filesInHtml - the contents of the html page  
	 * @param source - the url prefix 
	 * @return - a {@link List} of files (as {@link String})
	 */
	public static List<String> parseFilenamesFromHtml( String filesInHtml, String source) {
		   List<String> result = new ArrayList<String>();
		   if (filesInHtml == null)
			   return result;
		   List<String> hrefs = extractHrefs(filesInHtml);
			
			for (String href : hrefs) {
				if (href.endsWith( "/"))
					continue;
				int p = href.lastIndexOf( "/");
				String name = null;
				if (p > 0) {
					name = href.substring( p+1);
				} else {
					name = href;
				}
				name = sanitizeHrefExpression(name);
				String fullName = source.endsWith("/") ? source + name : source + "/" + name;
				result.add( fullName);
			}
		   return result;
	}
	
	/**
	 * returns a list of all href targets of a html code 
	 * @param html - the html code
	 * @return - a list of all href targets found 
	 */
	public static List<String> extractHrefs( String html) {
		
		List<String> result = new ArrayList<String>();
		int p = html.indexOf( HREF_TOKEN_BEGIN);
		if (p < 0)
			return result;
		do {
			int p2 = p + HREF_TOKEN_BEGIN.length();
			int q = html.indexOf( HREF_TOKEN_END, p2);
			String part = html.substring( p2, q);
			if (part.startsWith("\"")) {
				part = part.substring(1);
			}
			if (part.endsWith("\"")) {
				part = part.substring( 0, part.length()-1);
			}			
			result.add( part);
			p = html.indexOf( HREF_TOKEN_BEGIN, q+1);			
		} while (p > 0);
		
		return result;
	}
}
