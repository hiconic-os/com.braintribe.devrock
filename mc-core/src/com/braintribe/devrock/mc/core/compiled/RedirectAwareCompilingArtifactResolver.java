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
