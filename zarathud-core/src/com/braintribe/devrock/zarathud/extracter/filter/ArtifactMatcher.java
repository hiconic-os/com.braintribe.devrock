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
package com.braintribe.devrock.zarathud.extracter.filter;

import com.braintribe.model.zarathud.data.Artifact;

public class ArtifactMatcher {
	public static boolean matchArtifactTo( Artifact one, Artifact two) {
		if (one == two)
			return true;
		
		if (two == null) {
			// some java complex type leads to this
			return false;
		}
		
		return (
					one.getGroupId().equalsIgnoreCase(two.getGroupId()) &&
					one.getArtifactId().equalsIgnoreCase( two.getArtifactId()) &&
					one.getVersion().equalsIgnoreCase( two.getVersion())
			   );
	}
}
