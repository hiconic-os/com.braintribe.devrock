// ============================================================================
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
// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.devrock.mc.core.configuration;

import java.io.File;
import java.util.Date;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationLoaded;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationLocated;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationResolving;
import com.braintribe.devrock.model.mc.reason.InvalidRepositoryConfiguration;
import com.braintribe.devrock.model.mc.reason.InvalidRepositoryConfigurationLocation;
import com.braintribe.devrock.model.mc.reason.NoDefaultRepositoryConfiguration;
import com.braintribe.devrock.model.mc.reason.NoRepositoryConfiguration;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.config.yaml.ModeledYamlConfigurationLoader;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.api.VirtualEnvironment;

/**
 * This class loads {@link RepositoryConfiguration} from certain potential positions and resolves variables and relative paths.
 * 
 * The {@link RepositoryConfiguration} can be found by different ways of the follow kind and order:
 * 
 * 1. A Development Environment is detected and its artifacts/repository-configuration.yaml is existing
 * 2. The Well known environment variable (DEVROCK_REPOSITORY_CONFIGURATION) points to an existing yaml file
 * 3. The default <user-home>/.devrock/repository-configuration.yaml is existing 
 *
 * dev-env
 *   mc-ng
 *     dev-environment.yaml
 *     artifacts
 *       repository-configuration.yaml ( localRepository: "./local-repository" )
 *       local-repository (potentially symbol link to a shared one eg. ~/.m2/repository)
 *     git
 *     	 com.braintribe.gm
 *       com.braintribe.devrock
 *       com.braintribe.devrock.ant
 *     eclipse-workspace
 *     
 * @author Dirk Scheffler
 */
public class RepositoryConfigurationLoader implements Supplier<Maybe<RepositoryConfiguration>> {
	public static final String FOLDERNAME_ARTIFACTS = "artifacts";
	public static final String FOLDERNAME_DEVROCK = ".devrock";
	public static final String ENV_DEVROCK_REPOSITORY_CONFIGURATION = "DEVROCK_REPOSITORY_CONFIGURATION";
	public static final String FILENAME_REPOSITORY_CONFIGURATION = "repository-configuration.yaml";
	
	private static YamlMarshaller yamlMarshaller = new YamlMarshaller();
	
	static {
		yamlMarshaller.setV2(true);
	}
	
	private File developmentEnvironmentRoot;
	private File repositoryConfigurationLocation;
	private VirtualEnvironment virtualEnvironment;
	
	@Configurable
	public void setRepositoryConfigurationLocation(File repositoryConfigurationLocation) {
		this.repositoryConfigurationLocation = repositoryConfigurationLocation;
	}
	
	@Required @Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	@Configurable
	public void setDevelopmentEnvironmentRoot(File developmentEnvironmentRoot) {
		this.developmentEnvironmentRoot = developmentEnvironmentRoot;
	}
	
	@Override
	public Maybe<RepositoryConfiguration> get() {
		Pair<Maybe<File>, Reason> configurationFilePair = getRepositoryConfigurationFile();
		Maybe<File> configurationFilePotential = configurationFilePair.first;
				
		if (configurationFilePotential.isEmpty())
			return configurationFilePotential.emptyCast();

		File configurationFile = configurationFilePotential.get();

		Maybe<RepositoryConfiguration> maybeConfig = new ModeledYamlConfigurationLoader() //
				.virtualEnvironment(virtualEnvironment) //
				.loadConfig(RepositoryConfiguration.T, configurationFile, false);
		
		if (maybeConfig.isUnsatisfied())
			return maybeConfig;
		
		RepositoryConfiguration repositoryConfiguration = maybeConfig.get();
		repositoryConfiguration.setOrigination( configurationFilePair.second);

		return Maybe.complete(repositoryConfiguration);
	}

	/**
	 * @return a Pair of the {@link Maybe} of the File to be used and the origination {@link Reason}
	 */
	private Pair<Maybe<File>, Reason> getRepositoryConfigurationFile() {
		
		Reason collectorReason = Reasons.build(NoRepositoryConfiguration.T).text("No RepositoryConfiguration found").toReason();
		Date now = new Date();
		Reason originationReason = TemplateReasons.build(RepositoryConfigurationResolving.T) //
										.assign( RepositoryConfigurationResolving::setTimestamp, now) //
										.assign(RepositoryConfigurationResolving::setAgent, "RepositoryConfigurationLoader")
										.toReason();
		
		if (repositoryConfigurationLocation != null) {
			if (repositoryConfigurationLocation.exists()) {
				// taken from configured location
				originationReason.getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign(RepositoryConfigurationLocated::setExpression, "wired location") //
						.toReason());//
				Reason loadingReason = TemplateReasons.build(RepositoryConfigurationLoaded.T) //
						.assign(RepositoryConfigurationLoaded::setUrl,repositoryConfigurationLocation.getAbsolutePath()) //
						.toReason(); //
				originationReason.getReasons().add(loadingReason);
				
//				originationReason.getReasons().add( Reasons.build(RepositoryConfigurationLoaded.T) //
//						.enrich(r -> r.setUrl(repositoryConfigurationLocation.toURI().toString())) //
//						.text( "declared in file [" + repositoryConfigurationLocation.getAbsolutePath() + "]").toReason());
				
				return Pair.of( Maybe.complete(repositoryConfigurationLocation), originationReason);
			}
		}
		
		if (developmentEnvironmentRoot != null) {
			
			File repositoryConfigurationFile = UniversalPath.from(developmentEnvironmentRoot) //
				.push(FOLDERNAME_ARTIFACTS) //
				.push(FILENAME_REPOSITORY_CONFIGURATION) //
				.toFile();
			
			if (repositoryConfigurationFile.exists()) {
				// taken from declared placement in dev-evn
				originationReason.getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign(RepositoryConfigurationLocated::setExpression, "dev-env default location") //						
						.toReason()); //
				originationReason.getReasons().add( TemplateReasons.build(RepositoryConfigurationLoaded.T) //				
						.assign(RepositoryConfigurationLoaded::setUrl,repositoryConfigurationFile.getAbsolutePath()) //
						//.text( "declared in file [" + repositoryConfigurationFile.getAbsolutePath() + "]") 
						.toReason());				
				return Pair.of( Maybe.complete(repositoryConfigurationFile), originationReason);
			}
			
			
			collectorReason.getReasons().add(Reasons.build(InvalidRepositoryConfigurationLocation.T) //
					.text("The detected development environment has no " + repositoryConfigurationFile.getAbsolutePath()).toReason());
		}
		
		String envVar = virtualEnvironment.getEnv(ENV_DEVROCK_REPOSITORY_CONFIGURATION);
		
		if (envVar != null && !envVar.isEmpty()) {
			File repositoryConfigurationFile = new File(envVar);
			
			if (repositoryConfigurationFile.exists()) {
				// taken from env variable
				originationReason.getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign(RepositoryConfigurationLocated::setExpression, ENV_DEVROCK_REPOSITORY_CONFIGURATION) //
						//.text("pointed to by [" + ENV_DEVROCK_REPOSITORY_CONFIGURATION + "]") // 
						.toReason());
				originationReason.getReasons().add( TemplateReasons.build(RepositoryConfigurationLoaded.T) //
						.assign(RepositoryConfigurationLoaded::setUrl,repositoryConfigurationFile.getAbsolutePath()) //
						//.text( "file taken [" + repositoryConfigurationFile.getAbsolutePath() + "]") //
						.toReason()); //
				return Pair.of( Maybe.complete(repositoryConfigurationFile), originationReason);
			}
			else
				return Pair.of( Reasons.build(InvalidRepositoryConfiguration.T) //
						.text(ENV_DEVROCK_REPOSITORY_CONFIGURATION + " pointed to non existing file: " + repositoryConfigurationFile.getAbsolutePath()).toMaybe(), null);
		}
		
		
		String userHome = virtualEnvironment.getProperty("user.home");
		File repositoryConfigurationFile = UniversalPath.empty().push(userHome) //
				.push(FOLDERNAME_DEVROCK) //
				.push(FILENAME_REPOSITORY_CONFIGURATION) //
				.toFile();
		
		if (repositoryConfigurationFile.exists()) {
			// taken from user home
			originationReason.getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
					.assign(RepositoryConfigurationLocated::setExpression, "$${user.home}") //
					//.text("pointed to by ${user.home}") 
					.toReason());// 
			originationReason.getReasons().add( TemplateReasons.build(RepositoryConfigurationLoaded.T)
					.assign(RepositoryConfigurationLoaded::setUrl,repositoryConfigurationFile.getAbsolutePath()) //
					//.text( "file taken [" + repositoryConfigurationFile.getAbsolutePath() + "]") //
					.toReason()); //
	
			return Pair.of( Maybe.complete(repositoryConfigurationFile), originationReason);
		}
		
		collectorReason.getReasons().add(Reasons.build(NoDefaultRepositoryConfiguration.T) //
				.text("No default respository configuration found at " + repositoryConfigurationFile.getAbsolutePath()).toReason());
		
		return Pair.of( collectorReason.asMaybe(), null);
	}
}