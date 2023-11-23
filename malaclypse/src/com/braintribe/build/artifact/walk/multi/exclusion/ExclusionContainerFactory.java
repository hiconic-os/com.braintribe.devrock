// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.exclusion;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.artifact.Exclusion;
import com.braintribe.model.artifact.Identification;


/**
 * a factory for {@link ExclusionContainer} <br/>
 * if possible, it will create a fast {@link SetBasedExclusionContainer}, if any {@link Exclusion} contains a wild card for group or artifact, it will
 * generate a slower {@link ListBasedExclusionContainer} that supports the wild card on querying
 * @author Pit
 *
 */
public class ExclusionContainerFactory implements Function<Set<Exclusion>, ExclusionContainer>{

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ExclusionContainer apply(Set<Exclusion> exclusions) throws RuntimeException {
		
		boolean listBasedContainerRequired = false;
		// sanitize
		Iterator<Exclusion> iterator = exclusions.iterator();
		while (iterator.hasNext()) {
			Exclusion exclusion = iterator.next();		
			// exclusion itself might be null 
			if (exclusion == null) {
				iterator.remove();
				exclusion = Exclusion.T.create();
				exclusions.add(exclusion);
			}
			// might be null 
			String eGroup = exclusion.getGroupId();
			if (eGroup == null) {
				eGroup ="*";
				exclusion.setGroupId(eGroup);
			}
			// might be null 
			String eArtifact = exclusion.getArtifactId();
			if (eArtifact == null) {
				eArtifact="*";
				exclusion.setArtifactId( eArtifact);
			}
			
			if (			
					eGroup.equalsIgnoreCase("*") ||					
					eArtifact.equalsIgnoreCase("*")
				) {
				listBasedContainerRequired = true;
				break;
			}
		}
		ExclusionContainer container;
		if (listBasedContainerRequired) {
			container = new ListBasedExclusionContainer();
		}
		else {
			container = new SetBasedExclusionContainer();			
		}
		container.addAll((Set<Identification>)(Set)exclusions);
		return container;
	}

	
}
