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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocaterBuilder;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocation;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocator;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocatorContext;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationLocated;
import com.braintribe.devrock.model.mc.reason.InvalidRepositoryConfigurationLocation;
import com.braintribe.devrock.model.mc.reason.NoRepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.utils.paths.UniversalPath;

public class RepositoryConfigurationLocatorBuilderImpl implements RepositoryConfigurationLocaterBuilder, RepositoryConfigurationLocator {

	private final List<RepositoryConfigurationLocator> locators = new ArrayList<>();
	private String collectorReasonMessage = "No RespositoryConfiguration found";
	
	@Override
	public RepositoryConfigurationLocaterBuilder addLocation(File file) {
		add(c -> locate(file, true));
		return this;
	}
	
	@Override
	public RepositoryConfigurationLocator addRequiredLocation(File file) {
		add(c -> locate(file, false));
		return this;
	}

	@Override
	public RepositoryConfigurationLocaterBuilder addLocationEnvVariable(String envVar) {
		add(c -> locateByEnvVariable(c, envVar));
		return this;
	}

	@Override
	public RepositoryConfigurationLocaterBuilder addDevEnvLocation(UniversalPath path) {
		add(c -> locateInDevEnv(c, path));
		return this;
	}
	
	@Override
	public RepositoryConfigurationLocaterBuilder addUserDirLocation(UniversalPath path) {
		add(c -> locateInUserDir(c, path));
		return this;
	}

	@Override
	public RepositoryConfigurationLocaterBuilder add(RepositoryConfigurationLocator locator) {
		locators.add(locator);
		return this;
	}
	
	@Override
	public RepositoryConfigurationLocaterBuilder collectorReasonMessage(String text) {
		this.collectorReasonMessage = text;
		return this;
	}
	
	@Override
	public RepositoryConfigurationLocator done() {
		return this;
	}

	@Override
	public Maybe<RepositoryConfigurationLocation> locateRepositoryConfiguration(RepositoryConfigurationLocatorContext context) {
		Reason collectorReason = Reasons.build(NoRepositoryConfiguration.T).text(collectorReasonMessage).toReason();
		
		for (RepositoryConfigurationLocator locator: locators) {
			Maybe<RepositoryConfigurationLocation> locationMaybe = locator.locateRepositoryConfiguration(context);
			
			if (locationMaybe.isSatisfied())
				return locationMaybe;
			
			if (locationMaybe.isUnsatisfiedBy(NoRepositoryConfiguration.T))
				collectorReason.getReasons().add(locationMaybe.whyUnsatisfied());
			else
				return locationMaybe;
		}
		
		return collectorReason.asMaybe();
	}
	
	private Maybe<RepositoryConfigurationLocation> locateByEnvVariable(RepositoryConfigurationLocatorContext context, String name) {
		String envVar = context.getVirtualEnvironment().getEnv(name);
		
		if (envVar != null && !envVar.isEmpty()) {
			File repositoryConfigurationFile = new File(envVar);
			return locate(repositoryConfigurationFile, "environment variable " + name, false);
		}
		
		return Reasons.build(NoRepositoryConfiguration.T) //
				.text("Environment variable " + name + " not present").toMaybe(); 
 
	}
	
	private Maybe<RepositoryConfigurationLocation> locate(File repositoryConfigurationFile, String specialLocation, boolean lenient) {
		return locate(repositoryConfigurationFile, specialLocation, lenient, null);
	}
	
	private Maybe<RepositoryConfigurationLocation> locate(File repositoryConfigurationFile, String specialLocation, boolean lenient, Map<String, String> properties) {
		String msg = specialLocation != null? //
				"at " + repositoryConfigurationFile.getAbsolutePath(): //
				"from " + specialLocation + " at " + repositoryConfigurationFile.getAbsolutePath();

		if (repositoryConfigurationFile.exists()) {
			RepositoryConfigurationLocationImpl location = new RepositoryConfigurationLocationImpl(
					repositoryConfigurationFile, //
					//Reasons.build(RepositoryConfigurationLocated.T).text("Repository configuration located " + msg).toReason() //
					TemplateReasons.build(RepositoryConfigurationLocated.T).assign( RepositoryConfigurationLocated::setExpression, repositoryConfigurationFile.getAbsolutePath()).toReason() //
			);
			
			if (properties != null)
				location.setProperties(properties);
			
			return Maybe.complete(location);
		}
		
		if (lenient)
			return Reasons.build(NoRepositoryConfiguration.T) //
					.text("Could not find repository configuration " + msg).toMaybe();
		else
			return Reasons.build(InvalidRepositoryConfigurationLocation.T) //
					.text("Missing required repository configuration " + msg).toMaybe();
			
	}
	
	private Maybe<RepositoryConfigurationLocation> locate(File repositoryConfigurationFile, boolean lenient) {
		return locate(repositoryConfigurationFile, null, lenient);
	}
	
	private Maybe<RepositoryConfigurationLocation> locateInDevEnv(RepositoryConfigurationLocatorContext context, UniversalPath path) {
		File developmentEnvironmentRoot = context.getDevelopmentEnvironmentRoot();
		
		if (developmentEnvironmentRoot != null) {
			
			File repositoryConfigurationFile = UniversalPath.from(developmentEnvironmentRoot) //
				.push(path) //
				.toFile();
			
			Path devRoot = developmentEnvironmentRoot.toPath().toAbsolutePath().normalize();
			
			Map<String, String> properties = new HashMap<>();
			properties.put("config.dev.root", devRoot.toString());
			
			String sdkHome = Optional.ofNullable(System.getProperty("DEVROCK_SDK_HOME")).orElseGet(() -> devRoot.getParent().toString());
			properties.put("config.sdk.root", sdkHome);
			
			return locate(repositoryConfigurationFile, "development environment " + developmentEnvironmentRoot.getAbsolutePath(), true, properties);
		}
		
		return Reasons.build(NoRepositoryConfiguration.T) //
				.text("No development environment detected").toMaybe(); 
	}
	
	private Maybe<RepositoryConfigurationLocation> locateInUserDir(RepositoryConfigurationLocatorContext context, UniversalPath path) {
		String userHome = context.getVirtualEnvironment().getProperty("user.home");

		File repositoryConfigurationFile = UniversalPath.empty().push(userHome) //
				.push(path) //
				.toFile();

		return locate(repositoryConfigurationFile, "user home directory", true);
	}

}
