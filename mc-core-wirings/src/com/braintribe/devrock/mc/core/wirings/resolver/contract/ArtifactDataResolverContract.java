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
package com.braintribe.devrock.mc.core.wirings.resolver.contract;

import java.io.File;

import com.braintribe.devrock.mc.api.download.PartDownloadManager;
import com.braintribe.devrock.mc.api.download.PartEnricher;
import com.braintribe.devrock.mc.api.repository.configuration.ArtifactChangesSynchronization;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.resolver.ArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.api.resolver.PartAvailabilityReflection;
import com.braintribe.devrock.mc.core.commons.FilesystemLockPurger;
import com.braintribe.devrock.mc.core.wirings.backend.contract.ArtifactDataBackendContract;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.wire.api.space.WireSpace;

/**
 * the contract to get the intermediate resolvers (middle tier)
 * @author pit / dirk
 *
 */
// TODO: shouldn't we rename this to ArtifactResolverContract as there is not ArtifactDataResolver available here
public interface ArtifactDataResolverContract extends WireSpace {

	/**
	 * @return the repository reflection based on the effective probed {@link RepositoryConfiguration}
	 */
	RepositoryReflection repositoryReflection();
	
	/**
	 * looks up dependencies 
	 * @return - the {@link DependencyResolver}
	 */
	DependencyResolver dependencyResolver();
	
	/**
	 * a {@link CompiledArtifactResolver} that can handle redirects, i.e. automatically 
	 * returns the end point of a redirection (or chain of redirections)
	 * @return - a {@link CompiledArtifactResolver} that handles redirects
	 */
	CompiledArtifactResolver redirectAwareCompiledArtifactResolver();
	
	/**
	 * a {@link CompiledArtifactResolver} that ignores redirects, i.e. just returns 
	 * what artifact identified by the {@link CompiledArtifactIdentification}
	 * @return - a {@link CompiledArtifactResolver} that ignores redirects
	 */
	CompiledArtifactResolver directCompiledArtifactResolver();

	/**
	 * A {@link DeclaredArtifactCompiler} that is able to compile a given {@link DeclaredArtifact} by resolving all its internal 
	 * dependencies (e.g. parents, imports) and resolves all property references.
	 */
	DeclaredArtifactCompiler declaredArtifactCompiler();
	
	/**
	 * @return - the resolver 
	 */
	ArtifactResolver artifactResolver();

	/**
	 * @return - the {@link PartDownloadManager} for parallel downloads
	 */
	PartDownloadManager partDownloadManager();
	
	/**
	 * return - the {@link PartEnricher} for managed parallel downloads controlled by an enrichment expert
	 */
	PartEnricher partEnricher();
	
	/**
	 * @return - the {@link PartAvailabilityReflection} to access currently known parts 
	 */
	PartAvailabilityReflection partAvailabilityReflection();
	
	/**
	 * @return
	 */
	File localRepositoryRoot();
	
	/**
	 * @return
	 */
	ArtifactDataBackendContract backendContract();

	/**
	 * @return - a qualified {@link ArtifactChangesSynchronization}, the RH processor
	 */
	ArtifactChangesSynchronization changesSynchronization();
	
		
	/**
	 * @return - A {@link FilesystemLockPurger}
	 */
	FilesystemLockPurger lockFilePurger();
		
	
}

