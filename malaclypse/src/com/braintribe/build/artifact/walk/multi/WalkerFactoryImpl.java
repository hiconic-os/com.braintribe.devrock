// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi;

import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricherFactory;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolverFactory;
import com.braintribe.build.artifact.walk.multi.clash.ClashResolverFactory;
import com.braintribe.build.artifact.walk.multi.clash.ClashResolverFactoryImpl;
import com.braintribe.build.artifact.walk.multi.clash.ConfigurableClashResolver;
import com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMerger;
import com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMergerFactory;
import com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMergerFactoryImpl;
import com.braintribe.build.artifact.walk.multi.exclusion.ExclusionControlFactory;
import com.braintribe.build.artifact.walk.multi.scope.ScopeControlFactory;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverDenotationType;


public class WalkerFactoryImpl implements WalkerFactory {
	private DependencyResolverFactory multiRepositoryDependencyResolverFactory;
	private MultiRepositorySolutionEnricherFactory multiRepositorySolutionEnricherFactory;
	private ExclusionControlFactory exclusionControlFactory;
	private ScopeControlFactory scopeControlFactory;
	private ClashResolverFactory clashResolvingFactory;
	private DependencyMergerFactory dependencyMergerFactory;
	private PomExpertFactory pomExpertFactory;
	

	@Override @Configurable @Required 
	public void setMultiRepositoryDependencyResolverFactory(DependencyResolverFactory factory) {
		this.multiRepositoryDependencyResolverFactory = factory;
	}

	@Override @Configurable @Required
	public void setSolutionEnricherFactory(MultiRepositorySolutionEnricherFactory factory) {
		this.multiRepositorySolutionEnricherFactory = factory;		
	}
	

	@Override @Configurable
	public void setExclusionControlFactory(ExclusionControlFactory factory) {
		this.exclusionControlFactory = factory;
	}
	
	private ExclusionControlFactory getExclusionControlFactory() {
		if (exclusionControlFactory == null) {
			exclusionControlFactory = new ExclusionControlFactory();
		}
		return exclusionControlFactory;
	}

	@Override @Configurable
	public void setScopeControl(ScopeControlFactory factory) {
		this.scopeControlFactory = factory;
	}
	private ScopeControlFactory getScopeControlFactory() {
		if (scopeControlFactory == null) {
			scopeControlFactory = new ScopeControlFactory();
		}
		return scopeControlFactory;
	}
	
	@Override @Configurable
	public void setClashResolverFactory(ClashResolverFactory factory) {
		clashResolvingFactory = factory;		
	}
	public ClashResolverFactory getClashResolvingFactory() {
		if (clashResolvingFactory == null) {
			clashResolvingFactory = new ClashResolverFactoryImpl();
		}
		return clashResolvingFactory;
	}
			
	@Override @Configurable
	public void setDependencyMergerFactory(DependencyMergerFactory factory) {
		dependencyMergerFactory = factory;		
	}
	public DependencyMergerFactory getDependencyMergerFactory() {
		if (dependencyMergerFactory == null) {
			dependencyMergerFactory = new DependencyMergerFactoryImpl();
		}
		return dependencyMergerFactory;
	}

	@Override
	public void setPomExpertFactory(PomExpertFactory factory) {
		pomExpertFactory = factory;		
	}

	@Override
	public Walker apply(WalkDenotationType denotation) throws RuntimeException {
		ConfigurableWalker walker = new WalkerImpl();
		
		
		// force a new pom reader instance in the factory 
		pomExpertFactory.forceNewPomReaderInstance();
		
		DependencyResolver repositoryDependencyResolver = multiRepositoryDependencyResolverFactory.get();		
		walker.setResolver( repositoryDependencyResolver);
		
		MultiRepositorySolutionEnricher enricher = multiRepositorySolutionEnricherFactory.get();		
		walker.setEnricher( enricher);
		
		walker.setExclusionControl( getExclusionControlFactory().apply( denotation.getExclusionControlDenotationType()));
		walker.setScopeControl( getScopeControlFactory().apply( denotation.getScopeControlDenotationType()));
		walker.setPomExpertFactory( pomExpertFactory);
		walker.acknowledeDenotationType(denotation);
		walker.setTypeFilter( denotation.getTypeFilter());
		
		ClashResolverDenotationType clashResolverDenotationType = denotation.getClashResolverDenotationType();
		ConfigurableClashResolver clashResolver = (ConfigurableClashResolver) getClashResolvingFactory().apply( clashResolverDenotationType);
		
		walker.setClashResolver( clashResolver);
		
		if (!clashResolverDenotationType.getOmitDependencyMerging()) {
			DependencyMerger merger = getDependencyMergerFactory().get();
			clashResolver.setDependencyMerger(merger);
		}
		
		walker.setWalkKind( denotation.getWalkKind());
		
		walker.setTagRule( denotation.getTagRule());
		
		return walker;
	}

}
