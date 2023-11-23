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
package com.braintribe.devrock.mc.api.transitive;

import com.braintribe.model.artifact.analysis.AnalysisDependency;

/**
 * a node standing in for a dependency in the traversing protocol
 * @author pit / dirk
 * 
 *
 */
public interface DependencyPathElement extends ResolutionPathElement {
	@Override
	ArtifactPathElement getParent();
	/**
	 * @return - the dependency the node stands for
	 */
	AnalysisDependency getDependency();
	
	@Override
	default String asString() {
		return getDependency().asString();
	}
}
