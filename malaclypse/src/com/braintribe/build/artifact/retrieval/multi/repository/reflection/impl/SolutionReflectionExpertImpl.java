// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClient;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.repository.filtering.ArtifactFilterExpertSupplier;
import com.braintribe.build.artifact.retrieval.multi.repository.filtering.IdentificationConversion;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.PartDownloadInfo;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflectionSupport;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.SolutionReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.MavenMetadataPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.SolutionContainerPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.SolutionPartReflectionContainerPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.DownloadHelper;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClient;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessException;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.maven.metadata.MavenMetaDataProcessor;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.model.ravenhurst.data.RavenhurstSolutionDataContainer;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.utils.paths.PathList;


public class SolutionReflectionExpertImpl extends AbstractReflectionExpert implements SolutionReflectionExpert {
	private static Logger log = Logger.getLogger(SolutionReflectionExpertImpl.class);
	
	private RepositoryReflectionSupport registrySupport;
	private Map<String, List<String>> bundleToPartList;
	private Map<String, MavenMetaData> bundleToMavenMetaData;
	private Solution reflectingSolution;
	private RavenhurstScope scope;
	private File location;
	private RavenhurstSolutionDataContainer ravenhurstSolutionDataContainer;
	private ArtifactFilterExpertSupplier artifactFilterExpertSupplier;
	
	public SolutionReflectionExpertImpl(Solution solution) {
		reflectingSolution = solution;
	}

	@Configurable @Required
	public void setScope(RavenhurstScope scope) {
		this.scope = scope;
	}
	@Configurable @Required
	public void setRegistrySupport(RepositoryReflectionSupport downloader) {
		this.registrySupport = downloader;
	}
	
	@Configurable @Required
	public void setArtifactFilterExpertSupplier(ArtifactFilterExpertSupplier artifactFilterExpertSupplier) {
		this.artifactFilterExpertSupplier = artifactFilterExpertSupplier;
	}
	
	@Override
	public File getLocation() {
		if (location == null) {
			location = RepositoryReflectionHelper.getFilesystemLocation( new File(localRepositoryLocationProvider.getLocalRepository(null)), reflectingSolution);
		}
		return location;
	}
	
	private Object bundleMonitor = new Object();
	
	private Map<String, List<String>> getBundleToPartlist() {
		if (bundleToPartList != null) {
			return bundleToPartList;
		}
		
		synchronized( bundleMonitor) {
			if (bundleToPartList != null) {
				return bundleToPartList;
			}
			
			Map<String, List<String>> bundleToPartList = new HashMap<>();
			try {
				List<RavenhurstBundle> ravenhurstBundles = scope.getRavenhurstBundles();
				
				for (RavenhurstBundle bundle : ravenhurstBundles) {
					// only update information if the bundle contains this version 
					if (registrySupport.solutionExistsInBundle(bundle, reflectingSolution)) {
						List<String> retrieveInfo = retrieveArtifactInformationOfBundle(bundle, false);
						bundleToPartList.put( bundle.getRepositoryId(), retrieveInfo);
					}
					else {
						log.debug("solution [" + NameParser.buildName(reflectingSolution) + "] doesn't exist in bundle [" + bundle.getRepositoryId() + "], ignored");
					}
				}
				
			} catch (Exception e) {
				Exceptions.contextualize(e, "cannot retrieve part lists for [" + NameParser.buildName( reflectingSolution) + "]");
			} 
			this.bundleToPartList = bundleToPartList;
		}
		return bundleToPartList;
		
	}

	private List<String> retrieveArtifactInformationOfBundle(RavenhurstBundle bundle, boolean reset) throws RepositoryPersistenceException {
		String bundleId = bundle.getRepositoryId();
		File file = SolutionPartReflectionContainerPersistenceExpert.getLocationForBundle(getLocation(), bundleId);
		List<String> decodedList;
		// if reset is not requested, see what's stored, otherwise retrieve it from the repository 
		if (!reset) {
			if (file.exists()) {
				decodedList = SolutionPartReflectionContainerPersistenceExpert.decode(getLocation(), bundleId);
			}
			else {
				// check if there's a .upated.solution file
				// if so, load it an retrieve the information from there? 				
				decodedList = readFromObsoleteContainer( bundle);
				// if nothing is present, and we may do get a listing from the repo, retrieve the file list. 
				if (decodedList == null && bundle.getListingLenient()) {
					decodedList = retrievePartListFromRemoteRepository( bundle);
				}
			}
		}
		else {
			decodedList = retrievePartListFromRemoteRepository( bundle);
		}
		return decodedList;
	}
	
	private List<String> updateArtifactInformationOfBundle( RavenhurstBundle bundle, String filename, boolean exists) {
		String storeName = filename;
		if (!exists) {
			storeName = "!:" + filename;
		}
		String bundleId = bundle.getRepositoryId();
		List<String> parts = bundleToPartList.get( bundleId);
		if (parts == null) {
			parts = new ArrayList<>();			
		}
		if (!parts.contains( storeName)) {
			parts.add(storeName);
		}
		
		changePartListOfRemoteRepository( bundle, parts);		
		return parts;
	}
	
	private Object mavenMetadataMonitor = new Object();
	
	private Map<String, MavenMetaData> getMavenMetaData() {
		if (bundleToMavenMetaData != null) {
			return bundleToMavenMetaData;
		}
		synchronized ( mavenMetadataMonitor) {
			
			if (bundleToMavenMetaData != null) {
				return bundleToMavenMetaData;
			}
		
			HashMap<String,MavenMetaData> bundleToMavenMetaData = new HashMap<>();
			try {
				List<RavenhurstBundle> ravenhurstBundles = scope.getRavenhurstBundles();
				
				for (RavenhurstBundle bundle : ravenhurstBundles) {
					// do not update maven-metadata if bundle doesn't contain the solution 
					if (!registrySupport.solutionExistsInBundle(bundle, reflectingSolution)) {
						log.debug("solution [" + NameParser.buildName(reflectingSolution) + "] doesn't exist in bundle [ "+ bundle.getRepositoryId() +"], ignored");
						continue;
					}
					String bundleId = bundle.getRepositoryId();
					File mavenMetaDataFile = MavenMetadataPersistenceExpert.getFileForRepository(getLocation(), bundleId);
					MavenMetaData metaData;
					if (mavenMetaDataFile.exists()) {
						metaData = MavenMetadataPersistenceExpert.decode( registrySupport, mavenMetaDataFile);
					}
					else {
						metaData = updateMavenMetaData(mavenMetaDataFile, bundle);
					}
					bundleToMavenMetaData.put(bundleId, metaData);				
				}
				this.bundleToMavenMetaData = bundleToMavenMetaData;
			} catch (Exception e) {
				Exceptions.contextualize(e, "cannot retrieve maven metadata for [" + NameParser.buildName( reflectingSolution) + "]");
			} 
		}
		return bundleToMavenMetaData;
		
	}
	
	
	
	private List<String> readFromObsoleteContainer(RavenhurstBundle bundle) {
		if (ravenhurstSolutionDataContainer == null) {
			File dotUpdated = new File( getLocation(), SolutionContainerPersistenceExpert.RAVENHURST_SOLUTION_CONTAINER);
			if (!dotUpdated.exists()) {
				return null;
			}
			try {
				ravenhurstSolutionDataContainer = SolutionContainerPersistenceExpert.decode(registrySupport, getLocation());
			} catch (RepositoryPersistenceException e) {
				return null;
			}	
		}
		
		//System.out.println("Found old persisted data for [" + NameParser.buildName( reflectingSolution) + "]");
		List<String> names = new ArrayList<>();
	
		Artifact artifact = ravenhurstSolutionDataContainer.getUrlToSolutionMap().get( bundle.getRepositoryUrl());
		if (artifact != null) {
		
			for (com.braintribe.model.ravenhurst.Part part : artifact.getParts()) {
				names.add( part.getName());
			}
		}
		File locationForBundle = SolutionPartReflectionContainerPersistenceExpert.getLocationForBundle(getLocation(), bundle.getRepositoryId());
		storePartlist(bundle, locationForBundle, names);
		
		return names;
	}

	@Override
	public Collection<PartDownloadInfo> getDownloadInfo(Part part, String expectedPartName, String target, RepositoryRole role) {
		Map<String, PartDownloadInfo> untrustWorthyInfoMap = new HashMap<String, PartDownloadInfo>();
		
		if (expectedPartName == null) {			
			expectedPartName = NameParser.buildName(part);			
		}

		Map<String, List<String>> currentlyRelevantPartlists = getBundleToPartlist();
		Map<String, MavenMetaData> currentlyRelevantMetaData = getMavenMetaData();

		String partName = expectedPartName;
		// this url is ok to use, so check the part list and find it
		// if role's a snapshot, then we must modify the name of the snapshot		
		// otherwise, it's ok to use 
		
		// find the current snapshot
		
		try {
			CompiledPartIdentification cpi = IdentificationConversion.toCompiledPartIdentification(reflectingSolution, part.getType());
			for (RavenhurstBundle bundle : scope.getRavenhurstBundles()) {
				String bundleId = bundle.getRepositoryId();
				// filter per artifact filter
				ArtifactFilterExpert afe = artifactFilterExpertSupplier.artifactFilter( bundleId);
				if (!afe.matches( cpi)) {
					log.debug("part [" + NameParser.buildName( part) + "] is excluded for repository [" + bundleId + "] per artifact filter");
					continue;
				}
				if (bundle.getInaccessible()) {
					log.debug("bundle [" + bundle.getRepositoryUrl() + "] is inaccessible, is not queried for part [" + expectedPartName + "]");
					continue;
				}
				if (!RepositoryReflectionHelper.repositoryRoleMatch( role, bundle)) {
					continue;
				}
				String match = null;

				// if trustworthy, the version must exist in the bundle (via metadata), otherwise, we'll try to get it
				if (registrySupport.solutionExistsInBundle(bundle, reflectingSolution) || !bundle.getTrustworthyRepository()) {
					
					// if its is a snapshot that we're looking for, we need to modify the expected name to reflect how
					// snapshots are named remotely 
					if (role == RepositoryRole.snapshot) {
						
						MavenMetaData mavenMetaData = currentlyRelevantMetaData.get(bundleId);
						
						String snapShotPrefix = MavenMetaDataProcessor.getSnapshotPrefix( mavenMetaData);
						PartTuple partTuple = part.getType();
						String classifier = partTuple.getClassifier();
						if (classifier != null && classifier.length() > 0) {
							partName = snapShotPrefix + "-" + classifier + "." + partTuple.getType();
						} else {
							partName = snapShotPrefix + "." + partTuple.getType();
						}
						
					}
					List<String> names = currentlyRelevantPartlists.get( bundleId);
					
				
					// find a matching name in the list of files 
					if (names != null && names.size() > 0) {
						for (String suspect : names) {
														
							switch ( filterMatch( suspect, partName) ) {
								case absent: // not listed 
									break;
								case missing: // explicitly listed as not existing 
									return Collections.emptyList();									
								case present: // present				
								default:
									match = suspect;
									break;								
							}
							if (match != null)
								break;
						}
					}
					if (match == null && names != null && expectedPartName.endsWith( ".pom")) {
						System.out.println("version present in metadata of [" + bundle.getRepositoryId() + "@" + bundle.getRepositoryUrl() + "], yet not in index for [" + NameParser.buildName( reflectingSolution));
					}
				}
				
				
				if (match == null) {
					// if no info or trustworthy, well, no use to try to access it 
					if (bundle.getTrustworthyRepository() && bundle.getListingLenient()) {
						continue;
					}
					else {							 
						//fake a download info for the unknown artifact if the repository is untrustworthy..				
						String source = PathList.create().push( bundle.getRepositoryUrl()).push( part.getGroupId().replace('.', '/')).push(part.getArtifactId()).push( VersionProcessor.toString( part.getVersion())).push( partName).toSlashPath();
						PartDownloadInfo info = new PartDownloadInfo( bundle, source, target);
						info.setIdentifiedAsNotTrustworthy(true);
						info.setName( partName);
						info.setActualName( partName);
						untrustWorthyInfoMap.put( bundle.getRepositoryId(), info);
						continue;
					} 					
				}
				
				// build download information 
				String source = PathList.create().push( bundle.getRepositoryUrl()).push( part.getGroupId().replace('.', '/')).push(part.getArtifactId()).push( VersionProcessor.toString( part.getVersion())).push( partName).toSlashPath();				
				PartDownloadInfo info = new PartDownloadInfo( bundle, source, target);
				info.setIdentifiedAsNotTrustworthy(false);
				return Collections.singleton(info);				
			}
				
			// 
			// nothing found, any untrustworthy? 
			// 
			if (untrustWorthyInfoMap.size() > 0) {
				return untrustWorthyInfoMap.values();
			}
		} catch (RavenhurstException e) {
			log.error( "cannot generate any download information", e);
		}
		return null;
	}
	
	
	private SolutionListPresence filterMatch(String s, String expectedPartName) {		
		if (s.equalsIgnoreCase(expectedPartName))
			return SolutionListPresence.present;
		if (s.equalsIgnoreCase( "!:" + expectedPartName))
			return SolutionListPresence.missing;
		return SolutionListPresence.absent;
	}
 
	private boolean matchPart( Part part) {
		CompiledPartIdentification cpi = IdentificationConversion.toCompiledPartIdentification(part, part.getType());
		try {
			
			List<RavenhurstBundle> ravenhurstBundles = scope.getRavenhurstBundles();
			List<String> repositoryIds = ravenhurstBundles.stream().map( rb -> rb.getRepositoryId()).collect(Collectors.toList());
			repositoryIds.add("local");
					
			for (String repositoryId : repositoryIds) {
				ArtifactFilterExpert afe = artifactFilterExpertSupplier.artifactFilter( repositoryId);
				if (afe.matches(cpi)) {
					return true;
				}
			}
			
		} catch (RavenhurstException e) {
			log.error( "cannot retrieve repository bundles for filtering", e);
		}
		log.debug("existing part [" + NameParser.buildName( part) + "] is excluded as no artifact filter matches it");
		return false;
	}
	
	private Object updateingMonitor = new Object();
	@Override
	public File getPart(Part part, String expectedPartName, RepositoryRole repositoryRole) throws RepositoryPersistenceException{
		
		// filter per artifact filter
		if (!matchPart( part)) {
			return null;
		}
		
		if (expectedPartName == null) {			
			expectedPartName = NameParser.buildFileName(part);			
		}
		File parent = getLocation();
		File suspect = new File( parent, expectedPartName);
		
		// access local file system to find file
		try {
			if (suspect.exists()) { 
				return suspect;
			}
			
			synchronized (updateingMonitor) {
				if (suspect.exists()) { 
					return suspect;
				}
				// 
				// determine download info by accessing the metadata of the repositories in question 
				//
				
				// might get several download infos in case none of the repositories actually declare it, and more than one are untrustworthy 
				Collection<PartDownloadInfo> downloadInfos = getDownloadInfo(part, expectedPartName, suspect.getAbsolutePath(), repositoryRole);
	
				// none found, nothing to download
				if (downloadInfos == null) {
					return null;
				}
				
				// in the best case, it's only one, but there might be several, see above
				for (PartDownloadInfo downloadInfo : downloadInfos) {
					RavenhurstBundle bundle = downloadInfo.getBundle();
	
					
						// get download
						File file = registrySupport.downloadFileFrom(downloadInfo, repositoryRole);
						
						if (file != null) {
		
							if (downloadInfo.getIdentifiedAsNotTrustworthy()) {
								// adapt to updating solution list
								// update the container data? In case of a not trustworthy repository, this file have been downloaded
								// despite it not being declared. So as it obviously worked, it could be declared now.
								// trigger all experts to update information about this repository..
								
								// trigger artifact expert to add the solution to the metadata of the repository 
								registrySupport.updateRepositoryInformationOfArtifact(bundle, reflectingSolution);
								
								// update MC's file list data to mark that this file does exist in the repository
								 
									List<String> updatedArtifactInformationOfBundle = updateArtifactInformationOfBundle(bundle, downloadInfo.getActualName(), true);
									getBundleToPartlist().put( bundle.getRepositoryId(), updatedArtifactInformationOfBundle);																					
							}
							// found a file, that's enough, so even if there are more, that's all it takes 
							return file;
						}
						else {
							// update MC's file list data to mark that this file doesn't exist in the repo
							synchronized (updateingMonitor) {
								List<String> informationOfBundle = updateArtifactInformationOfBundle(bundle, downloadInfo.getActualName(), false);
								getBundleToPartlist().put( bundle.getRepositoryId(), informationOfBundle);
							}
						}
					
				}
				// no file found, even in untrustworthy repositories 
				return null;
			}
			
		} catch (RepresentationException e) {
			String msg = "cannot determine location in local repository";
			log.error( msg, e);
			throw new RepositoryPersistenceException(msg, e);
			
		} catch (RepositoryPersistenceException e) {
			String msg ="cannot download [" + expectedPartName + "] of [" + NameParser.buildName( (com.braintribe.model.artifact.Artifact) part) + "]";
			log.error( msg, e);
			throw new RepositoryPersistenceException(msg, e);
		}			
	}
	
	
	
	@Override
	public PartDownloadInfo transactionalGetPart(Part candidate, String expectedPartName, RepositoryRole repositoryRole) {
			
		if (expectedPartName == null) {			
			expectedPartName = NameParser.buildName(candidate);			
		}
		// access local file system to find file
		try {
			File target = new File( getLocation(), expectedPartName);
			if (target.exists()) {
				if (log.isDebugEnabled()) {
					log.debug("target [" + target.getAbsolutePath() + "] exists");
				}
				// filter per artifact filter
				if (!matchPart( candidate)) {
					return null;
				}
				return new PartDownloadInfo(target);
			}
			// 
			// determine download info by accessing the metadata of the repositories in question 
			//
			
			// might get several download infos in case none of the repositories actually declare it, and more than one are untrustworthy
			String transactionalTarget = DownloadHelper.deriveDownloadFileName(target);
			Collection<PartDownloadInfo> downloadInfos = getDownloadInfo(candidate, expectedPartName, transactionalTarget, repositoryRole);

			// none found, nothing to download
			if (downloadInfos == null) {
				if (log.isDebugEnabled()) {
					log.debug("no possible sources found for part [" + expectedPartName + "] of [" + NameParser.buildName(candidate) + "]");
				}
				return null;
			}
			
			// in the best case, it's only one, but there might be several, see above
			for (PartDownloadInfo downloadInfo : downloadInfos) {
				//RavenhurstBundle bundle = downloadInfo.getBundle();

				// get download
				File file = registrySupport.downloadFileFrom(downloadInfo, repositoryRole);
				
				RavenhurstBundle bundle = downloadInfo.getBundle();
				
				if (file != null) {
					if (log.isDebugEnabled()) {
						log.debug("downloaded [" + file.getAbsolutePath() + "] for [" + expectedPartName + "] of [" + NameParser.buildName(candidate) + "] from [" + bundle.getRepositoryUrl() +"]");
					}
					downloadInfo.setFile(file);
					downloadInfo.setFreshDownload(true);
					if (downloadInfo.getIdentifiedAsNotTrustworthy()) {
						// this is an unexpected source, so a few things need to be done
						// 
						// despite it not being declared. So as it obviously worked, it could be declared now.

						// trigger artifact expert to add the solution to the metadata of the repository 
						registrySupport.updateRepositoryInformationOfArtifact(bundle, reflectingSolution);
						
						// update MC's file list data to mark that this file does exist in the repository
						List<String> updateArtifactInformationOfBundle = updateArtifactInformationOfBundle(bundle, downloadInfo.getActualName(), true);				
						getBundleToPartlist().put(bundle.getRepositoryId(), updateArtifactInformationOfBundle);
					}
					
					// found a file, that's enough, so even if there are more, that's all it takes 
					return downloadInfo;
				}
				else {
					if (log.isDebugEnabled()) {
						log.debug("nothing found for [" + expectedPartName + "] of [" + NameParser.buildName(candidate) + "] in [" + bundle.getRepositoryUrl() +"]");
					}
					// update MC's file list data to mark that this file doesn't exist in the repo
					updateArtifactInformationOfBundle(bundle, downloadInfo.getActualName(), false);
				}
			}
			// no file found, even in untrustworthy repositories 
			if (log.isDebugEnabled()) {
				log.debug("nothing found for [" + expectedPartName + "] of [" + NameParser.buildName(candidate) + "] in any active repository");
			}

			return null;
			
		} catch (RepresentationException e) {
			String msg = "cannot determine location in local repository";
			log.error(msg, e);
			throw new RepositoryPersistenceException(msg, e);
			
		} catch (RepositoryPersistenceException e) {
			String msg ="cannot download [" + expectedPartName + "] of [" + NameParser.buildName( (com.braintribe.model.artifact.Artifact) candidate) + "]";
			log.error(msg, e);
			throw new RepositoryPersistenceException(msg, e);
		}			
	}
	

	@Override
	public void processUpdateInformation(RavenhurstBundle bundle) throws RepositoryPersistenceException{
	}
	
	/**
	 * updates the maven metadata and persists the data 
	 * @param metadataFile - the {@link File} containing the metadata 
	 * @param bundle - the {@link RavenhurstBundle}
	 * @throws RepositoryPersistenceException -
	 */
	public MavenMetaData updateMavenMetaData( File metadataFile, RavenhurstBundle bundle) throws RepositoryPersistenceException {
		MavenMetaData metaData = null;
		try {
			RepositoryAccessClient repositoryAccessClient = accessClientFactory.apply( bundle);
			String source = RepositoryReflectionHelper.safelyCombineUrlPathParts(RepositoryReflectionHelper.getSolutionUrlLocation( bundle.getRepositoryUrl(), reflectingSolution), "maven-metadata.xml");					
			metaData = repositoryAccessClient.extractMavenMetaData( registrySupport, source, bundle.getRavenhurstRequest().getServer());
			
		} catch (RuntimeException e) {
			String msg ="not repository access client exists for bundle [" + bundle.getRepositoryUrl() + "]";
			log.error(msg, e);
			throw new RepositoryPersistenceException( msg, e);
		} catch (RepositoryAccessException e) {
			// something ugly happenend while accessing, but prob' no 404 
		} 
		//container.getMavenMetaDataToRepositoryRoleMap().put(metaData, RepositoryReflectionHelper.getRepositoryRoleOfBundle(bundle));
		if (metaData == null) { 
			// create maven meta data (might be 404) 
			metaData = MavenMetadataPersistenceExpert.generateMavenMetaData( (Identification) reflectingSolution);		
		}
		
		// persist
		MavenMetadataPersistenceExpert.encode(registrySupport, metaData, metadataFile);
		
		return metaData;
	}
	
	
	/**
	 * updates the part in the solution list that corresponds with the bundle. CAUTION : need to persist after all changes have happened,
	 * it actually accesses the remote repository to download the list of files.. 
	 * @param bundle - {@link RavenhurstBundle} to update its solutions 
	 * @throws RepositoryPersistenceException - 
	 */
	@Override
	public List<String> retrievePartListFromRemoteRepository( RavenhurstBundle bundle) throws RepositoryPersistenceException {
		try {
			RepositoryInterrogationClient repositoryInterrogationClient = interrogationClientFactory.apply( bundle);
			List<com.braintribe.model.ravenhurst.Part> parts = repositoryInterrogationClient.extractPartList(bundle, RepositoryReflectionHelper.ravenhurstArtifactFromSolution( reflectingSolution));
			File locationForBundle = SolutionPartReflectionContainerPersistenceExpert.getLocationForBundle(getLocation(), bundle.getRepositoryId());
			List<String> names = new ArrayList<>();
			if (parts != null && parts.size() > 0) {
				for (com.braintribe.model.ravenhurst.Part part : parts) {
					String name = part.getName();
					names.add( name);
				}
			}
			return storePartlist(bundle, locationForBundle, names);
			
		} catch (RuntimeException e) {
			String msg ="no repository interrogation client exists for bundle [" + bundle.getRepositoryUrl() + "]. Cannot store cache";
			log.error( msg,e);
			throw new RepositoryPersistenceException( msg, e);
		} catch (RepositoryInterrogationException e) {
			String msg ="cannot interrogate [" + bundle.getRepositoryUrl() + "] about [" + NameParser.buildName( reflectingSolution) + "]'s partlist. Cannot store cache";
			log.error( msg,e);
			throw new RepositoryPersistenceException( msg, e);
		} 
	}
	
	/**
	 * stores the list of file names to the respective part list file - used while incrementally updating the part list (as opposed to above)
	 * for "listing non lenient" repositories 
	 * @param bundle
	 * @param names
	 * @return
	 */
	public List<String> changePartListOfRemoteRepository( RavenhurstBundle bundle, List<String> names) {
		File locationForBundle = SolutionPartReflectionContainerPersistenceExpert.getLocationForBundle(getLocation(), bundle.getRepositoryId());	
		return storePartlist(bundle, locationForBundle, names);
	}

	/**
	 * store a part list while locking the file 
	 * @param bundle - the {@link RavenhurstBundle} of which the data has been retrieved
	 * @param locationForBundle - the {@link File} to write to 
	 * @param names - the {@link List} of the file names 
	 * @return - the names as written
	 */
	private List<String> storePartlist( RavenhurstBundle bundle, File locationForBundle, List<String> names) {
		Lock semaphore = registrySupport.getLockInstance( locationForBundle).writeLock();
		try {					
			// lock 
			semaphore.lock();
			// delete 
			SolutionPartReflectionContainerPersistenceExpert.encode(getLocation(), bundle.getRepositoryId(), names);
			return names;
		}
		finally {
			semaphore.unlock();
		}
	}
		
	@Override
	public List<PartTuple> listExistingPartTuples(Solution solution, RepositoryRole repositoryRole) {
		Set<String> collectedNames = new HashSet<String>();
		
		for (Entry<String, List<String>> entry : getBundleToPartlist().entrySet()) {
			RavenhurstBundle bundle;
			try {
				bundle = scope.getRavenhurstBundleByName( entry.getKey());
			} catch (RavenhurstException e) {
				continue;
			}
			
			if (!RepositoryReflectionHelper.repositoryRoleMatch(repositoryRole, bundle)) {
				continue;
			}
			
			// extract all names while filtering unwanted remote files 
			List<String> names = entry.getValue().stream()
				.filter( 
					p -> {
							if (
								p.endsWith(".md5") ||
								p.endsWith(".sha1") || 
								p.equalsIgnoreCase("maven-metadata.xml")
							   ) 
								return false;
							else 
								return true;
						})
				.collect( Collectors.toList());
			collectedNames.addAll(names);
		}
		// extract the part tuples 
		String prefix = solution.getArtifactId() + "-" + VersionProcessor.toString( solution.getVersion());		
		int len = prefix.length();
		List<PartTuple> result = collectedNames.stream().map( n -> extractPartTuple(len, n)).collect( Collectors.toList());
		return result;		
	}
	
	/**
	 * extract a {@link PartTuple} from the file name, given the solution's matching
	 * @param len
	 * @param name
	 * @return
	 */
	public static PartTuple extractPartTuple( int len, String name) {
		if (len > name.length()) {
			return null;
		}
		String remainder = name.substring(len);
		String classifier=null, extension;
		if (remainder.startsWith( "-")) {
			String[] split = remainder.substring(1).split("\\.");
			classifier = split[0];
			extension = split[1];
		}
		else if (remainder.startsWith( ".")){
			extension = remainder.substring(1);
		}
		else if (remainder.length() == 0){
			extension = "";			
		}	
		else {
			return null;
		}
		return PartTupleProcessor.fromString(classifier, extension);	
	}
	
	/**
	 * gets the correctly named file for a bundle's part list 
	 * @param location
	 * @param bundle
	 * @return
	 */
	public static File getFileForBundle( File location, RavenhurstBundle bundle) {
		return SolutionPartReflectionContainerPersistenceExpert.getLocationForBundle(location, bundle.getRepositoryId());
	}
	
	
}
