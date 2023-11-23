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
