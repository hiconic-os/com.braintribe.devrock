// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.xml.stream.XMLStreamException;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.marshaller.maven.metadata.MavenMetaDataMarshaller;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.meta.Versioning;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

public class MavenMetadataPersistenceExpert {
	public static final String MAVEN_METADATA_CONTAINER_PREFIX = "maven-metadata";
	public static final String MAVEN_METADATA_CONTAINER_SUFFFIX = ".xml";

	private static MavenMetaDataMarshaller mavenMetaDataMarshaller = new MavenMetaDataMarshaller();	
	public static File getFileForRepository( File location, String repositoryId) {
		return new File( location, MAVEN_METADATA_CONTAINER_PREFIX + "-" + repositoryId + MAVEN_METADATA_CONTAINER_SUFFFIX);		
	}
	
	public static File getFileForBundle( File location, RavenhurstBundle bundle) {
		return getFileForRepository(location, bundle.getRepositoryId());
	}
	
	/**
	 * fixes issues with remote maven-metadata 
	 * @param mmd
	 * @return
	 */
	private static MavenMetaData sanitize( MavenMetaData mmd) {
		Versioning versioning = mmd.getVersioning();
		if (versioning == null) {
			return mmd;
		}
		Version version = mmd.getVersion();
		if (version != null && versioning.getVersions().isEmpty()) {
			versioning.getVersions().add(version);
		}
		return mmd;
	}
	
	public static MavenMetaData decode(LockFactory lockFactory, File file) throws RepositoryPersistenceException {
		Lock semaphore = lockFactory.getLockInstance(file).writeLock();
		try {					
			// lock 
			semaphore.lock();
			MavenMetaData metadata = mavenMetaDataMarshaller.unmarshall( file);
			return sanitize( metadata);																
		} catch (XMLStreamException e) {
			String msg="cannot decode maven meta data file [" + file.getAbsolutePath() + "]";
			throw new RepositoryPersistenceException(msg, e);
		} 
		finally {
			semaphore.unlock();
		}
	}
	
	public static MavenMetaData decode( String contents) throws RepositoryPersistenceException {
		
		try {					
			MavenMetaData metadata = mavenMetaDataMarshaller.unmarshall( contents);
			// 
			return sanitize( metadata);																
		} catch (XMLStreamException e) {
			String msg="cannot decode maven meta data from [" + contents + "]";
			throw new RepositoryPersistenceException(msg, e);
		} 
		
	}
	
	
	
	public static void encode( LockFactory lockFactory,MavenMetaData metadata, File mavenMetaDataFile) throws RepositoryPersistenceException {
		Lock mavenDataSemaphore = lockFactory.getLockInstance(mavenMetaDataFile).writeLock();
		try {
			mavenDataSemaphore.lock();
			mavenMetaDataMarshaller.marshall(mavenMetaDataFile, metadata);
		} catch (XMLStreamException e) {
			String msg ="cannot save maven metadata to [" + mavenMetaDataFile.getAbsolutePath() + "]";
			throw new RepositoryPersistenceException(msg, e);
		}
		finally {
			mavenDataSemaphore.unlock();
		}
	}
	
	public static MavenMetaData generateMavenMetaData( Identification identification, List<String> versions) throws RepositoryPersistenceException {		
		MavenMetaData mavenMetaData = MavenMetaData.T.create();
		mavenMetaData.setGroupId( identification.getGroupId());
		mavenMetaData.setArtifactId( identification.getArtifactId());			
		
		Versioning versioning = Versioning.T.create();
		mavenMetaData.setVersioning(versioning);		
		versioning.setLastUpdated( new Date());
		versioning.getVersions().clear();
		for (String versionAsString : versions) {
			try {
				Version version = VersionProcessor.createFromString(versionAsString);
				versioning.getVersions().add( version);
			} catch (VersionProcessingException e) {
				throw new RepositoryPersistenceException( "cannot create version from [" + versionAsString + "]", e);
			}
		}
		return mavenMetaData;
	}
	
	public static MavenMetaData generateMavenMetaData( Solution solution){		
		MavenMetaData mavenMetaData = MavenMetaData.T.create();
		mavenMetaData.setGroupId( solution.getGroupId());
		mavenMetaData.setArtifactId( solution.getArtifactId());
		mavenMetaData.setVersion( solution.getVersion());		
		return mavenMetaData;
	}
	public static MavenMetaData generateMavenMetaData( Identification solution){		
		MavenMetaData mavenMetaData = MavenMetaData.T.create();
		mavenMetaData.setGroupId( solution.getGroupId());
		mavenMetaData.setArtifactId( solution.getArtifactId());	
		return mavenMetaData;
	}
}
