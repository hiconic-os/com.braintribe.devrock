// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.metadata;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.codec.CodecException;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.meta.Snapshot;
import com.braintribe.model.artifact.meta.Versioning;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;


/**
 * a processor for the different functions that can work on a maven meta data 
 * 
 * for now : only adding a solution to a group meta data or creating a solution's meta data  
 * @author pit
 *
 */
public class MavenMetaDataProcessor {
	
	private static SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd.HHmmss");

	/**
	 * add a solution to a group meta data
	 * @param metaData - may be null, will be created 
	 * @param solution - the solution to add 
	 * @return - the {@link MavenMetaData}
	 * @throws CodecException - arrgh
	 */
	public static MavenMetaData addSolution( MavenMetaData metaData, Solution solution) throws CodecException {
		return addSolution(metaData, solution, null);
	}
	
	/**
	 * add a solution to a group meta data 
	 * @param metaData - may be null, will be created 
	 * @param solution - the {@link Solution} to add 
	 * @param instanceProvider - a provider to provide the instances (or null to use GMF)
	 * @return - the {@link MavenMetaData}
	 * @throws CodecException - arrgh
	 */
	public static MavenMetaData addSolution( MavenMetaData metaData, Solution solution, Function<EntityType<?>, GenericEntity> instanceProvider) throws CodecException {
		if (metaData == null) {			
			metaData = create( MavenMetaData.T, instanceProvider);					
			metaData.setGroupId( solution.getGroupId());
			metaData.setArtifactId( solution.getArtifactId());
		}
		
		Versioning versioning = metaData.getVersioning();
	
		if (versioning == null) {			
			versioning = create( Versioning.T, instanceProvider);		
			metaData.setVersioning(versioning);
		}
		
		versioning.setLastUpdated( new Date());
		
		// read versions, inject new version, sort them
		List<Version> versions = versioning.getVersions();
		if (versions.size() == 0) {
			versions.add( solution.getVersion());
			versioning.setLatest( solution.getVersion());
			versioning.setRelease( solution.getVersion());
		}
		else {
			Set<Version> versionSet =  CodingSet.createHashSetBased( new VersionCodingCodec());
			versionSet.addAll(versions);
			versionSet.add( solution.getVersion());
			List<Version> sortedList = new ArrayList<Version>( versionSet);
			Collections.sort(sortedList, VersionProcessor.comparator);
			versioning.setVersions(sortedList);
			versioning.setRelease( sortedList.get( sortedList.size()-1));
			versioning.setLatest( sortedList.get( sortedList.size()-1));
		}
		
		return metaData;
	}
	
	/**
	 * create a {@link MavenMetaData} for a single solution using GMF
	 * @param solution - {@link Solution} to create the meta data for
	 * @return - {@link MavenMetaData} representing the solution
	 * @throws CodecException - arrgh
	 */
	public static MavenMetaData createMetaData( Solution solution) throws CodecException{
		return createMetaData(solution, null);
	}	
	
	/**
	 * create a {@link MavenMetaData} for a single solution using the passed provider (which may be null)
	 * @param solution - the {@link Solution}
	 * @param instanceProvider - the provider for the instance or null to use GMF
	 * @return - the {@link MavenMetaData}
	 * @throws CodecException - arrgh
	 */
	public static MavenMetaData createMetaData( Solution solution, Function<EntityType<?>, GenericEntity> instanceProvider) throws CodecException {
		
		MavenMetaData metaData = create( MavenMetaData.T, instanceProvider);
		metaData.setGroupId( solution.getGroupId());
		metaData.setArtifactId( solution.getArtifactId());
		metaData.setVersion( solution.getVersion());
		return metaData;

	}
	
	/**
	 * creates an instance 
	 * @param entityType - the {@link EntityType} of the instance to create 
	 * @param instanceProvider - the {@link Function} that can create such types 
	 * @return - the expected T type 
	 * @throws CodecException - arrgh
	 */
	@SuppressWarnings("unchecked")
	private static <T> T create( EntityType<? extends GenericEntity> entityType, Function<EntityType<?>, GenericEntity> instanceProvider) throws CodecException {	
		if (instanceProvider != null) {
			try {
				return (T) instanceProvider.apply(entityType);
			} catch (RuntimeException e) {
				String msg ="instance provider cannot provide new instance of type [" + entityType.getTypeSignature() + "]";				
				throw new CodecException(msg, e);
			}
		} 
		else {
			return (T) entityType.create();
		}
	}
	
	/**
	 * returns true if chances are that this is a group maven meta data<br/>
	 * - has a versioning (for now only check)
	 */
	public static boolean isGroupMavenMetaData( MavenMetaData mavenMetaData) {
		if (mavenMetaData == null)
			return false;
		if (mavenMetaData.getVersioning() == null)
			return false;
		return true;				
	}
	
	/**
	 *  
	 */
	/**
	 * return the name of the remote snapshot
	 * @param mavenMetaData - the {@link MavenMetaData}
	 * @return - the name of the snapshot 
	 */
	public static String getSnapshotPrefix( MavenMetaData mavenMetaData) {
		if (mavenMetaData == null)
			return null;
		Versioning versioning = mavenMetaData.getVersioning();
		if (versioning == null)
			return null;
		Snapshot snapshot = versioning.getSnapshot();
		if (snapshot == null)
			return null;
				
		int buildNumber = snapshot.getBuildNumber();
		Date stamp = snapshot.getTimestamp();
	
		String versionAsString = VersionProcessor.toString( mavenMetaData.getVersion());
		String versionPrefix = versionAsString.substring(0, versionAsString.toUpperCase().indexOf( "-SNAPSHOT"));
		String prefix = mavenMetaData.getArtifactId() + "-" +  versionPrefix + "-" + format.format(stamp) + "-" + buildNumber;
		return prefix;
	
	}
	
}
