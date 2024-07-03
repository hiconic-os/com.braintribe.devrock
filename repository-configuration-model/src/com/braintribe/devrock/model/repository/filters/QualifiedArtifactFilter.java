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
package com.braintribe.devrock.model.repository.filters;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * An {@link ArtifactFilter} that is based on multiple properties to match {@link #getGroupId() group id},
 * {@link #getArtifactId() artifact id}, {@link #getClassifier() classifier} and {@link #getType() type}, each
 * supporting wildcards (i.e. <code>*</code>), as well as a {@link #getVersion() version expression} to match artifact
 * version(s).
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public interface QualifiedArtifactFilter extends ArtifactFilter {

	EntityType<QualifiedArtifactFilter> T = EntityTypes.T(QualifiedArtifactFilter.class);

	String groupId = "groupId";
	String artifactId = "artifactId";
	String version = "version";
	String classifier = "classifier";
	String type = "type";

	/**
	 * A regular expression used to match group id(s).
	 */
	String getGroupId();
	void setGroupId(String groupId);

	/**
	 * A regular expression used to match artifact id(s).
	 */
	String getArtifactId();
	void setArtifactId(String artifactId);

	/**
	 * A version or version expression (see <code>com.braintribe.model.version.VersionExpression</code>) used to match
	 * version(s).
	 */
	String getVersion();
	void setVersion(String version);

	/**
	 * A regular expression used to match classifier(s).
	 */
	String getClassifier();
	void setClassifier(String classifier);

	/**
	 * A regular expression used to match type(s).
	 */
	String getType();
	void setType(String type);
}
