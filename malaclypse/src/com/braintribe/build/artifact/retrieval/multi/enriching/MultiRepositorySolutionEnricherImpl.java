// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.enriching;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.cache.PartCache;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.SolutionReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.ravenhurst.data.RepositoryRole;

public class MultiRepositorySolutionEnricherImpl implements ConfigurableMultiRepositorySolutionEnricher, SolutionEnricherNotificationListener {
	private static Logger log = Logger.getLogger(MultiRepositorySolutionEnricherImpl.class);
	private PartCache cache;
	private Collection<PartTuple> relevantPartTuples;
	private RepositoryReflection repositoryRegistry;
	private Set<SolutionEnricherNotificationListener> listeners = new HashSet<>();
	private ProcessAbortSignaller abortSignaller; 	
	private Predicate<? super PartTuple> relevantPartPredicate;
	private boolean disableDependencyPartExpectationClassifierInfluence = false;
	private LockFactory lockFactory;
	private static int POOL_SIZE = 5;
	
	@Override @Configurable
	public void setAbortSignaller(ProcessAbortSignaller signaller) {
		this.abortSignaller = signaller;
		
	}

	@Configurable @Required
	public void setRepositoryRegistry( RepositoryReflection registry) {
		this.repositoryRegistry = registry;
	}

	@Override @Configurable @Required
	public void setCache(PartCache cache) {	
		this.cache = cache;
	}
	
	@Configurable
	public void setDisableDependencyPartExpectationClassifierInfluence(boolean disableDependencyPartExpectationClassifierInfluence) {
		this.disableDependencyPartExpectationClassifierInfluence = disableDependencyPartExpectationClassifierInfluence;
	}

	@Override
	public void clearCache() {
		cache.clearCache();		
	}

	@Override
	public void setRelevantPartTuples(Collection<PartTuple> tuples) {
		this.relevantPartTuples = tuples;		
	}
	
	
	@Override @Configurable
	public void setLockFactory(LockFactory lockFactory) {	
		this.lockFactory = lockFactory;
	}

	public void setRelevantPartPredicate(Predicate<? super PartTuple> relevantPartPredicate) {
		this.relevantPartPredicate = relevantPartPredicate;
	}
	

	@Override
	public void addListener(SolutionEnricherNotificationListener listener) {
		synchronized ( listeners) {
			listeners.add( listener);					
		}
		
	}

	@Override
	public void removeListener(SolutionEnricherNotificationListener listener) {
		synchronized( listeners) {
			listeners.remove(listener);		
		}
		
	}

	@Override
	public Collection<Solution> enrich(String walkScopeId, Collection<Solution> solutions) throws EnrichingException {
		ForkJoinPool pool = new ForkJoinPool(POOL_SIZE);
		Runnable task = () -> solutions.parallelStream().forEach( solution -> { enrich( walkScopeId, solution);});
		try {
			pool.submit(task).get();
		} catch (Exception e) {
			throw new EnrichingException(e);			
		}
		finally {
			pool.shutdown();		
		}
		
		return solutions;
	}
	
	@Override
	public Pair<Part, Boolean> enrichAndReflectDownload(String walkScopeId, Solution solution, PartTuple tuple) throws EnrichingException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Collection<Solution> enrich(String walkScopeId, Collection<Solution> solutions, PartTuple tuple) throws EnrichingException {
		ForkJoinPool pool = new ForkJoinPool(POOL_SIZE);
		Runnable task = () -> solutions.parallelStream().forEach( solution -> { enrich( walkScopeId, solution, tuple);});
		try {
			pool.submit(task).get();
		} catch (Exception e) {
			throw new EnrichingException(e);			
		}
		finally {
			pool.shutdown();		
		}
		return solutions;				
	}

	private void enrich( String walkScopeId, Solution solution) {
		
		if (abortSignaller != null && abortSignaller.abortScan()) {
			return;
		}		
		// broadcast enriching
		acknowledgeSolutionEnriching(walkScopeId, solution);
		
	
		SolutionReflectionExpert solutionExpert;
		try {
			solutionExpert = repositoryRegistry.acquireSolutionReflectionExpert(solution);
		} catch (RepositoryPersistenceException e2) {
			String msg = "cannot retrieve solution expert for [" + NameParser.buildName(solution) + "]";
			throw new EnrichingException( msg, e2);
		}			
		Version version = solution.getVersion();
		RepositoryRole repositoryRole = RepositoryRole.release;

		if (VersionProcessor.toString(version).endsWith("-SNAPSHOT")) {
			repositoryRole = RepositoryRole.snapshot;
		}
		
		Set<Part> parts = solution.getParts(); 
		
		Collection<PartTuple> partTuplesToBeFetched = null;
		
		if (relevantPartTuples != null) {
			partTuplesToBeFetched = relevantPartTuples;
		}
		else if (relevantPartPredicate != null) {
			List<PartTuple> existingPartTuples = solutionExpert.listExistingPartTuples(solution, repositoryRole);
			existingPartTuples.removeIf(relevantPartPredicate.negate());
			partTuplesToBeFetched = existingPartTuples;
		}
		final RepositoryRole role = repositoryRole;
		if (partTuplesToBeFetched != null) {
			partTuplesToBeFetched.stream().forEach( p -> { enrichPart(walkScopeId, solution, solutionExpert, role, p, parts);});			
		}
		
	}

	private void enrichPart(String walkScopeId, Solution solution, SolutionReflectionExpert solutionExpert, RepositoryRole repositoryRole, PartTuple tuple, Set<Part> parts) {		
		// TODO supply a way to do a non magical part resolution (perfect match part tuple)
		Part part = enrich( walkScopeId, solution, tuple, solutionExpert, repositoryRole /*, true for perfectMatch*/);
		if (part != null) {
			parts.add(part);
		}		
	}
	
	
	@Override
	public Part enrich(String walkScopeId, Solution solution, PartTuple tuple) throws EnrichingException {
		SolutionReflectionExpert solutionExpert;
		try {
			solutionExpert = repositoryRegistry.acquireSolutionReflectionExpert(solution);
		} catch (RepositoryPersistenceException e2) {
			String msg = "cannot retrieve solution expert for [" + NameParser.buildName(solution) + "]";
			throw new EnrichingException( msg, e2);
		}			
		Version version = solution.getVersion();
		RepositoryRole repositoryRole = RepositoryRole.release;
		
		if (VersionProcessor.toString(version).endsWith("-SNAPSHOT")) {
			repositoryRole = RepositoryRole.snapshot;
		}
		
		Part part = enrich(walkScopeId, solution, tuple, solutionExpert, repositoryRole);
		if (part != null) {
			solution.getParts().add(part);
		}
		return part;		
	}

	private Part enrich(String walkScopeId, Solution solution, PartTuple tuple, SolutionReflectionExpert solutionExpert, RepositoryRole repositoryRole) throws EnrichingException {
		if (containsPart(solution, tuple)) {
			if (log.isDebugEnabled()) {
				log.debug( "solution [" + NameParser.buildName(solution) + "] contains part [" + PartTupleProcessor.toString(tuple) + "]. Skipping");
			}
			return null;
		}
		Part candidate = Part.T.create();
		candidate.setGroupId( solution.getGroupId());
		candidate.setArtifactId( solution.getArtifactId());
		candidate.setVersion( solution.getVersion());
		candidate.setType(tuple);
		
		if (!disableDependencyPartExpectationClassifierInfluence) {
			//
			// if a dependency has a classifier, all part-tuples without classifier (mostly jar) should have it inserted,
			// so that A-1.0.jar is turned into A-1.0-<classifier>.jar. 
			// Of course: this means that if one of several requesters have a differing classifier, there's an issue 
			// 
			String tupleClassifier = tuple.getClassifier();
			if (tupleClassifier == null || tupleClassifier.length() == 0) {
				Set<Dependency> requestors = solution.getRequestors();
				for (Dependency requester : requestors) {
					String classifier = requester.getClassifier();
					if (classifier != null) {						
						candidate.setType( PartTupleProcessor.fromString(classifier,  tuple.getType()));
						break;
					}
				}
			}
		}
		// 				
	
		boolean foundAny = false;
		Collection<String> possibleFileNames = PartTupleProcessor.getFileNameCandidatesForPart( candidate);
		for (String name : possibleFileNames) {				
			Part resolvedPart = cache.getPartFromCache(name);
			if (resolvedPart != null) {
				candidate = resolvedPart;
				break;
			}	
			File file;
			try {
				file = solutionExpert.getPart(candidate, name, repositoryRole);
			} catch (RepositoryPersistenceException e) {
				String msg ="cannot retrieve part [" + name + "] for [" + NameParser.buildName(solution) + "]";
				throw new EnrichingException(msg, e);
			}
			if (file != null) {
				candidate.setLocation( file.getAbsolutePath());
				acknowledgeFileEnrichmentSuccess(walkScopeId, name);
				foundAny = true;
				cache.addToCache(candidate);
				break;
			}		
		}
		if (!foundAny) {
			acknowledgeFileEnrichmentFailure(walkScopeId, solution, tuple);
			return null;
		}
		return candidate;				
	}

	
	private boolean containsPart( Solution solution, PartTuple type) {
		Set<Part> parts = solution.getParts();
		if (parts == null)
			return false;
		for (Part part : parts) {
			if (PartTupleProcessor.equals( part.getType(), type))
				return true;
		}
		return false;
	}

	@Override
	public void acknowledgeFileEnrichmentSuccess(String walkScopeId, String file) {
		synchronized( listeners) {
			for (SolutionEnricherNotificationListener listener : listeners) {
				listener.acknowledgeFileEnrichmentSuccess(walkScopeId, file);
			}	
		}
	}

	@Override
	public void acknowledgeFileEnrichmentFailure(String walkScopeId, Solution solution, PartTuple tuple) {
		synchronized( listeners) {
			for (SolutionEnricherNotificationListener listener : listeners) {
				listener.acknowledgeFileEnrichmentFailure( walkScopeId, solution, tuple);
			}	
		}			
	}

	@Override
	public void acknowledgeSolutionEnriching(String walkScopeId, Solution solution) {
		synchronized(listeners) {		
			for (SolutionEnricherNotificationListener listener : listeners) {
				listener.acknowledgeSolutionEnriching( walkScopeId, solution);
			}	
		}
		
	}
	
	

}
