// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.BasicRavenhurstRepositoryIndexPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.FilesystemBasedPersistenceExpertForRavenhurstBundle;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhurstPersistenceExpertForMainDataContainer;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhurstPersistenceExpertForRavenhurstBundle;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhurstRepositoryIndexPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.ravenhurst.data.RavenhurstArtifactDataContainer;
import com.braintribe.model.ravenhurst.data.RavenhurstMainDataContainer;
import com.braintribe.model.ravenhurst.data.RavenhurstSolutionDataContainer;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

public class RavenhurstPersistenceRegistryImpl implements RavenhurstPersistenceRegistry {
	private static Logger log = Logger.getLogger(RavenhurstPersistenceRegistryImpl.class);
	

	private RavenhurstPersistenceExpertForMainDataContainer mainDataExpert;
	private RavenhurstPersistenceExpertForRavenhurstBundle bundleExpert;
	private RavenhurstRepositoryIndexPersistenceExpert indexExpert;
	
	private LocalRepositoryLocationProvider localRepositoryLocationProvider;
	private LockFactory lockFactory;
	
	private RavenhurstMainDataContainer mainContainer;
	private Map<String, RavenhurstArtifactDataContainer> locationToArtifactContainerMap = new HashMap<String, RavenhurstArtifactDataContainer>();
	private Map<String, RavenhurstSolutionDataContainer> locationToSolutionContainerMap = new HashMap<String, RavenhurstSolutionDataContainer>();
	
	@Override @Configurable @Required
	public void setLocalRepositoryLocationProvider(LocalRepositoryLocationProvider reader) {
		this.localRepositoryLocationProvider = reader;
	}
	@Configurable @Required @Override
	public void setLockFactory(LockFactory lockFactory) {
		this.lockFactory = lockFactory;
	}
	/**
	 * lazy instantiation of main data expert 
	 */
	private Object lazyMainDataContainerExpertInitMonitor = new Object();
	private RavenhurstPersistenceExpertForMainDataContainer getMainDataExpert() {
		if (mainDataExpert != null)
			return mainDataExpert;
		
		synchronized( lazyMainDataContainerExpertInitMonitor) {
			if (mainDataExpert != null)
				return mainDataExpert;			
			RavenhurstPersistenceExpertForMainDataContainer _mainDataExpert = new RavenhurstPersistenceExpertForMainDataContainer();
			_mainDataExpert.setLocalRepositoryLocationProvider(localRepositoryLocationProvider);
			_mainDataExpert.setLockFactory(lockFactory);
			mainDataExpert = _mainDataExpert;	
		}
		return mainDataExpert;
	}

	@Override
	public RavenhurstMainDataContainer getRavenhurstMainDataContainer() {
		
		if (mainContainer != null)
			return mainContainer;
		try {
			mainContainer = getMainDataExpert().decode();			
			return mainContainer;
		} catch (RavenhurstException e) {
			log.error("cannot read main container file", e);
		}
		return null;
	}
	
	/**
	 * lazy instantiation of bundle expert
	 */
	private Object lazyBundleExpertInitializationMonitor = new Object();
	private RavenhurstPersistenceExpertForRavenhurstBundle getBundleDataExpert() {
		if (bundleExpert != null) {
			return bundleExpert;
		}
		synchronized (lazyBundleExpertInitializationMonitor) {
			if (bundleExpert != null)
				return bundleExpert;
				
			RavenhurstPersistenceExpertForRavenhurstBundle _bundleExpert = new FilesystemBasedPersistenceExpertForRavenhurstBundle();
			_bundleExpert.setLockFactory( lockFactory);
			bundleExpert = _bundleExpert;
		}							
		return bundleExpert;
	}
	
	

	@Override
	public void persistRavenhurstBundle(RavenhurstBundle bundle) {
		try {
			String localRepositoryAsString = localRepositoryLocationProvider.getLocalRepository(null);
			File file = RavenhurstPersistenceHelper.deriveDumpFile( localRepositoryAsString, bundle);
			getBundleDataExpert().encode(file, bundle);
		} catch (Exception e) {
			log.error("cannot persist ravenhurst bundle for [" + bundle.getProfileId() + ":" + bundle.getRepositoryId() + "]");			
		}	
	}

	@Override
	public void persistRavenhustMainDataContainer() {
		if (mainContainer != null) {
			try {
				getMainDataExpert().encode(mainContainer);
			} catch (RavenhurstException e) {
				log.error( "cannot write main container file", e);
			}
		}		
	}
	
	/**
	 * lazy instantiation of main data expert 
	 */
	private Object lazyRavenhurstIndexExpertInitMonitor = new Object();
	private RavenhurstRepositoryIndexPersistenceExpert getIndexExpert() {
		if (indexExpert != null)
			return indexExpert;
		
		synchronized( lazyRavenhurstIndexExpertInitMonitor) {
			if (indexExpert != null)
				return indexExpert;			
			RavenhurstRepositoryIndexPersistenceExpert _indexExpert = new BasicRavenhurstRepositoryIndexPersistenceExpert();
			_indexExpert.setLockFactory(lockFactory);
			indexExpert = _indexExpert;	
		}
		return indexExpert;
	}

	@Override
	public void updateRavenhurstIndexPersistence(RavenhurstBundle bundle, Collection<String> touchedGroups) {
		String localRepositoryAsString = localRepositoryLocationProvider.getLocalRepository(null);
		File indexFile = RavenhurstPersistenceHelper.deriveIndexFile( localRepositoryAsString, bundle);
		getIndexExpert().update(indexFile, touchedGroups);
		
	}
	@Override
	public void persistRavenhurstIndex(RavenhurstBundle bundle, Collection<String> touchedGroups) {
		String localRepositoryAsString = localRepositoryLocationProvider.getLocalRepository(null);
		File indexFile = RavenhurstPersistenceHelper.deriveIndexFile( localRepositoryAsString, bundle);
		getIndexExpert().encode(indexFile, touchedGroups);
		
	}
	@Override
	public Set<String> loadRavenhurstIndex(RavenhurstBundle bundle) {
		String localRepositoryAsString = localRepositoryLocationProvider.getLocalRepository(null);
		File indexFile = RavenhurstPersistenceHelper.deriveIndexFile( localRepositoryAsString, bundle);
		return getIndexExpert().decode(indexFile);
	}
	
	@Override
	public boolean existsRavenhurstIndexPersistence(RavenhurstBundle bundle) {
		String localRepositoryAsString = localRepositoryLocationProvider.getLocalRepository(null);
		File indexFile = RavenhurstPersistenceHelper.deriveIndexFile( localRepositoryAsString, bundle);
		return indexFile.exists();
	}
	
	@Override
	public void clear() {
		mainContainer = null;
		locationToArtifactContainerMap.clear();
		locationToSolutionContainerMap.clear();		
	}

}
