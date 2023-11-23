package com.braintribe.build.artifacts.mc.wire.repositoryExtract.contract;

import com.braintribe.build.artifacts.mc.wire.repositoryExtract.expert.RepositoryExtractRunner;
import com.braintribe.wire.api.space.WireSpace;

/**
 * actual contract of the repository extractor
 * @author pit
 *
 */
public interface RepositoryExtractContract extends WireSpace {

	/**
	 * @return - a fully configured {@link RepositoryExtractRunner}
	 */
	RepositoryExtractRunner extractor();
}
