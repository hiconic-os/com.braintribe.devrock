// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.devrock.mc.core.configuration;

import java.io.File;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationLoaded;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.config.yaml.ConfigVariableResolver;
import com.braintribe.gm.config.yaml.YamlConfigurations;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * simple isolated YAML marshaller setup that allows to use variables within the YAML file. 
 * will fail fully if no repository configuration file exist, otherwise may return an incomplete 
 * repository configuration - check the Maybe
 *   
 * @author pit
 *
 */
public class StandaloneRepositoryConfigurationLoader {
	
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	private boolean absentify;
	
	/**
	 * @param virtualEnvironment - override the {@link StandardEnvironment}
	 */
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	/**
	 * @param absentify - true if missing properties should be absentified
	 */
	@Configurable
	public void setAbsentify(boolean absentify) {
		this.absentify = absentify;
	}
	
	/**
	 * @param file - the configuration file to load 
	 * @return - the {@link Maybe} of the {@link RepositoryConfiguration}
	 */
	public Maybe<RepositoryConfiguration> loadRepositoryConfiguration( File file) {
		
		if (!file.exists()) {
			return Reasons.build(NotFound.T).text("configuration file [" + file.getAbsolutePath() + "] doesn't exist").toMaybe();
		}
		
		ConfigVariableResolver variableResolver = new ConfigVariableResolver(virtualEnvironment, file);
		
		Maybe<RepositoryConfiguration> maybeConfig = YamlConfigurations.read(RepositoryConfiguration.T) //
				.placeholders(variableResolver::resolve)
				.noDefaulting()
				.options( ob -> ob.absentifyMissingProperties( absentify))				
				.from(file);
				
		
		if (maybeConfig.isUnsatisfied()) {
			return maybeConfig;
		}
		// attach origination 
		if (maybeConfig.hasValue()) {
			RepositoryConfiguration rcfg = maybeConfig.value(); 
			Reason loadingReason = TemplateReasons.build(RepositoryConfigurationLoaded.T).assign(RepositoryConfigurationLoaded::setUrl,file.getAbsolutePath()).toReason();
			rcfg.setOrigination( loadingReason);
		}
		
		if (maybeConfig.isSatisfied()) {
			// attach failures of the variable resolver
			if (variableResolver.getFailure() != null) {
				return Maybe.incomplete(maybeConfig.get(), variableResolver.getFailure());
			}
			return maybeConfig;
		} 
		else if (maybeConfig.isIncomplete()) {
			// attach failures of the variable resolver 
			if (variableResolver.getFailure() != null) {
				RepositoryConfiguration rcfg = maybeConfig.value();
				return Maybe.incomplete( rcfg, variableResolver.getFailure());
			}			
		}
	 		
		return maybeConfig;
	}
}
