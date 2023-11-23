// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection;

import java.util.List;
import java.util.Map;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.model.artifact.info.VersionInfo;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

/**
 * an expert to handle the repository information on the artifact level (aka identification level)<br/>
 * returns a list of versions of an artifact that is contained in the repository information, 
 * while doing that, if necessary, updates the maven meta data information. 
 * <br/>and<br/>
 * processes update information, by MARKING the bundle's metadata to be updated at the next access via the
 * {@link #getVersions(RepositoryRole)}  
 * @author pit
 *
 */
public interface ArtifactReflectionExpert {
	
	
	/**
	 * get a sorted list of all available versions of this artifact, where a version only occurs once
	 * @param repositoryRole - the {@link RepositoryRole} that is used as filter
	 * @return - a sorted {@link List} of {@link Version}
	 * @throws RepositoryPersistenceException - arrgh
	 */
	List<Version> getVersions(RepositoryRole repositoryRole) throws RepositoryPersistenceException;
	
	/**
	 * get a sorted list of all available versions of this artifact, where a version only occurs once
	 * @param repositoryRole - the {@link RepositoryRole} that is used as filter
	 * @param range - the {@link VersionRange} - if passed and not an interval, and there are untrustworthy bundles around, it will produce fake entries
	 * @return - a sorted {@link List} if {@link Version}
	 * @throws RepositoryPersistenceException - if arrghed
	 */
	List<Version> getVersions(RepositoryRole repositoryRole, VersionRange range) throws RepositoryPersistenceException;
	
	/**
	 * returns a map of version/url for any versions of this role
	 * @param role - the {@link RepositoryRole} to work with
	 * @return - a {@link Map} linking {@link Version} to the URL as {@link String}
	 */
	public Map<Version,VersionInfo> getVersionData(RepositoryRole role);
	
	/**
	 * returns the URL of the repository that contains the version passed (the FIRST MATCH is taken!)
	 * @param version - the version to get the info from 
	 * @param bundles - the currently active bundles (or null to retrieve it by itself)
	 * @return
	 */
	public VersionInfo getVersionOrigin(Version version, List<RavenhurstBundle> bundles);
	
	/**
	 * returns the URL of the repository that contains the version passed (the FIRST MATCH is taken!)
	 * @param version - the version to get the info from 	 
	 * @return
	 */
	public VersionInfo getVersionOrigin(Version version);
	/**
	 * process update information about an solution within this artifact's range 
	 * @param bundle - the {@link RavenhurstBundle} that signaled the update
	 */
	@Deprecated
	void processUpdateInformation( RavenhurstBundle bundle)  throws RepositoryPersistenceException;
	
}
