// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection;

import java.io.File;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBias;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

/**
 * external interface to provide features to the {@link ArtifactReflectionExpert} and the {@link SolutionReflectionExpert}
 * @author pit
 *
 */
public interface RepositoryReflectionSupport extends LockFactory{
	File downloadFileFrom ( PartDownloadInfo downloadInfo, RepositoryRole repositoryRole) throws RepositoryPersistenceException;
	RavenhurstBundle getRavenhurstBundleForUrl(String url)  throws RepositoryPersistenceException;
	RavenhurstBundle getRavenhurstBundleForId(String id)  throws RepositoryPersistenceException;
	void updateRepositoryInformationOfArtifact(RavenhurstBundle bundle, Identification unversionedArtifact) throws RepositoryPersistenceException;
	boolean getPreemptiveConnectionTestMode();
	ArtifactBias getArtifactBias( Identification unversionedArtifact);
	boolean solutionExistsInBundle( RavenhurstBundle bundle, Solution solution);
	boolean groupExistsInBundle( RavenhurstBundle bundle, String group);
}
