// ============================================================================
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
import java.util.Map;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClient;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessException;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.maven.settings.Server;

public class PackedRepositoryAccessClient extends AbstractPackedClientBase implements RepositoryAccessClient {

	public PackedRepositoryAccessClient(File zipFile) {
		super(zipFile);
	}
	
	

	@Override
	public boolean checkConnectivity(String location, Server server) {	
		return false;
	}



	@Override
	public List<String> extractFilenamesFromRepository(String location, Server server)
			throws RepositoryAccessException {
		return null;
	}

	@Override
	public List<String> extractVersionDirectoriesFromRepository(String location, Server server)
			throws RepositoryAccessException {
		return null;
	}

	@Override
	public File extractFileFromRepository(String source, String target, Server server)
			throws RepositoryAccessException {
		return null;
	}

	@Override
	public String extractFileContentsFromRepository(String source, Server server) throws RepositoryAccessException {
		return null;
	}

	@Override
	public MavenMetaData extractMavenMetaData(LockFactory lockFactory, String source, Server server) throws RepositoryAccessException {
		return null;
	}

	@Override
	public Integer uploadFile(Server server, File source, String target) throws RepositoryAccessException {
		return null;
	}

	@Override
	public Map<File, Integer> uploadFile(Server server, Map<File, String> sourceToTargetMap) throws RepositoryAccessException {
		return null;
	}

}
