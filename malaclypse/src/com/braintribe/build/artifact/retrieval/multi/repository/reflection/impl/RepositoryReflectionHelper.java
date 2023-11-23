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
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.info.RepositoryOrigin;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.meta.Versioning;
import com.braintribe.model.artifact.processing.version.VersionMetricTuple;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

/**
 * helper functions for 
 * @author Pit
 *
 */
public class RepositoryReflectionHelper {
	private static Logger log = Logger.getLogger(RepositoryReflectionHelper.class);
	private static boolean doNotDelete = false; // true if in testmode for purging, false if armed

	/**
	 * extract an {@link Artifact} from a {@link Part}
	 * @param part - the {@link Part}
	 * @return - {@link Artifact}	
	 */
	public static Artifact artifactFromPart( com.braintribe.model.artifact.Part part){
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( part.getGroupId());
		artifact.setArtifactId( part.getArtifactId());		
		artifact.setVersion( VersionProcessor.toString( part.getVersion()));		
		return artifact;
	}
	
	/**
	 * extract an {@link Artifact} from a {@link Solution}
	 * @param solution - the {@link Solution}
	 * @return - {@link Artifact}
	 */
	public static Artifact ravenhurstArtifactFromSolution( com.braintribe.model.artifact.Solution solution){
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( solution.getGroupId());
		artifact.setArtifactId( solution.getArtifactId());		
		artifact.setVersion( VersionProcessor.toString( solution.getVersion()));		
		return artifact;
	}
	
	/**
	 * create an {@link Artifact} from an {@link Identification}, version is missing of course 
	 * @param identification - the {@link Identification}
	 * @return - the {@link Artifact} with groupid and artifactid transferred
	 */
	public static Artifact ravenhurstArtifactFromIdentification( com.braintribe.model.artifact.Identification identification){
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( identification.getGroupId());
		artifact.setArtifactId( identification.getArtifactId());
		return artifact;
	}
	/**
	 * extract an {@link Artifact} from a {@link Solution}
	 * @param artifact - the {@link Solution}
	 * @return - {@link Artifact}
	 */
	public static Artifact ravenhurstArtifactFromArtifact( com.braintribe.model.artifact.Artifact mArtifact){
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( mArtifact.getGroupId());
		artifact.setArtifactId( mArtifact.getArtifactId());		
		artifact.setVersion( VersionProcessor.toString( mArtifact.getVersion()));		
		return artifact;
	}
	
	
	/**
	 * create a {@link Part} from an {@link Artifact}
	 * @param artifact - the {@link Artifact}
	 * @return - {@link Part}
	 * @throws RavenhurstException -
	 */
	public static com.braintribe.model.artifact.Part partFromArtifact( Artifact artifact) throws RavenhurstException {
		com.braintribe.model.artifact.Part part = com.braintribe.model.artifact.Part.T.create();
		part.setGroupId( artifact.getGroupId());
		part.setArtifactId( artifact.getArtifactId());
		try {
			part.setVersion( VersionProcessor.createFromString( artifact.getVersion()));
		} catch (VersionProcessingException e) {
			throw new RavenhurstException(e);
		}
		return part;
	}
	
	/**
	 * extract the {@link Identification} (groupid & artifactid) from an {@link Artifact}
	 * @param artifact - the {@link Artifact}
	 * @return - {@link Identification} 
	 */
	public static com.braintribe.model.artifact.Identification identificationFromArtifact( Artifact artifact){
		Identification identification = Identification.T.create();
		identification.setGroupId( artifact.getGroupId());
		identification.setArtifactId( artifact.getArtifactId());
		return identification;
	}
	
	/**
	 * create a {@link Solution} from an {@link Artifact}
	 * @param artifact - {@link Artifact}
	 * @return - {@link Solution}
	 * @throws RavenhurstException -
	 */
	public static com.braintribe.model.artifact.Solution solutionFromArtifact( Artifact artifact) throws RavenhurstException {
		try {
			Solution solution = Solution.T.create();
			solution.setGroupId( artifact.getGroupId());
			solution.setArtifactId( artifact.getArtifactId());
			solution.setVersion( VersionProcessor.createFromString(artifact.getVersion()));
			return solution;
		} catch (VersionProcessingException e) {			
			throw new RavenhurstException(e);
		}
	}
		
	
	/**
	 * build an artifact from string 
	 * @param grp - group id
	 * @param art - artifact id 
	 * @param vrs - version 
	 * @return - {@link Artifact}
	 */
	public static Artifact toArtifact( String grp, String art, String vrs) {
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( grp);
		artifact.setArtifactId( art);
		artifact.setVersion( vrs);
		return artifact;
	}
	
	/**
	 * match two {@link Artifact}	
	 */
	public static boolean match(Artifact one, Artifact two) {
		if (				
				one.getGroupId().equalsIgnoreCase(two.getGroupId()) &&
				one.getArtifactId().equalsIgnoreCase( two.getArtifactId()) &&
				one.getVersion().equalsIgnoreCase(two.getVersion())
			) {
			return true;
		}
		return false;
	}
	
	// URL path utils
	/**
	 * safe path (with correct number of slashes) from an {@link Artifact}, i.e. identification disregarding the version!! 
	 * @param url - the prefix (repo url) 
	 * @param artifact - the {@link Artifact}
	 * @return - the url of the artifact 
	 */
	public static String getArtifactUrlLocation( String url, Artifact artifact) {
		return safelyCombineUrlPathParts(url,  artifact.getGroupId().replace('.', '/'), artifact.getArtifactId());
	}
	
	/**
	 * safe path (with correct number of slashes) from an {@link Identification}, i.e. identification disregarding the version!! 
	 * @param url - the prefix (repo url) 
	 * @param identification - the {@link Identification}
	 * @return - the path to the {@link Identification}'s location 
	 */
	public static String getArtifactUrlLocation( String url, Identification identification) {
		return safelyCombineUrlPathParts(url,  identification.getGroupId().replace('.', '/'), identification.getArtifactId());
	}
	
	/**
	 * safe path (with correct number of slashes) from an {@link Artifact} i.e. identification disregarding the version!!
	 * @param url - the prefix (repo url)
	 * @param part - the {@link Part}
	 * @return - the location of the part 
	 */
	public static String getArtifactUrlLocation( String url, Part part){
		return getArtifactUrlLocation(url, artifactFromPart(part));
	}
	
	/**
	 * safe path (with correct number of slashes) from an {@link Artifact}, i.e. solution WITH VERSION
	 * @param url - the prefix 
	 * @param artifact - the {@link Artifact}
	 * @return - the path to the location of the {@link Artifact}
	 */
	public static String getSolutionUrlLocation( String url, Artifact artifact) {
		return safelyCombineUrlPathParts(getArtifactUrlLocation(url, artifact),artifact.getVersion());
	}
	
	/**
	 * the the url location of a solution 
	 * @param url - the url (prefix)
	 * @param artifact - the {@link Solution}
	 * @return - the url of the solution 
	 */
	public static String getSolutionUrlLocation( String url, Solution artifact){		
		return safelyCombineUrlPathParts(getArtifactUrlLocation(url, artifact), VersionProcessor.toString( artifact.getVersion()));		
	}
	/**
	 * get the url location of the solution derived from a part of the solution 
	 * @param url - the url (prefix)
	 * @param part - the {@link Part}
	 * @return - the url of the solution of which the part is part of
	 */
	public static String getSolutionUrlLocation( String url, Part part){		
		return safelyCombineUrlPathParts(getArtifactUrlLocation(url, part), VersionProcessor.toString( part.getVersion()));						
	}	
	
	// filesystem path utils 
	/**
	 * get the file system location of the artifact (with correct path separator char)
	 * @param url - the file system prefix 
	 * @param artifact - the {@link Artifact} in question  
	 * @return - the file system path
	 */
	public static String getArtifactFilesystemLocation( String url, Artifact artifact) {
		return safelyCombineFilesystemPathParts(url,  artifact.getGroupId().replace('.', File.separatorChar), artifact.getArtifactId());
	}

	/**
	 * get the file system location of the {@link Identification}
	 * @param url - the prefix 
	 * @param identification - the {@link Identification}
	 * @return - the file system path
	 */
	public static String getArtifactFilesystemLocation( String url, Identification identification) {
		return safelyCombineFilesystemPathParts(url,  identification.getGroupId().replace('.', File.separatorChar), identification.getArtifactId());
	}
	public static String getArtifactFilesystemLocation( String url, Part part){
		return getArtifactFilesystemLocation(url, artifactFromPart(part));
	}
	
	public static String getSolutionFilesystemLocation( String url, Artifact artifact) {
		return safelyCombineFilesystemPathParts(getArtifactUrlLocation(url, artifact),artifact.getVersion());
	}
	
	public static String getSolutionFilesystemLocation( String url, Part part) {	
		return safelyCombineFilesystemPathParts(getArtifactFilesystemLocation(url, part), VersionProcessor.toString( part.getVersion()));					
	}	
	
	public static String getHotfixSavySolutionFilesystemLocation( String url, Part part) throws RavenhurstException {
		try {
			Version version = part.getVersion();
			VersionMetricTuple metricTuple = VersionProcessor.getVersionMetric(version);
			String versionExpression;
			if (metricTuple.revision == null) {
				versionExpression = VersionProcessor.toString( version);
			} else {
				versionExpression = String.format( "%d.%d", metricTuple.major, metricTuple.minor);
			}			
			// count dots?
			return safelyCombineFilesystemPathParts(getArtifactFilesystemLocation(url, part), versionExpression);				
		} catch (VersionProcessingException e) {
			throw new RavenhurstException(e);
		}
	}
	
	/**
	 * retrieves all {@link RavenhurstBundle} that have the same url
	 * @param bundles - the {@link Collection} of {@link RavenhurstBundle} to traverse
	 * @param url - the url to scan for 
	 * @return - a {@link Set} of matching {@link RavenhurstBundle}
	 */ 
	public static Set<RavenhurstBundle> matchBundlesToUrl( Collection<RavenhurstBundle> bundles, String url) {
		Set<RavenhurstBundle> result = new HashSet<RavenhurstBundle>();
		for (RavenhurstBundle bundle : bundles) {
			if (bundle.getRepositoryUrl().equalsIgnoreCase(url)) {
				result.add(bundle);
			}
		}
		return result;
	}
	
	/**
	 * combine path tokens using a slash as delimiter 
	 * @param prefix - the first entry 
	 * @param parts - the parts to add 
	 * @return  - the combined parts 
	 */
	public static String safelyCombineUrlPathParts( String prefix, String ... parts) {
		return safelyCombinePathParts(prefix, "/", parts);
	}
	/**
	 * combine path tokens using the {#File.separator} as delimiter 
	 * @param prefix - the first entry 
	 * @param parts - the parts to add 
	 * @return -- the combined parts 
	 */
	public static String safelyCombineFilesystemPathParts( String prefix, String ... parts) {
		return safelyCombinePathParts(prefix, File.separator, parts);
	}
	
	/**
	 * combine path tokens using the supplied separator, making sure only 1 separator is used 
	 * @param prefix - the {@link String} prefix
	 * @param separator - the separator to use 
	 * @param parts - the parts to add 
	 * @return - the combined parts 
	 */
	public static String safelyCombinePathParts( String prefix, String separator, String ... parts) {
		StringBuilder builder = new StringBuilder( prefix);
		for (String part : parts) {
			if (!builder.toString().endsWith( separator)) {
				builder.append( separator);
			}
			builder.append( part);			
		}
		return builder.toString();		
	}
	
	
	
	public static List<Version> getVersionListFromDirectory( File directory) throws RavenhurstException{
		
		File [] dirs = directory.listFiles();		
		List<Version> versions = new ArrayList<Version>();
		if (dirs == null || dirs.length == 0) 
			return versions;
		for (File file : dirs) {
			File [] contents  = file.listFiles();
			if ((contents == null) || (contents.length == 0))
				continue;
			try {
				versions.add( VersionProcessor.createFromString(file.getName()));
			} catch (VersionProcessingException e) {
				throw new RavenhurstException(e);
			}
		}
		// sort
		Collections.sort( versions, new Comparator<Version>() {
			@Override
			public int compare( Version one, Version two) {
				if (VersionProcessor.isLess( one, two))
					return -1;
				if (VersionProcessor.isHigher( one, two))
					return 1;
				return 0;
			}
		});
		return versions;
	}
	
	public static List<String> getVersionsFromDirectory( File directory) throws RavenhurstException{
		List<Version> versions = getVersionListFromDirectory(directory);
		List<String> result = new ArrayList<String>(versions.size());
		for (Version version : versions){
			result.add( VersionProcessor.toString(version));
		}		
		return result;
	}


	/**
	 * return the file system location of an artifact
	 * @param localRepository - the {@link File} that points to the local repository 
	 * @param identification - the {@link Identification} of the artifact
	 * @return - a {@link File} pointing to the location 
	 */
	public static File getFilesystemLocation( File localRepository, Identification identification) {
		return new File( localRepository, identification.getGroupId().replace('.', File.separatorChar) + File.separator + identification.getArtifactId());
	}
	
	/**
	 * return the file system location of a solution 
	 * @param localRepository - the {@link File} that points to the local repository 
	 * @param artifact - the {@link Artifact} that points to the solution
	 * @return - a {@link File} pointing to the location.
	 */
	public static File getFilesystemLocation( File localRepository, com.braintribe.model.artifact.Artifact artifact) {		
		return new File( localRepository, artifact.getGroupId().replace('.', File.separatorChar) + File.separator + artifact.getArtifactId() + File.separator + VersionProcessor.toString( artifact.getVersion()));		
	}
	
	/**
	 * determines the relevant update interval of a repository 
	 * @param bundle - the {@link RavenhurstBundle} associated with the repository
	 * @param metaData - the {@link MavenMetaData} associated with the repository
	 * @return - true if update is required 
	 */
	public static boolean requiresUpdate( RavenhurstBundle bundle, MavenMetaData metaData) {
		// dynamic - updated outside the scope of the registry 
		if (bundle.getDynamicRepository())
			return false;
		
		int releaseInterval = bundle.getUpdateIntervalForRelease();
		int snapshotInterval = bundle.getUpdateIntervalForSnapshot();
		
		// if both are never, no update 
		if (releaseInterval < 0 && snapshotInterval < 0){
			return false;
		}
		int updateInterval;
		// release is not never, and smaller as snapshot, yet snapshot's not never  
		if (releaseInterval >= 0 && (releaseInterval < snapshotInterval || snapshotInterval < 0)) {
			updateInterval = releaseInterval;
		}
		else  {
			updateInterval = snapshotInterval;
		}
		// no versioning -> update (it will get a versioning/lastUpdated through that
		Versioning versioning = metaData.getVersioning();
		if (versioning == null) {
			return true;
		}
		Date lastAccess = versioning.getLastUpdated();
		// no last access -> update (it will get a lastUpdated through that
		if (lastAccess == null) {
			log.warn("metadata received from [" + bundle.getRepositoryId() + "] of artifact [" + metaData.getGroupId() + ":" + metaData.getArtifactId() + "#" + metaData.getVersion() + "] contains not lastupdated timestamp. Update forced");
			return true;
		}
		if (new Date().getTime() > (lastAccess.getTime() + updateInterval)) {
			return true;
		}
		return false;
		
	}
	
	public static RepositoryRole getRepositoryRoleOfBundle( RavenhurstBundle bundle){
		if (bundle.getRelevantForRelease()) {
			if (bundle.getRelevantForSnapshot()) {
				return RepositoryRole.both;
			}
			else {
				return RepositoryRole.release;
			}
		}
		else if (bundle.getRelevantForSnapshot()){
			return RepositoryRole.snapshot;
		}
		else {
			return RepositoryRole.none;
		}
	}
	
	/**
	 * purges all files with a matching file name recursivly 
	 * @param directory
	 * @param matchingNames
	 */
	private static void purgeMetadata(LockFactory lockFactory, File directory, Set<String> matchingNames) {
		File [] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				purgeMetadata( lockFactory, file, matchingNames);
			}
			if (matchingNames.contains( file.getName())) {
				if (!doNotDelete) {
					lockedFileDelete(lockFactory, file);
				} 
				else {
					System.out.println("file [" + file.getAbsolutePath() + "] is to be purged");
				}
			}
		}
	}

	/**
	 * purge all mavenmetadata 
	 * @param localRepository
	 * @param bundles
	 */
	public static void purgeMetadata( LockFactory lockFactory, File localRepository, List<RavenhurstBundle> bundles) {
		if (!localRepository.exists()) {
			return;
		}
		if (bundles == null || bundles.size() == 0) {
			return;
		}
		
		Set<String> suspectNames = new HashSet<String>();
		for (RavenhurstBundle bundle : bundles) {
			String name = bundle.getRepositoryId();
			
			String mdName = "maven-metadata-" + name + ".xml";
			suspectNames.add(mdName);
			
			String partlistName = name + ".solution";
			suspectNames.add(partlistName);
		}
		purgeMetadata(lockFactory, localRepository, suspectNames);
		
	}
	
	public static void purgeMetadataByName( LockFactory lockFactory, File localRepository, List<String> names) {
		if (!localRepository.exists()) {
			return;
		}
		if (names == null || names.size() == 0) {
			return;
		}
		
		Set<String> suspectNames = new HashSet<String>();
		for (String name : names) {
			
			String mdName = "maven-metadata-" + name + ".xml";
			suspectNames.add(mdName);
			
			String partlistName = name + ".solution";
			suspectNames.add(partlistName);
		}
		purgeMetadata(lockFactory, localRepository, suspectNames);		
	}
	
	public static void purgeIndex( LockFactory lockFactory, File localRepository, List<RavenhurstBundle> bundles) {
		if (!localRepository.exists()) {
			return;
		}
		if (bundles == null || bundles.size() == 0) {
			return;
		}		
		purgeIndexByName( lockFactory, localRepository, bundles.stream().map( b -> b.getRepositoryId()).collect(Collectors.toList()));
	}
	
	public static void purgeIndexByName( LockFactory lockFactory, File localRepository, List<String> names) {
		File updateInfoRoot = new File( localRepository, "updateinfo");
		
		for (String name : names) {
			File repoUpdate = new File( updateInfoRoot, name);
			File indexFile = new File( repoUpdate, ".index");
			if (indexFile.exists()) {
				if (!doNotDelete) {
					lockedFileDelete(lockFactory, indexFile);
				} 
				else { 
					System.out.println("file [" + indexFile.getAbsolutePath() + "] is to be purged");
				}
			}
		}
	}
	
	
	
	
	public static boolean repositoryRoleMatch(RepositoryRole role, RavenhurstBundle bundle) {
		switch ( role) {
			case both: // no matter 
				break;
			case none: // none at all, return empty list 
				return false;
			case snapshot:
				if (!bundle.getRelevantForSnapshot()) {
					return false;
				}
				break;
			case release:
				if (!bundle.getRelevantForRelease()) {
					return false;
				}
				break;
		}
		return true;
	}
	
	/**
	 * delete a file, and make sure only we have access by using the lock
	 * @param file - the file to delete
	 */
	public static void lockedFileDelete(LockFactory lockFactory, File file) {
		if (file != null && file.exists()) {
			// acquire lock 
			Lock semaphore = lockFactory.getLockInstance(file).writeLock();
			try {					
				// lock 
				semaphore.lock();
				// delete 
				file.delete();
			}
			finally {
				semaphore.unlock();
			}
		}
	}
	
	/**
	 * use the information stored in the bundle to build info about the repo
	 * @param b - the {@link RavenhurstBundle}
	 * @return - a {@link RepositoryOrigin}
	 */
	public static RepositoryOrigin generateRepositoryOrigin(RavenhurstBundle b) {
		RepositoryOrigin origin = RepositoryOrigin.T.create();
		origin.setUrl( b.getRepositoryUrl());
		origin.setName( b.getRepositoryId());
		return origin;
	}
		
}
