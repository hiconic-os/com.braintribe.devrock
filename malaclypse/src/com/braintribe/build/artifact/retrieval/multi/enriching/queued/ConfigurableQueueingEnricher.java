package com.braintribe.build.artifact.retrieval.multi.enriching.queued;

import java.util.function.Predicate;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.model.artifact.PartTuple;

public interface ConfigurableQueueingEnricher extends QueueingEnricher {
	void setRepositoryReflection(RepositoryReflection repositoryRegistry);
	void setDisableDependencyPartExpectationClassifierInfluence(boolean disableDependencyPartExpectationClassifierInfluence);
	void setAbortSignaller(ProcessAbortSignaller signaller);
	void setRelevantPartTuples(PartTuple ... relevantPartTuples);
	void setRelevantPartPredicate(Predicate<PartTuple> predicate);	
	void setLockFactory( LockFactory lockFactory);
}
