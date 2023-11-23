// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash;

import java.util.HashSet;
import java.util.function.Supplier;

import com.braintribe.build.artifact.walk.multi.clash.listener.ClashResolverNotificationListener;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverByDepthDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverByIndexDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.InitialDependencyClashPrecedence;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;


public class ClashResolverFactoryImpl implements ClashResolverFactory {
	private HashSet<ClashResolverNotificationListener> listeners = new HashSet<ClashResolverNotificationListener>();

	@Override
	public void addListener(ClashResolverNotificationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ClashResolverNotificationListener listener) {
		listeners.remove(listener);
	}

	
	@Override @Deprecated
	public void setTerminalProvider(Supplier<Artifact> terminalProvider) {		
	}
	

	@Override
	public ClashResolver apply(ClashResolverDenotationType clashResolverDenotationType) throws RuntimeException {
		// clash resolver	
		ConfigurableClashResolver clashResolver;
		if (clashResolverDenotationType instanceof ClashResolverByDepthDenotationType) {
			clashResolver = new DepthBasedWeedingClashResolver();
		}
		else if (clashResolverDenotationType instanceof ClashResolverByIndexDenotationType) {		
			clashResolver = new IndexedBasedWeedingClashResolver();			
		}
		else {
			clashResolver = new OptimisticWeedingClashResolver();
		}
		
		ResolvingInstant resolvingInstant = clashResolverDenotationType.getResolvingInstant();
		if (resolvingInstant != null) {
			clashResolver.setResolvingInstant( resolvingInstant);
		}
		
		for (ClashResolverNotificationListener listener : listeners) {
			clashResolver.addListener( listener);
		}
		
		// set the sorter  
		InitialDependencyClashPrecedence precedence = clashResolverDenotationType.getInitialClashPrecedence();
		if (precedence == null) {
			precedence = InitialDependencyClashPrecedence.hierarchyIndex;
		}
		switch (precedence) {
			case pathIndex :
				clashResolver.setInitialPrecedenceSorter( new InitialDependencySortByPathIndex());
				break;
			case hierarchyIndex:
			default:
				clashResolver.setInitialPrecedenceSorter( new InitialDependencySortByHierarchy());
				break;
		}
		return clashResolver;
	}
}
