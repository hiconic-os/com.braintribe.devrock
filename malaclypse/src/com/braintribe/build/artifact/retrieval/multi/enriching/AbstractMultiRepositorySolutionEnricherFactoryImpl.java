// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.enriching;

import java.util.HashSet;

import com.braintribe.build.artifact.retrieval.multi.cache.PartCacheFactory;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

public abstract class AbstractMultiRepositorySolutionEnricherFactoryImpl implements MultiRepositorySolutionEnricherFactory {

	protected ProcessAbortSignaller abortSignaller;
	protected HashSet<SolutionEnricherNotificationListener> listeners = new HashSet<SolutionEnricherNotificationListener>();
	protected RelevantPartTupleFactory partTupleFactory;
	protected RepositoryReflection repositoryRegistry;
	protected PartCacheFactory partCacheFactory;
	
	@Override
	public void addListener(SolutionEnricherNotificationListener listener) {
		listeners.add(listener);
	}
	@Override
	public void removeListener(SolutionEnricherNotificationListener listener) {
		listeners.remove(listener);
	}
	
	@Configurable @Override
	public void setAbortSignaller(ProcessAbortSignaller abortSignaller) {
		this.abortSignaller = abortSignaller;
	}
	
	@Override @Required @Configurable
	public void setRelevantPartTupleFactory(RelevantPartTupleFactory factory) {
		partTupleFactory = factory;		
	}
	
	@Override @Configurable @Required
	public void setRepositoryRegistry( RepositoryReflection registry){		
		this.repositoryRegistry = registry;
	}
	@Override
	@Configurable @Required
	public void setPartCacheFactory(PartCacheFactory partCache) {
		this.partCacheFactory = partCache;
	}
	

}
