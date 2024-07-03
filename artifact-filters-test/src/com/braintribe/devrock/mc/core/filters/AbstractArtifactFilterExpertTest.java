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

import com.braintribe.devrock.model.repository.filters.AllMatchingArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ConjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.GroupsArtifactFilter;
import com.braintribe.devrock.model.repository.filters.LockArtifactFilter;
import com.braintribe.devrock.model.repository.filters.NegationArtifactFilter;
import com.braintribe.devrock.model.repository.filters.NoneMatchingArtifactFilter;
import com.braintribe.devrock.model.repository.filters.QualifiedArtifactFilter;
import com.braintribe.devrock.model.repository.filters.StandardDevelopmentViewArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.version.Version;

/**
 * Abstract super class for {@link ArtifactFilterExpert} tests.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public abstract class AbstractArtifactFilterExpertTest {

	/**
	 * There is no GroupIdentification entity type (yet) and thus this method just returns the passed groupId. Using
	 * this method improves consistency of the tests though. Plus we should probably anyway have GroupIdentification
	 * entity type in the future.
	 */
	protected static String gi(String groupId) {
		return groupId;
	}

	protected static ArtifactIdentification ai(String groupId, String artifactId) {
		ArtifactIdentification result = ArtifactIdentification.T.create();
		result.setGroupId(groupId);
		result.setArtifactId(artifactId);
		return result;
	}

	protected static CompiledArtifactIdentification cai(String groupId, String artifactId, String version) {
		CompiledArtifactIdentification result = CompiledArtifactIdentification.T.create();
		result.setGroupId(groupId);
		result.setArtifactId(artifactId);
		if (version != null) {
			result.setVersion(version(version));
		}
		return result;
	}

	protected static CompiledPartIdentification cpi(String groupId, String artifactId, String version, String classifier, String type) {
		CompiledPartIdentification result = CompiledPartIdentification.T.create();
		result.setGroupId(groupId);
		result.setArtifactId(artifactId);
		if (version != null) {
			result.setVersion(version(version));
		}
		result.setClassifier(classifier);
		result.setType(type);
		return result;
	}

	private static Version version(String versionString) {
		Version result = Version.T.create();
		if (versionString.matches("\\d+\\.\\d+\\.\\d+")) {
			String[] versionParts = versionString.split("\\.");
			result.setMajor(Integer.parseInt(versionParts[0]));
			result.setMinor(Integer.parseInt(versionParts[1]));
			result.setRevision(Integer.parseInt(versionParts[2]));
		} else {
			result.setAnonmalousExpression(versionString);
		}
		return result;
	}

	protected static QualifiedArtifactFilter qualified(String groupId, String artifactId, String version, String classifier, String type) {
		QualifiedArtifactFilter result = QualifiedArtifactFilter.T.create();
		result.setGroupId(groupId);
		result.setArtifactId(artifactId);
		result.setVersion(version);
		result.setClassifier(classifier);
		result.setType(type);
		return result;
	}

	protected static LockArtifactFilter locks(String... locks) {
		LockArtifactFilter result = LockArtifactFilter.T.create();
		result.getLocks().addAll(Arrays.asList(locks));
		return result;
	}

	protected static GroupsArtifactFilter groups(String... groups) {
		GroupsArtifactFilter result = GroupsArtifactFilter.T.create();
		result.getGroups().addAll(Arrays.asList(groups));
		return result;
	}

	protected static StandardDevelopmentViewArtifactFilter standardDevelopmentView(ArtifactFilter restrictionFilter,
			boolean restrictOnArtifactInsteadOfGroupLevel) {
		StandardDevelopmentViewArtifactFilter result = StandardDevelopmentViewArtifactFilter.T.create();
		result.setRestrictionFilter(restrictionFilter);
		result.setRestrictOnArtifactInsteadOfGroupLevel(restrictOnArtifactInsteadOfGroupLevel);
		return result;
	}

	protected static AllMatchingArtifactFilter all() {
		AllMatchingArtifactFilter result = AllMatchingArtifactFilter.T.create();
		return result;
	}

	protected static NoneMatchingArtifactFilter none() {
		NoneMatchingArtifactFilter result = NoneMatchingArtifactFilter.T.create();
		return result;
	}

	protected static ConjunctionArtifactFilter and(ArtifactFilter... operands) {
		ConjunctionArtifactFilter result = ConjunctionArtifactFilter.T.create();
		result.setOperands(Arrays.asList(operands));
		return result;
	}

	protected static DisjunctionArtifactFilter or(ArtifactFilter... operands) {
		DisjunctionArtifactFilter result = DisjunctionArtifactFilter.T.create();
		result.setOperands(Arrays.asList(operands));
		return result;
	}

	protected static NegationArtifactFilter not(ArtifactFilter operand) {
		NegationArtifactFilter result = NegationArtifactFilter.T.create();
		result.setOperand(operand);
		return result;
	}
}
