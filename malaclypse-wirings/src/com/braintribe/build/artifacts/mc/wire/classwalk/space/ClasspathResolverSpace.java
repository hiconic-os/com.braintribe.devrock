package com.braintribe.build.artifacts.mc.wire.classwalk.space;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.cache.PartCacheInstance;
import com.braintribe.build.artifact.retrieval.multi.enriching.ConfigurableMultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.enriching.TransactionalMultiRepositorySolutionEnricherImpl;
import com.braintribe.build.artifact.retrieval.multi.enriching.queued.QueueingMultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhurstPersistenceExpertForMainDataContainer;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflectionSupport;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.MultiRepositoryDependencyResolverImpl;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.build.artifact.walk.multi.EnrichingWalkerImpl;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifact.walk.multi.WalkerImpl;
import com.braintribe.build.artifact.walk.multi.clash.ConfigurableClashResolver;
import com.braintribe.build.artifact.walk.multi.clash.OptimisticWeedingClashResolver;
import com.braintribe.build.artifact.walk.multi.clash.listener.ClashResolverNotificationListener;
import com.braintribe.build.artifact.walk.multi.exclusion.ExclusionControl;
import com.braintribe.build.artifact.walk.multi.listener.WalkNotificationListener;
import com.braintribe.build.artifact.walk.multi.scope.ScopeControl;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.MagicScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.Scope;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;

/**
 * wiring for the classpath walk
 * 
 * @author pit
 *
 */
@Managed
public class ClasspathResolverSpace implements ClasspathResolverContract {
	@Import
	private ClasspathResolverExternalContract externalConfiguration;
	
	@Override
	public ArtifactPomReader pomReader() {
		ArtifactPomReader bean = pomExpertFactory().getReader();
		return bean;
	}
	
	
	@Managed
	public MavenSettingsExpertFactory settingsExpertFactory() {
		MavenSettingsExpertFactory bean = new MavenSettingsExpertFactory();
		bean.setVirtualEnvironment(externalConfiguration.virtualEnvironment());		
		bean.setSettingsPeristenceExpert(externalConfiguration.overrideSettingsPersistenceExpert());
		bean.setInjectedRepositoryRetrievalExpert(externalConfiguration.overrideLocalRepositoryLocationExpert());
		bean.setMavenProfileActivationExpert( externalConfiguration.overrideProfileActivationExpert());
		return bean;
	}
	
	@Managed
	@Override
	public DependencyResolver dependencyResolver() {
		Function<DependencyResolver, DependencyResolver> topDependencyResolver = externalConfiguration.dependencyResolverEnricher();
		
		if (topDependencyResolver == null)
			return standardDependencyResolver();
		else
			return topDependencyResolver.apply(standardDependencyResolver());
	}
	
	@Managed
	public DependencyResolver standardDependencyResolver() {
		MultiRepositoryDependencyResolverImpl bean = new MultiRepositoryDependencyResolverImpl();
		
		bean.setRepositoryRegistry(repositoryReflection());
		bean.setPomExpertFactory(pomExpertFactory());
		
		return bean;
	}
	
	@Managed 
	public PomExpertFactory pomExpertFactory() {
		PomExpertFactory bean = new PomExpertFactory();

		bean.setSettingsReader(settingsReader());
		bean.setVirtualEnvironment(externalConfiguration.virtualEnvironment());
		bean.setDependencyResolverFactory(this::dependencyResolver);
		bean.setCacheFactory(cacheFactory());
		bean.setEnforceParentResolving(true);
		PomReaderNotificationListener pomReaderListener = externalConfiguration.pomReaderNotificationListener();
		if (pomReaderListener != null) {
			bean.addListener( pomReaderListener);
		}
		
		return bean;

	}
		
	
	@Managed
	public CacheFactoryImpl cacheFactory() {
		CacheFactoryImpl bean = new CacheFactoryImpl();
		return bean;
	}
	
	@Managed
	@Override
	public LockFactory lockFactory() {		
		LockFactory bean = new FilesystemSemaphoreLockFactory();		
		return bean;
	}
	
	@Managed
	public RepositoryReflectionImpl repositoryReflection() {
		RepositoryReflectionImpl bean = new RepositoryReflectionImpl();
		
		bean.setInterrogationClientFactory(repositoryInterrogationClientFactory());
		bean.setAccessClientFactory(repositoryAccessClientFactory());
		bean.setRavenhurstScope(ravenhurstScope());
		bean.setMavenSettingsReader(settingsReader());
		bean.setLocalRepositoryLocationProvider(settingsReader());
		bean.setArtifactFilterExpertSupplier(ravenhurstScope());
		LockFactory lockFactory = externalConfiguration.lockFactory();
		if (lockFactory != null) {
			bean.setLockFactory( lockFactory);
		}
		else {
			bean.setLockFactory( lockFactory());
		}
	
		String scopeId = externalConfiguration.globalMalaclypseScopeId();
		if (scopeId != null) {		
			bean.setCurrentScopeId(scopeId);
		}
				
		InstanceConfiguration.currentInstance().onDestroy(bean::closeContext);
	
		return bean;
	}
	
	
	
	@Override
	public RepositoryReflectionSupport repositoryReflectionSupport() {	
		return repositoryReflection();
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
	
	@Managed
	@Override
	public MavenSettingsReader settingsReader() {	
		MavenSettingsReader bean = settingsExpertFactory().getMavenSettingsReader();		
		return bean;
	}
	
	@Override
	@Managed
	public RavenhurstScopeImpl ravenhurstScope() {
		RavenhurstScopeImpl bean = new RavenhurstScopeImpl();
		bean.setReader(settingsReader());
		bean.setVirtualEnvironment(externalConfiguration.virtualEnvironment());
		LockFactory lockFactory = externalConfiguration.lockFactory();
		if (lockFactory != null) {
			bean.setLockFactory( lockFactory);
		}
		else {
			bean.setLockFactory( lockFactory());
		}
		return bean;
	}
	
	

	@Override
	public RavenhurstPersistenceExpertForMainDataContainer ravenhurstMainContainerExpert() {
		RavenhurstPersistenceExpertForMainDataContainer bean = new RavenhurstPersistenceExpertForMainDataContainer();
		bean.setLocalRepositoryLocationProvider(settingsReader());
		bean.setLockFactory(lockFactory());
		return bean;
	}


	@Managed(com.braintribe.wire.api.annotation.Scope.prototype)
	public ScopeControl scopeControl(WalkerContext context) {
		ScopeControl bean = new ScopeControl();
		// no scopes wanted.. 
		if (context.getIgnoreDependencyScopes()) {
			bean.setPassthru(true);
			return bean;
		}
		// use deprecated as fall back 
		Boolean skipOptional = context.getSkipOptionals();
		if (skipOptional != null) {
			bean.setSkipOptional( skipOptional);				
		}
		else {
			bean.setSkipOptional( externalConfiguration.skipOptional());
		}
		// use deprecated as fall back
		Set<Scope> scopes = context.getScopes();
		if (scopes == null) 
			scopes = externalConfiguration.scopes();
		
		for (Scope scope : scopes) {
			if (scope instanceof MagicScope) {
				MagicScope magicScope = (MagicScope) scope;
				for (DependencyScope dependencyScope : magicScope.getScopes()) {
					dependencyScope.setName(dependencyScope.getName().toUpperCase());
					bean.addScope( dependencyScope);
				}
			}
			else {
				DependencyScope dependencyScope = (DependencyScope) scope;
				dependencyScope.setName(dependencyScope.getName().toUpperCase());
				bean.addScope( dependencyScope);
			}
		}
		return bean;
	}
	
	@Managed(com.braintribe.wire.api.annotation.Scope.prototype)
	public ExclusionControl exclusionControl() {
		ExclusionControl bean = new ExclusionControl( externalConfiguration.exclusions());			
		return bean;
	}
		

	@Managed
	public ConfigurableClashResolver clashResolver() {
		ConfigurableClashResolver bean = new OptimisticWeedingClashResolver();
		bean.setDependencyMerger( externalConfiguration.dependencyMerger());
		bean.setInitialPrecedenceSorter( externalConfiguration.clashResolvingPrecedenceSorter());
		bean.setResolvingInstant( externalConfiguration.resolvingInstant());
		bean.setLeniency( externalConfiguration.clashResolverLeniency());
		ClashResolverNotificationListener clashResolverNotificationListener = externalConfiguration.clashResolverNotificationListener();
		if (clashResolverNotificationListener != null) {
			bean.addListener( clashResolverNotificationListener);
		}
		return bean;
	}
	
	@Override
	@Managed
	public MultiRepositorySolutionEnricher enricher() {
		ConfigurableMultiRepositorySolutionEnricher bean = new TransactionalMultiRepositorySolutionEnricherImpl();
		
		bean.setAbortSignaller(externalConfiguration.abortSignaller());
		
		bean.setRepositoryRegistry( repositoryReflection());
		bean.setCache( new PartCacheInstance());
		bean.setLockFactory( lockFactory());
		
		bean.setRelevantPartTuples( Arrays.asList( externalConfiguration.relevantPartTuples()));
		bean.setRelevantPartPredicate( externalConfiguration.relevantPartPredicate());
		
		LockFactory lockFactory = externalConfiguration.lockFactory();
		if (lockFactory != null) {
			bean.setLockFactory( lockFactory);
		}
		else {
			bean.setLockFactory( lockFactory());
		}
		
		return bean;
	}
			
	@Override
	@Managed( com.braintribe.wire.api.annotation.Scope.prototype)
	public MultiRepositorySolutionEnricher contextualizedEnricher(WalkerContext context) {
		ConfigurableMultiRepositorySolutionEnricher bean = new TransactionalMultiRepositorySolutionEnricherImpl();
		
		bean.setAbortSignaller(externalConfiguration.abortSignaller());
		
		bean.setRepositoryRegistry( repositoryReflection());
		bean.setCache( new PartCacheInstance());
		bean.setLockFactory( lockFactory());
		
		// check if part tuples were overridden or used on a per-scope basis
		Collection<PartTuple> relevantPartTuples = context.getRelevantPartTuples();
		if (relevantPartTuples == null) {
			relevantPartTuples = Arrays.asList(externalConfiguration.relevantPartTuples());
		}
		bean.setRelevantPartTuples( relevantPartTuples);

		// check if predicate was overridden or used on a per-scope basis
		Predicate<? super PartTuple> relevantPartPredicate = context.getRelevantPartPredicate();
		if (relevantPartPredicate == null) {
			relevantPartPredicate = externalConfiguration.relevantPartPredicate();
		}
		bean.setRelevantPartPredicate( relevantPartPredicate);
		
		LockFactory lockFactory = externalConfiguration.lockFactory();
		if (lockFactory != null) {
			bean.setLockFactory( lockFactory);
		}
		else {
			bean.setLockFactory( lockFactory());
		}
		return bean;
	}
	
	

	@Managed( com.braintribe.wire.api.annotation.Scope.prototype)
	public QueueingMultiRepositorySolutionEnricher contextualizedQueuedEnricher(WalkerContext context) {
		QueueingMultiRepositorySolutionEnricher bean = new QueueingMultiRepositorySolutionEnricher();
		
		bean.setAbortSignaller(externalConfiguration.abortSignaller());
		
		bean.setRepositoryReflection( repositoryReflection());
		//solutionEnricher.setCache( new PartCacheInstance());
		
		// check if part tuples were overridden or used on a per-scope basis
		Collection<PartTuple> relevantPartTuples = context.getRelevantPartTuples();
		if (relevantPartTuples == null) {			
			bean.setRelevantPartTuples( externalConfiguration.relevantPartTuples());
		}
		else {
			bean.setRelevantPartTuples(context.getRelevantPartTuples().toArray(new PartTuple[0]));
		}
	
		// check if predicate was overridden or used on a per-scope basis
		Predicate<? super PartTuple> relevantPartPredicate = context.getRelevantPartPredicate();
		if (relevantPartPredicate == null) {
			relevantPartPredicate = externalConfiguration.relevantPartPredicate();
		}
		bean.setRelevantPartPredicate( (Predicate<PartTuple>) relevantPartPredicate);
		
		LockFactory lockFactory = externalConfiguration.lockFactory();
		if (lockFactory != null) {
			bean.setLockFactory( lockFactory);
		}
		else {
			bean.setLockFactory( lockFactory());
		}
		
		return bean;
	}
	
	
	@Managed( com.braintribe.wire.api.annotation.Scope.prototype)
	public String walkScopeId() {
		return UUID.randomUUID().toString();
	}

	@Override
	@Managed(com.braintribe.wire.api.annotation.Scope.prototype)
	public WalkerImpl walker(WalkerContext context) {
		WalkerImpl walker = new WalkerImpl();
			
		// force a new pom reader instance in the factory 
		pomExpertFactory().forceNewPomReaderInstance();
		
				
		walker.setResolver( dependencyResolver());
		 
		walker.setEnricher( contextualizedEnricher(context));
		
		walker.setExclusionControl( exclusionControl());
		walker.setPomExpertFactory( pomExpertFactory());
	
		// scopes, i.e. scopeControl may better coming from the context.. 
		walker.setScopeControl( scopeControl( context));
		walker.setTypeFilter( context.getTypeRule());
		walker.setTagRule( context.getTagRule());
		walker.setAbortIfUnresolvedDependencyIsFound( context.getAbortOnUnresolvedDependency());
		
		walker.setAbortSignaller( externalConfiguration.abortSignaller());
							
		walker.setClashResolver( clashResolver());
		
		WalkNotificationListener walkNotificationListener = context.getWalkNotificationListener();
		if (walkNotificationListener == null) {
			walkNotificationListener = externalConfiguration.walkNotificationListener();
		}
		if (walkNotificationListener != null) {
			walker.addListener( walkNotificationListener);
		}
		
		//walker.acknowledeDenotationType(denotation);	
		
		walker.setWalkKind( externalConfiguration.walkKind());
				
		return walker;
		
	}
	
	@Override
	@Managed(com.braintribe.wire.api.annotation.Scope.prototype)
	public Walker enrichingWalker(WalkerContext context) {
		EnrichingWalkerImpl walker = new EnrichingWalkerImpl();
			
		// force a new pom reader instance in the factory 
		pomExpertFactory().forceNewPomReaderInstance();
		
				
		walker.setResolver( dependencyResolver());
		 
		walker.setEnricher( contextualizedQueuedEnricher(context));
		
		walker.setExclusionControl( exclusionControl());
		walker.setPomExpertFactory( pomExpertFactory());
	
		// scopes, i.e. scopeControl may better coming from the context.. 
		walker.setScopeControl( scopeControl( context));
		walker.setTypeFilter( context.getTypeRule());
		walker.setTagRule( context.getTagRule());
		walker.setAbortIfUnresolvedDependencyIsFound( context.getAbortOnUnresolvedDependency());
		
		walker.setAbortSignaller( externalConfiguration.abortSignaller());
							
		walker.setClashResolver( clashResolver());
		
		WalkNotificationListener walkNotificationListener = context.getWalkNotificationListener();
		if (walkNotificationListener == null) {
			walkNotificationListener = externalConfiguration.walkNotificationListener();
		}
		if (walkNotificationListener != null) {
			walker.addListener( walkNotificationListener);
		}
		
		//walker.acknowledeDenotationType(denotation);	
		
		walker.setWalkKind( externalConfiguration.walkKind());
				
		return walker;
		
	}
	
	
	
}
