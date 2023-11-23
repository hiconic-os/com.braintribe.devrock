package com.braintribe.build.artifact.retrieval.multi.enriching.queued;

import java.util.function.Predicate;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationListener;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;

/**
 * simple transfer packet for the queue in the {@link QueueingMultiRepositorySolutionEnricher}
 * @author pit
 *
 */
public class EnrichingPacket {
	Solution solution;
	PartTuple [] tuples;
	Predicate<PartTuple> predicate;
	String walkScopeId;
	SolutionEnricherNotificationListener listener;
	ProcessAbortSignaller abortSignaller;	
	
	public String toString() {
		return NameParser.buildName(solution);
	}
}
