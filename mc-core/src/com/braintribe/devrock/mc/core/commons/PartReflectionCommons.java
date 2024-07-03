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
package com.braintribe.devrock.mc.core.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;

/**
 * helper for {@link PartReflection}
 * @author pit
 *
 */
public class PartReflectionCommons {
	private static Logger log = Logger.getLogger(PartReflectionCommons.class);
	
	/**
	 * transposes the files names into {@link CompiledPartIdentification}
	 * @param compiledArtifactIdentification - the {@link CompiledArtifactIdentification} of which need to get the parts
	 * @param files - a {@link List} of the file names as {@link String}
	 * @return - 
	 */
	public static List<PartReflection> transpose(CompiledArtifactIdentification compiledArtifactIdentification, String repositoryId, List<String> files) {
		List<PartReflection> result = new ArrayList<>( files.size());
		String prefix = compiledArtifactIdentification.getArtifactId() + "-" + compiledArtifactIdentification.getVersion().asString();
		for (String file : files) {
			if (file.startsWith("/")) { 
				file = file.substring(1);
			}
			if (file.equals("maven-metadata.xml")) {
				continue;
			}
					
			if (!file.startsWith( prefix)) {
				log.warn("file [" + file + "] associated with [" + compiledArtifactIdentification.asString() + "] is not a valid part as it doesn't follow the naming scheme");
				continue;
			}
					 
			String suffix = file.substring( prefix.length());
			int dot = suffix.indexOf( '.');
			String extension = suffix.substring( dot + 1);		
			String rem = suffix.substring(0, dot);
			
			// check classifier
			String classifier = null;
			if (suffix.startsWith( "-")) {
				classifier = rem.substring(1);				
			}
			PartReflection pf = PartReflection.create( classifier, extension, repositoryId);			
			result.add( pf);
		}
		return result;
	}
	/**
	 * @param parts
	 * @param repositoryId
	 * @param repositoryRoot
	 * @return
	 */
	public static List<PartReflection> transpose(Collection<CompiledPartIdentification> parts, String repositoryId) {			
		return parts.stream().map( p -> PartReflection.from(p, repositoryId)).collect( Collectors.toList());		
	}

}
