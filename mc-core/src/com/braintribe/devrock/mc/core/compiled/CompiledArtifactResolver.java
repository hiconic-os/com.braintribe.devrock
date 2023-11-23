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
package com.braintribe.devrock.mc.core.compiled;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.compiled.DeclaredArtifactCompilingNode;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactIdentificationAssocResolver;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactResolver;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

/**
 * a basic implementation of the {@link CompiledArtifactResolver}
 * @author pit/dirk
 *
 */
public class CompiledArtifactResolver implements CompiledArtifactIdentificationAssocResolver<CompiledArtifact> {	
	private CompiledArtifactIdentificationAssocResolver<DeclaredArtifactCompilingNode> compilingNodeResolver;
			
	/**
	 * @param declaredArtifactResolver - the {@link DeclaredArtifactResolver} to use
	 */
	@Configurable @Required
	public void setCompilingNodeResolver( CompiledArtifactIdentificationAssocResolver<DeclaredArtifactCompilingNode> compilingNodeResolver) {
		this.compilingNodeResolver = compilingNodeResolver;
	}
	
	@Override
	public Maybe<CompiledArtifact> resolve(CompiledArtifactIdentification artifactIdentification) {		  				
		Maybe<DeclaredArtifactCompilingNode> node = compilingNodeResolver.resolve(artifactIdentification);
		if (node.isSatisfied()) {
			return Maybe.complete( node.get().getCompiledArtifact(null));
		}
		else {
			return node.emptyCast();
		}
	}
}
