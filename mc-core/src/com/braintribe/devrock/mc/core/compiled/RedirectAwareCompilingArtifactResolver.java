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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactIdentificationAssocResolver;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.model.mc.reason.RelocationCycle;
import com.braintribe.devrock.model.mc.reason.UnresolvedRelocation;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.declared.Relocation;

/**
 * a node resolver that handles redirects, i.e. artifacts pointing to another artifact
 * 
 * @author pit / dirk
 *
 */
public class RedirectAwareCompilingArtifactResolver implements CompiledArtifactResolver {
	private CompiledArtifactIdentificationAssocResolver<CompiledArtifact> delegate;
	
	@Configurable @Required
	public void setDelegate(CompiledArtifactIdentificationAssocResolver<CompiledArtifact> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public Maybe<CompiledArtifact> resolve(CompiledArtifactIdentification compiledArtifactIdentification) {
		
		Set<EqProxy<CompiledArtifactIdentification>> cycleCandidates = null;
		
		
		List<CompiledArtifactIdentification> relocationIdentifications = null; 
		
		while (true) {
			Maybe<CompiledArtifact> artifactMaybe = delegate.resolve(compiledArtifactIdentification);
			

			if (artifactMaybe.isUnsatisfied()) {
				Reason reason = artifactMaybe.whyUnsatisfied();
				
				if (relocationIdentifications != null) {
					for (CompiledArtifactIdentification relocation : relocationIdentifications) {
						reason = Reasons.build(UnresolvedRelocation.T).text( "could not resolve relocation " + relocation.asString()).cause( reason).toReason();					
					}
				}
				
				return Maybe.empty(reason);				
			}
			
			CompiledArtifact artifact = artifactMaybe.get();
			Relocation relocation = artifact.getRelocation();
			
			if (relocation == null)
				return Maybe.complete(artifact);
			
			if (relocationIdentifications == null) {
				relocationIdentifications = new ArrayList<>();
				relocationIdentifications.add(compiledArtifactIdentification);
			}
		
			Relocation sanitizedRelocation = Relocation.from(relocation, compiledArtifactIdentification.getGroupId(), compiledArtifactIdentification.getArtifactId(), compiledArtifactIdentification.getVersion().asString());
			
			// prepare relocation cycle check
			if (cycleCandidates == null) {
				cycleCandidates = new LinkedHashSet<>();
				cycleCandidates.add(HashComparators.compiledArtifactIdentification.eqProxy(compiledArtifactIdentification));
			}
			
			// create CAI from relocation
			compiledArtifactIdentification = CompiledArtifactIdentification.from( sanitizedRelocation);
			
			relocationIdentifications.add(compiledArtifactIdentification);
			
			// cycle check
			if (!cycleCandidates.add(HashComparators.compiledArtifactIdentification.eqProxy(compiledArtifactIdentification))) {
				return TemplateReasons.build(RelocationCycle.T).assign(RelocationCycle::setArtifacts, relocationIdentifications).toMaybe();
			}
		}
	}
}
