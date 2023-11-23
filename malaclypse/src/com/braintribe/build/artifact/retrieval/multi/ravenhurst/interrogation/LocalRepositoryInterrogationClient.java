// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.model.ravenhurst.Part;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstRequest;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstResponse;

public class LocalRepositoryInterrogationClient implements RepositoryInterrogationClient {
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
	public RavenhurstResponse interrogate(RavenhurstRequest request) throws RepositoryInterrogationException {		
		RavenhurstResponse response = RavenhurstResponse.T.create();		
		return response;
	}
	

	@Override
	public RavenhurstResponse extractIndex(RavenhurstRequest request) throws RepositoryInterrogationException {
		RavenhurstResponse response = RavenhurstResponse.T.create();		
		return response;
	}

	@Override
	public List<Part> extractPartList(RavenhurstBundle bundle, Artifact artifact) throws RepositoryInterrogationException {
		List<Part> result = new ArrayList<Part>();
		String solutionFilesystemLocation = RepositoryReflectionHelper.getSolutionFilesystemLocation( stripProtocolPrefix( bundle.getRepositoryUrl()), artifact);
		File location = new File(solutionFilesystemLocation);
		File files[] = location.listFiles();
		if (files == null || files.length == 0) {
			return result;
		}
		for (File file : files) {
			String name = file.getName();
			if (name.startsWith( ".updated")) 
				continue;
			Part part = Part.T.create();
			part.setSource( file.getAbsolutePath());
			part.setName( name);
			result.add(part);
		}
		
		
		return result;
	}

	@Override
	public List<String> extractVersionList(RavenhurstBundle bundle, Identification artifact) throws RepositoryInterrogationException {
		List<String> result = new ArrayList<String>();
		File location = new File(RepositoryReflectionHelper.getArtifactFilesystemLocation( stripProtocolPrefix( bundle.getRepositoryUrl()), artifact));
		try {
			result.addAll( RepositoryReflectionHelper.getVersionsFromDirectory( location));
		} catch (RavenhurstException e) {
			throw new RepositoryInterrogationException( e);
		}		
		return result;
		
	}

}
