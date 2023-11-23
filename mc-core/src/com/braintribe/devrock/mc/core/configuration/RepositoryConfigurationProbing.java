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
package com.braintribe.devrock.mc.core.configuration;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.repository.RepositoryProbingSupport;
import com.braintribe.devrock.mc.api.repository.configuration.ArtifactChangesSynchronization;
import com.braintribe.devrock.mc.api.repository.configuration.HasConnectivityTokens;
import com.braintribe.devrock.model.mc.reason.configuration.ProbingFailed;
import com.braintribe.devrock.model.repository.ChangesIndexType;
import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ConjunctionArtifactFilter;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.changes.RepositoryProbingResult;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.utils.lcd.LazyInitialized;


/**
 * the {@link RepositoryConfigurationProbing} enriches the {@link RepositoryConfiguration} with additionally data 
 * it interactively retrieves from the attached repository servers
 * @author pit / dirk
 *
 */
public class RepositoryConfigurationProbing implements Supplier<RepositoryConfiguration>, HasConnectivityTokens {
	private static Logger log = Logger.getLogger(RepositoryConfigurationProbing.class);
	private final LazyInitialized<RepositoryConfiguration> effective = new LazyInitialized<RepositoryConfiguration>( this::probe); 
	private Supplier<RepositoryConfiguration> repositoryConfigurationSupplier;
	private Function<Repository, RepositoryProbingSupport> repositoryProbingSupportSupplier;
	private ArtifactChangesSynchronization changesSynchronization;
	private BiFunction<File, Repository,ArtifactFilter> artifactFilterSupplier;
	private ProbingResultPersistenceExpert probingResultPersistenceExpert;

	/**
	 * @param probingResultPersistenceExpert - the expert that handles the persistence of the {@link RepositoryProbingResult}
	 */
	@Configurable @Required
	public void setProbingResultPersistenceExpert(ProbingResultPersistenceExpert probingResultPersistenceExpert) {
		this.probingResultPersistenceExpert = probingResultPersistenceExpert;
	}
	
	/**
	 * @param repositoryProbingSupportSupplier - a {@link Function} that returns an appropriate {@link RepositoryProbingSupport} for the given {@link Repository}
	 */
	@Configurable @Required
	public void setRepositoryProbingSupportSupplier( Function<Repository, RepositoryProbingSupport> repositoryProbingSupportSupplier) {
		this.repositoryProbingSupportSupplier = repositoryProbingSupportSupplier;
	}
	/**
	 * @param repositoryConfigurationSupplier - a {@link Supplier} that delivers the {@link RepositoryConfiguration}
	 */
	@Required @Configurable
	public void setRepositoryConfigurationSupplier(Supplier<RepositoryConfiguration> repositoryConfigurationSupplier) {
		this.repositoryConfigurationSupplier = repositoryConfigurationSupplier;
	}
	
	@Configurable @Required
	public void setChangesSynchronization(ArtifactChangesSynchronization changesSynchronization) {
		this.changesSynchronization = changesSynchronization;
	}
	@Configurable @Required
	public void setArtifactFilterSupplier(BiFunction<File, Repository, ArtifactFilter> artifactFilterSupplier) {
		this.artifactFilterSupplier = artifactFilterSupplier;
	}
	
	/**
	 * @return - the 'probed' or 'enriched' or 'fully qualified' {@link RepositoryConfiguration}
	 */
	private RepositoryConfiguration probe() {
		RepositoryConfiguration config = repositoryConfigurationSupplier.get();
		
		if (config.hasFailed())
			return config;
	
		RepositoryConfiguration probedRepositoryConfiguration = config.clone( ConfigurableCloningContext.build().done());
		if (probedRepositoryConfiguration.getOffline()) {
			for (Repository repository : probedRepositoryConfiguration.getRepositories()) {
				if (repository instanceof MavenHttpRepository) {
					repository.setOffline(true);
				}
			}
			return probedRepositoryConfiguration;
		}
				
		// probe all repositories of the configuration in parallel	
		Map<Repository, List<VersionedArtifactIdentification>> repositoryToChangedArtifactIdentificatons = new HashMap<>();
		ExecutorService executorService = Executors.newFixedThreadPool( Math.min(probedRepositoryConfiguration.getRepositories().size(), 10));
		try {
			Map<Repository,Future<List<VersionedArtifactIdentification>>> futures = new LinkedHashMap<>(probedRepositoryConfiguration.getRepositories().size());
			for (Repository repository : probedRepositoryConfiguration.getRepositories()) {
				
				// declared offline, hence can be skipped
				if (repository.getOffline()) {
					continue;
				}
				// TODO: is it correct to simply omit the local repository by name?
				// no probing for the repo standing in for the 'local repository'
				if (repository.getName().equals("local")) {
					continue;
				}
				if (repository instanceof CodebaseRepository) {
					continue;
				}
				if (repository instanceof WorkspaceRepository) {
					continue;
				}
				futures.put( repository, executorService.submit( () -> probe(probedRepositoryConfiguration, repository)));
			}
			// collect the values and 'contextualize' all thrown exceptions
			List<Throwable> throwables = new ArrayList<>();
			for (Map.Entry<Repository,Future<List<VersionedArtifactIdentification>>> entry : futures.entrySet()) {
				try {
					Future<List<VersionedArtifactIdentification>> future = entry.getValue();
					List<VersionedArtifactIdentification> changedArtifactIdentifications = future.get();
					repositoryToChangedArtifactIdentificatons.put( entry.getKey(), changedArtifactIdentifications);
				} catch (InterruptedException e) {
					Repository repository = entry.getKey();
					IllegalStateException ilsException = new IllegalStateException( "error while probing [" + repository.getName() + "]", e);				
					throwables.add( ilsException);
				} catch (ExecutionException e) {
					Repository repository = entry.getKey();
					IllegalStateException ilsException = new IllegalStateException( "error while probing [" + repository.getName() + "]", e.getCause());				
					throwables.add( ilsException);						
				}
			}
			if (throwables.size() > 0) {
				RuntimeException re = new RuntimeException("probing failed");
				for (Throwable throwable : throwables) {
					re.addSuppressed(throwable);
				}
				throw re;
			}		
		}
		finally {
			executorService.shutdown();
		}
		// TODO : review
		// if any of the probed repository has a probing failure attached, the repository configuration has failed as well.
		List<Reason> failedRepositoryReasons = probedRepositoryConfiguration.getRepositories().stream().filter( r -> r.hasFailed()).map( r -> r.getFailure()).collect(Collectors.toList());
		
		if (failedRepositoryReasons.size() > 0) {
			
			ProbingFailed umbrellaReason = TemplateReasons.build(ProbingFailed.T)
											.assign( ProbingFailed::setTimestamp, new Date())
											.causes( failedRepositoryReasons)
											.toReason();
			probedRepositoryConfiguration.setFailure(umbrellaReason);
			
		}
		changesSynchronization.purge(new File(probedRepositoryConfiguration.cachePath()), repositoryToChangedArtifactIdentificatons);
		return probedRepositoryConfiguration;
	}
	/**
	 * @param probedRepositoryConfiguration - the {@link RepositoryConfiguration} to probe (actually only its local repo data is used)
	 * @param repository - the {@link Repository} to probe
	 */
	private List<VersionedArtifactIdentification> probe(RepositoryConfiguration probedRepositoryConfiguration, Repository repository) {
		long before = System.currentTimeMillis();
		boolean isOffline = repository.getOffline();
		if (isOffline) {
			return new ArrayList<>();
		}
		RepositoryProbingSupport probingSupport = repositoryProbingSupportSupplier.apply(repository);
		RepositoryProbingResult probingResult = probingSupport.probe();
		
		if (probingResult.hasFailed()) {
			Reason failure = probingResult.getFailure();

			log.warn("Repository probing result state for [" + repository.getName() + "(" + (repository.getSnapshotRepo() ? "snapshot" : "release")
					+ ")] is " + failure.stringify() + ". Switched to offline");
			
			repository.setOffline( true);
			repository.setFailure(failure);
		}
		if (repository.getRestSupport() == null) {
			repository.setRestSupport( probingResult.getRepositoryRestSupport());
		}
		if (repository.getChangesUrl() == null) {
			repository.setChangesUrl( probingResult.getChangesUrl());
		}
		
		//
		// store last probing result.
		if (repository.getCachable()) {
			File localRepository = new File(probedRepositoryConfiguration.cachePath());
			probingResultPersistenceExpert.writeProbingResult(probingResult, repository, localRepository);
		}
		
		
		// actually do some probing on the repository		
		List<VersionedArtifactIdentification> changedVersionedArtifactIdentifications = null;
		
		File localRepo = new File( probedRepositoryConfiguration.cachePath());
		changedVersionedArtifactIdentifications = changesSynchronization.queryChanges(localRepo, repository);
		if (changedVersionedArtifactIdentifications != null && changedVersionedArtifactIdentifications.size() != 0) {
		 
			 // let the consumer of the changes artifacts know what has changed 
			  // let the filter supplier give us the the current filter  
			 if (artifactFilterSupplier != null) { 
				ArtifactFilter artifactFilter = artifactFilterSupplier.apply(localRepo, repository);				
				if (repository.getArtifactFilter() == null) {
					// no preconfigured filter, assign whatever the supplier returned 
					repository.setArtifactFilter(artifactFilter);
				}
				else {
					// preconfigured filter, build a conjunction filter
					ConjunctionArtifactFilter conjunctionFilter = ConjunctionArtifactFilter.T.create();
					conjunctionFilter.getOperands().add(repository.getArtifactFilter());
					conjunctionFilter.getOperands().add(artifactFilter);
					repository.setArtifactFilter(conjunctionFilter);
				}
			 }
		}
		else {
			changedVersionedArtifactIdentifications = Collections.emptyList();
		}
	
	
		if (log.isDebugEnabled()) {
			long after = System.currentTimeMillis();
			long dif = after - before;
			log.debug("probing [" + repository.getName() + "] took [" + dif + "] ms");
		}
		return changedVersionedArtifactIdentifications;
	}

	
	
	@Override
	public RepositoryConfiguration get() {	
		return effective.get();
	} 
	
	

}
