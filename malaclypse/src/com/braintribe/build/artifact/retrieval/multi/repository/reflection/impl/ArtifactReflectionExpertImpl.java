// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.coding.VersionWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClient;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.repository.filtering.ArtifactFilterExpertSupplier;
import com.braintribe.build.artifact.retrieval.multi.repository.filtering.IdentificationConversion;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.ArtifactReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflectionSupport;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBias;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.MavenMetadataPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClient;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessException;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.info.RepositoryOrigin;
import com.braintribe.model.artifact.info.VersionInfo;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.meta.Versioning;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;


/**
 * the expert that handles everything concerning the repositories on an (unversioned) artifact level
 * @author pit
 *
 */
public class ArtifactReflectionExpertImpl extends AbstractReflectionExpert implements ArtifactReflectionExpert {
	private static Logger log = Logger.getLogger(ArtifactReflectionExpertImpl.class);
	
	private RepositoryReflectionSupport registrySupport;
	private boolean handleUntrustworthy = true; // emergency flag to switch off trustworthy/non-trustworthy handling
	private Identification reflectingIdentification;
	private RavenhurstScope scope;
	private Map<RavenhurstBundle, MavenMetaData> bundleToMavenmetadata;
	private MavenMetaData localMavenMetaData;
	private ArtifactFilterExpertSupplier artifactFilterExpertSupplier;

	public ArtifactReflectionExpertImpl(Identification identification) {
		this.reflectingIdentification = identification;
		
	}
	@Configurable @Required
	public void setRegistrySupport(RepositoryReflectionSupport downloader) {
		this.registrySupport = downloader;
	}	
	
	@Configurable @Required
	public void setScope(RavenhurstScope scope) {
		this.scope = scope;
	}
	@Configurable @Required
	public void setArtifactFilterExpertSupplier(ArtifactFilterExpertSupplier artifactFilterExpertSupplier) {
		this.artifactFilterExpertSupplier = artifactFilterExpertSupplier;
	}
	
	private Object bundleToMavenMetadataMonitor = new Object();
	
	private void setup() {
		
		if (bundleToMavenmetadata != null)
			return;
		
		synchronized ( bundleToMavenMetadataMonitor) {
			if (bundleToMavenmetadata != null)
				return;
						
			Map<RavenhurstBundle, MavenMetaData> bundleToMavenmetadata = new HashMap<>();
			
			File location = RepositoryReflectionHelper.getFilesystemLocation( new File(localRepositoryLocationProvider.getLocalRepository(null)), reflectingIdentification);
	
			List<RavenhurstBundle> bundles;
			try {
				bundles = scope.getRavenhurstBundles();
			} catch (RavenhurstException e) {
				throw Exceptions.contextualize(new IllegalStateException(), "cannot retrieve currently active repositories from system");
				
			}
		
			Iterator<RavenhurstBundle> iterator = bundles.iterator();
			while (iterator.hasNext()) {			
				RavenhurstBundle bundle = iterator.next();
				if (registrySupport.groupExistsInBundle(bundle, reflectingIdentification.getGroupId())) { 
					MavenMetaData mavenMetaData = updateRepositoryInformationOfArtifact(location, bundle, false);
					if (mavenMetaData != null) {
						bundleToMavenmetadata.put( bundle, mavenMetaData);
					}
				}
				else {
					log.debug( "index of bundle [" + bundle.getRepositoryId() + "] doesn't contain group [" + reflectingIdentification.getGroupId() + "], skipped");
					// should we drop the metadata file? i.e. overwrite with empty? 
					MavenMetaData metaData = MavenMetadataPersistenceExpert.generateMavenMetaData( reflectingIdentification, Collections.emptyList()); 					
					metaData.setMcComment( "injected after [" + reflectingIdentification.getGroupId() + "] not in index of [" + bundle.getRepositoryUrl() + "]");
					bundleToMavenmetadata.put(bundle, metaData);
					File metadataFile = MavenMetadataPersistenceExpert.getFileForBundle( location, bundle);
					MavenMetadataPersistenceExpert.encode(registrySupport, metaData, metadataFile);
				}
			}
			
		
			//bundles.parallelStream().forEach( b -> updateRepositoryInformationOfArtifact(location, b, false));
			// add local metadata 
			
			File localMetadataFile = new File(location, "maven-metadata-local.xml");
			if (localMetadataFile.exists()) {
				try {
					localMavenMetaData = MavenMetadataPersistenceExpert.decode( registrySupport, localMetadataFile);
				} catch (RepositoryPersistenceException e) {
					log.warn("cannot read local maven meta data [" + localMetadataFile.getAbsolutePath() + "]");
				}
			}			
			this.bundleToMavenmetadata = bundleToMavenmetadata;
		}
	}
	public MavenMetaData updateRepositoryInformationOfArtifact(File location, RavenhurstBundle bundle, boolean reset) {
		try {
			File metadataFile = MavenMetadataPersistenceExpert.getFileForBundle( location, bundle);
			boolean requiresUpdate = false;
			MavenMetaData metaData = null;
			// if no reset is requested, standard logic is used to determine whether the metadata should be updated,
			// otherwise it will be.. 
			boolean exists = true;
			if (!reset) {
				if (!metadataFile.exists()) {
					requiresUpdate = true;
					exists = false;
				}
				else {
					metaData = MavenMetadataPersistenceExpert.decode(registrySupport, metadataFile);				
					requiresUpdate = RepositoryReflectionHelper.requiresUpdate(bundle, metaData);			
				}
			}
			else {
				requiresUpdate = true;
			}
			
			if (requiresUpdate) {
				if (bundle.getInaccessible()) {
					if (exists) {
						log.warn("maven metadata ["+ metadataFile.getAbsolutePath() + "] of bundle [" + bundle.getRepositoryId() + "] of profile [" + bundle.getProfileId() + "] requires updating, yet the the repository [" + bundle.getRepositoryUrl() + "] is inaccessible");
					}
					else {
						log.warn("maven metadata ["+ metadataFile.getAbsolutePath() + "] of bundle [" + bundle.getRepositoryId() + "] of profile [" + bundle.getProfileId() + "] has been purged and therefore requires updating, yet the the repository [" + bundle.getRepositoryUrl() + "] is inaccessible");
					}
				}
				else {						
					resetSolutionFiles( location);																	
					metaData = updateMavenMetaData( metadataFile, bundle);
				}
			}
			return metaData;		
		} catch (Exception e) {
			// 
			if (!registrySupport.getPreemptiveConnectionTestMode()) {
				// 
				log.warn("repository [" + bundle.getRepositoryId() + "] cannot be reached via [" + bundle.getRepositoryUrl() + "] and will be ignored in the currently active scope");
				bundle.setInaccessible(true);
			}
		}
		return null;
	}

	/**
	 * delete all *.solution files in the versioned directories of an unversioned directory,
	 * as prompted by a metadata update.
	 * @param unversionedDirectory
	 */
	private void resetSolutionFiles(File unversionedDirectory) {
		// find all versioned directories (aka sub directories)
		File [] versionedDirectories = unversionedDirectory.listFiles( new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				if (!file.isDirectory())
					return false;
				else
					return true;
			}
		});
		// find all solution files in there 
		for (File versionDirectory : versionedDirectories) {
			File [] files = versionDirectory.listFiles( new FileFilter() {
				
				@Override
				public boolean accept(File file) {
					if (file.getName().endsWith( ".solution"))
						return true;
					return false;
				}
			});
			// delete the files 
			for (File file : files) {
				RepositoryReflectionHelper.lockedFileDelete(registrySupport, file);
			}
		}				
	}
	
	@Override
	public List<Version> getVersions(RepositoryRole repositoryRole) throws RepositoryPersistenceException {	
		return getVersions( repositoryRole, null);
	}
	
	//private Object versionsMonitor = new Object();
	
	@Override
	public synchronized List<Version> getVersions(RepositoryRole role, VersionRange requestedRange)  throws RepositoryPersistenceException {
		setup();	
		return retrieveVersionList(role, requestedRange);
	}
	private List<Version> retrieveVersionList(RepositoryRole role, VersionRange requestedRange) {
		ArtifactIdentification ai = IdentificationConversion.toArtifactIdentification(reflectingIdentification);

		ArtifactBias bias = registrySupport.getArtifactBias(reflectingIdentification);
		
		Set<Version> versionSet = CodingSet.createHashSetBased( new VersionWrapperCodec());
		List<Version> result = new ArrayList<Version>();
		// get local versions first - not all artifacts exist remotely	
		boolean foundLocal = false;
		if (localMavenMetaData != null) {
			ArtifactFilterExpert afe = artifactFilterExpertSupplier.artifactFilter("local");
			if (afe.matches(ai)) {							
				if (!filterOut(bias, "local")) { // may be filtered 
					Versioning localVersioning = localMavenMetaData.getVersioning();
					if (localVersioning != null) {
						foundLocal = true;						
						List<Version> localVersions = localVersioning.getVersions();
						// 
						List<Version> filteredLocalVersions = new ArrayList<>();
						for (Version version : localVersions) {
							if (afe.matches( IdentificationConversion.toCompiledArtifactIdentification(reflectingIdentification, version)))
								filteredLocalVersions.add(version);
						}
						versionSet.addAll( filteredLocalVersions);
						// if some local versions have been found, check whether they are biased and if so, return these
						if (filteredLocalVersions.size() > 0) {
							// bias to local set, i.e. only use that one 
							if (bias!= null && bias.hasLocalBias()) {
								localVersions.sort( VersionProcessor.comparator);						
								return new ArrayList<Version>(filteredLocalVersions);
							}
						}
					}
				}
			}
		}	
		
		
		// retrieve the information about the versions of this artifact 
		for (Entry<RavenhurstBundle, MavenMetaData> entry : bundleToMavenmetadata.entrySet()) {
			MavenMetaData metaData = entry.getValue();
			RavenhurstBundle bundle = entry.getKey();
			
			// filter the current repository 
			String repositoryId = bundle.getRepositoryId();
		
			// Artifact filter expert filtering - filtering on groupId & artifactId
			ArtifactFilterExpert afe = artifactFilterExpertSupplier.artifactFilter(repositoryId);
			if (!afe.matches(ai)) {
				continue;
			}
		
				
			if (!RepositoryReflectionHelper.repositoryRoleMatch(role, bundle)) {
				continue;
			}
			if (filterOut(bias, repositoryId)) {
				continue;
			}
			
			Set<Version> remoteVersionSet = CodingSet.createHashSetBased( new VersionWrapperCodec());
			
			if (metaData != null) {
				Versioning versioning = metaData.getVersioning();
				if (versioning != null) {
					for (Version version : versioning.getVersions()) {					
						if (remoteVersionSet.contains(version)) {
							continue;
						}
						else {						
							remoteVersionSet.add(version);					
						}
					}
				}
			}
			else {
				log.warn("no maven metadata available for [" + bundle + "]");
			}
			
			// 
			// UNHOLY maven quirk : even if metadata say that there's no such version, we still add it to the list of version,
			// but only if
			// - the requested range is not an interval 
			// - the found versions of a bundle don't contain the requested version 
			// - no local version has been found - that would have preference anyway 
			// - the repository is *not* trustworthy 
			// ADDED IT ONLY UNDER PROTEST!
			//
			if (handleUntrustworthy && requestedRange != null && !requestedRange.getInterval()) {
				Version version = VersionProcessor.createFromString( VersionRangeProcessor.toString(requestedRange));
				if (	!remoteVersionSet.contains( version) && 
						!foundLocal  						
					) {				
					if (!bundle.getTrustworthyRepository()) {
						remoteVersionSet.add( version);
					}
				}
			}
			// artifact filter filtering - groupId, artifactId, version
			Set<Version> filteredRemoteVersionSet = CodingSet.createHashSetBased( new VersionWrapperCodec());
			for (Version version : remoteVersionSet) {
				if (afe.matches( IdentificationConversion.toCompiledArtifactIdentification(reflectingIdentification, version)))
					filteredRemoteVersionSet.add(version);
			}
			versionSet.addAll( filteredRemoteVersionSet);
			
			
			// 			
			
		} // for repository
		result.addAll(versionSet);
		
		
		// sort so that the lower versions come first (ascending sort) 
		result.sort( VersionProcessor.comparator);
		
		 
		
		
		return result;
	}
	
	private boolean filterOut(ArtifactBias bias, String repositoryId) {
		if (bias == null)
			return false;
		// negative bias active - filter using the negative list
		if (bias.hasNegativeBias() && bias.hasNegativeBias(repositoryId)) {
			return true;
		}
		// positive bias active - filter using the positive list 
		if (bias.hasPositiveBias() && !bias.hasPositiveBias(repositoryId)) {
			return true;
		}	
		return false;
	}	

	@Override
	public void processUpdateInformation(RavenhurstBundle bundle)  throws RepositoryPersistenceException { 			
	}
	
	/**
	 * update the maven meta data that is associated with the bundle passed
	 * @param bundle - the {@link RavenhurstBundle} 
	 * @throws RepositoryPersistenceException -
	 */
	public MavenMetaData updateMavenMetaData( File metadataFile, RavenhurstBundle bundle) throws RepositoryPersistenceException {
		MavenMetaData metaData = null;
		try {
			RepositoryAccessClient repositoryAccessClient = accessClientFactory.apply( bundle);
			String source = RepositoryReflectionHelper.safelyCombineUrlPathParts(RepositoryReflectionHelper.getArtifactUrlLocation( bundle.getRepositoryUrl(), reflectingIdentification), "maven-metadata.xml");					
			metaData = repositoryAccessClient.extractMavenMetaData(registrySupport, source, bundle.getRavenhurstRequest().getServer());
			// if no metadata were found 
			if (metaData == null ) {
				if (!bundle.getTrustworthyRepository()) {
					// if the repository isn't trust worthy, try to enumeration 
					metaData = extractMavenMetaDataByEnumeration(bundle, metaData); // TODO : mark as enumeration result
					metaData.setMcComment( "enumerated from [" + bundle.getRepositoryUrl() + "]");
				}
				else {
					// create empty maven metadata
					metaData = MavenMetadataPersistenceExpert.generateMavenMetaData( reflectingIdentification, Collections.emptyList()); // TODO : mark as 404 result
					metaData.setMcComment( "injected after 404 from [" + bundle.getRepositoryUrl() + "]");
				}
			}
			else {
				metaData.setMcComment( "retrieved from [" + bundle.getRepositoryUrl() + "]");
			}
		} catch (RuntimeException e) {
			String msg ="no repository access client exists for bundle [" + bundle.getRepositoryUrl() + "]";
			throw new RepositoryPersistenceException( msg, e);
		} catch (RepositoryAccessException e) {
			String msg ="cannot access [" + bundle.getRepositoryUrl() + "] to retrieve maven-metadata.xml ";
			throw new RepositoryPersistenceException( msg, e);
		}
		// any metadata retrieved or generated
		if (metaData != null) {
		
			// update the last accessed tag of the maven-metadata 
			Versioning versioning = metaData.getVersioning();
			if (versioning == null) {
				versioning = Versioning.T.create();
				metaData.setVersioning(versioning);
			}
			versioning.setLastUpdated( new Date());
			// persist
			MavenMetadataPersistenceExpert.encode(registrySupport, metaData, metadataFile);
		}		
		return metaData;
	}
	/**
	 * accesses the repository and uses http get (depending on the type of repo) to retrieve information about the 
	 * versions on the repository- 
	 * @param bundle - the {@link RavenhurstBundle} that contains the data for the repository 
	 * @param metaData - the {@link MavenMetaData} (will be overridden)
	 * @return - the extracted {@link MavenMetaData} - faked, i.e. produced from version list
	 * @throws RepositoryPersistenceException
	 */
	private MavenMetaData extractMavenMetaDataByEnumeration(RavenhurstBundle bundle, MavenMetaData metaData) throws RepositoryPersistenceException {
		/*
		if (!bundle.getListingLenient()) {
			String msg ="no maven metadata found for [" + NameParser.buildName( reflectingIdentification) + "] within bundle [" + bundle.getRepositoryUrl() + "], may not enumerate as non listing-lenient";
			log.warn(msg);
			return metaData;
		}
		*/
		try {
			RepositoryInterrogationClient repositoryInterrogationClient = interrogationClientFactory.apply( bundle);
			List<String> extractVersionList = repositoryInterrogationClient.extractVersionList(bundle, reflectingIdentification);
			metaData = MavenMetadataPersistenceExpert.generateMavenMetaData( reflectingIdentification, extractVersionList);		
		} catch (RuntimeException e1) {
			String msg ="no repository interrogation client exists for bundle [" + bundle.getRepositoryUrl() + "]";
			throw new RepositoryPersistenceException( msg, e1);
		} catch (RepositoryInterrogationException e1) {
			String msg ="interrogation of bundle [" + bundle.getRepositoryUrl() + "] failed";
			throw new RepositoryPersistenceException( msg, e1);
		}
		return metaData;
	}
	
	

	/**
	 * checks whether the version passed exists in the list of versions of the bundle in question
	 * @param bundle - the {@link RavenhurstBundle} that identifies the repository 
	 * @param version - the {@link Version} to check 
	 * @return - true if the version is listed, false otherwise
	 */
	public boolean versionExistsInBundle( RavenhurstBundle bundleX, Version version) {
		setup();
		if (bundleToMavenmetadata == null) {
			return false;
		}

		// hm.. after setup, the bundles may be changed somehow?? need to match this bundle to the instance from setup.. CHECK THAT
		RavenhurstBundle bundle = null;
		for (RavenhurstBundle b : bundleToMavenmetadata.keySet()) {
			if (b.getRepositoryUrl().equalsIgnoreCase( bundleX.getRepositoryUrl())) {
				bundle = b;
				break;
			}
		}
		if (bundle == null) {
			log.debug("no matching internal bundle found for [" + bundleX.getRepositoryUrl() + "] while resolving [" + NameParser.buildName( reflectingIdentification) + "], no metadata can be present");
			return false;
		}
		// if bundle is inaccessible, we do not have any metadata, so the version can't exist as well
		if (bundle.getInaccessible()) {
			log.debug("bundle [" + bundle.getRepositoryUrl() + "] is inaccessible, is not queried for version [" + VersionProcessor.toString(version) + "]");
			return false;
		}
		MavenMetaData mavenMetaData = bundleToMavenmetadata.get(bundle);
		// if no information is available, the version can't exist either
		if (mavenMetaData == null) {
			log.warn("no stored maven metadata on [" + NameParser.buildName(reflectingIdentification) + "] found for bundle [" + bundle.getRepositoryUrl() + "]");
			return false;
		}
		Versioning versioning = mavenMetaData.getVersioning();
		if (versioning == null)
			return false;
		
		for (Version suspect : versioning.getVersions()) {
			if (VersionProcessor.hardMatches(version, suspect)) {
				return true;
			}
		}		
		return false;
	}
	
	@Override
	public Map<Version,VersionInfo> getVersionData(RepositoryRole role) {
		setup();
		List<RavenhurstBundle> bundles;
		try {
			bundles = scope.getRavenhurstBundles();
		} catch (RavenhurstException e) {
			throw Exceptions.contextualize(new IllegalStateException(), "cannot retrieve currently active repositories from system");			
		}	
		Map<Version, VersionInfo> result = new HashMap<>();		
		List<Version> versions = getVersions(role);
		versions.stream().forEach( v -> {
			result.put( v, getVersionOrigin(v, bundles));
		});
		return result;
	}
	
	
	@Override
	public VersionInfo getVersionOrigin(Version version) {
		return getVersionOrigin(version, null);
	}
	
	@Override
	public VersionInfo getVersionOrigin(Version version, List<RavenhurstBundle> bundles) {
		setup();
		List<RavenhurstBundle> bs = bundles;
		if (bs == null) {
			try {
				bs = scope.getRavenhurstBundles();
			} catch (RavenhurstException e) {
				throw Exceptions.contextualize(new IllegalStateException(), "cannot retrieve currently active repositories from system");			
			}		
		}
		VersionInfo info = VersionInfo.T.create();
		info.setVersion( VersionProcessor.toString(version));
		boolean isRemote = false;
		for (RavenhurstBundle b : bs) {
			if (versionExistsInBundle(b, version)) {
				info.getRepositoryOrigins().add(RepositoryReflectionHelper.generateRepositoryOrigin( b));
				isRemote = true;
			}			
		}
		if (!isRemote) {
			RepositoryOrigin rO = RepositoryOrigin.T.create();
			rO.setName( "local");
			try {
				URL url = new File(localRepositoryLocationProvider.getLocalRepository(null)).toURI().toURL(); 
				rO.setUrl( url.toExternalForm());
			} catch (Exception e) {
				rO.setUrl( "<unknown>");						
			}
			info.getRepositoryOrigins().add( rO);
		}
				
		return info;
	}
	
	
}
