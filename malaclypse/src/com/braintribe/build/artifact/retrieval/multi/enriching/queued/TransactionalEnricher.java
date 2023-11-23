package com.braintribe.build.artifact.retrieval.multi.enriching.queued;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.enriching.EnrichingException;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.PartDownloadInfo;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.SolutionReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.DownloadHelper;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
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
import com.braintribe.utils.IOTools;

/**
 * simple transactional enricher to be used by the caller/worker pair in the {@link QueueingMultiRepositorySolutionEnricher}
 * 
 * @author pit
 *
 */
public class TransactionalEnricher {
	
	private static Logger log = Logger.getLogger(TransactionalEnricher.class);
	private SolutionEnricherNotificationListener listener;
	private List<PartTuple> relevantPartTuples;
	private Predicate<PartTuple> relevantPartPredicate;
	private ProcessAbortSignaller abortSignaller;
	private RepositoryReflection repositoryReflection;
	private boolean disableDependencyPartExpectationClassifierInfluence = false;	
	
	@Configurable @Required
	public void setRepositoryReflection(RepositoryReflection repositoryReflection) {
		this.repositoryReflection = repositoryReflection;
	}
	
	@Configurable
	public void setAbortSignaller(ProcessAbortSignaller abortSignaller) {
		this.abortSignaller = abortSignaller;
	}
	
	@Configurable
	public void setDisableDependencyPartExpectationClassifierInfluence( boolean disableDependencyPartExpectationClassifierInfluence) {
		this.disableDependencyPartExpectationClassifierInfluence = disableDependencyPartExpectationClassifierInfluence;
	}
	
	@Configurable
	public void setListener(SolutionEnricherNotificationListener listener) {
		this.listener = listener;
	}
	
	/**
	 * enrich a solution using the tuples passed 
	 * @param walkScopeId - the id of the current walk scope
	 * @param solution - the {@link Solution} to enrich
	 * @param tuples - the {@link PartTuple}s to use
	 * @throws EnrichingException - thrown if it catastrophically cannot be enriched
	 */
	public void enrich( String walkScopeId, Solution solution, PartTuple ...tuples) throws EnrichingException {
		if (tuples != null) {				
			relevantPartTuples = Arrays.asList(tuples);
		}
		transactionalEnrich(walkScopeId, solution);
	}
	
	
	/**
	 * enrich a solution using the part predicate 
	 * @param walkScopeId - the id of the current walk scope
	 * @param solution - the {@link Solution} to enrich
	 * @param predicate - the {@link Predicate} to filter the existing parts (requires index!!)
	 * @throws EnrichingException - thrown if it catastrophically cannot be enriched
	 */
	public void enrich( String walkScopeId, Solution solution, Predicate<PartTuple> predicate) throws EnrichingException {
		relevantPartPredicate = predicate;
		transactionalEnrich(walkScopeId, solution);
	}
	
	/**
	 * enriches a full solution, i.e. all 
	 * @param walkScopeId - the id of the current walk scope
	 * @param solution - the {@link Solution}  to enrich 
	 */
	private void transactionalEnrich( String walkScopeId, Solution solution) throws EnrichingException {
				
		// broadcast enriching
		if (listener != null) {
			listener.acknowledgeSolutionEnriching(walkScopeId, solution);		
		}
		
	
		SolutionReflectionExpert solutionExpert;
		try {
			solutionExpert = repositoryReflection.acquireSolutionReflectionExpert(solution);
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
		List<PartTuple> partTupleToBeResolved = partTuplesToBeFetched.stream().filter( p -> {return !containsPart( solution, p);}).collect( Collectors.toList());
		File localRepositoryLocationOfSolution = solutionExpert.getLocation();
		
		Lock semaphore = repositoryReflection.getLockInstance(localRepositoryLocationOfSolution).writeLock();
		boolean didLock = true;
		try {
			// if we can't get a lock on this solution, another instance is working on it
			if (!semaphore.tryLock()) {
				didLock = false;
				return;
			}			
			//
			// start transaction 
			//
			final RepositoryRole role = repositoryRole;
			Map<PartTuple, PartDownloadInfo> result = new ConcurrentHashMap<>();
			try {
				for (PartTuple t : partTupleToBeResolved) {			
					if (abortSignaller != null && abortSignaller.abortScan()) {
						// just abort now.. pretty ugly..
						throw new Exception();
					}
					PartDownloadInfo info = transactionalEnrich( walkScopeId, t, solution, solutionExpert, role, false);
					if (info != null) {
						result.put( t, info);
					}
				}
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
						File actualFile = new File( DownloadHelper.deriveActualFileName(downloadedFile));
						ensureContentsOfActualFile(downloadedFile, actualFile);
						
						downloadedFile.delete();
						part.setLocation( actualFile.getAbsolutePath());
					}
					else {
						// no suffix - was already present
						part.setLocation(downloadedFile.getAbsolutePath());
					}
					
					/*
						// has suffix, i.e. has just beenm downloaded 
						File actualFile = new File( downloadedFile.getParentFile(), downloadedFilename.substring( 0, downloadedFilename.indexOf( SolutionReflectionExpert.SUFFIX_TRANSACTION)));
						if (!actualFile.exists()) {
							try {
								Files.move( downloadedFile.toPath(), actualFile.toPath());
								if (listener != null) {
									listener.acknowledgeFileEnrichmentSuccess(walkScopeId, downloadedFilename);
								}
							} catch (IOException e) {
								throw new EnrichingException("cannot move file [" + downloadedFile.getAbsolutePath() + "] to [" + actualFile.getAbsolutePath() +"]", e);
							}							
						}
						else {
							downloadedFile.delete();
						}
						part.setLocation( actualFile.getAbsolutePath());
					
						// as we were able to download, we can add it to the index of the bundle 
					}
					else {
						// no suffix - was already present
						part.setLocation(downloadedFile.getAbsolutePath());
					}
					*/
					solution.getParts().add(part);
				}
				else {
					; // nothing found
				}
				
			}		
		}
		finally {
			// only unlock if we locked it
			if (didLock) {
				semaphore.unlock();
			}
		}
	}
	
	/**
	 * enriches a single part - either in a transaction or standalone (simple commit after download)
	 * @param walkScopeId - the id of the current walk scope id
	 * @param tuple - the {@link PartTuple} that designates the part 
	 * @param solution - the {@link Solution} to enrich
	 * @param solutionExpert - the {@link SolutionReflectionExpert} that handles the {@link Solution}
	 * @param repositoryRole - the {@link RepositoryRole}, i.e. either {@link RepositoryRole#release} or {@link RepositoryRole#snapshot} or {@link RepositoryRole#both}
	 * @param standalone - false if run within a transaction, true if standalone
	 * @return
	 */
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
					if (downloadedFile != null && downloadedFile.exists()) {
						String downloadedFilename = downloadedFile.getName();
						if (DownloadHelper.isDownloadFile(downloadedFile)) {
							File actualFile = new File( DownloadHelper.deriveActualFileName(downloadedFile));
							ensureContentsOfActualFile(downloadedFile, actualFile);
							candidate.setLocation( actualFile.getAbsolutePath());
							info.setFreshDownload(true);
							//downloadedFile.delete();
						}
						else {
							candidate.setLocation( downloadedFile.getAbsolutePath());
						}
					}
					
				}
				info.setOwningPart(candidate);
				
				return info;
			}
			
		}
		if (listener != null) {
			listener.acknowledgeFileEnrichmentFailure(walkScopeId, solution, tuple);
		}
		return null;
	
	}
	
	/**
	 * checks whether a part is already present in the solution's part list 
	 * @param solution - the {@link Solution}
	 * @param type - the {@link PartTuple} designating the part 
	 * @return - true if the {@link Solution} contains the {@link Part}
	 */
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
	
	private void ensureContentsOfActualFile(File downloadedFile, File actualFile) {
		Path path = actualFile.toPath();
		try {
			Files.createDirectories( actualFile.getParentFile().toPath());
		} catch (IOException e2) {
			throw new IllegalStateException("cannot create directories required for [" + actualFile.getAbsolutePath() + "]", e2);
		}
		try {
			Files.createFile( path);					
		} catch (FileAlreadyExistsException f) {
			if (downloadedFile.exists()) {
				if (!downloadedFile.delete()) {
					System.out.println("cannot delete [" + downloadedFile.getAbsolutePath() + "]");
				}
			}
			return;
		} catch (IOException e1) {
			throw new IllegalStateException("failed while leniently grapping [" + actualFile.getAbsolutePath() + "]", e1);
		}
		if (downloadedFile.exists() == false) {
			throw new IllegalStateException("source file [" + downloadedFile.getAbsolutePath() + "] doesn't exist");
		}
		try (
				InputStream in = new FileInputStream(downloadedFile);
				OutputStream out = new FileOutputStream(actualFile);
			) {						
			IOTools.pump(in, out, IOTools.SIZE_64K);
		} catch (Exception e) {
			throw new IllegalStateException("failed to transfer contents from [" + downloadedFile.getAbsolutePath() + "] to [" + actualFile.getAbsolutePath() + "]", e);
		}
		if (downloadedFile.exists()) {
			if (!downloadedFile.delete()) {
				System.out.println("cannot delete [" + downloadedFile.getAbsolutePath() + "]");
			}
		}
	}
}
