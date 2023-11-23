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
package com.braintribe.model.artifact.processing.artifact;

import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

/**
 * a helper class to deal with solutions.. 
 * @author Pit
 *
 */
public class SolutionProcessor {
	
	private static Logger log = Logger.getLogger(SolutionProcessor.class);

	/**
	 * get the matching {@link Part} from a {@link Solution} as defined by {@link PartTuple}
	 * @param solution - the {@link Solution}
	 * @param partTuple - the {@link PartTuple} that describes the {@link Part}
	 * @return - the {@link Part} or null
	 */
	public static Part getPart( Solution solution, PartTuple partTuple) {		
		for (Part part : solution.getParts()) {
			if (PartTupleProcessor.equals( part.getType(), partTuple))
				return part;
		}
		return null;		
	}
	
	
	/**
	 * get the matching {@link Part} from a {@link Solution} as defined by the {@link PartType}
	 * @param solution - the {@link Solution}
	 * @param partType - the {@link PartType}
	 * @return - the {@link Part} or null 
	 */
	public static Part getPartLike( Solution solution, PartType partType) {		
			for (Part part : solution.getParts()) {					
				
				PartType suspectPartType = PartTupleProcessor.toPartType( part.getType());
				if (suspectPartType != null) {
					if (partType == suspectPartType) {					
						return part;
					}
				}
				else {
					String msg = "cannot extract part type from part tuple [" + PartTupleProcessor.toString( part.getType()) + "]";
					log.warn( msg);
				}							
			}
			return null;			
	}
	
	public static String toString(Solution s) {
		String version = VersionProcessor.toString(s.getVersion());
		return s.getGroupId() + ":" + s.getArtifactId() + "#" + version;
	}

}
