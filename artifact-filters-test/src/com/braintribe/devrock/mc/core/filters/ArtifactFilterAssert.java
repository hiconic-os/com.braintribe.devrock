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
package com.braintribe.devrock.mc.core.filters;

import java.util.Arrays;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assert;

import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.testing.junit.assertions.assertj.core.api.SharedAssert;

/**
 * {@link Assert} implementation for {@link ArtifactFilter} assertions. The respective {@link ArtifactFilterExpert}s are
 * created in the background using {@link ArtifactFilters#forDenotation(ArtifactFilter)}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class ArtifactFilterAssert extends AbstractObjectAssert<ArtifactFilterAssert, ArtifactFilter>
		implements SharedAssert<ArtifactFilterAssert, ArtifactFilter> {

	private ArtifactFilterExpert expert;

	public ArtifactFilterAssert(ArtifactFilter actual) {
		super(actual, ArtifactFilterAssert.class);
		this.expert = ArtifactFilters.forDenotation(actual);
	}

//	public ArtifactFilterAssert matchesAllGroups(String groupIds) {
//		for (String groupId : Arrays.asList(groupIds)) {
//			if (!expert.matchesGroup(groupId)) {
//				failWithMessage("Expert " + expert + " unexpectedly does not match group " + groupId + "!");
//			}
//		}
//		return this;
//	}
//	
//	public ArtifactFilterAssert matchesNoGroups(String groupIds) {
//		for (String groupId : Arrays.asList(groupIds)) {
//			if (expert.matchesGroup(groupId)) {
//				failWithMessage("Expert " + expert + " unexpectedly matches group " + groupId + "!");
//			}
//		}
//		return this;
//	}	
	
	public ArtifactFilterAssert matchesAll(Object... identifications) {
		for (Object identification : Arrays.asList(identifications)) {
			if (!matches(identification)) {
				failWithMessage("Expert " + expert + " unexpectedly does not match " + identification + "!");
			}
		}
		return this;
	}

	public ArtifactFilterAssert matchesNone(Object... identifications) {
		for (Object identification : Arrays.asList(identifications)) {
			if (matches(identification)) {
				failWithMessage("Expert " + expert + " unexpectedly matches " + identification + "!");
			}
		}
		return this;
	}
		
	private boolean matches(Object identification) {
		boolean matches;
		if (identification instanceof String) {
			matches = expert.matchesGroup((String) identification);
		} else if (CompiledPartIdentification.T.isInstance(identification)) {
			matches = expert.matches((CompiledPartIdentification) identification);
		} else if (CompiledArtifactIdentification.T.isInstance(identification)) {
			matches = expert.matches((CompiledArtifactIdentification) identification);
		} else {
			matches = expert.matches((ArtifactIdentification) identification);
		}
		return matches;
	}
}
