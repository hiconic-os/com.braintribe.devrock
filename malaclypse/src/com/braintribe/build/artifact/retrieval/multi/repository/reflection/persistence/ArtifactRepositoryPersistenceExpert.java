// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.json.genericmodel.GenericModelJsonStringCodec;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.ravenhurst.data.RavenhurstArtifactDataContainer;
import com.braintribe.model.ravenhurst.data.RavenhurstArtifactRedeployDataContainer;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.utils.IOTools;

public class ArtifactRepositoryPersistenceExpert {
	private static Logger log = Logger.getLogger(ArtifactRepositoryPersistenceExpert.class);
	public static final String RAVENHURST_ARTIFACT_REDEPLOY_CONTAINER = ".updated.artifact.redeploy";
	private static final String MAVEN_METADATA_CONTAINER_PREFIX = "maven-metadata";
	private static final String MAVEN_METADATA_CONTAINER_SUFFFIX = ".xml";
	
	private GenericModelJsonStringCodec<RavenhurstArtifactRedeployDataContainer> codec = new GenericModelJsonStringCodec<RavenhurstArtifactRedeployDataContainer>();

	private MavenSettingsReader reader;
	private RavenhurstScope scope;
	private RepositoryReflection repositoryReflection;
	
	private MavenMetadataPersistenceExpert mavenMetaDataPersistenceExpert;
	
	@Configurable @Required
	public void setRepositoryReflection(RepositoryReflection repositoryReflection) {
		this.repositoryReflection = repositoryReflection;
	}
	
	@Configurable @Required
	public void setReader(MavenSettingsReader reader) {
		this.reader = reader;		
	}
	
	@Configurable @Required
	public void setScope(RavenhurstScope scope) {
		this.scope = scope;
	}
		
	public File getPersistenceContainerLocation(Artifact artifact) throws RepositoryPersistenceException {
		
		String localRepository;
		try {
			localRepository = reader.getLocalRepository(null);
		} catch (RepresentationException e) {
			String msg ="cannot find local repository's location";
			throw new RepositoryPersistenceException( msg, e);
		}
		// build name				
		String location = localRepository + File.separator + artifact.getGroupId().replace('.',  File.separatorChar) + File.separator + artifact.getArtifactId() + File.separator + VersionProcessor.toString(artifact.getVersion());
		return new File( location);
				
	}

	public RavenhurstArtifactDataContainer decode( File location) throws RavenhurstException{
		final RavenhurstArtifactDataContainer container = RavenhurstArtifactDataContainer.T.create();
		if (!location.exists())
			return container;			
		
		try {
			// collect metadata
			location.listFiles( new FileFilter() {
				
				@Override
				public boolean accept(File file) {
					String name = file.getName();
					if (name.equals( RAVENHURST_ARTIFACT_REDEPLOY_CONTAINER)) {
						Lock semaphore = repositoryReflection.getLockInstance(file).readLock();
						try {
							// get lock 
							semaphore.lock();
							String contents = IOTools.slurp(file, "UTF-8");
							RavenhurstArtifactRedeployDataContainer redeployContainer = codec.decode(contents);
							container.setUrlToRedeployMap( redeployContainer.getUrlToRedeployMap());
							return true;		
						} catch (IOException e) {
							String msg="cannot read redeploy container [" + file.getAbsolutePath() + "]";
							throw new RuntimeException(msg, e);
						} catch (CodecException e) {
							String msg="cannot decode redeploy container [" + file.getAbsolutePath() + "]";
							throw new RuntimeException(msg, e);
						} 				
						finally {
							semaphore.unlock();
						}
					}
					else {
						if (name.startsWith( MAVEN_METADATA_CONTAINER_PREFIX) && name.endsWith( MAVEN_METADATA_CONTAINER_SUFFFIX)) {
							String repoName = name.substring( MAVEN_METADATA_CONTAINER_PREFIX.length()+1, name.length() - MAVEN_METADATA_CONTAINER_SUFFFIX.length());
						//	Lock semaphore = repositoryReflection.getLockInstance(file).readLock(); 
							try {
								RavenhurstBundle associatedBundle = scope.getRavenhurstBundleByName(repoName);
								//TODO : look into the role of "maven-metadata-local.xml"
								if (associatedBundle == null ) {
									if (!repoName.equalsIgnoreCase("local")) {
										if (log.isWarnEnabled()) {
											log.warn("scope has no information about a repository named [" + repoName + "]");
										}
									}									
									return false;
								}
								// lock 
							//	semaphore.lock();
								MavenMetaData metadata = MavenMetadataPersistenceExpert.decode(repositoryReflection, file);
								container.getUrlToMetaDataMap().put( associatedBundle.getRepositoryUrl(), metadata);																
							} catch (RepositoryPersistenceException e) {
								String msg="cannot read maven meta data file [" + file.getAbsolutePath() + "]";
								throw new RuntimeException(msg, e);
							} catch (RavenhurstException e) {
								String msg="cannot determine bundle for [" + file.getAbsolutePath() + "]";
								throw new RuntimeException(msg, e);
							} 
							finally {
							//semaphore.unlock();
							}
						}
					}
					return false;
				}
			});
		} catch (Exception e) {
			throw new RavenhurstException( e.getMessage(), e.getCause());
		}
		return container;
		
	}
	
	public void encode(RavenhurstArtifactDataContainer container, File location) throws RavenhurstException{
		// write 
		if (!location.exists()) {
			location.mkdirs();
		}
		RavenhurstArtifactRedeployDataContainer redeployContainer = RavenhurstArtifactRedeployDataContainer.T.create();
		redeployContainer.setUrlToRedeployMap( container.getUrlToRedeployMap());
		File redeployFile = new File( location, RAVENHURST_ARTIFACT_REDEPLOY_CONTAINER);
		Lock redeploySemaphore = repositoryReflection.getLockInstance(redeployFile).writeLock();
		try {
			redeploySemaphore.lock();
			String contents = codec.encode(redeployContainer);			
			IOTools.spit( redeployFile, contents, "UTF-8", false);
		} catch (CodecException e) {
			String msg = "cannot encode redeploy map";
			throw new RavenhurstException(msg, e);
		} catch (IOException e) {
			String msg = "cannot write redeploy map to [" + redeployFile.getAbsolutePath() + "]";
			throw new RavenhurstException(msg, e);
		}
		finally {
			redeploySemaphore.unlock();
		}
		
		Map<String, MavenMetaData> urlToVersionsMap = container.getUrlToMetaDataMap();
		for (Entry<String, MavenMetaData> entry : urlToVersionsMap.entrySet()) {
			RavenhurstBundle associatedBundle = scope.getRavenhurstBundleByUrl( entry.getKey());
			if (associatedBundle == null) {
				log.warn("scope has no information about a repository representing URL [" + entry.getKey() + "]");
				continue;
			}			
			// store
			File mavenMetaDataFile = new File( location, MAVEN_METADATA_CONTAINER_PREFIX + "-" + associatedBundle.getRepositoryId() + MAVEN_METADATA_CONTAINER_SUFFFIX);
			//Lock mavenDataSemaphore = repositoryReflection.getLockInstance( mavenMetaDataFile).writeLock();
			try {
				//mavenDataSemaphore.lock();
				MavenMetadataPersistenceExpert.encode( repositoryReflection, entry.getValue(), mavenMetaDataFile);
			} 
			catch (RepositoryPersistenceException e) {
				String msg ="cannot save maven metadata to [" + mavenMetaDataFile.getAbsolutePath() + "]";
				throw new RavenhurstException(msg, e);
			}
			finally {
				//mavenDataSemaphore.unlock();
			}
		}
				
	}
}
