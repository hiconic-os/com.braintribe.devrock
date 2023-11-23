// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.exclusion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.braintribe.model.artifact.Identification;

/**
 * a container for exclusions that may contain wild cards, such as in this maven 3 construct:
 * {@code
 	<exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
    }
 * <br/>
 * any "*" are turned into regexp patterns ".*", and containment is checked by iteration.
 * 
 * @author Pit
 *
 */
public class ListBasedExclusionContainer implements ExclusionContainer {
	private List<Identification> container = new ArrayList<Identification>();

	@Override
	public boolean contains(Identification identification) {
		for (Identification suspect : container) {
			if (
					identification.getGroupId().matches( suspect.getGroupId()) &&
					identification.getArtifactId().matches( suspect.getArtifactId())
				) {
				return true;
			}
		}		
		return false;
	}

	@Override
	public void addAll(Set<Identification> identifications) {
		for (Identification suspect : identifications) {	
			
			suspect.setGroupId( expandToRegExp( suspect.getGroupId()));
			suspect.setArtifactId( expandToRegExp( suspect.getArtifactId()));
			container.add( suspect);
		}		
	}
	
	/**
	 * expand value passed to a regular expression, ie. any point "." is escaped to "\.", any "*" is replaced by ".*"	 * 
	 */
	private String expandToRegExp( String expression) {	
		if (expression == null) {
			return ".*";
		}
		String result = expression.replace(".", "\\.");
		return result.replace( "*", ".*");
	}

	
}
