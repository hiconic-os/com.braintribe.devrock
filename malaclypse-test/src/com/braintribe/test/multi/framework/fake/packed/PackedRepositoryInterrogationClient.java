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
package com.braintribe.test.multi.framework.fake.packed;

import java.io.File;
import java.util.List;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClient;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationException;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.model.ravenhurst.Part;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstRequest;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstResponse;

public class PackedRepositoryInterrogationClient extends AbstractPackedClientBase implements RepositoryInterrogationClient {

	public PackedRepositoryInterrogationClient(File zipFile) {
		super(zipFile);
	}

	@Override
	public RavenhurstResponse interrogate(RavenhurstRequest request) throws RepositoryInterrogationException {		
		return null;
	}
	
	@Override
	public RavenhurstResponse extractIndex(RavenhurstRequest request) throws RepositoryInterrogationException {	
		return null;
	}

	@Override
	public List<Part> extractPartList(RavenhurstBundle bundle, Artifact artifact) throws RepositoryInterrogationException {
		
		return null;
	}

	@Override
	public List<String> extractVersionList(RavenhurstBundle bundle, Identification artifact) throws RepositoryInterrogationException {
		
		return null;
	}



}
