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
