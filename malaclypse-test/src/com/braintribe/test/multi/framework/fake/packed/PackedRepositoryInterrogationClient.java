// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
