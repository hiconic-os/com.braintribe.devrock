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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.braintribe.devrock.mc.api.view.RepositoryViewResolutionContext;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolutionResult;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolver;
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
import com.braintribe.gm.model.reason.ReasonBuilder;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.reason.TemplateReasonBuilder;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.utils.lcd.LazyInitialization;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class ViewRepositoryConfigurationCompiler  {
	private RepositoryConfiguration repositoryConfiguration;
	
	private LazyInitialization lazyInitialization = new LazyInitialization(this::compile);
	private RepositoryConfiguration compiledConfiguration;
	private RepositoryViewResolution repositoryViewResolution;
	private VirtualEnvironment virtualEnvironment;

	public ViewRepositoryConfigurationCompiler(Maybe<RepositoryConfiguration> repositoryConfigurationMaybe, VirtualEnvironment ve) {
		super();
		if (repositoryConfigurationMaybe.isUnsatisfied()) {
			this.repositoryConfiguration = RepositoryConfiguration.T.create();
			repositoryConfiguration.setFailure(repositoryConfigurationMaybe.whyUnsatisfied());
		}
		else {
			this.repositoryConfiguration = repositoryConfigurationMaybe.get();
		}
		
		this.virtualEnvironment = ve;
		
	}
	
	private <T extends GenericEntity, T1 extends T> T cloneDown(T1 instance, EntityType<T> toType) {
		T cloned = toType.createRaw();
		
		for (Property property: toType.getProperties()) {
			property.setDirectUnsafe(cloned, property.getDirectUnsafe(instance));
		}
		
		return cloned;
	}
	
	private synchronized void compile() {
		if (repositoryConfiguration instanceof ViewRepositoryConfiguration) {
			
			ViewRepositoryConfiguration viewRepositoryConfiguration = (ViewRepositoryConfiguration)repositoryConfiguration;
			RepositoryConfiguration viewRepositoryConfigurationForResolving = cloneDown(viewRepositoryConfiguration, RepositoryConfiguration.T);
			
			try (WireContext<RepositoryViewResolutionContract> wireContext = Wire.context(new RepositoryViewResolutionWireModule(viewRepositoryConfigurationForResolving, virtualEnvironment))) {
				RepositoryViewResolver repositoryViewResolver = wireContext.contract().repositoryViewResolver();
				RepositoryViewResolutionContext resolutionContext = RepositoryViewResolutionContext.build() //
					.baseConfiguration(viewRepositoryConfiguration.getBaseConfiguration()) //
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
				
				compiledConfiguration = repositoryViewResolutionResult.getMergedRepositoryConfiguration();
				repositoryViewResolution = repositoryViewResolutionResult.getRepositoryViewResolution();
				
				TemplateReasonBuilder<ViewRepositoryConfigurationCompiled> reasonBuilder = TemplateReasons.build(ViewRepositoryConfigurationCompiled.T)
																								.assign(ViewRepositoryConfigurationCompiled::setTimestamp, new Date())
																								.assign(ViewRepositoryConfigurationCompiled::setAgent, "view repository-configuration compiler");
				
				Optional.ofNullable(repositoryConfiguration.getOrigination()).ifPresent(reasonBuilder::cause);
				
				compiledConfiguration.setOrigination(reasonBuilder.toReason());
			}
		}
		else {
			compiledConfiguration = repositoryConfiguration.clone(new StandardCloningContext());
		}
	}
	
	public RepositoryConfiguration repositoryConfiguration() {
		lazyInitialization.run();
		return compiledConfiguration; 
	}
	
	public RepositoryViewResolution repositoryViewResolution() {
		lazyInitialization.run();
		return repositoryViewResolution;
	}
	
	
}
