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
package com.braintribe.devrock.mc.core.repository.local;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;

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
	 * @param relevancyFilter - a {@link Predicate} that filters whether the repository reflected is relevant
	 * @param localRepository - the path to the local repostory's root 
	 * @param repositoryId - the id of the repository  
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
