package com.braintribe.build.artifact.retrieval.multi.enriching;

import java.util.Collection;
import java.util.function.Predicate;

import com.braintribe.build.artifact.retrieval.multi.cache.PartCache;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.model.artifact.PartTuple;

public interface ConfigurableMultiRepositorySolutionEnricher extends MultiRepositorySolutionEnricher {
	/**
	 * set the currently relevant {@link PartTuple}
	 * @param tuples - {@link Collection} of {@link PartTuple}, in case you wondered 
	 */
	void setRelevantPartTuples( Collection<PartTuple> tuples);
	
	void setRelevantPartPredicate(Predicate<? super PartTuple> relevantPartPredicate);
	
	void setAbortSignaller( ProcessAbortSignaller signaller);
	
	void setCache(PartCache cache);	
	void setRepositoryRegistry( RepositoryReflection registry);
	void setLockFactory( LockFactory lockFactory);
}

