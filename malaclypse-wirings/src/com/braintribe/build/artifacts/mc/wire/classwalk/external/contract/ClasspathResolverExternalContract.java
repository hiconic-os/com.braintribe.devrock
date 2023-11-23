package com.braintribe.build.artifacts.mc.wire.classwalk.external.contract;

import java.lang.reflect.Array;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenProfileActivationExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.walk.multi.clash.ClashResolver;
import com.braintribe.build.artifact.walk.multi.clash.InitialDependencyPrecedenceSorter;
import com.braintribe.build.artifact.walk.multi.clash.InitialDependencySortByHierarchy;
import com.braintribe.build.artifact.walk.multi.clash.InitialDependencySortByPathIndex;
import com.braintribe.build.artifact.walk.multi.clash.listener.ClashResolverNotificationListener;
import com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMerger;
import com.braintribe.build.artifact.walk.multi.listener.WalkNotificationListener;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.model.artifact.Exclusion;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.Scope;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.space.WireSpace;


/**
 * a contract to configure a class-path walker, 
 * apart of {@link #scopes()}, everything has default values 
 * 
 * @author pit
 *
 */
public interface ClasspathResolverExternalContract extends WireSpace {
		/**
//		 * sets the scopes for all walks within the contract, can be overridden by the {@link WalkerContext}..
		 * return the scopes (magic or standard) to honor
		 * @return a {@link Set} of {@link Scope}
		 */		
		Set<Scope> scopes();

		/**
		 * a function that is called to chain this additional resolver with the standard resolver from the 
		 * contract
		 * @return - a function to be called to bind resolvers or null for only the standard one 
		 */
		default Function<DependencyResolver, DependencyResolver> dependencyResolverEnricher() { return null;}
		
		/**
		 * a {@link VirtualEnvironment} to overload, for instances with a {@link OverrideableVirtualEnvironment}
		 * @return - the {@link VirtualEnvironment}, default {@link StandardEnvironment}
		 */
		default VirtualEnvironment virtualEnvironment() {return StandardEnvironment.INSTANCE;}
		
		/**
		 * overrides the standard settings persistence expert (i.e. from where to get settings.xml)
		 * @return - a specialized {@link MavenSettingsPersistenceExpert}, default is null
		 */
		default MavenSettingsPersistenceExpert overrideSettingsPersistenceExpert() { return null;}
	
		/**
		 * overrides the standard provider for the local repository (i.e. where to put/get the files)
		 * @return - a specialized {@link LocalRepositoryLocationProvider}, default is null, take if from the settings
		 */
		default LocalRepositoryLocationProvider overrideLocalRepositoryLocationExpert() { return null;}
		
		/**
		 * adds the {@link MavenProfileActivationExpert} as the first expert in the chain of activation experts,
		 * the standard one is still active, yet this one is asked first 
		 * @return - an overriding {@link MavenProfileActivationExpert}
		 */
		default MavenProfileActivationExpert overrideProfileActivationExpert() { return null;}
		
		
		/**
		 * default listener for all walks - can be overridden in the {@link WalkerContext} 
		 * adds a {@link WalkNotificationListener}
		 * @return
		 */		
		default WalkNotificationListener walkNotificationListener() { return null;}
		
		
		/**
		 * default listener for all {@link ClashResolver}, can be overridden in the {@link WalkerContext}
		 * adds a {@link ClashResolverNotificationListener}
		 * @return
		 */
		default ClashResolverNotificationListener clashResolverNotificationListener() { return null;}
		
		
		default PomReaderNotificationListener pomReaderNotificationListener() {return null;}
		
		/**
		 * the id to be used within the {@link RepositoryReflection}, so it doesn't rescan RH
		 * NOT to be confused with the walk scope id
		 * @return - the scope id to be used, default is null
		 */
		default String globalMalaclypseScopeId() { return null;}
		
		/**
		 * sets the default for all walks within the scope, can be overrriden by the {@link WalkerContext}
		 * sets whether dependencies marked as optional should be skipped - default is yes, skip that stuff 
		 * @return
		 */		
		default boolean skipOptional() { return true;}
		
		/**
		 * sets the default exclusions for all walks within this scope, can be overridden by the {@link WalkerContext}... 
		 * sets initial (global) exclusions 
		 * @return - a {@link Set} of {@link Exclusion} or null for none
		 */
		default Set<Exclusion> exclusions() {return null;}
		
		/**
		 * sets the default initial sort oder for all walks within this scope, can be overridden by the {@link WalkerContext}...
		 * sets the initial sort order that is used to enter the clash resolving phase, default is top down 
		 * @return - an {@link InitialDependencyPrecedenceSorter}
		 */
		default InitialDependencyPrecedenceSorter clashResolvingPrecedenceSorter() {return new InitialDependencySortByPathIndex();}
		
		
		/**
		 * sets the default leniency for all walks within this scope, can be overridden by the {@link WalkerContext}...
		 * determine the leniency of the {@link ClashResolver},
		 * if set to true, it will accept (and resolve clashes), 
		 * if set to false, it will throw an exception if a clash is detected  
		 * @return
		 */
		default boolean clashResolverLeniency() { return true;}
		
		/**
		 * determines when the clash resolving should take place. 
		 * Basically, there are two choices : {@link ResolvingInstant.adhoc} or {@link ResolvingInstant.posthoc}.
		 * adhoc means that clash resolving occurs when dependencies are detected, and posthoc means that it will
		 * happen after the dependency tree has been traversed.
		 * @return - the {@link ResolvingInstant}, default is {@link ResolvingInstant.adhoc}
		 */
		default ResolvingInstant resolvingInstant() { return ResolvingInstant.adhoc;}
		
		/**
		 * sets the dependency merger to the clash resolver - default is none 
		 * @return - an instance of a {@link DependencyMerger}
		 */
		default DependencyMerger dependencyMerger() { return null;}
	
		/**
		 * sets the kind of walk we want - default is a classpath walk
		 * @return - the {@link WalkKind}
		 */
		default WalkKind walkKind() { return WalkKind.classpath;}
		
		
		/**
		 * the tuples the enricher should get - pom is here for matter of completeness.
		 * alternatively, a predicate can be used, see {@link #relevantPartPredicate()}.
		 * default is the usual suspects
		 * @return - an {@link Array} of {@link PartTuple}
		 */
		default PartTuple [] relevantPartTuples() {
			return PartTuples.standardPartTuples();
		}
		/**
		 * sets a predicate that is used to filter to (so you do the same as above, but programmatically
		 * @return - a {@link Predicate} to filter unwanted {@link PartTuple}
		 */
		default Predicate<? super PartTuple> relevantPartPredicate() { return null;}
		
		
		/**
		 * sets instances that can regularly be called to check whether the walk has been aborted, 
		 * default is nothing
		 * @return - a {@link ProcessAbortSignaller} instance
		 */
		default ProcessAbortSignaller abortSignaller() {return null;}
		
		default LockFactory lockFactory() {return null;}
		
	
}

