// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpRetrievalExpert;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.model.ravenhurst.Part;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

/**
 * abstract implementation of a {@link RepositoryInterrogationClient}, with HTML based features to retrieve
 * parts (actual files) and versions of an given artifact. 
 * @author Pit
 *
 */
public abstract class AbstractRepositoryHttpInterrogationClient implements RepositoryInterrogationClient {
	private static Logger log = Logger.getLogger(AbstractRepositoryHttpInterrogationClient.class);
		
	private HttpRetrievalExpert retrievalExpert;
	
	public AbstractRepositoryHttpInterrogationClient(HttpAccess httpAccess) {
		retrievalExpert = new HttpRetrievalExpert( httpAccess);
	}

	@Override
	public List<Part> extractPartList(RavenhurstBundle bundle, Artifact artifact) throws RepositoryInterrogationException {
		String remoteLocation = RepositoryReflectionHelper.safelyCombineUrlPathParts(bundle.getRepositoryUrl(), artifact.getGroupId().replace('.', '/'), artifact.getArtifactId(), artifact.getVersion());				
		Server server = bundle.getRavenhurstRequest().getServer();
		List<String> fileNames;
		try {
			fileNames = retrievalExpert.extractFilenamesFromRepository(remoteLocation, server);			
		} catch (Exception e) {		
			String msg="cannot retrieve file list from url [" + remoteLocation + "] as deduced from [" + artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + artifact.getVersion() +"]";
			log.error( msg, e);
			return null;					
		}
		if (fileNames == null)
			return null;
		List<Part> parts = new ArrayList<Part>( fileNames.size());
		for (String fileName : fileNames) {
			Part part = Part.T.create();
			part.setSource(fileName);
			part.setName( fileName.substring( fileName.lastIndexOf('/')+1));
			parts.add(part);
		}
		return parts;
	}

	@Override
	public List<String> extractVersionList(RavenhurstBundle bundle, Identification artifact) throws RepositoryInterrogationException {
		String remoteLocation = RepositoryReflectionHelper.safelyCombineUrlPathParts(bundle.getRepositoryUrl(), artifact.getGroupId().replace('.', '/'), artifact.getArtifactId());					
		Server server = bundle.getRavenhurstRequest().getServer();		
		List<String> directories = null;
		try {
			directories = retrievalExpert.extractVersionDirectoriesFromRepository(remoteLocation, server);
		} catch (RepositoryAccessException e) {
			String msg = "Cannot extract directory information from [" + remoteLocation + "] as " + e;
			log.error(msg, e);		
		}
		return directories;
	}
	
	
}
