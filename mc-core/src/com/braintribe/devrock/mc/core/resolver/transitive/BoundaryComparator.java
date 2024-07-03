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
package com.braintribe.devrock.mc.core.resolver.transitive;

import java.util.function.Function;

import com.braintribe.devrock.mc.api.transitive.BoundaryHit;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

public class BoundaryComparator implements Function<CompiledArtifactIdentification, BoundaryHit> {
	
	private boolean open;
	private ArtifactIdentification artifactIdentification;
	
	public BoundaryComparator(ArtifactIdentification artifactIdentification, boolean open) {
		super();
		this.artifactIdentification = artifactIdentification;
		this.open = open;
	}

	@Override
	public BoundaryHit apply(CompiledArtifactIdentification t) {
		if (
				artifactIdentification.getGroupId().equals(t.getGroupId()) && 
				artifactIdentification.getArtifactId().equals(t.getArtifactId()) 
			) {
			return open? BoundaryHit.open: BoundaryHit.closed;
		}
		return BoundaryHit.none;
	}

}
