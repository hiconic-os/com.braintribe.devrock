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
