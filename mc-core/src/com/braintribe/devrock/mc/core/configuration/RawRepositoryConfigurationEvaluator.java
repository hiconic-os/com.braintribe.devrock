package com.braintribe.devrock.mc.core.configuration;

import java.io.File;

import com.braintribe.devrock.mc.api.repository.configuration.RawRepositoryConfiguration;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.config.yaml.ConfigVariableResolver;
import com.braintribe.gm.config.yaml.YamlConfigurations;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.ve.api.VirtualEnvironment;

public class RawRepositoryConfigurationEvaluator {
	private VirtualEnvironment virtualEnvironment;
	
	public RawRepositoryConfigurationEvaluator(VirtualEnvironment virtualEnvironment) {
		super();
		this.virtualEnvironment = virtualEnvironment;
	}

	public Maybe<RepositoryConfiguration> evaluate(RawRepositoryConfiguration rawRepositoryConfiguration) {
		return evaluate(rawRepositoryConfiguration.repositoryConfiguration(), rawRepositoryConfiguration.file());
	}
	
	public Maybe<RepositoryConfiguration> evaluate(RepositoryConfiguration rawConfig, File file) {
		ConfigVariableResolver resolver = new ConfigVariableResolver(virtualEnvironment, file);
		
		var configMaybe = YamlConfigurations.resolvePlaceholders(rawConfig, resolver::resolve);
		
		if (configMaybe.isUnsatisfied())
			return configMaybe;
		
		if (resolver.getFailure() != null)
			return Maybe.incomplete(rawConfig, resolver.getFailure());
		
		return configMaybe;
	}
}
