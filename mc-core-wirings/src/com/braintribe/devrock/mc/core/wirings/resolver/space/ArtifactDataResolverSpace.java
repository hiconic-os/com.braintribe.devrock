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
package com.braintribe.devrock.mc.core.wirings.resolver.space;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.repository.configuration.ArtifactChangesSynchronization;
import com.braintribe.devrock.mc.api.repository.configuration.HasConnectivityTokens;
import com.braintribe.devrock.mc.api.repository.local.ArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.api.resolver.PartAvailabilityReflection;
import com.braintribe.devrock.mc.core.commons.FilesystemLockPurger;
import com.braintribe.devrock.mc.core.compiled.ArtifactCompiler;
import com.braintribe.devrock.mc.core.compiled.CachingCompiledArtifactResolver;
import com.braintribe.devrock.mc.core.compiled.RedirectAwareCompilingArtifactResolver;
import com.braintribe.devrock.mc.core.configuration.BasicArtifactChangesSynchronization;
import com.braintribe.devrock.mc.core.configuration.BasicGroupFilterPersistenceExpert;
import com.braintribe.devrock.mc.core.configuration.BasicRepositoryReflection;
import com.braintribe.devrock.mc.core.configuration.OriginationPreparation;
import com.braintribe.devrock.mc.core.configuration.ProbingResultPersistenceExpert;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationEnriching;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationProbing;
import com.braintribe.devrock.mc.core.configuration.bias.PcBiasCompiler;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactResolver;
import com.braintribe.devrock.mc.core.download.BasicPartDownloadManager;
import com.braintribe.devrock.mc.core.download.BasicPartEnricher;
import com.braintribe.devrock.mc.core.filters.ArtifactFilters;
import com.braintribe.devrock.mc.core.repository.local.BasicArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.core.resolver.BasicDependencyResolver;
import com.braintribe.devrock.mc.core.resolver.CachingCompiledArtifactAssocResolver;
import com.braintribe.devrock.mc.core.resolver.CachingDependencyResolver;
import com.braintribe.devrock.mc.core.resolver.DeferredCompiledArtifactIdentificationAssocResolver;
import com.braintribe.devrock.mc.core.resolver.FailingArtifactResolver;
import com.braintribe.devrock.mc.core.resolver.LocalRepositoryCachingArtifactResolver;
import com.braintribe.devrock.mc.core.resolver.ReflectedArtifactResolver;
import com.braintribe.devrock.mc.core.wirings.backend.contract.ArtifactDataBackendContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.configuration.space.RepositoryViewResolutionSpace;
import com.braintribe.devrock.mc.core.wirings.impl.configuration.ViewRepositoryConfigurationCompiler;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryBiasAdded;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryBiasLoaded;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryGloballyOffline;
import com.braintribe.devrock.model.repository.LocalRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.AllMatchingArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ConjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.JunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.NoneMatchingArtifactFilter;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.changes.RepositoryProbingResult;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;

/**
 * the wirespace for the {@link ArtifactDataResolverContract}
 * 
 * @author pit / dirk
 *
 */
@Managed
public class ArtifactDataResolverSpace implements ArtifactDataResolverContract {

	private static final String LOCAL = "local";

	@Import
	RepositoryConfigurationContract repositoryConfiguration;

	@Import
	ArtifactDataBackendContract artifactDataBackend;
	
	@Import
	VirtualEnvironmentContract virtualEnvironment;
	
	@Import
	RepositoryViewResolutionSpace repositoryViewResolution;

	/**
	 * @return - the {@link DeclaredArtifactResolver} that resolves DECLARED artifacts
	 */
	@Managed
	private DeclaredArtifactResolver backingDeclaredArtifactResolver() {
		DeclaredArtifactResolver bean = new DeclaredArtifactResolver();
		DeclaredArtifactMarshaller marshaller = new DeclaredArtifactMarshaller();
		bean.setMarshaller(marshaller);
		bean.setPartResolver(artifactResolver());
		return bean;
	}

	/**
	 * @return - a caching DECLARED artifact resolver, wrapped around the {@link #backingDeclaredArtifactResolver()}
	 */
	@Managed
	private CachingCompiledArtifactAssocResolver<DeclaredArtifact> declaredArtifactResolver() {
		CachingCompiledArtifactAssocResolver<DeclaredArtifact> bean = new CachingCompiledArtifactAssocResolver<>();
		bean.setDelegate(backingDeclaredArtifactResolver());
		// ev. add update filter (stale cache entry)
		return bean;
	}

	@Override
	public DeclaredArtifactCompiler declaredArtifactCompiler() {
		return artifactCompiler();
	}

	/**
	 * @return - low level {@link DependencyResolver}
	 */
	@Managed
	private DependencyResolver backingDependencyResolver() {
		BasicDependencyResolver bean = new BasicDependencyResolver(artifactResolver());
		return bean;
	}

	@Override
	@Managed
	public CachingDependencyResolver dependencyResolver() {
		CachingDependencyResolver bean = new CachingDependencyResolver(backingDependencyResolver());
		return bean;
	}
	
	@Managed
	private ArtifactCompiler artifactCompiler() {
		ArtifactCompiler bean = new ArtifactCompiler();
		bean.setDependencyResolver(dependencyResolver());
		bean.setDeclaredArtifactResolver(declaredArtifactResolver());
		bean.setCompiledArtifactResolver(redirectAwareCompiledArtifactResolver());
		return bean;
	}
	
	@Managed
	private CachingCompiledArtifactResolver cachingArtifactResolver() {
		CachingCompiledArtifactResolver bean = new CachingCompiledArtifactResolver();
		bean.setDelegate(deferringArtifactCompiler());
		return bean;
	}
	
	
	@Managed
	// this bean exists to defer bean resolution to avoid a concurrency issue when resolving cachingArtifactResolver and artifactCompiler concurrent from parallel threads
	private DeferredCompiledArtifactIdentificationAssocResolver<CompiledArtifact> deferringArtifactCompiler() {
		DeferredCompiledArtifactIdentificationAssocResolver<CompiledArtifact> bean = new DeferredCompiledArtifactIdentificationAssocResolver<>(this::artifactCompiler);
		return bean;
	}
	
	@Override 
	@Managed
	public RedirectAwareCompilingArtifactResolver redirectAwareCompiledArtifactResolver() {
		RedirectAwareCompilingArtifactResolver bean = new RedirectAwareCompilingArtifactResolver();
		bean.setDelegate(cachingArtifactResolver());
		return bean;
	}
	
	@Override 
	public CompiledArtifactResolver directCompiledArtifactResolver() {
		return cachingArtifactResolver();
	}
	
	/**
	 * @return - a {@link ProbingResultPersistenceExpert} to handle the persistenc of {@link RepositoryProbingResult}
	 */
	@Managed 
	public ProbingResultPersistenceExpert probingResultPersistenceExpert() {
		ProbingResultPersistenceExpert bean = new ProbingResultPersistenceExpert();
		bean.setLockSupplier(backendContract().lockSupplier());
		return bean;
	}

	/**
	 * @param repository - the {@link Repository} the {@link ArtifactPartResolverPersistenceDelegate} is delegate of
	 * @return - a fully wired {@link ArtifactPartResolverPersistenceDelegate}
	 */
	@Managed(Scope.prototype)
	private ArtifactPartResolverPersistenceDelegate artifactPartResolverPersistenceDelegate(Repository repository) {

		BasicArtifactPartResolverPersistenceDelegate bean = new BasicArtifactPartResolverPersistenceDelegate();
		bean.setResolver(artifactDataBackend.repository(repository));
		bean.setRestSupport( repository.getRestSupport());
		
		// here the repository can reflect whether it's a RH driven repository and convey it to the delegate
		bean.setDynamic( repository.getChangesUrl() != null);
		
		bean.setProbingResultPersistenceExpert(probingResultPersistenceExpert());
		bean.setCachable(repository.getCachable());

		TimeSpan updateTimeSpan = repository.getUpdateTimeSpan();
		if (updateTimeSpan != null) {
			bean.setDuration(updateTimeSpan.toDuration());
		}
		bean.setRepository(repository);
		
		// filter for artifacts 
		ArtifactFilter artifactFilter = repository.getArtifactFilter();					
		if (artifactFilter == null) {											
			artifactFilter = AllMatchingArtifactFilter.T.create();
		}			
		bean.setArtifactFilter(ArtifactFilters.forDenotation(artifactFilter));
			
		// filter for dominance
		ArtifactFilter dominanceFilter = repository.getDominanceFilter();
		if (dominanceFilter ==  null) {
			dominanceFilter = NoneMatchingArtifactFilter.T.create();
		}
		bean.setDominanceFilter(ArtifactFilters.forDenotation(dominanceFilter));
		
		return bean;
	}
	
	/**
	 * @return - a qualified {@link BasicGroupFilterPersistenceExpert}
	 */
	@Managed 
	private BasicGroupFilterPersistenceExpert groupFilterPersistenceExpert() {
		BasicGroupFilterPersistenceExpert bean = new BasicGroupFilterPersistenceExpert();
		bean.setLockSupplier(backendContract().lockSupplier());
		return bean;
	}
	
	@Managed
	@Override
	public ArtifactChangesSynchronization changesSynchronization() {
		BasicArtifactChangesSynchronization bean = new BasicArtifactChangesSynchronization();
		bean.setHttpClient(artifactDataBackend.httpClient());
		bean.setLockSupplier(backendContract().lockSupplier());
		bean.setGroupFilterExpert(groupFilterPersistenceExpert());
		bean.setArtifactDataResolverFactory( this.artifactDataBackend::artifactoryRepository);
		return bean;
	}

	/**
	 * @return - a wrapper around the 'raw' {@link RepositoryConfiguration} as
	 *         return by the imported space, used to enrich the configuration with
	 *         runtime 'probed' data
	 */
	@Managed
	private RepositoryConfigurationProbing repositoryConfigurationProbing() {
		RepositoryConfigurationProbing bean = new RepositoryConfigurationProbing();
		bean.setRepositoryConfigurationSupplier(this::enrichedRepositoryConfiguration);
		bean.setRepositoryProbingSupportSupplier(artifactDataBackend::probingSupport);	
		bean.setArtifactFilterSupplier( changesSynchronization()::getFilterForRepository);
		bean.setChangesSynchronization(changesSynchronization());
		bean.setProbingResultPersistenceExpert(probingResultPersistenceExpert());
		return bean;
	}
	
	/**
	 * @param localRepository - the {@link File} pointing the the 'local repository's root'
	 * @return - a qualified {@link PcBiasCompiler}
	 */
	@Managed
	private PcBiasCompiler biasCompiler(File localRepository) {
		PcBiasCompiler bean = new PcBiasCompiler();
		bean.setLocalRepository(localRepository);
		bean.setLockSupplier(backendContract().lockSupplier());
		return bean;
	}
	
	@Managed
	public ViewRepositoryConfigurationCompiler viewRepositoryConfigurationCompiler() {
		ViewRepositoryConfigurationCompiler bean = new ViewRepositoryConfigurationCompiler(repositoryConfiguration.repositoryConfiguration(), virtualEnvironment.virtualEnvironment());
		return bean;
	}
	
	@Managed
	private RepositoryConfiguration baseRepositoryConfiguration() {
		RepositoryConfiguration bean = viewRepositoryConfigurationCompiler().repositoryConfiguration();
		return bean;
	}

	@Managed
	public RepositoryConfiguration enrichedRepositoryConfiguration() {
		RepositoryConfiguration declaredRepositoryConfiguration = baseRepositoryConfiguration();
		
		if (declaredRepositoryConfiguration.hasFailed())
			return declaredRepositoryConfiguration;
		
		ensureLocalRepository(declaredRepositoryConfiguration);
		
		// timespan logic : if it's updateable, and it has no timespan set, the timespan is defaulted to 'daily'
		for (Repository repository: declaredRepositoryConfiguration.getRepositories()) {
			if (repository.getUpdateable() && repository.getUpdateTimeSpan() == null) {
				repository.setUpdateTimeSpan( TimeSpan.create(1, TimeUnit.day));			
			}
		}
					
		// load bias 
		PcBiasCompiler biasCompiler = biasCompiler( new File(declaredRepositoryConfiguration.cachePath()));
		boolean anyBias = biasCompiler.loadPcBias();
		
		// any bias loaded? 
		if (anyBias) {		
			// create a bias origination 
			String pcBiasFile = biasCompiler.getBiasFile().getAbsolutePath();						
			Reason biasActiveReason = TemplateReasons.build( RepositoryBiasLoaded.T).assign(RepositoryBiasLoaded::setBiasFilename, pcBiasFile).toReason();								
			Reason origination = OriginationPreparation.acquireOrigination(declaredRepositoryConfiguration);
			origination.getReasons().add(biasActiveReason);
			
			
			for (Repository repository: declaredRepositoryConfiguration.getRepositories()) {
				
				// bias 
				Pair<ArtifactFilter,ArtifactFilter> biasFiltersForCurrentRepo = biasCompiler.findBiasFilters( repository.getName());
				
				if (biasFiltersForCurrentRepo != null) {
					// add a bias 
					Reason biasReason = TemplateReasons.build( RepositoryBiasAdded.T)							
							.assign( RepositoryBiasAdded::setRepositoryId, repository.getName())
							.toReason();									
					
					biasActiveReason.getReasons().add(biasReason);
					
					ArtifactFilter dominanceFilter = biasFiltersForCurrentRepo.first();
					ArtifactFilter artifactFilter = biasFiltersForCurrentRepo.second();
					
					repository.setArtifactFilter(enrichFilter(repository.getArtifactFilter(), artifactFilter, ConjunctionArtifactFilter.T::create));
					repository.setDominanceFilter(enrichFilter(repository.getDominanceFilter(), dominanceFilter, DisjunctionArtifactFilter.T::create));
				}
			}
		}
		
		repositoryConfigurationEnriching().enrich(declaredRepositoryConfiguration);
		
		// check global offline switch via environment
		String mode = virtualEnvironment.virtualEnvironment().getEnv( HasConnectivityTokens.MC_CONNECTIVITY_MODE);
		if (mode != null && mode.equalsIgnoreCase( HasConnectivityTokens.MODE_OFFLINE)) {
			declaredRepositoryConfiguration.setOffline( true);
			 Reason reason = OriginationPreparation.acquireOrigination(declaredRepositoryConfiguration);
			 reason.getReasons().add( Reasons.build( RepositoryGloballyOffline.T)
					 								.text( "switched to offline because [" + HasConnectivityTokens.MC_CONNECTIVITY_MODE + "] was set to [" + HasConnectivityTokens.MODE_OFFLINE + "]")
					 								.toReason()
					 				);
		}
		
		// only set remote http repositories to offline
		if (declaredRepositoryConfiguration.getOffline()) {
			declaredRepositoryConfiguration.getRepositories().stream().filter( r -> r instanceof MavenHttpRepository).forEach(r -> r.setOffline(true));
		}

		return declaredRepositoryConfiguration;
	}
	
	@Override
	@Managed
	public BasicRepositoryReflection repositoryReflection() {
		BasicRepositoryReflection bean = new BasicRepositoryReflection();
		bean.setRepositoryConfiguration(effectiveRepositoryConfiguration());
		bean.setRepositoryViewResolutionSupplier(repositoryViewResolution.repositoryViewResolutionHolder());
		return bean;
	}
	
	private ArtifactFilter enrichFilter(ArtifactFilter existingFilter, ArtifactFilter additionalFilter, Supplier<JunctionArtifactFilter> junctionSupplier) {
		if (existingFilter == null)
			return additionalFilter;
		
		if (additionalFilter == null)
			return existingFilter;
		
		JunctionArtifactFilter combinedFilter = junctionSupplier.get();
		List<ArtifactFilter> operands = combinedFilter.getOperands();
		operands.add(existingFilter);
		operands.add(additionalFilter);

		return combinedFilter;
	}

	@Managed
	public RepositoryConfigurationEnriching repositoryConfigurationEnriching() {
		RepositoryConfigurationEnriching bean = new RepositoryConfigurationEnriching();
		return bean;
	}
	
	@Override
	@Managed
	/* (non-Javadoc)
	* 
	* @see com.braintribe.devrock.mc.core.wirings.resolver.contract.
	* ArtifactDataResolverContract#artifactResolver()
	*/
	public ReflectedArtifactResolver artifactResolver() {
		RepositoryConfiguration effectiveRepositoryConfiguration = effectiveRepositoryConfiguration();
		
		if (effectiveRepositoryConfiguration.hasFailed()) {
			return new FailingArtifactResolver(effectiveRepositoryConfiguration.getFailure());
		}
		else {
			return artifactResolver(effectiveRepositoryConfiguration);
		}
	}
	
	public LocalRepositoryCachingArtifactResolver artifactResolver(RepositoryConfiguration effectiveRepositoryConfiguration) {
		LocalRepositoryCachingArtifactResolver bean = new LocalRepositoryCachingArtifactResolver();
		
		File localRepositoryDirectory = new File( effectiveRepositoryConfiguration.cachePath());
		
		bean.setLocalRepository(localRepositoryDirectory);
		List<ArtifactPartResolverPersistenceDelegate> delegates = new ArrayList<>();
		
		for (Repository repository : effectiveRepositoryConfiguration.getRepositories()) {
			// returns an empty-repo-delegate if repository is offline 
			ArtifactPartResolverPersistenceDelegate persistenceDelegate = artifactPartResolverPersistenceDelegate(repository);
			delegates.add( persistenceDelegate);
		}
		
		bean.setDelegates(delegates);
		bean.setLockProvider(backendContract().lockSupplier());
		return bean;
	}

	private RepositoryConfiguration effectiveRepositoryConfiguration() {
		return repositoryConfigurationProbing().get();
	}
	
	@Override
	@Managed
	public File localRepositoryRoot() {
		RepositoryConfiguration effectiveRepositoryConfiguration = repositoryConfigurationProbing().get();
		//File localRepositoryDirectory = new File(effectiveRepositoryConfiguration.getLocalRepositoryPath());
		File localRepositoryDirectory = new File(effectiveRepositoryConfiguration.cachePath());
		return localRepositoryDirectory;
	}
	
	/**
	 * ensures that 
	 */
	private void ensureLocalRepository(RepositoryConfiguration declaredRespositoryConfiguration) {
		
		if (declaredRespositoryConfiguration.getCachePath() != null)
			return;
		
		Optional<Repository> localRepoOptional = declaredRespositoryConfiguration.getRepositories().stream().filter(r -> r instanceof LocalRepository).findFirst();
		
		final LocalRepository localRepository;
		
		if (localRepoOptional.isPresent()) {
			localRepository = (LocalRepository) localRepoOptional.get();
		}
		else {
			localRepository = LocalRepository.T.create();
			declaredRespositoryConfiguration.getRepositories().add(0, localRepository);
		}
		
		if (localRepository.getRootPath() == null) {
			localRepository.setRootPath(declaredRespositoryConfiguration.getLocalRepositoryPath());
		}
		
		localRepository.setName(LOCAL);
	}

	@Override
	@Managed
	public BasicPartDownloadManager partDownloadManager() {
		BasicPartDownloadManager bean = new BasicPartDownloadManager();
		bean.setPartResolver(artifactResolver());
		return bean;
	}
	
	@Override
	public BasicPartEnricher partEnricher() {
		BasicPartEnricher bean = new BasicPartEnricher();
		bean.setPartDownloadManager(partDownloadManager());
		return bean;
	}

	@Override
	public PartAvailabilityReflection partAvailabilityReflection() {
			return artifactResolver();
	}

	@Override
	public ArtifactDataBackendContract backendContract() {	
		return artifactDataBackend;
	}

	@Override
	@Managed
	public FilesystemLockPurger lockFilePurger() {	
		FilesystemLockPurger bean = new FilesystemLockPurger();
		bean.setRepositoryRoot( repositoryConfiguration.repositoryConfiguration().get().cachePath());
		return bean;
	}

	
	
}
