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

