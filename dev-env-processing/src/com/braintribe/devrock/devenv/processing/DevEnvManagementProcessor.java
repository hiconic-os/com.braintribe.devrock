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
package com.braintribe.devrock.devenv.processing;

import static com.braintribe.console.ConsoleOutputs.brightGreen;
import static com.braintribe.console.ConsoleOutputs.brightYellow;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.devrock.devenv.processing.eclipse.EclipseLocation;
import com.braintribe.devrock.env.api.DevEnvironment;
import com.braintribe.devrock.model.devenv.api.DevEnvManagementRequest;
import com.braintribe.devrock.model.devenv.api.FixEclipseProjectLocations;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.utils.paths.UniversalPath;

public class DevEnvManagementProcessor extends AbstractDispatchingServiceProcessor<DevEnvManagementRequest, Object> {
	
	@Override
	protected void configureDispatching(DispatchConfiguration<DevEnvManagementRequest, Object> dispatching) {
		dispatching.registerReasoned(FixEclipseProjectLocations.T, this::fixEclipseProjectLocations);
	}
	
	private Maybe<Neutral> fixEclipseProjectLocations(ServiceRequestContext context, FixEclipseProjectLocations request) {
		DevEnvironment devEnvironment = context.findOrNull(DevEnvironment.class);
		
		if (devEnvironment == null)
			return Reasons.build(NotFound.T).text("Could not find a development environment for current working directory").toMaybe();

		File devEnvFolder = devEnvironment.getRootPath();
		File eclipseProjectsFolder = devEnvironment.resolveRelativePath("eclipse-workspace/.metadata/.plugins/org.eclipse.core.resources/.projects").getAbsoluteFile();
		String devEnvPath = UniversalPath.from(eclipseProjectsFolder).toSlashPath();
		
		Pattern pattern = Pattern.compile("(URI\\/\\/file\\:\\/)(.*)(\\/git\\/.*)");
		
		for (File file: devEnvFolder.listFiles()) {
			
			if (file.isFile())
				continue;
			
			File locationFile = new File(file, ".location");
			
			if (!locationFile.exists())
				continue;
			
			EclipseLocation eclipseLocation = new EclipseLocation(locationFile);
			
			String uri = eclipseLocation.getUri();
			
			Matcher matcher = pattern.matcher(uri);
			
			if (matcher.matches()) {
				String relativePath = matcher.group(3);
				
				String fixedUri = "URI//file:/" + devEnvPath + relativePath;
				System.out.println(fixedUri);
				
				eclipseLocation.changeUri(fixedUri);
				eclipseLocation.write(locationFile);
				
				ConsoleOutputs.println(sequence(
						text("Fixed project "),
						brightYellow(file.getName()),
						text(" to uri "),
						brightGreen(fixedUri)
				));
			}
		}
		
		return Maybe.complete(Neutral.NEUTRAL);
	}
}
