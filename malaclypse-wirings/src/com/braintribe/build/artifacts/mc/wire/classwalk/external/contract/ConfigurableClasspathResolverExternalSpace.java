// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.classwalk.external.contract;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenProfileActivationExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.LocalRepositoryProvider;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.walk.multi.clash.ClashResolver;
import com.braintribe.build.artifact.walk.multi.clash.listener.ClashResolverNotificationListener;
import com.braintribe.build.artifact.walk.multi.listener.WalkNotificationListener;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.Scope;
import com.braintribe.model.maven.settings.Settings;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * a basic {@link ClasspathResolverExternalContract}, only needs the scopes passed, see {@link CompileClasspathResolverExternalSpace} or  {@link RuntimeClasspathResolverExternalSpace} 
 * 
 * @author pit
 *
 */
public class ConfigurableClasspathResolverExternalSpace implements ClasspathResolverExternalContract {

	private Function<DependencyResolver, DependencyResolver> dependencyResolverEnricher;
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	private Set<Scope> scopes;
	private MavenSettingsPersistenceExpert overrideSettingsPersistenceExpert;
	private LocalRepositoryLocationProvider overrideLocalRepositoryExpert;
	private MavenProfileActivationExpert overrideProfileActivationExpert;
	private boolean clashResolverLeniency = true;
	private boolean skipOptional = false;
	private ResolvingInstant resolvingInstant = ResolvingInstant.posthoc;
	private PartTuple [] relevantPartTuples;
	private Predicate<? super PartTuple> partTupleFilter;
	private WalkNotificationListener walkNotificationListener;
	private ClashResolverNotificationListener clashResolverNotificationListener;
	private PomReaderNotificationListener pomReaderNotificationListener;
	private LockFactory lockFactory;
	
	/**
	 * allows to install a new {@link DependencyResolver} as top of the chain 
	 *  
	 * @param dependencyResolverEnricher - a {@link Function} to attach a {@link DependencyResolver} instance as new head of the chain
	 */
	@Configurable
	public void setDependencyResolverEnricher(Function<DependencyResolver, DependencyResolver> dependencyResolverEnricher) {
		this.dependencyResolverEnricher = dependencyResolverEnricher;
	}
	@Override
	public Function<DependencyResolver, DependencyResolver> dependencyResolverEnricher() {
		return dependencyResolverEnricher;
	}
	
	/**
	 * allows to install a {@link WalkNotificationListener} 
	 */
	@Configurable
	public void setWalkNotificationListener(WalkNotificationListener walkNotificationListener) {
		this.walkNotificationListener = walkNotificationListener;
	}	
	@Override
	public WalkNotificationListener walkNotificationListener() {	
		return walkNotificationListener;
	}
	
	@Configurable
	public void setClashResolverNotificationListener( ClashResolverNotificationListener clashResolverNotificationListener) {
		this.clashResolverNotificationListener = clashResolverNotificationListener;
	}
	
	@Override
	public ClashResolverNotificationListener clashResolverNotificationListener() {	
		return clashResolverNotificationListener;
	}
	
	@Configurable
	public void setPomReaderNotificationListener( PomReaderNotificationListener pomReaderNotificationListener) {
		this.pomReaderNotificationListener = pomReaderNotificationListener;
	}
	
	@Override
	public PomReaderNotificationListener pomReaderNotificationListener() {	
		return pomReaderNotificationListener;
	}
	
	/**
	 * set the virtual environment to use 
	 * @param virtualEnvironment - a {@link VirtualEnvironment} (typically {@link OverrideableVirtualEnvironment})
	 */
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	@Override
	public VirtualEnvironment virtualEnvironment() {
		return virtualEnvironment;
	}
	

	/**
	 * sets the default scopes to be used by the walkers of this scope. Can be overridden by the {@link WalkerContext} 	 *   
	 * @param scopes - a {@link Set} of {@link Scope}, get it from {@link Scopes}
	 */
	@Configurable @Required
	public void setScopes(Set<Scope> scopes) {
		this.scopes = scopes;
	}
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract#scopes()
	 */
	@Override
	public Set<Scope> scopes() {	
		return scopes;
	}

	/**
	 * allows to replace the {@link MavenSettingsPersistenceExpertImpl}
	 * @param overrideSettingsPersistenceExpert - your custom {@link MavenSettingsPersistenceExpert}
	 */
	@Configurable
	public void setOverrideSettingsPersistenceExpert(MavenSettingsPersistenceExpert overrideSettingsPersistenceExpert) {
		this.overrideSettingsPersistenceExpert = overrideSettingsPersistenceExpert;
	}
	@Override
	public MavenSettingsPersistenceExpert overrideSettingsPersistenceExpert() {		
		return overrideSettingsPersistenceExpert;
	}
	
	/**
	 * allows to replace the value for the local repository (default is as defined in the {@link Settings})
	 * @param overrideLocalRepositoryExpert - your custom {@link LocalRepositoryProvider}
	 */
	@Configurable
	public void setOverrideLocalRepositoryExpert(LocalRepositoryLocationProvider overrideLocalRepositoryExpert) {
		this.overrideLocalRepositoryExpert = overrideLocalRepositoryExpert;
	}	
	@Override
	public LocalRepositoryLocationProvider overrideLocalRepositoryLocationExpert() {
		return overrideLocalRepositoryExpert;
	}
	
	/**
	 * allows to insert a {@link MavenProfileActivationExpert} on top of the chain
	 * @param overrideProfileActivationExpert - your custom {@link MavenProfileActivationExpert}
	 */
	@Configurable
	public void setOverrideProfileActivationExpert(MavenProfileActivationExpert overrideProfileActivationExpert) {
		this.overrideProfileActivationExpert = overrideProfileActivationExpert;
	}
	@Override
	public MavenProfileActivationExpert overrideProfileActivationExpert() {
		return overrideProfileActivationExpert;
	}

	@Override
	public boolean clashResolverLeniency() {		
		return clashResolverLeniency;
	}
	/**
	 * allows to change the leniency of the {@link ClashResolver}
	 * @param clashResolverLeniency - true if lenient, false if a single clash will throw an exception 
	 */
	@Configurable
	public void setClashResolverLeniency(boolean clashResolverLeniency) {
		this.clashResolverLeniency = clashResolverLeniency;
	}
	
	@Override
	public ResolvingInstant resolvingInstant() {	
		return resolvingInstant;
	}
	/**
	 * allows to set the instant, clash resolving should take place
	 * @param resolvingInstant - {@link ResolvingInstant} - ad-hoc (during traversing), post-hoc (after traversing)
	 */
	@Configurable
	public void setResolvingInstant(ResolvingInstant resolvingInstant) {
		this.resolvingInstant = resolvingInstant;
	}
	
	@Override
	public boolean skipOptional() {
		return skipOptional;
	}
	/**
	 * sets whether to skip any optionals... 
	 * use only in walks, so it's moved to the {@link WalkerContext}
	 * @param skipOptional - true to skip, false otherwise
	 */
	@Configurable
	public void setSkipOptional(boolean skipOptional) {
		this.skipOptional = skipOptional;
	}	
	
	@Override
	public PartTuple[] relevantPartTuples() {
		if (relevantPartTuples == null) {
			return PartTuples.standardPartTuples();
		}
		else {
			return relevantPartTuples;
		}
	}	
	/**
	 * override the relevant part tuples
	 * @param relevantPartTuples - the {@link PartTuple} that describe the parts you want 
	 */
	@Configurable
	public void setRelevantPartTuples(PartTuple ... relevantPartTuples) {
		this.relevantPartTuples = relevantPartTuples;
	}
	
	@Override
	public Predicate<? super PartTuple> relevantPartPredicate() {
		if (partTupleFilter == null) {
			return ClasspathResolverExternalContract.super.relevantPartPredicate();
		}
		else {
			return partTupleFilter;
		}
	}
	/**
	 * override the part tuple filter 
	 * @param partTupleFilter
	 */
	@Configurable
	public void setPartTupleFilter(Predicate<? super PartTuple> partTupleFilter) {
		this.partTupleFilter = partTupleFilter;
	}
	
	@Override
	public LockFactory lockFactory() {
		return lockFactory;
	}
	
	public void setLockFactory(LockFactory lockFactory) {
		this.lockFactory = lockFactory;
	}
	
	
	
	

	
	
	
}
