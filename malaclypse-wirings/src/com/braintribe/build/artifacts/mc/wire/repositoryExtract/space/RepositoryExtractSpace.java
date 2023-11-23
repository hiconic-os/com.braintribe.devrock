package com.braintribe.build.artifacts.mc.wire.repositoryExtract.space;

import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.contract.ExternalConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.contract.RepositoryExtractContract;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.expert.RepositoryExtractRunner;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class RepositoryExtractSpace implements RepositoryExtractContract {
	
	@Import
	private ExternalConfigurationContract externalConfiguration;
	
	@Import
	private BuildDependencyResolutionContract buildResolver;

	@Override
	public RepositoryExtractRunner extractor() {
		RepositoryExtractRunner bean = new RepositoryExtractRunner();
				
		bean.setBuildDependencyResolver( buildResolver.buildDependencyResolver());
		bean.setPlainDependencyResolver( buildResolver.plainOptimisticDependencyResolver());
		bean.setPomReader( buildResolver.pomReader());
		
		bean.setCondensedTerminalNames( externalConfiguration.terminals());
				
		return bean;
	}

	
}
