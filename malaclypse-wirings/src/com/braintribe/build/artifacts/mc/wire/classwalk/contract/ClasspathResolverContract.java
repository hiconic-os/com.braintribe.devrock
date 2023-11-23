package com.braintribe.build.artifacts.mc.wire.classwalk.contract;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhurstPersistenceExpertForMainDataContainer;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflectionSupport;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.cfg.ScopeContext;
import com.braintribe.wire.api.space.WireSpace;

/**
 * a {@link WireSpace} for a class-path style walker (i.e. with clash resolving and solution enriching)
 * 
 * @author pit
 *
 */
public interface ClasspathResolverContract extends WireSpace {
	/**
	 * a fully configured {@link ArtifactPomReader}
	 * @return - a {@link ArtifactPomReader}
	 */
	ArtifactPomReader pomReader();
	
	/**
	 * the current top level {@link DependencyResolver}, i.e. the head of the chain
	 * @return - the top level {@link DependencyResolver}
	 */
	
	DependencyResolver dependencyResolver();
	
	/**
	 * the currently active (and configured via {@link ClasspathResolverExternalContract}) {@link MultiRepositorySolutionEnricher}
	 * @return - the configured {@link MultiRepositorySolutionEnricher}
	 */
	MultiRepositorySolutionEnricher enricher();
	
	/**
	 * a new {@link MultiRepositorySolutionEnricher} (and configured via {@link WalkerContext})
	 * @return - the configured {@link MultiRepositorySolutionEnricher}
	 */
	MultiRepositorySolutionEnricher contextualizedEnricher(WalkerContext context);
	
	 
	
	/**
	 * the settings reader, i.e. access to the maven compatible configuration
	 * @return - the active {@link MavenSettingsReader}
	 */
	MavenSettingsReader settingsReader();
	
	/**
	 * the repository reflection 
	 * @return - the {@link RepositoryReflection} to access the local (and remote) repositories
	 */
	RepositoryReflection repositoryReflection();
	
	RepositoryReflectionSupport repositoryReflectionSupport();
	
	/**
	 * @return - an implementation of a {@link RavenhurstScope} to access enhanced repo info
	 */
	RavenhurstScope ravenhurstScope();	
	
	RavenhurstPersistenceExpertForMainDataContainer ravenhurstMainContainerExpert();
	
	/**
	 * a new walker that actually can perform the classpath walk,
	 * @param context - the {@link WalkerContext} with the parameters for the walk, and the
	 * {@link ScopeContext} that encapsulates the walker
	 * @return - a fully configured {@link Walker}
	 */
	Walker walker(WalkerContext context);
	
	Walker enrichingWalker( WalkerContext context);

	LockFactory lockFactory();

}
