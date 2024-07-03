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
package com.braintribe.devrock.mc.core.compiled;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.compiled.DeclaredArtifactCompilingNode;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactIdentificationAssocResolver;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactResolver;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

/**
 * a basic implementation of the {@link BasicCompiledArtifactResolver}
 * @author pit/dirk
 *
 */
public class BasicCompiledArtifactResolver implements CompiledArtifactResolver {	
	private CompiledArtifactIdentificationAssocResolver<DeclaredArtifactCompilingNode> compilingNodeResolver;
			
	/**
	 * @param declaredArtifactResolver - the {@link DeclaredArtifactResolver} to use
	 */
	@Configurable @Required
	public void setCompilingNodeResolver( CompiledArtifactIdentificationAssocResolver<DeclaredArtifactCompilingNode> compilingNodeResolver) {
		this.compilingNodeResolver = compilingNodeResolver;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.mc.core.compiled.CompiledArtifactResolver#resolve(com.braintribe.model.artifact.compiled.CompiledArtifactIdentification)
	 */
	@Override
	public Maybe<CompiledArtifact> resolve(CompiledArtifactIdentification artifactIdentification) {		  				
		Maybe<DeclaredArtifactCompilingNode> node = compilingNodeResolver.resolve(artifactIdentification);
		if (node.isUnsatisfied()) {
			return node.emptyCast();
		}
		return Maybe.complete( node.get().getCompiledArtifact(null));					
	}

}
