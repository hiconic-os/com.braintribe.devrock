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
