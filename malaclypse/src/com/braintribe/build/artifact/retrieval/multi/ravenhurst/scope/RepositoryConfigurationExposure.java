package com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope;

import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;

/**
 * gives access to the {@link RepositoryConfiguration} that was injected into MC to be used as view filters.
 * pure read only : changes are not reflected in the current installation, as this is only available post-setup
 * @author pit
 *
 */
public interface RepositoryConfigurationExposure {
	/**
	 * @return - the {@link RepositoryConfiguration} as injected 
	 */
	RepositoryConfiguration exposeRepositoryConfiguration();

	/**
	 * @return return the optional resolution of repository views that where used to build the repository configuration
	 */
	RepositoryViewResolution getRepositoryViewResolution();
}
