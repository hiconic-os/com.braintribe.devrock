// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.retrieval.access;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.MavenMetadataPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

public class LocalRepositoryAccessClient implements RepositoryAccessClient {
	
	private static Logger log = Logger.getLogger(LocalRepositoryAccessClient.class);
	private static final int UPLOAD_ERROR = 500;
	private static final int UPLOAD_SUCCESS = 200;
	private static final String FILE_PROTOCOL = "file://";
	
	//
	private String stripProtocolPrefix( String url) {
		if (url.startsWith(FILE_PROTOCOL)) {
			String result = url.substring( FILE_PROTOCOL.length());
			return result.replace( "/", File.separator);
		}
		return url;
	}
	
	

	@Override
	public boolean checkConnectivity(String location, Server server) {
		String source = stripProtocolPrefix(location);
		File sourceFile = new File( source);
		if (!sourceFile.exists()) {				
			return false;
		}
		else
			return true;
	}


	@Override
	public List<String> extractFilenamesFromRepository(String location, Server server) throws RepositoryAccessException {
		String source = stripProtocolPrefix(location);
		File sourceFile = new File( source);
		if (!sourceFile.exists()) {
			String msg ="location [" + source + "] doesn't exist";
			log.warn( msg);
			return new ArrayList<String>();
		}
		File [] files = sourceFile.listFiles();
		if (files == null) {
			return new ArrayList<String>();
		}
		List<String> names = new ArrayList<String>( files.length);
		for (File file : files) {
			names.add( file.getName());
		}
		return names;
	}

	@Override
	public List<String> extractVersionDirectoriesFromRepository(String location, Server server) throws RepositoryAccessException {
		String source = stripProtocolPrefix(location);
		File sourceFile = new File( source);
		if (!sourceFile.exists()) {
			String msg ="location [" + source + "] doesn't exist";
			log.warn( msg);
			return new ArrayList<String>();
		}
		File [] files = sourceFile.listFiles();
		if (files == null) {
			return new ArrayList<String>();
		}
		List<String> result = new ArrayList<String>();
		for (File file : files) {
			if (file.isDirectory() == false)
				continue;
			String name = file.getName();
			// filter directory.. 
			if (name.matches( ".*[0-9].*")) {				
				result.add( name); // only add the version part of the directory 
			} else {
				// otherwise try to build some kind of version representation.. 
				try {
					VersionProcessor.createFromString( name);			
					result.add( name); // only add the version part of the directory
				} catch (VersionProcessingException e) {
					log.warn("Directory [" + name + "] doesn't yield a valid version");
				}		
			}
			result.add( file.getName());
		}
		return result;
	}

	@Override
	public File extractFileFromRepository(String source, String target, Server server) throws RepositoryAccessException {
		File sourceFile = new File( stripProtocolPrefix(source));
		if (!sourceFile.exists())
			return null;
		File targetFile = new File( target);
		try {
			FileTools.copyFile(sourceFile, targetFile);
		} catch (Exception e) {

		}
		return targetFile;
	}

	@Override
	public String extractFileContentsFromRepository(String source, Server server) throws RepositoryAccessException {
		try {
			File file = new File( stripProtocolPrefix(source));
			if (!file.exists())
				return null;
			return IOTools.slurp(file, "UTF-8");
		} catch (IOException e) {
			throw new RepositoryAccessException( "cannot read file [" + source + "]", e);
		}
	}

	@Override
	public MavenMetaData extractMavenMetaData(LockFactory lockFactory, String source, Server server) throws RepositoryAccessException {
		try {		
			File file = new File( stripProtocolPrefix(source));
			if (!file.exists()) {
				return null;
			}
			return MavenMetadataPersistenceExpert.decode(lockFactory, file);
		} catch (RepositoryPersistenceException e) {
			throw new RepositoryAccessException("cannot decode [" + source + "]", e);
		}						
	}

	@Override
	public Integer uploadFile(Server server, File source, String target) throws RepositoryAccessException {
		String targetLocation = stripProtocolPrefix( target);
		File targetFile = new File( targetLocation);
		try {
			FileTools.copyFile(source, targetFile);
		} catch (Exception e) {
			return UPLOAD_ERROR;
		}		
		return UPLOAD_SUCCESS;
	}

	@Override
	public Map<File, Integer> uploadFile(Server server, Map<File, String> sourceToTargetMap) throws RepositoryAccessException {
		//
		Map<File, Integer> result = new HashMap<File, Integer>();
		for (Entry<File, String> entry : sourceToTargetMap.entrySet()) {
			File source = entry.getKey();
			File target = new File(entry.getValue());
			File directory = target.getParentFile();
			if (!directory.exists()) {
				directory.mkdirs();
			}
			try {
				FileTools.copyFile(source, target);
				result.put(source, UPLOAD_SUCCESS);
			} catch (Exception e) {
				result.put( source, UPLOAD_ERROR);
			}
		}
		return result;
	}

}
