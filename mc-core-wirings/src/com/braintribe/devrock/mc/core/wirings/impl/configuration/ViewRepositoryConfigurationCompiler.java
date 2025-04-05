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
package com.braintribe.devrock.mc.core.wirings.impl.configuration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.ScalarsFirst;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.mc.api.repository.configuration.RawRepositoryConfiguration;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolutionContext;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolutionResult;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolver;
import com.braintribe.devrock.mc.core.configuration.RawRepositoryConfigurationEvaluator;
import com.braintribe.devrock.mc.core.wirings.view.RepositoryViewResolutionWireModule;
import com.braintribe.devrock.mc.core.wirings.view.contract.RepositoryViewResolutionContract;
import com.braintribe.devrock.model.mc.cfg.origination.ViewRepositoryConfigurationCompiled;
import com.braintribe.devrock.model.mc.reason.InvalidRepositoryConfiguration;
import com.braintribe.devrock.model.mc.reason.MalformedDependency;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repositoryview.ViewRepositoryConfiguration;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.reason.TemplateReasonBuilder;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.utils.encryption.Md5Tools;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class ViewRepositoryConfigurationCompiler  {
	private RepositoryConfiguration repositoryConfiguration;
	
	private Maybe<RepositoryConfiguration> maybeCompiledConfiguration;
	private VirtualEnvironment virtualEnvironment;
	private File file;

	private Function<File, ReadWriteLock> lockSupplier;
	
	private LazyInitialized<Maybe<CompiledRepositoryConfiguration>> lazyCompiledRepositoryConfiguration = new LazyInitialized<>(this::compileConfiguration);
	private LazyInitialized<Maybe<RepositoryConfiguration>> lazyDirectRepositoryConfiguration = new LazyInitialized<>(this::compileConfigurationDirect);

	public ViewRepositoryConfigurationCompiler(Maybe<RawRepositoryConfiguration> rawRepositoryConfigurationMaybe, VirtualEnvironment ve) {
		super();
		if (rawRepositoryConfigurationMaybe.isUnsatisfied()) {
			this.repositoryConfiguration = RepositoryConfiguration.T.create();
			repositoryConfiguration.setFailure(rawRepositoryConfigurationMaybe.whyUnsatisfied());
		}
		else {
			RawRepositoryConfiguration rawRepositoryConfiguration = rawRepositoryConfigurationMaybe.get();
			this.repositoryConfiguration = rawRepositoryConfiguration.repositoryConfiguration();
			this.file = rawRepositoryConfigurationMaybe.get().file();
		}
		
		this.virtualEnvironment = ve;
	}
	
	@Required
	public void setLockSupplier(Function<File, ReadWriteLock> lockSupplier) {
		this.lockSupplier = lockSupplier;
	}
	
	public RepositoryConfiguration repositoryConfiguration() {
		var maybe = lazyCompiledRepositoryConfiguration.get();
		
		if (maybe.isUnsatisfied()) {
			RepositoryConfiguration failedConfig = RepositoryConfiguration.T.create();
			failedConfig.setFailure(maybe.whyUnsatisfied());
			return failedConfig;
		}
		
		return maybe.get().repositoryConfiguration();
	}
	
	public RepositoryViewResolution repositoryViewResolution() {
		var maybe = lazyCompiledRepositoryConfiguration.get();
		
		if (maybe.isUnsatisfied())
			return null;
		
		return maybe.get().repositoryViewResolution();
	}

	private Maybe<CompiledRepositoryConfiguration> compileConfiguration() {
		var directConfigMaybe = compileConfigurationDirect();
		
		if (directConfigMaybe.isUnsatisfied())
			return directConfigMaybe.whyUnsatisfied().asMaybe();
		
		var directConfig = directConfigMaybe.get();
		
		if (!(directConfig instanceof ViewRepositoryConfiguration))
			return Maybe.complete(new CompiledRepositoryConfiguration(directConfig, null));
		
		return compileViewConfiguration((ViewRepositoryConfiguration)directConfig);
	}
	
	private Maybe<RepositoryConfiguration> compileConfigurationDirect() {
		if (repositoryConfiguration.hasFailed()) {
			return Maybe.empty(repositoryConfiguration.getFailure());
		}

		var evaluatedConfigMaybe = new RawRepositoryConfigurationEvaluator(virtualEnvironment).evaluate(repositoryConfiguration, file);
		
		if (evaluatedConfigMaybe.isUnsatisfied())
			return evaluatedConfigMaybe;
		
		RepositoryConfiguration evaluatedConfig = evaluatedConfigMaybe.get();

		if (evaluatedConfig.getCachePath() == null) {
			var error = Reasons.build(InvalidRepositoryConfiguration.T) //
					.text("Missing cachePath configuration") //
					.toReason();
			return Maybe.empty(error);
		}
		
		return evaluatedConfigMaybe;
	}
	
	private Maybe<CompiledRepositoryConfiguration> compileViewConfiguration(ViewRepositoryConfiguration viewRepositoryConfiguration) {
		var baseConfiguration = extractBaseConfiguration((ViewRepositoryConfiguration)repositoryConfiguration);
		
		var viewResolutionConfig = cloneDown(viewRepositoryConfiguration, RepositoryConfiguration.T);
		
		try (WireContext<RepositoryViewResolutionContract> wireContext = Wire.context(new RepositoryViewResolutionWireModule(viewResolutionConfig, virtualEnvironment))) {
			RepositoryViewResolver repositoryViewResolver = wireContext.contract().repositoryViewResolver();
			RepositoryViewResolutionContext resolutionContext = RepositoryViewResolutionContext.build() //
				.baseConfiguration(baseConfiguration) //
				.enrich(viewRepositoryConfiguration.getEnrichments()) //
				.done(); 
			
			List<CompiledDependencyIdentification> terminals = new ArrayList<>(viewRepositoryConfiguration.getViews().size());
			
			List<Reason> reasons = null;
			
			for (String terminalAsStr: viewRepositoryConfiguration.getViews()) {
				try {
					CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(terminalAsStr);
					terminals.add(cdi);
				}
				catch (Exception e) {
					Reason parseProblem = Reasons.build(MalformedDependency.T).text(e.getMessage()).toReason();
					
					if (reasons == null)
						reasons = new ArrayList<>();
					
					reasons.add(parseProblem);
				}
			}
			
			if (reasons != null) {
				throw new ReasonException(Reasons.build(InvalidRepositoryConfiguration.T).causes(reasons).text("Could not compile ViewRepositoryConfiguration").toReason());
			}
			
			Maybe<RepositoryViewResolutionResult> resultMaybe = repositoryViewResolver.resolveRepositoryViews(resolutionContext, terminals);
			
			if (resultMaybe.isUnsatisfied()) {
				throw new ReasonException(Reasons.build(InvalidRepositoryConfiguration.T).cause(resultMaybe.whyUnsatisfied()).text("Could not compile ViewRepositoryConfiguration").toReason());
			}
			
			RepositoryViewResolutionResult repositoryViewResolutionResult = resultMaybe.get();
			
			var mergedConfig = repositoryViewResolutionResult.getMergedRepositoryConfiguration();
			
			TemplateReasonBuilder<ViewRepositoryConfigurationCompiled> reasonBuilder = TemplateReasons.build(ViewRepositoryConfigurationCompiled.T)
																							.assign(ViewRepositoryConfigurationCompiled::setTimestamp, new Date())
																							.assign(ViewRepositoryConfigurationCompiled::setAgent, "view repository-configuration compiler");
			
			Optional.ofNullable(repositoryConfiguration.getOrigination()).ifPresent(reasonBuilder::cause);
			
			mergedConfig.setOrigination(reasonBuilder.toReason());
			
			Maybe<RepositoryConfiguration> evaluatedConfigMaybe = new RawRepositoryConfigurationEvaluator(virtualEnvironment).evaluate(mergedConfig, file);
			
			if (evaluatedConfigMaybe.isUnsatisfied())
				return evaluatedConfigMaybe.whyUnsatisfied().asMaybe();
			
			return Maybe.complete(new CompiledRepositoryConfiguration( //
					evaluatedConfigMaybe.get(), //
					repositoryViewResolutionResult.getRepositoryViewResolution()));
		}
	}

	private RepositoryConfiguration extractBaseConfiguration(ViewRepositoryConfiguration viewRepositoryConfiguration) {
		var baseConfiguration = viewRepositoryConfiguration.getBaseConfiguration();
		
		if (baseConfiguration == null) {
			baseConfiguration = RepositoryConfiguration.T.create();
			
			for (Property property: RepositoryConfiguration.T.getProperties()) {
				property.set(baseConfiguration, property.get(viewRepositoryConfiguration));
			}
		}
		else {
			baseConfiguration = cloneDown(baseConfiguration, RepositoryConfiguration.T);
		}
		
		return baseConfiguration;
	}
	
	private <T extends GenericEntity, T1 extends T> T cloneDown(T1 instance, EntityType<T> toType) {
		T cloned = toType.createRaw();
		
		for (Property property: toType.getProperties()) {
			property.setDirectUnsafe(cloned, property.getDirectUnsafe(instance));
		}
		
		return cloned;
	}
	
	record CompiledRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration, RepositoryViewResolution repositoryViewResolution) {}
	
}
