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
package com.braintribe.devrock.mc.api.transitive;

import java.util.function.Function;

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

/**
 * a build range - basically a container for functions that can test boundary hits
 * @author pit / dirk
 *
 */
public interface BuildRange {
	static CompiledArtifactIdentification boundaryFloor = CompiledArtifactIdentification.create("<floor>", "<floor>", "0");
	Function<CompiledArtifactIdentification, BoundaryHit> lowerBound();
	Function<CompiledArtifactIdentification, BoundaryHit> upperBound();
	
	
	/**
	 * creates a build range of the two functions for the lower/upper bounds 
	 * @param lowerBound - a function to return {@link BoundaryHit} for lower bounds of the build range 
	 * @param upperBound - a function to return {@link BoundaryHit} for upper bounds of the build range
	 * @return - the new {@link BuildRange}
	 */
	static BuildRange of(Function<CompiledArtifactIdentification, BoundaryHit> lowerBound, Function<CompiledArtifactIdentification, BoundaryHit> upperBound) {
		return new BuildRange() {
			@Override
			public Function<CompiledArtifactIdentification, BoundaryHit> lowerBound() {
				return lowerBound;
			}
			
			@Override
			public Function<CompiledArtifactIdentification, BoundaryHit> upperBound() {
				return upperBound;
			}
		};
	}
}
