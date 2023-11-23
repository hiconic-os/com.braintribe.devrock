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
import java.time.Duration;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.repository.configuration.HasConnectivityTokens;
import com.braintribe.devrock.mc.api.repository.local.ArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.api.repository.local.PartAvailabilityAccess;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.core.configuration.ProbingResultPersistenceExpert;
import com.braintribe.devrock.mc.core.filters.AllMatchingArtifactFilterExpert;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.devrock.mc.core.filters.NoneMatchingArtifactFilterExpert;
import com.braintribe.devrock.mc.core.resolver.EmptyRepositoryArtifactDataResolver;
import com.braintribe.devrock.model.repository.LocalRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryRestSupport;
import com.braintribe.model.artifact.changes.RepositoryProbingResult;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.version.Version;

/**
 * a basic implementation of {@link ArtifactPartResolverPersistenceDelegate}
 * 
 * @author pit
 *
 */
public class BasicArtifactPartResolverPersistenceDelegate implements ArtifactPartResolverPersistenceDelegate, HasConnectivityTokens {	
	private ArtifactDataResolver resolver;
	private Duration duration;
	private Repository repository;
	private ArtifactFilterExpert artifactFilter;
	private ArtifactFilterExpert dominanceFilter = NoneMatchingArtifactFilterExpert.instance;
	
	private boolean isLocalDelegate = false;
	private RepositoryRestSupport restSupport = RepositoryRestSupport.none;
	private ProbingResultPersistenceExpert probingResultPersistenceExpert;
	private boolean cachable = true;
	private boolean dynamic = false;
		
	public static BasicArtifactPartResolverPersistenceDelegate createLocal() {
		// the local delegates needs to be configured with its actual filter,
		// and then injected as delegate..
		BasicArtifactPartResolverPersistenceDelegate local = new BasicArtifactPartResolverPersistenceDelegate();
		local.setResolver(EmptyRepositoryArtifactDataResolver.instance);
		Repository localRepo = LocalRepository.T.create();
		localRepo.setName( "local");
		local.setRepository(localRepo);
		local.setArtifactFilter( AllMatchingArtifactFilterExpert.instance);
		local.isLocalDelegate = true;
		return local;
	}
		
	public BasicArtifactPartResolverPersistenceDelegate() {
		super();
	}

	/**
	 * @param probingResultPersistenceExpert - the expert that handles the persistence of the {@link RepositoryProbingResult}
	 */
	@Configurable @Required
	public void setProbingResultPersistenceExpert(ProbingResultPersistenceExpert probingResultPersistenceExpert) {
		this.probingResultPersistenceExpert = probingResultPersistenceExpert;
	}
		

	@Configurable
	public void setCachable(boolean cachable) {
		this.cachable = cachable;
	}
	
	@Override
	public boolean isCachable() {
		return cachable;
	}
	
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
	
	@Override
	public boolean isDynamic() {	
		return dynamic;
	}
	
	@Override
	public boolean isOffline() {	
		return repository.getOffline();
	}

	@Configurable @Required
	public void setResolver(ArtifactDataResolver resolver) {
		this.resolver = resolver;
	}

	@Configurable @Required
	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	@Configurable @Required
	public void setRepository(Repository repository) {
		this.repository = repository;		
		this.isLocalDelegate = repository.getName().equalsIgnoreCase("local");
	}

	@Configurable @Required
	public void setArtifactFilter(ArtifactFilterExpert relevancyFilter) {
		this.artifactFilter = relevancyFilter;
	}
	
	@Override
	public ArtifactFilterExpert repositoryDominanceFilter() {
		return dominanceFilter;
	}
	@Configurable
	public void setDominanceFilter(ArtifactFilterExpert dominanceFilter) {
		this.dominanceFilter = dominanceFilter;
	}

	@Configurable
	public void setRestSupport(RepositoryRestSupport restSupport) {
		this.restSupport = restSupport;
	}
	

	@Override
	public ArtifactDataResolver resolver() {
		return resolver;
	}

	@Override
	public Duration updateInterval() {
		return duration;
	}

	@Override
	public String repositoryId() {	
		return repository.getName();
	}

	@Override
	public ArtifactFilterExpert artifactFilter() {
		return artifactFilter;
	}

	
	@Override
	public boolean cacheDefaultMetadataFile() {		
		return !isLocalDelegate();
	}

	@Override
	public boolean isLocalDelegate() {	
		return isLocalDelegate;
	}

	@Override
	public PartAvailabilityAccess createPartAvailabilityAccess( CompiledArtifactIdentification compiledArtifactIdentification, File localRepository, Function<File, ReadWriteLock> lockProvider, BiFunction<ArtifactIdentification, Version, Boolean> versionPredicate) {
		String qualifier = compiledArtifactIdentification.getVersion().getQualifier();
		boolean isSnapshot = qualifier != null ? qualifier.equalsIgnoreCase("SNAPSHOT") : false;
		if (isLocalDelegate() && !isSnapshot) {
			return new LocalReleasePartAvailabilityAccess(compiledArtifactIdentification, lockProvider, artifactFilter, localRepository, repository, this, versionPredicate);
		}
		else if (isSnapshot) {
			return new SnapshotPartAvailabilityAccess(compiledArtifactIdentification, lockProvider, artifactFilter, localRepository, repository, this);
		}
		else {		
			if (!isCachable())
				return new TransientPartAvailabilityAccess(compiledArtifactIdentification, repository, this);
			
			// TODO : REVIEW : if no rest support and offline, read last probing result   
			if (restSupport == null) {
				if (repository.getOffline()){			
					restSupport = determineRestSupportFromPersistedLastProbingResult( localRepository);
					if (restSupport == null)
						restSupport = RepositoryRestSupport.none;
				}
				else {
					restSupport = RepositoryRestSupport.none;
				}
			}
			switch ( restSupport) {
				case artifactory :
					return new ArtifactoryRestReleaseAvailabilityAccess( compiledArtifactIdentification, lockProvider, artifactFilter, localRepository, repository, this);		
				default:
					return new ReleasePartAvailabilityAccess(compiledArtifactIdentification, lockProvider, artifactFilter, localRepository, repository, this);					
			}
		}
	}

	/**
	 * reads the last probing result of the repository and returns the {@link RepositoryRestSupport} 
	 * @param localRepository - {@link File} pointing to the local repository 
	 * @param compiledArtifactIdentification - the 
	 * @return
	 */
	private RepositoryRestSupport determineRestSupportFromPersistedLastProbingResult(File localRepository) {
		RepositoryProbingResult probingResult = probingResultPersistenceExpert.readProbingResult(repository, localRepository);
		if (probingResult == null) {
			return RepositoryRestSupport.none;
		}
		else {
			return probingResult.getRepositoryRestSupport();
		}				
	}
	
	

}
