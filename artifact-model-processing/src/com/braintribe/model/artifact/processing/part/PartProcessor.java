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
package com.braintribe.model.artifact.processing.part;

import java.util.Collection;

import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;

/**
 * @author pit
 *
 */
public class PartProcessor {
	
	public static boolean contains( Collection<Part> parts, Part suspect){
		for (Part part : parts) {
			if (equals( part, suspect))
				return true;
		}
		return false;
	}
	
	public static boolean equals( Part one, Part two) {
		if (one.getGroupId().equalsIgnoreCase( two.getGroupId()) == false)
			return false;
		if (one.getArtifactId().equalsIgnoreCase( two.getArtifactId()) == false)
			return false;
		
		if (VersionProcessor.matches( one.getVersion(), two.getVersion()) == false)
			return false;		
		return PartTupleProcessor.equals(one.getType(), two.getType()); 	
	}
	

	public static Part createPartFromPart( Part part, PartTuple type) {
		Part sibling = ArtifactProcessor.createPartFromIdentification( part, part.getVersion(), type);		
		return sibling;
	}
	
	public static Part createPartFromPart( Part part, PartTuple type, Version version) {
		Part sibling = ArtifactProcessor.createPartFromIdentification( part, version, type);
		return sibling;
	}
	
	public static Part createPartFromIdentification( Identification identification, Version version, PartTuple tuple) {
		Part sibling = ArtifactProcessor.createPartFromIdentification(identification, version, tuple);
		return sibling;
	}
	
	
	
}
