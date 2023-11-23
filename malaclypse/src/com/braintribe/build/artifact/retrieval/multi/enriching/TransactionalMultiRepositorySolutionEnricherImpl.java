// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.enriching;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.cache.PartCache;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.PartDownloadInfo;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.SolutionReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.DownloadHelper;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.ravenhurst.data.RepositoryRole;

public class TransactionalMultiRepositorySolutionEnricherImpl implements ConfigurableMultiRepositorySolutionEnricher, SolutionEnricherNotificationListener {
	private static Logger log = Logger.getLogger(TransactionalMultiRepositorySolutionEnricherImpl.class);
	private PartCache cache;
	private Collection<PartTuple> relevantPartTuples;
	private RepositoryReflection repositoryRegistry;
	private static int POOL_SIZE = 5;
	private Set<SolutionEnricherNotificationListener> listeners = new HashSet<>();
	private ProcessAbortSignaller abortSignaller; 	
	private Predicate<? super PartTuple> relevantPartPredicate;
	private boolean disableDependencyPartExpectationClassifierInfluence = false;
	private LockFactory lockFactory = new FilesystemSemaphoreLockFactory();
	
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
	
	@Configurable
	public void setLockFactory(LockFactory lockFactory) {
		this.lockFactory = lockFactory;
	}

	@Override
	public void clearCache() {
		cache.clearCache();		
	}

	@Override
	public void setRelevantPartTuples(Collection<PartTuple> tuples) {
		this.relevantPartTuples = tuples;		
	}
	
	public void setRelevantPartPredicate(Predicate<? super PartTuple> relevantPartPredicate) {
		this.relevantPartPredicate = relevantPartPredicate;
	}
	

	@Override
	public void addListener(SolutionEnricherNotificationListener listener) {
		synchronized (listeners) {
			listeners.add( listener);					
		}		
	}

	@Override
	public void removeListener(SolutionEnricherNotificationListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);		
		}		
	}

	@Override
	public Collection<Solution> enrich(String walkScopeId, Collection<Solution> solutions) throws EnrichingException {
		ForkJoinPool pool = new ForkJoinPool(POOL_SIZE);
		Runnable task = () ->solutions.parallelStream().forEach( solution -> { transactionalEnrich( walkScopeId, solution);});
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
	public Collection<Solution> enrich(String walkScopeId, Collection<Solution> solutions, PartTuple tuple) throws EnrichingException {
		ForkJoinPool pool = new ForkJoinPool(POOL_SIZE);
		Runnable task = () ->solutions.parallelStream().forEach( solution -> { enrich( walkScopeId, solution, tuple);});
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
	public Part enrich(String walkScopeId, Solution solution, PartTuple tuple) throws EnrichingException {
		Pair<Part, Boolean> part = enrichAndReflectDownload(walkScopeId, solution, tuple);
		
		return part == null? null: part.first();
	}
	
	@Override
	public Pair<Part, Boolean> enrichAndReflectDownload(String walkScopeId, Solution solution, PartTuple tuple) throws EnrichingException {
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
		
		PartDownloadInfo info = transactionalEnrich(walkScopeId, tuple, solution, solutionExpert, repositoryRole, true);
		if (info != null) {
			synchronized (solution) {
				solution.getParts().add(info.getOwningPart());
			}
			return Pair.of(info.getOwningPart(), info.isFreshDownload());		
		}
		
		return null;
	}
	
	
	
	@Override
	public List<Pair<Part, Boolean>> enrichAndReflectDownload(String walkScopeId, Solution solution, List<PartTuple> tuples) throws EnrichingException {
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
		List<Pair<Part,Boolean>> pairs = new ArrayList<>();
		
		for (PartTuple tuple : tuples) {
			PartDownloadInfo info = transactionalEnrich(walkScopeId, tuple, solution, solutionExpert, repositoryRole, true);
			if (info != null) {
				synchronized (solution) {
					solution.getParts().add(info.getOwningPart());
				}
				Pair<Part,Boolean> pair = Pair.of(info.getOwningPart(), info.isFreshDownload());
				pairs.add(pair);
			}
			
		}
		return pairs;		 
	}

	/**
	 * enriches a full solution, i.e. all 
	 * @param walkScopeId
	 * @param solution
	 */
	private void transactionalEnrich( String walkScopeId, Solution solution) throws EnrichingException {
		
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
		
		
		Collection<PartTuple> partTuplesToBeFetched = null;
		
		// determine what part tuple are relevant
		if (relevantPartTuples != null) {
			partTuplesToBeFetched = relevantPartTuples;
		}
		else if (relevantPartPredicate != null) {
			List<PartTuple> existingPartTuples = solutionExpert.listExistingPartTuples(solution, repositoryRole);
			existingPartTuples.removeIf(relevantPartPredicate.negate());
			partTuplesToBeFetched = existingPartTuples;
		}
		
		// no part tuples to be fetched, don't do anything
		if (partTuplesToBeFetched == null) {
			return;
		}
		
		// filter the part tuples that we already have, so only the ones remain we need to download
		List<PartTuple> partTupleToBeResolved = partTuplesToBeFetched.stream().filter( p -> p != null).filter( p -> {return !containsPart( solution, p);}).collect( Collectors.toList());

		//
		// start transaction 
		//
		final RepositoryRole role = repositoryRole;
		Map<PartTuple, PartDownloadInfo> result = new ConcurrentHashMap<>();
		try {
			partTupleToBeResolved.stream().forEach( t -> {
					PartDownloadInfo info = transactionalEnrich( walkScopeId, t, solution, solutionExpert, role, false);
					if (info != null) {
						result.put( t, info);
					}
				});
		} 
		catch (Exception e1) {
			//
			// rollback
			//
			log.warn( "rolling back download on [" + NameParser.buildName(solution) + "] because of exception", e1);
			for (PartDownloadInfo info : result.values()) {
				if (info.isFreshDownload()) {
					boolean deleted = info.getFile().delete();
					if (deleted) {
						log.debug( "deleted [" + info.getFile().getAbsolutePath() + "]");
					} else { 
						log.debug( "couldn't delete [" + info.getFile().getAbsolutePath() + "]");
					}
				}
			}			
		}
		//
		// commit
		//
		for (Entry<PartTuple, PartDownloadInfo> entry : result.entrySet()) {
	
			PartDownloadInfo downloadInfo = entry.getValue();
			if (downloadInfo != null) {
				Part part = downloadInfo.getOwningPart();			
			
				File downloadedFile = downloadInfo.getFile();				
				if (DownloadHelper.isDownloadFile(downloadedFile)) {
					// has suffix, i.e. has just been downloaded 
					File actualFile = new File( DownloadHelper.deriveActualFileName(downloadedFile));					
					DownloadHelper.ensureContentsOfActualFile(lockFactory, downloadedFile, actualFile); 																						
					if (actualFile.exists()) {
						part.setLocation( actualFile.getAbsolutePath());
					}
					
					// as we were able to download, we can add it to the index of the bundle 
				}
				else {
					// no suffix - was already present
					part.setLocation(downloadedFile.getAbsolutePath());
				}
				
				solution.getParts().add(part);
			}
			else {
				; // nothing found
			}
			
		}			
	}

	
	
	private PartDownloadInfo transactionalEnrich(String walkScopeId, PartTuple tuple, Solution solution, SolutionReflectionExpert solutionExpert, RepositoryRole repositoryRole, boolean standalone) {
		 
		Part candidate = Part.T.create();
		ArtifactProcessor.transferIdentification(candidate, solution);
		candidate.setType(tuple);
	
		// check if we should react to specific classifiers (as introduced by the requesting dependencies)
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
		// generate the possible file names for the parttuple				
		Collection<String> possibleFileNames = PartTupleProcessor.getFileNameCandidatesForPart( candidate);
		for (String name : possibleFileNames) {				
			PartDownloadInfo info;
			try {
				info = solutionExpert.transactionalGetPart(candidate, name, repositoryRole);
			} catch (Exception e) {
				String msg ="cannot retrieve part [" + name + "] for [" + NameParser.buildName(solution) + "]";
				throw new EnrichingException(msg, e);
			}
			if (info != null) {
				// if this is run in standalone mode (i.e. a single part needs to be enriched), do the file rename magic. Otherwise, the transaction will do that, so don't
				if (standalone) {
					// we were able to download the file
					File downloadedFile = info.getFile();					
					// is not a download file, i.e. was there already 
					if (!DownloadHelper.isDownloadFile( downloadedFile)) {
						candidate.setLocation( downloadedFile.getAbsolutePath());						
					}
					else {
						File actualFile = new File( DownloadHelper.deriveActualFileName(downloadedFile));
						DownloadHelper.ensureContentsOfActualFile(lockFactory, downloadedFile, actualFile);
						candidate.setLocation( actualFile.getAbsolutePath());
						info.setFreshDownload(true);
					}				
				}
				info.setOwningPart(candidate);
				
				return info;
			}
			
		}

		acknowledgeFileEnrichmentFailure(walkScopeId, solution, tuple);
		return null;
	
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
		synchronized (listeners) {
			for (SolutionEnricherNotificationListener listener : listeners) {
				listener.acknowledgeFileEnrichmentSuccess(walkScopeId, file);
			}	
		}
	}

	@Override
	public void acknowledgeFileEnrichmentFailure(String walkScopeId, Solution solution, PartTuple tuple) {
		synchronized (listeners) {
			for (SolutionEnricherNotificationListener listener : listeners) {
				listener.acknowledgeFileEnrichmentFailure( walkScopeId, solution, tuple);
			}	
		}
		
	}

	@Override
	public void acknowledgeSolutionEnriching(String walkScopeId, Solution solution) {
		synchronized (listeners) {
			for (SolutionEnricherNotificationListener listener : listeners) {
				listener.acknowledgeSolutionEnriching( walkScopeId, solution);
			}	
		}		
	}
}	
	

