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
