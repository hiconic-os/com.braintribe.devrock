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
package com.braintribe.devrock.mc.api.repository.local;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.devrock.mc.core.filters.NoneMatchingArtifactFilterExpert;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.version.Version;


/**
 * represent a semantic repository, i.e. reflects access and update-policies
 * @author pit
 *
 */
public interface ArtifactPartResolverPersistenceDelegate {
	/**
	 * @return - the pertinent resolver
	 */
	ArtifactDataResolver resolver();
	/**
	 * @return - the update interval, NULL for never, otherwise a {@link Duration}
	 */
	Duration updateInterval();
	
	/**
	 * @return - the id of the repository
	 */
	String repositoryId();
	
	/**
	 * @return - expose the filter that filters the {@link ArtifactIdentification}, i.e. checks whether this entry 
	 * does actually contain any matching artifacts. 
	 */
	ArtifactFilterExpert artifactFilter();
	
	/**
	 * @return - expose the dominance filter (FKA pc_bias filter)
	 */
	default ArtifactFilterExpert repositoryDominanceFilter() {
		return NoneMatchingArtifactFilterExpert.instance;
	}
	
	/**
	 * @return - true if the delegate should write an 'empty' maven-metadata file if nothing's present, otherwise no files is written 
	 */
	boolean cacheDefaultMetadataFile();
	
	/**
	 * @return - true if the delegate should put data into the local cache, aka the local repository. 
	 */
	boolean isCachable();
	
	/**
	 * @return - true if the delegate is backed by a RH supporting repository, i.e. can notify about changes
	 */
	boolean isDynamic();
	
	
	/**
	 * @return - true if the repository that backs this delegate is offline
	 */
	boolean isOffline();
	
	/**
	 * @return - true if the delegate represents the local repository
	 */
	boolean isLocalDelegate();
	
	/**
	 * create a {@link PartAvailabilityAccess} for a specificy {@link CompiledArtifactIdentification}
	 * @param compiledArtifactIdentification - the {@link CompiledArtifactIdentification}
	 * @param localRepository - the location of the local repository 
	 * @param lockProvider - the provider of the read/write locks 
	 * @param versionPredicate - 
	 * @return
	 */
	PartAvailabilityAccess createPartAvailabilityAccess(CompiledArtifactIdentification compiledArtifactIdentification, File localRepository, Function<File,ReadWriteLock> lockProvider, BiFunction<ArtifactIdentification, Version, Boolean> versionPredicate);
	
}
