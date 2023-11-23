// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.coding.IdentificationWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.coding.SolutionWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.enriching.EnrichingException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClient;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.repository.filtering.ArtifactFilterExpertSupplier;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.ArtifactReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.PartDownloadInfo;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.ReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflectionSupport;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RetrievalMode;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.SolutionReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBias;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBiasPersitenceExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.MavenMetadataPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.SolutionContainerPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.SolutionPartReflectionContainerPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.DownloadHelper;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClient;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessException;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.info.ArtifactInformation;
import com.braintribe.model.artifact.info.LocalRepositoryInformation;
import com.braintribe.model.artifact.info.PartInformation;
import com.braintribe.model.artifact.info.RemoteRepositoryInformation;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.model.ravenhurst.data.RavenhurstArtifactDataContainer;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.model.ravenhurst.interrogation.CrcValidationLevelForBundle;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstRequest;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstResponse;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.paths.PathCollectors;
import com.braintribe.utils.paths.PathList;
import com.braintribe.utils.stream.NullOutputStream;

public class RepositoryReflectionImpl extends AbstractReflectionExpert implements ReflectionExpert, RepositoryReflection, RepositoryReflectionSupport {
	private static final String LOCAL_BUNDLE = "local";
	private static final String INDEX_FILENAME = ".solution";
	private static final String MAVEN_METADATA_PREFIX = "maven-metadata";
	private static final int POOL_SIZE = 5;
	private static Logger log = Logger.getLogger(RepositoryReflectionImpl.class);
	
	private RavenhurstScope ravenhurstScope;	
	private Map<Identification, ArtifactReflectionExpertImpl> artifactToExpertMap = CodingMap.create(new ConcurrentHashMap<>(), new IdentificationWrapperCodec());
	private Map<Solution, SolutionReflectionExpertImpl> solutionToExpertMap = CodingMap.create(new ConcurrentHashMap<>(), new SolutionWrapperCodec());
	private Map<String, Set<String>> repositoryToGroupsMap = new HashMap<>();
	private CrcValidationLevel crcValidationLevel = CrcValidationLevel.warn;
	private LockFactory lockFactory = new FilesystemSemaphoreLockFactory();
	private MavenSettingsReader reader;
	private ArtifactFilterExpertSupplier artifactFilterExpertSupplier;
	private Supplier<RepositoryViewResolution> repositoryViewResolutionSupplier;

	private boolean isSetup = false;
	boolean premptiveConnectionTest = true;
	@SuppressWarnings("unused")
	private long hysteresis = 60*1000; // defaut is 60s hysteresis
	private List<ArtifactBias> biasedArtifacts;
	private String currentMcScopeId;
	
	@Override @Configurable
	public void setCrcValidationLevel(CrcValidationLevel level) {
		crcValidationLevel = level;		
	}
	
	public void setMavenSettingsReader(MavenSettingsReader reader) {
		this.reader = reader;

	}
	
	@Override @Configurable @Required
	public void setRavenhurstScope(RavenhurstScope ravenhurstScope) {
		this.ravenhurstScope = ravenhurstScope;				
		this.ravenhurstScope.setLockFactory( lockFactory);
	}
	
	@Configurable
	public void setRepositoryViewResolutionSupplier(
			Supplier<RepositoryViewResolution> repositoryViewResolutionSupplier) {
		this.repositoryViewResolutionSupplier = repositoryViewResolutionSupplier;
	}
	
	@Override
	public RepositoryViewResolution getRepositoryViewResolution() {
		return repositoryViewResolutionSupplier != null? repositoryViewResolutionSupplier.get(): null;
	}
	
	@Configurable
	public void setArtifactFilterExpertSupplier(ArtifactFilterExpertSupplier artifactFilterExpertSupplier) {
		this.artifactFilterExpertSupplier = artifactFilterExpertSupplier;
	}
	@Configurable
	public void setLockFactory(LockFactory lockFactory) {
		this.lockFactory = lockFactory;
		if (ravenhurstScope != null) {
			ravenhurstScope.setLockFactory(lockFactory);
		}
	}
	
	@Override
	public void setCurrentScopeId(String id) {
		this.currentMcScopeId = id;		
	}

	/**
	 * set hysteresis in ms (minimal time lapse between two RH interrogations)
	 * @param histeresis
	 */
	@Configurable
	public void setHysteresis(long histeresis) {
		this.hysteresis = histeresis;
	}
	
	public boolean getPreemptiveConnectionTestMode() {
		return premptiveConnectionTest;
	}

	@Configurable
	public void setPreemptiveConnectionTestMode( boolean mode) {
		premptiveConnectionTest = mode;
	}

	
	@Override
	public void clear() {
		artifactToExpertMap.clear();
		solutionToExpertMap.clear();	
		isSetup = false;
		ravenhurstScope.clear();
	}
	
	
	
	@Override
	public void closeContext() {
		clear();
		accessClientFactory.closeContext();
		interrogationClientFactory.closeContext();		
	}
	
	@Override
	public void purgeOutdatedMetadata() {
		List<RavenhurstBundle> bundles;
		
		try {
			bundles = ravenhurstScope.getRavenhurstBundles();
		} catch (RavenhurstException e) {
			throw new RepositoryPersistenceException("cannot retrieve current set of bundles", e);
		}	
		
		//
		for (RavenhurstBundle bundle : bundles) {
			if (bundle.getDynamicRepository()) {
				Set<String> ravenhurstIndex = null;
				if (ravenhurstScope.getPersistenceRegistry().existsRavenhurstIndexPersistence( bundle)) {
					ravenhurstIndex = ravenhurstScope.getPersistenceRegistry().loadRavenhurstIndex(bundle);				
				}
				else if (!bundle.getInaccessible()){
					ravenhurstIndex = extractRavenhurstIndex(bundle);
					ravenhurstScope.getPersistenceRegistry().persistRavenhurstIndex(bundle, ravenhurstIndex);
				}
				else {
					log.warn("repository [" + bundle.getRepositoryId() + "] of profile [" + bundle.getProfileId() + "] has no .index file which cannot be updated as the repoistory is inaccessible.");
				}
				if (ravenhurstIndex != null && ravenhurstIndex.size() > 0) {
					repositoryToGroupsMap.putIfAbsent(bundle.getRepositoryId(), ravenhurstIndex);
				}
			}						
		}
		
		// if no MC scope id is passed, then we are in a standalone situation, and need to interrogate RH
		if (currentMcScopeId == null) {		
			for (RavenhurstBundle bundle : bundles) {
				interrogateRavenhurst(bundle);
			}
		
		}
		
		loadArtifactBias();
		
		try {
			ravenhurstScope.persistData();
		} catch (RavenhurstException e) {
			String msg ="persisting bundle information failed";
			throw new RepositoryPersistenceException( msg, e);
		}
	}

	/**
	 * run a full query on RH for this repo 
	 * @param bundle
	 * @return
	 */
	private Set<String> extractRavenhurstIndex(RavenhurstBundle bundle) {
		RepositoryInterrogationClient repositoryInterrogationClient = interrogationClientFactory.apply( bundle);
		
		try {
			RavenhurstRequest ravenhurstRequest = bundle.getRavenhurstRequest();		 		
			RavenhurstResponse ravenhurstResponse = repositoryInterrogationClient.extractIndex(ravenhurstRequest);
			List<Artifact> touchedArtifacts = ravenhurstResponse.getTouchedArtifacts();
			if (touchedArtifacts.size() > 0) {
				Set<String> result = new HashSet<>();
				for (Artifact a : touchedArtifacts) {
					result.add( a.getGroupId());
				}
				return result;
			}			
		} catch (RepositoryInterrogationException e) {
			log.warn("couldn't extract RH index from [" + bundle.getRepositoryUrl() + "], will try next scope initialization");
		}
		// error or emtpy list
		return null;
	}

	private void loadArtifactBias() {
		ArtifactBiasPersitenceExpert pcBiasExpert = new ArtifactBiasPersitenceExpert();
		pcBiasExpert.setLocalRepositoryLocationProvider(localRepositoryLocationProvider);
		pcBiasExpert.setLockFactory(lockFactory);
		biasedArtifacts = pcBiasExpert.decode();
	}

	private void interrogateRavenhurst(RavenhurstBundle bundle) {
		if (!bundle.getInaccessible()) {
			RepositoryAccessClient repositoryAccessClient = accessClientFactory.apply(bundle);
			// Dirk don't like that approach .. 
			if (premptiveConnectionTest ) {
				if (!repositoryAccessClient.checkConnectivity( bundle.getRepositoryUrl(), bundle.getRavenhurstRequest().getServer())) {
					log.warn("repository [" + bundle.getRepositoryId() + "] cannot be reached via [" + bundle.getRepositoryUrl() + "] and will be ignored in the currently active scope");
					bundle.setInaccessible(true);
				}			
			}
			
			if (bundle.getDynamicRepository()) {				
				Date lastAccessdate = null;
				try {
					// if no main container has been stored, this is the current date!
					lastAccessdate = ravenhurstScope.getUpdateTimestamp( bundle.getRepositoryUrl());
				} catch (RavenhurstException e) {
					String msg = "cannot retrieve last update for bundle [" + bundle.getRepositoryId() +"] of profile [" + bundle.getProfileId() + "]";
					log.error(msg, e);
					lastAccessdate = new Date();
				}
				try {
					RepositoryInterrogationClient repositoryInterrogationClient = interrogationClientFactory.apply( bundle);
					RavenhurstResponse ravenhurstResponse = repositoryInterrogationClient.interrogate( bundle.getRavenhurstRequest());
					bundle.setRavenhurstResponse( ravenhurstResponse);
					if (!ravenhurstResponse.getIsFaulty()) { 
						bundle.setDate( bundle.getRavenhurstResponse().getResponseDate());
					}
					else {
						String msg = "ravenhurst access failed, resetting time stamp for bundle [" + bundle.getRepositoryId() + "] of profile [" + bundle.getProfileId() + "]";
						log.warn(msg);
						bundle.setDate(lastAccessdate);
					}
					processRavenhurstResponse(bundle);					
				} catch (RuntimeException e1) {
					String msg ="not repository interrogation client exists for bundle [" + bundle.getRepositoryUrl() + "]";
					throw new RepositoryPersistenceException( msg, e1);
				} catch (RepositoryInterrogationException e1) {
					String msg ="interrogation of bundle [" + bundle.getRepositoryUrl() + "] failed";
					throw new RepositoryPersistenceException( msg, e1);
				}
			}
		}
		else {
			log.warn("repository [" + bundle.getRepositoryId() + "] has been declared to be inaccessible by default, probably due to offline mode");
			
		}
	}
	
	private Object initializingMonitor = new Object();
	/**
	 * use the collected information in the ravenhurst scope to interrogate all repositories, and process their response
	 * @throws RepositoryPersistenceException - 
	 */
	private void initializeRegistry() throws RepositoryPersistenceException {

		if (isSetup) {
			return;
		}
		synchronized( initializingMonitor) {
			if (isSetup)
				return;			
			purgeOutdatedMetadata();
			isSetup = true;
		}		
	}
	
	/**
	 * process update information from ravenhurst 
	 * @param bundle - the {@link RavenhurstBundle} to process 
	 * @throws RepositoryPersistenceException - 
	 */
	public void processRavenhurstResponse( RavenhurstBundle bundle) throws RepositoryPersistenceException{
		RavenhurstResponse response = bundle.getRavenhurstResponse();
		
		long before = System.nanoTime();
		// 		
		List<Artifact> touchedArtifacts = response.getTouchedArtifacts();
		List<String> touchedGroups = new ArrayList<>();
		
		Map<Identification, List<String>> touchedIdentifications = CodingMap.createHashMapBased( new IdentificationWrapperCodec());
		touchedArtifacts.stream().forEach( a -> {
			Identification identification = Identification.T.create();
			String groupId = a.getGroupId();
			identification.setGroupId( groupId);
			identification.setArtifactId(a.getArtifactId());
			
			// store in index, but only if there's an index at all, otherwise, let the index be read first 		
			Set<String> repoGroups = repositoryToGroupsMap.get( bundle.getRepositoryId());
			if (repoGroups != null) {
				if (repoGroups.add( groupId)) {				
					touchedGroups.add(groupId);
				}
			}
			
			List<String> versions = touchedIdentifications.get(identification);
			if (versions == null) {
				versions = new ArrayList<>();
				touchedIdentifications.put(identification, versions);
			}
			versions.add( a.getVersion());
		});
		ForkJoinPool pool = new ForkJoinPool(POOL_SIZE);		
		try {
			Runnable task = () -> touchedIdentifications.keySet().parallelStream().forEach( i -> {
				processRavenhurstResponseByIdentification(bundle, response, i.getGroupId(), i.getArtifactId(), touchedIdentifications.get( i));
			});
			pool.submit(task).get();
		} catch (Exception e) {
			throw new EnrichingException(e);
		}
		finally {
			pool.shutdown();			
		}
		
	
		// persist response
		ravenhurstScope.getPersistenceRegistry().persistRavenhurstBundle(bundle);
		// persist updated index 
		ravenhurstScope.getPersistenceRegistry().updateRavenhurstIndexPersistence(bundle, touchedGroups);
	
		
		// persist index 
		
		double difms = getElapsedMillis(before, System.nanoTime());
		if (log.isDebugEnabled()) {
			log.debug( "processing bundle [" + bundle.getRepositoryUrl() + "] took [" + difms + "] ms");
		}
	}
	
	
	/**
	 * processes RH responses by identification, i.e. reorders the input to most efficiently process it
	 * @param bundle - the {@link RavenhurstBundle}
	 * @param response - the {@link RavenhurstResponse}
	 * @param groupId - the groupId 
	 * @param artifactId - the artifactId 
	 * @param versions - a {@link List} of versions as String
	 * @throws RepositoryPersistenceException
	 */
	private void processRavenhurstResponseByIdentification(RavenhurstBundle bundle, RavenhurstResponse response, String groupId, String artifactId, List<String> versions) throws RepositoryPersistenceException {
		ForkJoinPool pool = new ForkJoinPool(POOL_SIZE);
		try {
			// only update if we know of this location, otherwise the lookup system will work anyhow
			File identificationLocation = PathList.create().push( localRepositoryLocationProvider.getLocalRepository(null)).push( groupId.replace('.', '/')).push(artifactId).toFile();
			if (!identificationLocation.exists()) {
				return;
			}
			//
			// work for the artifact expert is done here 
			//
			
			// simply delete the appropriate meta data file, and done..
			File identificationMavenMetadataFile = MavenMetadataPersistenceExpert.getFileForBundle(identificationLocation, bundle);
			RepositoryReflectionHelper.lockedFileDelete(lockFactory, identificationMavenMetadataFile);
			
			
			Runnable task = () ->
			versions.parallelStream().forEach( v -> {
				//
				// work for the solution expert is done here 
				//
				File solutionLocation = PathList.create().push( localRepositoryLocationProvider.getLocalRepository(null)).push( groupId.replace('.', '/')).push(artifactId).push( v).toFile();				
				if (!solutionLocation.exists()) {
					return;
				}
				// simply delete the part list 
				File partListFile = SolutionPartReflectionContainerPersistenceExpert.getLocationForBundle( solutionLocation, bundle.getRepositoryId());
				RepositoryReflectionHelper.lockedFileDelete( lockFactory, partListFile);
				
				// and delete maven meta data file 
				File solutionMavenMetadataFile = MavenMetadataPersistenceExpert.getFileForBundle(solutionLocation, bundle);
				RepositoryReflectionHelper.lockedFileDelete(lockFactory, solutionMavenMetadataFile);
				
				// in case the old file still exists, it needs to be deleted now 
				File obsoletePartListFile = new File( solutionLocation, SolutionContainerPersistenceExpert.RAVENHURST_SOLUTION_CONTAINER);
				// note : lockedFileDelete checks for existance on its own
				RepositoryReflectionHelper.lockedFileDelete(lockFactory, obsoletePartListFile);
							
			});
			pool.submit(task).get();			
			
		} catch (Exception e) {
			String msg="cannot react to artifact information from payload answer [" + response.getPayload() +"] for [" + bundle.getRepositoryId() + "]";
			throw new RepositoryPersistenceException(msg, e);
		}
		finally {
			pool.shutdown();			
		}
	}
	
	@Override
	public  ArtifactReflectionExpert acquireArtifactReflectionExpert(Identification artifact) throws RepositoryPersistenceException {
		// initialize if not done yet
		
		initializeRegistry();
		
		return artifactToExpertMap.computeIfAbsent(artifact, k -> {
			// get the location
			File location = getLocation(artifact);
			
			// if files do not exist, get the information from the remote repos (depending on bundles)
			if (!location.exists()) {
				location.mkdirs();
			}
			
			RavenhurstArtifactDataContainer container = RavenhurstArtifactDataContainer.T.create();	
			container.setArtifact(artifact);
	
			
			ArtifactReflectionExpertImpl newExpert = new ArtifactReflectionExpertImpl( artifact);
			newExpert.setAccessClientFactory(accessClientFactory);
			newExpert.setInterrogationClientFactory(interrogationClientFactory);
			newExpert.setLocalRepositoryLocationProvider( localRepositoryLocationProvider);
			newExpert.setScope(ravenhurstScope);
			newExpert.setRegistrySupport( this);
			newExpert.setArtifactFilterExpertSupplier(artifactFilterExpertSupplier);
			return newExpert;
		});
	}

	private File getLocation(Identification identification) throws RepositoryPersistenceException {
		File location;
		try {
			location = RepositoryReflectionHelper.getFilesystemLocation( new File( localRepositoryLocationProvider.getLocalRepository( null)), identification);
		} catch (RepresentationException e) {
			throw new RepositoryPersistenceException("cannot determine the location of [" + NameParser.buildName(identification) + "]", e);			
		}
		return location;
	}
	
	
	@Override
	public  SolutionReflectionExpert acquireSolutionReflectionExpert(Solution solution) throws RepositoryPersistenceException{
		// initialize if not done yet
		acquireArtifactReflectionExpert( solution);
		
		return solutionToExpertMap.computeIfAbsent(solution, k -> {
			SolutionReflectionExpertImpl expert = solutionToExpertMap.get( solution);
			if (expert != null)
				return expert;
			expert = new SolutionReflectionExpertImpl( solution);
			expert.setAccessClientFactory(accessClientFactory);
			expert.setInterrogationClientFactory(interrogationClientFactory);
			expert.setLocalRepositoryLocationProvider( localRepositoryLocationProvider);
			expert.setScope(ravenhurstScope);
			expert.setArtifactFilterExpertSupplier(artifactFilterExpertSupplier);
			expert.setRegistrySupport( this);
			return expert;
		});
	}
	
	private CrcValidationLevel transpose( CrcValidationLevelForBundle crcLevel) {
		switch( crcLevel) {
		case fail:
			return CrcValidationLevel.enforce;			
		case warn:
			return CrcValidationLevel.warn;		
		default:
		case ignore:			
			return CrcValidationLevel.ignore;		
		}
	}
	
	private CrcValidationLevel determineValidationLevel( RavenhurstBundle bundle, RepositoryRole role) {
		switch (role) {
		case both:
			CrcValidationLevelForBundle bundleLevelForRelease = bundle.getCrcValidationLevelForRelease();
			CrcValidationLevelForBundle bundleLevelForSnapshot = bundle.getCrcValidationLevelForSnapshot();
			if (bundleLevelForRelease == CrcValidationLevelForBundle.fail || bundleLevelForSnapshot == CrcValidationLevelForBundle.fail) {
				return CrcValidationLevel.enforce;
			}
			else if (bundleLevelForRelease == CrcValidationLevelForBundle.warn || bundleLevelForSnapshot == CrcValidationLevelForBundle.warn) {
				return CrcValidationLevel.warn;
			}
			else {
				return CrcValidationLevel.ignore;
			}			
		case release:
			return transpose( bundle.getCrcValidationLevelForRelease());			
		case snapshot:
			return transpose( bundle.getCrcValidationLevelForSnapshot());			
		default:
		case none:
			return CrcValidationLevel.ignore;			
		}
	}
		

	/**
	 * @param bundle - the {@link RavenhurstBundle}
	 * @param group - the group to check 
	 * @return - null if no filter expression, true if pass, false if fail
	 */
	Boolean isFiltered( RavenhurstBundle bundle, String group) {
		String expression = bundle.getIndexFilterExpression();
		if (expression == null)
			return null;
		
		String [] dynamics = expression.split( ",");
								
		for (String dynamic : dynamics) {
			dynamic = dynamic.trim();
			boolean reverse = false;
			if (dynamic.startsWith( "!")) {
				dynamic = dynamic.substring(1);
				reverse = true;
			}						
			dynamic = dynamic.replaceAll("\\*", ".*");
			if (group.matches( dynamic))
				if (!reverse)
					return true;
				else
					return false;
		}			
		return null;
	}
	
	@Override
	public boolean groupExistsInBundle(RavenhurstBundle bundle, String group) {
		// check for a 'fake index' filter expression 
		Boolean filteredPerExpression = isFiltered(bundle, group);
		if (filteredPerExpression != null)
			return filteredPerExpression;
		
		// if the bundle isn't dynamic, it cannot have an index, so the group COULD be there
		if (!bundle.getDynamicRepository())
			return true;
		
		// 
		String key = bundle.getRepositoryId();
		Set<String> groups = repositoryToGroupsMap.get(key);
		if (groups == null)
			return false;
		return groups.contains(group);
	}

	@Override
	public File downloadFileFrom ( PartDownloadInfo downloadInfo, RepositoryRole repositoryRole) throws RepositoryPersistenceException {
		List<RavenhurstBundle> bundles;
		try {
			bundles = ravenhurstScope.getRavenhurstBundles();
		} catch (RavenhurstException e) {
			throw new RepositoryPersistenceException("cannot retrieve current set of bundles", e);
		}
	
		for (RavenhurstBundle bundle : bundles) {
			if (bundle.getRepositoryUrl().equalsIgnoreCase( downloadInfo.getUrl())) {
				String actualTarget = downloadInfo.getTarget();
				String target = actualTarget + ".part";
				try {
					RepositoryAccessClient repositoryAccessClient = accessClientFactory.apply( bundle);
					Server server = bundle.getRavenhurstRequest().getServer();
					
					File downloaded = repositoryAccessClient.extractFileFromRepository( downloadInfo.getSource(), target, server);					

					CrcValidationLevel validationLevel = determineValidationLevel(bundle, repositoryRole);
					
					if (downloaded != null) {
							
						if (validationLevel != CrcValidationLevel.ignore) {
					
							//
							// validate down-loaded file via CRC
							//
							// validation-passed flag 
							boolean validated = false;
		
							// at least one hash present on server 
							boolean hashPresent = false;
							
							String md5HashSource = downloadInfo.getSource() + ".md5";
							String md5OnServer = repositoryAccessClient.extractFileContentsFromRepository(md5HashSource, server);
							if (md5OnServer != null) {
								String md5OnDisk = generatHash(downloaded, "SHA-1");
								
								hashPresent = true;
								md5OnServer = prune(md5OnServer);
								validated = md5OnServer.equalsIgnoreCase(md5OnDisk);			
								if (!validated) {
									log.error( "MD5 : expected [" + md5OnServer + "] found [" + md5OnDisk + "] for [" + downloaded.getAbsolutePath());
								}
							}
							
							if (!validated) {
								String sha1HashSource = downloadInfo.getSource() + ".sha1";
								String sha1OnServer = repositoryAccessClient.extractFileContentsFromRepository(sha1HashSource, server);
								if (sha1OnServer != null) {
									MessageDigest sha1Digest = MessageDigest.getInstance("Sha1");
									try (DigestInputStream in = new DigestInputStream(new FileInputStream(downloaded), sha1Digest)) {
										IOTools.transferBytes(in, NullOutputStream.getInstance(), IOTools.BUFFER_SUPPLIER_64K);
									}
									
									String sha1OnDisk = generatHash(downloaded, "sha1");
	
									
									hashPresent = true;
									sha1OnServer = prune(sha1OnServer);
									validated = sha1OnServer.equalsIgnoreCase(sha1OnDisk);
									if (!validated) {
										log.error( "SHA1 : expected [" + sha1OnServer + "] found [" + sha1OnDisk + "] for [" + downloaded.getAbsolutePath());
									}
								}
							}
							// hashes were present and validation failed against hashes 
							if (hashPresent && !validated) {
								String msg ="validation via hash failed on downloaded [" + target + "] from [" + downloadInfo.getSource() + "] at repository [" + bundle.getRepositoryId() + "]";
								switch (validationLevel) {
								case enforce:
									log.error( msg);
									throw new RepositoryPersistenceException( msg);
								case warn:							
									log.warn(msg);
									break;
								default:
								case ignore:
									break;							
								}							
							}

						}	
										
						// rename
						File targetFile = new File( target);
						File actualTargetFile = new File( actualTarget);
						DownloadHelper.ensureContentsOfActualFile( lockFactory, targetFile, actualTargetFile);					
						return actualTargetFile;
					}
					
				} catch (RuntimeException e) {
					String msg ="not repository access client exists for bundle [" + bundle.getRepositoryUrl() + "]";
					throw new RepositoryPersistenceException( msg, e);
				} catch (RepositoryAccessException e) {
					String msg ="cannot download [" + downloadInfo.getSource() + "] from repository [" + bundle.getRepositoryId() + "] to [" + target + "]";
					throw new RepositoryPersistenceException( msg, e);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				} catch (NoSuchAlgorithmException e) {
					String msg ="cannot generate hash [" + downloadInfo.getSource() + "] from repository [" + bundle.getRepositoryId() + "] to [" + target + "]";
					throw new RepositoryPersistenceException( msg, e);
				}				
			}
		}
		return null;
	}
	/*
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
	
	*/

	@Override
	public ReadWriteLock getLockInstance(File fileToLock) {
		return lockFactory.getLockInstance(fileToLock);
	}

	private String generatHash(File downloaded, String algorithm) throws NoSuchAlgorithmException, IOException, FileNotFoundException {
		MessageDigest md5Digest = MessageDigest.getInstance(algorithm);
		try (DigestInputStream in = new DigestInputStream(new FileInputStream(downloaded), md5Digest)) {
			IOTools.transferBytes(in, NullOutputStream.getInstance(), IOTools.BUFFER_SUPPLIER_64K);
		}
		String md5OnDisk = StringTools.toHex(md5Digest.digest());
		return md5OnDisk;
	}

	private String prune(String hashOnServer) {
		int index = hashOnServer.indexOf( " ");
		if (index > 0) {
			hashOnServer = hashOnServer.substring(0, index);
		}
		return hashOnServer;
	}

	@Override
	public RavenhurstBundle getRavenhurstBundleForUrl(String url) throws RepositoryPersistenceException {
		List<RavenhurstBundle> bundles;
		try {
			bundles = ravenhurstScope.getRavenhurstBundles();
		} catch (RavenhurstException e) {
			throw new RepositoryPersistenceException("cannot retrieve current set of bundles", e);
		}
		for (RavenhurstBundle bundle : bundles) {
			if (bundle.getRepositoryUrl().equalsIgnoreCase( url)) {
				return bundle;
			}
		}
		return null;
	}
	
	

	@Override
	public RavenhurstBundle getRavenhurstBundleForId(String id) throws RepositoryPersistenceException {
		List<RavenhurstBundle> bundles;
		try {
			bundles = ravenhurstScope.getRavenhurstBundles();
		} catch (RavenhurstException e) {
			throw new RepositoryPersistenceException("cannot retrieve current set of bundles", e);
		}
		for (RavenhurstBundle bundle : bundles) {
			if (bundle.getRepositoryId().equalsIgnoreCase( id)) {
				return bundle;
			}
		}
		return null;	
	}

	@Override
	public void updateRepositoryInformationOfArtifact(RavenhurstBundle bundle, Identification unversionedArtifact)  throws RepositoryPersistenceException  {
		ArtifactReflectionExpertImpl expert = artifactToExpertMap.get(unversionedArtifact);
		if (expert == null) {
			throw new RepositoryPersistenceException( "unexpectedly, no expert stored for [" + NameParser.buildName(unversionedArtifact) + "]");
		}
		File location = RepositoryReflectionHelper.getFilesystemLocation( new File(localRepositoryLocationProvider.getLocalRepository(null)), unversionedArtifact);
		expert.updateRepositoryInformationOfArtifact(location, bundle, true);
	}

	@Override
	public ArtifactBias getArtifactBias(Identification unversionedArtifact) {
		if (biasedArtifacts == null) {
			loadArtifactBias();
		}
		for (ArtifactBias bias : biasedArtifacts) {
			if (bias.matches(unversionedArtifact))
				return bias;
		}		
		return null;		
	}

	@Override
	public boolean solutionExistsInBundle(RavenhurstBundle bundle, Solution solution) {
		ArtifactReflectionExpertImpl artifactReflectionExpertImpl = artifactToExpertMap.get(solution);
		if (artifactReflectionExpertImpl == null) {
			log.warn( "no artifact reflection  expert found for [" + NameParser.buildName(solution));
			return false;
		}
		return artifactReflectionExpertImpl.versionExistsInBundle(bundle, solution.getVersion());		
	}	

	public List<ArtifactBias> getBiasInformation() {
		if (biasedArtifacts == null) {
			loadArtifactBias();
		}
		return biasedArtifacts;
	}

	@Override
	public ArtifactInformation retrieveInformation(Solution solution, RetrievalMode retrievalMode) {
		//
		String localRepository = localRepositoryLocationProvider.getLocalRepository(null);
		String artifactFilesystemLocation = RepositoryReflectionHelper.getArtifactFilesystemLocation(localRepository, solution);
		
		String version = VersionProcessor.toString( solution.getVersion());
		File artifactFilesystem  = new File( artifactFilesystemLocation, version);
	
		if (!artifactFilesystem.exists()) {
			
			switch (retrievalMode) {
			case eager:
				// try to get the pom
				log.debug( "forcing a resolve on the pom of [" + NameParser.buildName( solution) + "]");				
				SolutionReflectionExpert reflectionExpert = acquireSolutionReflectionExpert(solution);
				Part part = Part.T.create();
				ArtifactProcessor.transferIdentification(part, solution);
				part.setType( PartTupleProcessor.createPomPartTuple());
				File file = reflectionExpert.getPart( part, null, RepositoryRole.both);
				if (file == null) {
					return null;
				}
				break;
			case passive:
				default:
					return null;			
			}
		}
		// if eager, update the index files 
		switch (retrievalMode) {
			case eager:
				log.debug( "updating all index files of [" + NameParser.buildName( solution) + "]");
				SolutionReflectionExpert reflectionExpert = acquireSolutionReflectionExpert(solution);
				// update all index files
				try {
					ravenhurstScope.getRavenhurstBundles().stream().forEach( b -> reflectionExpert.retrievePartListFromRemoteRepository(b));
				}
				catch (Exception e) {
					log.error( "cannot update all index files of [" + NameParser.buildName( solution) + "]", e);
				}
				break;
			case passive:
			default:
				break;
		}
		
		// 
		ArtifactInformation info = ArtifactInformation.T.create();
		info.setGroupId( solution.getGroupId());
		info.setArtifactId( solution.getArtifactId());
		info.setVersion( version);
		
		// look at the different maven metadata files 
		File [] mavenMetadataFiles = artifactFilesystem.listFiles();
		List<File> parts = new ArrayList<>();
		Map<String, List<String>> remotes = new HashMap<>();
		for (File file : mavenMetadataFiles) {
			String name = file.getName();
			// is a metadata file 
			if (name.endsWith( INDEX_FILENAME)) { 
						
				String bundleId = name.substring( 0, name.length()-INDEX_FILENAME.length());

				if (!bundleId.equalsIgnoreCase( LOCAL_BUNDLE)) { 
				
					File indexFile = SolutionPartReflectionContainerPersistenceExpert.getLocationForBundle(artifactFilesystem, bundleId);
					// if reset is not requested, see what's stored, otherwise retrieve it from the repository 			
					if (indexFile.exists()) {
						List<String> decodedList = SolutionPartReflectionContainerPersistenceExpert.decode(artifactFilesystem, bundleId);
						remotes.put(bundleId, decodedList);						
					}
					
				}
				// 
			}
			else if (!name.startsWith(MAVEN_METADATA_PREFIX)){
				parts.add(file);
			}							
		}
		
		info.setLocalInformation( generateLocalInformation( solution, parts));
		info.getRemoteInformation().addAll( generateRemoteInformation( solution, remotes));
		
		
		return info;
		
	}

	/**
	 * turn collected information of indexed files into the GE
	 * @param solution - the {@link Solution} we have information
	 * @param remotes - a {@link Map} that maps the bundle-id to the file list 
	 * @return - a {@link List} of {@link RemoteRepositoryInformation}
	 */
	private List<RemoteRepositoryInformation> generateRemoteInformation(Solution solution, Map<String, List<String>> remotes) {
		Map<String, RemoteRepository> repoMap = new HashMap<>();
		for (RemoteRepository repository : reader.getAllRemoteRepositories()) {
			repoMap.put( repository.getName(), repository);
		}
				
		String knownPrefix = solution.getArtifactId() + "-" + VersionProcessor.toString( solution.getVersion());
		List<RemoteRepositoryInformation> informationList = new ArrayList<RemoteRepositoryInformation>();
		for (Entry<String, List<String>> entry : remotes.entrySet()) {
			RemoteRepositoryInformation information = RemoteRepositoryInformation.T.create();
			RemoteRepository repository = repoMap.get(entry.getKey());
			information.setUrl( repository.getUrl());
			information.setName( repository.getName());
			
			for (String name : entry.getValue()) {
				if (!name.startsWith(knownPrefix)) 
					continue;
				Pair<String,String> tuple = extractTuple(name, knownPrefix);
				PartInformation partInformation = PartInformation.T.create();
				partInformation.setClassifier(tuple.first());
				partInformation.setType( tuple.second());
				
				partInformation.setUrl( RepositoryReflectionHelper.getSolutionUrlLocation(repository.getUrl(), solution) + "/" + name);
				information.getPartInformation().add(partInformation);
			}
			informationList.add(information);
		}
		
		return informationList;
	}
	
	private Pair<String,String> extractTuple( String name, String knownPrefix) {
		int lastDot = name.lastIndexOf('.');
		String extension = name.substring( lastDot+1);
		if (extension.equalsIgnoreCase("md5") || extension.equalsIgnoreCase("sha1") || extension.equalsIgnoreCase("asc")) {
			lastDot = name.substring(0, lastDot).lastIndexOf('.');
		}
		

		String remainder = name.substring( 0, lastDot);
		String classifier;
		if (remainder.equalsIgnoreCase(knownPrefix)) {
			classifier = null;
		}
		else {
			classifier = remainder.substring( knownPrefix.length()+1);
		}
		
		return new Pair<String, String>(classifier, extension);

	}

	/**
	 * turn the collected local information  
	 * 
	 * @param solution
	 * @param parts
	 * @return
	 */
	private LocalRepositoryInformation generateLocalInformation(Solution solution, List<File> parts) {
		LocalRepositoryInformation information = LocalRepositoryInformation.T.create();
		information.setUrl( localRepositoryLocationProvider.getLocalRepository());
		String knownPrefix = solution.getArtifactId() + "-" + VersionProcessor.toString( solution.getVersion());
		for (File file : parts) {
			try {
				String name = file.getName();
				if (!name.startsWith(knownPrefix)) 
					continue;
				
				Pair<String,String> tuple = extractTuple(name, knownPrefix);
				PartInformation partInformation = PartInformation.T.create();
				partInformation.setClassifier(tuple.first());
				partInformation.setType( tuple.second());
				
				partInformation.setUrl( file.toURI().toURL().toString());
				information.getPartInformation().add(partInformation);
			} catch (MalformedURLException e) {
				log.error( "cannot turn [" + file.getAbsolutePath() + "] into valid URL", e);
			}
		}
		return information;
	}
	
	private static double getElapsedMillis( long before, long after) {
		double dif = after - before;
		return (dif / 1E6);
	}	
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection#correctLocalRepositoryStateOf(java.lang.String)
	 */
	@Override
	public List<String> correctLocalRepositoryStateOf( String repo) {
		try {
			RavenhurstBundle bundle = ravenhurstScope.getRavenhurstBundleByName(repo);
			if (bundle != null) {
				return correctLocalRepositoryStateOf(bundle);
			}
		} catch (RavenhurstException e) {			
			;
		}
		return Collections.emptyList();
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection#correctLocalRepositoryStateOf(com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle)
	 */
	@Override
	public List<String> correctLocalRepositoryStateOf( RavenhurstBundle bundle) {
		// get current index
		Set<String> currentIndex = null;
		if (ravenhurstScope.getPersistenceRegistry().existsRavenhurstIndexPersistence( bundle)) {
			currentIndex = ravenhurstScope.getPersistenceRegistry().loadRavenhurstIndex(bundle);				
		}
		if (currentIndex == null)
			return Collections.emptyList();
			
		// retrieve new index
		Set<String> updatedIndex = extractRavenhurstIndex(bundle);
		
		// compare - extract groups that weren't there yet 
		List<String> newGroups = new ArrayList<>();
		for (String updatedGroup : updatedIndex) {
			if (!currentIndex.contains(updatedGroup)) {
				newGroups.add( updatedGroup);
			}
		}
		if (newGroups.size() == 0)
			return Collections.emptyList();
		
		
		ravenhurstScope.getPersistenceRegistry().persistRavenhurstIndex(bundle, updatedIndex);
		
		for (String grp : newGroups) {
			// get location
			String grpSplit = grp.replace( '.', '/');
			String join = PathCollectors.filePath.join( localRepositoryLocationProvider.getLocalRepository(), grpSplit);
			File grpRoot = new File( join);					
			RepositoryReflectionHelper.purgeMetadataByName(lockFactory, grpRoot, Collections.singletonList( bundle.getRepositoryId()));			
		}
				
		return newGroups;
 
	}
	
}



