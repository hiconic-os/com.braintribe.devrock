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
package com.braintribe.devrock.mc.core.repository.local;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import com.braintribe.devrock.mc.api.repository.local.ArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.api.repository.local.PartAvailabilityAccess;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;

/**
 * common functionality of all {@link PartAvailabilityAccess} implementations
 * @author pit / dirk
 *
 */
public abstract class AbstractPartAvailabilityAccess implements PartAvailabilityAccess {
	protected CompiledArtifactIdentification compiledArtifactIdentification;
	protected Function<File,ReadWriteLock> lockSupplier;	
	protected ArtifactFilterExpert artifactFilter;
	protected File localRepository;
	protected Repository repository;

	protected ArtifactPartResolverPersistenceDelegate repoDelegate;
	

	/**
	 * @param compiledArtifactIdentification - the full monty artifact
	 * @param lockSupplier - a {@link Function} that returns the {@link ReadWriteLock} for a specified file 
	 * @param localRepository - the path to the local repostory's root 
	 */
	public AbstractPartAvailabilityAccess(CompiledArtifactIdentification compiledArtifactIdentification,
			Function<File, ReadWriteLock> lockSupplier, ArtifactFilterExpert artifactFilter,
			File localRepository, Repository repository, ArtifactPartResolverPersistenceDelegate repoDelegate) {
		
		super();
		this.compiledArtifactIdentification = compiledArtifactIdentification;
		this.lockSupplier = lockSupplier;
		this.artifactFilter = artifactFilter;
		this.localRepository = localRepository;
		this.repository = repository;
		this.repoDelegate = repoDelegate;
	}

	@Override
	public PartAvailability getAvailability(PartIdentification partIdentification) {
		CompiledPartIdentification cpi = CompiledPartIdentification.from(compiledArtifactIdentification, partIdentification);
		// if the reflected repository cannot deliver (group-index filter, policies, content filter) 
		if (!artifactFilter.matches(cpi)) 
			return PartAvailability.unavailable;
		// delegate to actual instace
		return getAvailability(cpi);		
	}
	
	
	@Override
	public Repository repository() {	
		return repository;
	}
	
	@Override
	public ArtifactPartResolverPersistenceDelegate repoDelegate() {	
		return repoDelegate;
	}

	/**
	 * @param cpi - the {@link CompiledArtifactIdentification}
	 * @return - the {@link PartAvailability} of the specified part 
	 */
	protected abstract PartAvailability getAvailability( CompiledPartIdentification cpi);
	
}
