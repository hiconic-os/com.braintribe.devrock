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
package com.braintribe.devrock.mc.core.wirings.backend.contract;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import org.apache.http.impl.client.CloseableHttpClient;

import com.braintribe.devrock.mc.api.deploy.ArtifactDeployer;
import com.braintribe.devrock.mc.api.repository.HttpUploader;
import com.braintribe.devrock.mc.api.repository.RepositoryProbingSupport;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.wire.api.space.WireSpace;

/**
 * low level resolvers handling different types of repositories
 * 
 * @author pit / dirk
 *
 */
public interface ArtifactDataBackendContract extends WireSpace {
	
	/**
	 * @param repository - the {@link Repository} to represent as empty repo (offline for instance)
	 * @return - an attached {@link ArtifactDataResolver}
	 */
	ArtifactDataResolver emptyRepository( Repository repository);
	/**
	 * @param repository - the {@link Repository} with a file system (file://.. URL)
	 * @return - an attached {@link ArtifactDataResolver}
	 */
	ArtifactDataResolver fileSystemRepository( MavenFileSystemRepository repository);
	/**
	 * @param repository - the {@link Repository} with the http or https URL 
	 * @return - an attached {@link ArtifactDataResolver}
	 */
	ArtifactDataResolver httpRepository( MavenHttpRepository repository);
	/**
	 * @param repository - the {@link Repository} to get an appropriate {@link ArtifactDataResolver} for, 
	 * depending on the type, one of methods above are called 
	 * ({@link #emptyRepository(Repository)}, {@link #fileSystemRepository(Repository)}, {@link #httpRepository(Repository)}, {@link #artifactoryRepository(Repository)} 
	 * @return - an attached {@link ArtifactDataResolver}
	 */
	ArtifactDataResolver repository( Repository repository);
	

	/**
	 * @param repository - the {@link Repository} to get an appropriate {@link RepositoryProbingSupport} for 
	 * @return - the expert that can handle {@link RepositoryProbingSupport} for the repository passed
	 */
	RepositoryProbingSupport probingSupport( Repository repository);
	
	/**
	 * @return - a functional {@link CloseableHttpClient}
	 */
	CloseableHttpClient httpClient();
	
	/**
	 * creates an appropriate {@link ArtifactDataResolver} for a artifactory-backed standard Maven repository
	 * @param repository - the {@link MavenHttpRepository}
	 * @return - the appropriate {@link ArtifactDataResolver}
	 */
	ArtifactDataResolver artifactoryRepository(MavenHttpRepository repository);
	
	/**
	 * creates an appropriate {@link ArtifactDataResolver} for a dumb standard Maven repository
	 * @param repository - the {@link MavenHttpRepository}
	 * @return - the appropriate {@link ArtifactDataResolver}
	 */
	ArtifactDataResolver standardHttpRepository(MavenHttpRepository repository);
	
	/**
	 * creates an appropriate {@link ArtifactDataResolver} for a codebase (source-backed) repository, 
	 * reflecting a (or several) git codebase directories. 
	 * @param repository - the {@link CodebaseRepository}
	 * @return - the appropriate {@link ArtifactDataResolver}
	 */
	ArtifactDataResolver codebaseRepository(CodebaseRepository repository);
	
	/**
	 * creates a {@link ArtifactDataResolver} for workspace (eclipse workspace) repository, 
	 * actually it's based on a simple enumeration of {@link VersionedArtifactIdentification} and their 
	 * associated directory.
	 * @param repository - the {@link WorkspaceRepository} 
	 * @return - the appropriate {@link ArtifactDataResolver}
	 */
	ArtifactDataResolver workspaceRepository(WorkspaceRepository repository);
	
	/**
	 * Creates an {@link HttpUploader} able to upload artifact parts to a {@link MavenHttpRepository} 
	 */
	HttpUploader httpUploader();
	
	/**
	 * returns a deployer (uploader) for the repository passed 
	 * @param repository - an implementation of the {@link Repository}
	 * @return - an appropriate {@link ArtifactDeployer} for the repository type
	 */
	ArtifactDeployer artifactDeployer(Repository repository);
	
	
	Function<File, ReadWriteLock> lockSupplier();
	
}	
	
