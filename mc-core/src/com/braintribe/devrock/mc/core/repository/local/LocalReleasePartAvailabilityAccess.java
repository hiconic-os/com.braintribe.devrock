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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.repository.local.ArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.api.repository.local.PartAvailabilityAccess;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.Version;

/**
 * an implementation for the {@link PartAvailabilityAccess} for the *local* repository (i.e. for 'installed' artifact)
 * @author pit/dirk
 *
 */
public class LocalReleasePartAvailabilityAccess extends AbstractPartAvailabilityAccess {
	

	private BiFunction<ArtifactIdentification, Version, Boolean> versionPredicate;

	public LocalReleasePartAvailabilityAccess(CompiledArtifactIdentification compiledArtifactIdentification,
			Function<File, ReadWriteLock> lockSupplier, ArtifactFilterExpert artifactFilter,
			File localRepository, Repository repository, ArtifactPartResolverPersistenceDelegate repoDelegate, BiFunction<ArtifactIdentification, Version, Boolean> versionPredicate) {
		super(compiledArtifactIdentification, lockSupplier, artifactFilter, localRepository, repository, repoDelegate);
		this.versionPredicate = versionPredicate;
	
	}

	@Override
	public void setAvailablity(PartIdentification partIdentification, PartAvailability availablity) {
		throw new UnsupportedOperationException();

	}

	@Override
	public Version getActualVersion() {		
		return compiledArtifactIdentification.getVersion();
	}

	@Override
	protected PartAvailability getAvailability(CompiledPartIdentification cpi) {
		File part = ArtifactAddressBuilder.build().root(localRepository.getAbsolutePath()).compiledArtifact(cpi).part(cpi).toPath().toFile();
		if (!part.exists()) {
			return PartAvailability.unavailable;
		}
				
		if (versionPredicate.apply( cpi, cpi.getVersion())) {
			return PartAvailability.available;
		}
		
		return PartAvailability.unavailable;
	}

	@Override
	public Set<CompiledPartIdentification> getAvailableParts() {	
		File artifactDirectory = ArtifactAddressBuilder.build().root(localRepository.getAbsolutePath()).compiledArtifact(compiledArtifactIdentification).toPath().toFile();
		if (!artifactDirectory.exists()) {
			return Collections.emptySet();
		}
		Set<CompiledPartIdentification> result = new HashSet<>();
		File [] files = artifactDirectory.listFiles();
		for (File file : files) {
			String name = file.getName();
			CompiledPartIdentification cpi = CompiledPartIdentification.fromFile(compiledArtifactIdentification, name);
			if (cpi != null) {
				result.add(cpi);
			}
		}
		return result;
	}
	
	

}
