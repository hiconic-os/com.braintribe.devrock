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

import java.util.Collections;
import java.util.Set;

import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.repository.local.ArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.api.repository.local.PartAvailabilityAccess;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.Version;

public class TransientPartAvailabilityAccess implements PartAvailabilityAccess {
	
	private final CompiledArtifactIdentification compiledArtifactIdentification;
	private final Repository repository;
	private final ArtifactPartResolverPersistenceDelegate delegate;

	public TransientPartAvailabilityAccess(CompiledArtifactIdentification compiledArtifactIdentification, Repository repository, ArtifactPartResolverPersistenceDelegate delegate) {
		this.compiledArtifactIdentification = compiledArtifactIdentification;
		this.repository = repository;
		this.delegate = delegate;
	}

	@Override
	public void setAvailablity(PartIdentification partIdentification, PartAvailability availablity) {
		throw new UnsupportedOperationException("This should never be called as no unknown availability is ever returned.");
	}

	@Override
	public Version getActualVersion() {		
		return compiledArtifactIdentification.getVersion();
	}

	@Override
	public PartAvailability getAvailability(PartIdentification partIdentification) {
		Maybe<ArtifactDataResolution> partCandidate = delegate.resolver().resolvePart(compiledArtifactIdentification, partIdentification, getActualVersion());
		
		if (partCandidate.isUnsatisfiedBy(NotFound.T))
			return PartAvailability.unavailable;
		
		return partCandidate.get().isBacked()? PartAvailability.available: PartAvailability.unavailable;
	}

	@Override
	public Repository repository() {
		return repository;
	}

	@Override
	public ArtifactPartResolverPersistenceDelegate repoDelegate() {
		return delegate;
	}

	@Override
	public Set<CompiledPartIdentification> getAvailableParts() { 
		// TODO: check ... pom's for sure, right? 	
		return Collections.singleton( CompiledPartIdentification.from(compiledArtifactIdentification, PartIdentifications.pom));
	}
	
	
}
