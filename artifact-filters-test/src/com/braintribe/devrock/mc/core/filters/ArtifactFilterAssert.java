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
