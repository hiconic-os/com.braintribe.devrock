// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.space;

import java.io.File;

import com.braintribe.build.artifact.api.BuildRangeDependencyResolver;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.cache.PartCacheInstance;
import com.braintribe.build.artifact.retrieval.multi.enriching.TransactionalMultiRepositorySolutionEnricherImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RepositoryConfigurationExposure;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.MultiRepositoryDependencyResolverImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.PlainOptimisticDependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.build.artifact.walk.build.BuildDependencyResolver;
import com.braintribe.build.artifact.walk.build.ParallelBuildDependencyResolver;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.GeneralConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.IntransitiveResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.MavenSettingsContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.ResolutionVisitingContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class BuildDependencyResolutionSpace implements BuildDependencyResolutionContract {
	
	@Import
	private IntransitiveResolutionContract intransitiveResolution;
	
	@Import
	private GeneralConfigurationContract commons;
	
	@Import
	private FilterConfigurationContract filterConfiguration;
	
	@Import
	private ResolutionVisitingContract resolutionVisiting;
	
	@Import
	private MavenSettingsContract settingsContract;
	
	@Import
	private NotificationSpace notificationContract;
	
	
	@Managed
	public MavenSettingsExpertFactory settingsExpertFactory() {
		MavenSettingsExpertFactory bean = new MavenSettingsExpertFactory();
		//
		bean.setSettingsPeristenceExpert(settingsContract.settingsPersistenceExpert());
		bean.setInjectedRepositoryRetrievalExpert( settingsContract.localRepositoryLocationProvider());
		bean.setVirtualEnvironment(commons.virtualEnvironment());
		return bean;
	}
	
	public ArtifactPomReader leanPomReader() {
		ArtifactPomReader bean = leanPomExpertFactory().getReader();
		PomReaderNotificationListener listener = notificationContract.pomReaderNotificationListener();
		if (listener != null) {
			bean.addListener(listener);
		}
		return bean;
	}

	@Override
	public ArtifactPomReader pomReader() {
		ArtifactPomReader bean = pomExpertFactory().getReader();
		bean.setEnforceParentResolving(true);
		PomReaderNotificationListener listener = notificationContract.pomReaderNotificationListener();
		if (listener != null) {
			bean.addListener(listener);
		}
		return bean;
	}
	
	@Managed 
	public PomExpertFactory leanPomExpertFactory() {
		PomExpertFactory bean = new PomExpertFactory();
		
		bean.setSettingsReader(settingsReader());
		bean.setVirtualEnvironment(commons.virtualEnvironment());
		bean.setDependencyResolverFactory(intransitiveResolution::intransitiveDependencyResolver);
		bean.setCacheFactory(cacheFactory());
		bean.setEnforceParentResolving(false);
		bean.setIdentifyArtifactOnly(true);
		
		return bean;
	}

	@Managed 
	public PomExpertFactory pomExpertFactory() {
		PomExpertFactory bean = new PomExpertFactory();
		
		bean.setVirtualEnvironment(commons.virtualEnvironment());
		bean.setSettingsReader(settingsReader());
		bean.setDependencyResolverFactory(intransitiveResolution::intransitiveDependencyResolver);
		bean.setCacheFactory(cacheFactory());
		bean.setEnforceParentResolving(true);
		bean.setIdentifyArtifactOnly(false);
		
		return bean;
	}
	
	@Managed
	public DependencyResolver standardDependencyResolver() {
		MultiRepositoryDependencyResolverImpl bean = new MultiRepositoryDependencyResolverImpl();
		
		bean.setRepositoryRegistry(repositoryReflection());
		bean.setPomExpertFactory(pomExpertFactory());
		
		return bean;
	}
	
	
	@Managed
	public CacheFactoryImpl cacheFactory() {
		CacheFactoryImpl bean = new CacheFactoryImpl();
		return bean;
	}
	
	@Managed
	@Override
	public RepositoryReflectionImpl repositoryReflection() {
		RepositoryReflectionImpl bean = new RepositoryReflectionImpl();
		
		bean.setInterrogationClientFactory(repositoryInterrogationClientFactory());
		bean.setAccessClientFactory(repositoryAccessClientFactory());
		bean.setRavenhurstScope(ravenhurstScope());
		bean.setMavenSettingsReader(settingsReader());
		bean.setLocalRepositoryLocationProvider(settingsReader());
		bean.setArtifactFilterExpertSupplier(ravenhurstScope());
		bean.setRepositoryViewResolutionSupplier(ravenhurstScope()::getRepositoryViewResolution);
		bean.setCurrentScopeId( commons.globalMalaclypseScopeId());
		bean.setLockFactory(lockFactory());
		
		return bean;
	}
	
	@Managed 
	public RepositoryInterrogationClientFactoryImpl repositoryInterrogationClientFactory() {
		RepositoryInterrogationClientFactoryImpl bean = new RepositoryInterrogationClientFactoryImpl();
		return bean;
	}
	
	@Managed 
	public RepositoryAccessClientFactoryImpl repositoryAccessClientFactory() {
		RepositoryAccessClientFactoryImpl bean = new RepositoryAccessClientFactoryImpl();
		return bean;
	}
		
	public MavenSettingsReader settingsReader() {
		MavenSettingsReader bean = settingsExpertFactory().getMavenSettingsReader();	
		return bean;
	}
	
	@Override
	public File localRepository() {
		return new File(settingsReader().getLocalRepository());
	}
	
	
	@Override
	public RepositoryConfigurationExposure repositoryConfigurationExposure() {	
		return ravenhurstScope();
	}

	@Managed
	public RavenhurstScopeImpl ravenhurstScope() {
		RavenhurstScopeImpl bean = new RavenhurstScopeImpl();
		bean.setReader(settingsReader());
		bean.setVirtualEnvironment(commons.virtualEnvironment());
		bean.setLockFactory(lockFactory());
		return bean;
	}

	@Override
	public BuildRangeDependencyResolver buildDependencyResolver() {
		if (commons.resolveParallel())
			return parallelBuildDependencyResolver();
		else
			return sequentialBuildDependencyResolver(); 
	}
	
	@Managed
	private BuildDependencyResolver sequentialBuildDependencyResolver() {
		BuildDependencyResolver bean = new BuildDependencyResolver();
		
		bean.setPomReader(pomReader());
		bean.setSolutionDependencyVisitor(resolutionVisiting.solutionDependencyVisitor());
		bean.setDependencyResolver(intransitiveResolution.intransitiveDependencyResolver());
		bean.setLenient(commons.lenient());
		bean.setArtifactFilter(filterConfiguration.artifactFilter());
		bean.setDependencyFilter(filterConfiguration.dependencyFilter());
		bean.setSolutionDependencyFilter(filterConfiguration.solutionDependencyFilter());
		bean.setSolutionFilter(filterConfiguration.solutionFilter());
		bean.setEnricher(solutionEnricher());
		bean.setWalkParentStructure(commons.walkParentStructure());
		bean.setFilterSolutionBeforeVisit(filterConfiguration.filterSolutionBeforeVisit());
		
		return bean;
	}
	
	@Managed
	private ParallelBuildDependencyResolver parallelBuildDependencyResolver() {
		ParallelBuildDependencyResolver bean = new ParallelBuildDependencyResolver();
		
		bean.setPomReader(pomReader());
		bean.setSolutionDependencyVisitor(resolutionVisiting.solutionDependencyVisitor());
		bean.setDependencyResolver(intransitiveResolution.intransitiveDependencyResolver());
		bean.setLenient(commons.lenient());
		bean.setArtifactFilter(filterConfiguration.artifactFilter());
		bean.setDependencyFilter(filterConfiguration.dependencyFilter());
		bean.setSolutionDependencyFilter(filterConfiguration.solutionDependencyFilter());
		bean.setSolutionFilter(filterConfiguration.solutionFilter());
		bean.setEnricher(solutionEnricher());
		bean.setWalkParentStructure(commons.walkParentStructure());
		bean.setRespectExclusions(commons.respectExclusions());
		bean.setFilterSolutionBeforeVisit(filterConfiguration.filterSolutionBeforeVisit());
		
		return bean;
	}
	
	@Managed
	public LockFactory lockFactory() {		
		LockFactory bean = new FilesystemSemaphoreLockFactory();		
		return bean;
	}
	
	@Managed
	@Override
	public TransactionalMultiRepositorySolutionEnricherImpl solutionEnricher() {
		TransactionalMultiRepositorySolutionEnricherImpl bean = new TransactionalMultiRepositorySolutionEnricherImpl();
		
		bean.setRepositoryRegistry(repositoryReflection());
		bean.setRelevantPartTuples(filterConfiguration.partExpectation());
		bean.setRelevantPartPredicate(filterConfiguration.partFilter());
		bean.setCache(new PartCacheInstance());
		bean.setDisableDependencyPartExpectationClassifierInfluence(true);
		bean.setLockFactory(lockFactory());
		return bean;
	}

	@Override
	public com.braintribe.build.artifact.api.DependencyResolver plainOptimisticDependencyResolver() {
		PlainOptimisticDependencyResolver bean = new PlainOptimisticDependencyResolver();
		bean.setDelegate( intransitiveResolution.intransitiveDependencyResolver());
		bean.setEnricher( solutionEnricher());
		return bean;
	}
	
	
}
