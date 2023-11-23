// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.enriching;

import java.util.Arrays;

import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;


public class MultiRepositorySolutionEnricherFactoryImpl extends AbstractMultiRepositorySolutionEnricherFactoryImpl implements MultiRepositorySolutionEnricherFactory {	
	
	@Override
	public MultiRepositorySolutionEnricher get() throws RuntimeException {
		//ConfigurableMultiRepositorySolutionEnricher solutionEnricher = new MultiRepositorySolutionEnricherImpl();
		ConfigurableMultiRepositorySolutionEnricher solutionEnricher = new TransactionalMultiRepositorySolutionEnricherImpl();
		
		solutionEnricher.setAbortSignaller(abortSignaller);
		for (SolutionEnricherNotificationListener listener : listeners) {
			solutionEnricher.addListener(listener);
		}
		solutionEnricher.setRepositoryRegistry( repositoryRegistry);
		solutionEnricher.setCache(partCacheFactory.get());
		solutionEnricher.setRelevantPartTuples( Arrays.asList( partTupleFactory.get()));
		solutionEnricher.setLockFactory( new FilesystemSemaphoreLockFactory());
		return solutionEnricher;
	}

}
