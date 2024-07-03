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
package com.braintribe.devrock.env.processing;

import java.io.File;

import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.common.attribute.common.CallerEnvironment;
import com.braintribe.devrock.env.api.DevEnvironment;
import com.braintribe.devrock.env.impl.BasicDevEnvironment;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.api.VirtualEnvironmentAttribute;
import com.braintribe.ve.impl.ContextualizedVirtualEnvironment;

/**
 * TODO: create com.braintribe.devrock:dev-env-deployment-model and add DevEnvAware Metadata to it and configure it manually in Jinni on the relevant request and make this intereceptor using it
 * @author Dirk Scheffler
 *
 */
public class DevEnvAwareInterceptor implements ServiceAroundProcessor<ServiceRequest, Object> {
	public static final DevEnvAwareInterceptor INSTANCE = new DevEnvAwareInterceptor();
	private static interface DevEnvironmentProbed extends TypeSafeAttribute<Boolean> { /* noop */ }
	
	@Override
	public Object process(ServiceRequestContext requestContext, ServiceRequest request, ProceedContext proceedContext) {
		if (requestContext.findAttribute(DevEnvironmentProbed.class).orElse(false))
			return proceedContext.proceed(request);
		
		File currentWorkingDirectory = CallerEnvironment.getCurrentWorkingDirectory();
		File devEnvRoot = null;
		
		final ServiceRequestContext enrichedContext;
		
		if ((devEnvRoot = hasDevEnvParent(currentWorkingDirectory)) != null) {
			UniversalPath artifactsRoot = UniversalPath.start(devEnvRoot.getAbsolutePath()).push("artifacts");
			
			File exclusiveSettingsPath = artifactsRoot.push("settings.xml").toFile();
			File jsLibraryPath = artifactsRoot.push("js-libraries").toFile();
			
			VirtualEnvironment virtualEnvironment = ContextualizedVirtualEnvironment.deriveEnvironment(ve -> {
				if (exclusiveSettingsPath.exists())
					ve.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", exclusiveSettingsPath.getPath());
				if (jsLibraryPath.exists())
					ve.setEnv("DEVROCK_JS_LIBRARIES", jsLibraryPath.getPath());
			});
			
			enrichedContext = requestContext.derive() //
				.set(VirtualEnvironmentAttribute.class, virtualEnvironment) //
				.set(DevEnvironment.class, new BasicDevEnvironment(devEnvRoot)) //
				.set(DevEnvironmentProbed.class, true) //
				.build();
		}
		else {
			enrichedContext = requestContext.derive() //
				.set(DevEnvironmentProbed.class, true) //
				.build();
		}
		
		return proceedContext.proceed(enrichedContext, request);
	}

	private File hasDevEnvParent(File currentWorkingDirectory) {
		File file = new File(currentWorkingDirectory, "dev-environment.yaml");
		
		if (file.exists()) {
			return currentWorkingDirectory;
		}
		else {
			File parentFolder = currentWorkingDirectory.getParentFile();
			
			if (parentFolder != null) {
				return hasDevEnvParent(parentFolder);
			}
			else {
				return null;
			}
		}
	}
}
