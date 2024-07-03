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
package com.braintribe.devrock.mc.core.declared;



import java.io.BufferedInputStream;
import java.io.InputStream;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactPartResolver;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactIdentificationAssocResolver;
import com.braintribe.devrock.model.mc.reason.DeclaredArtifactReadError;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;

/**
 *
 * @author pit
 *
 */
public class DeclaredArtifactResolver implements CompiledArtifactIdentificationAssocResolver<DeclaredArtifact> {
	private ArtifactPartResolver partResolver;
	private DeclaredArtifactMarshaller marshaller;
	
	
	
	/**
	 * @param partResolver - the {@link ArtifactPartResolver} to use
	 */
	@Configurable @Required
	public void setPartResolver(ArtifactPartResolver partResolver) {
		this.partResolver = partResolver;
	}
	/**
	 * @param marshaller - the marshaller to decode {@link DeclaredArtifact}
	 */
	@Configurable @Required
	public void setMarshaller(DeclaredArtifactMarshaller marshaller) {
		this.marshaller = marshaller;
	}


	@Override
	public Maybe<DeclaredArtifact> resolve(CompiledArtifactIdentification artifactIdentification) {
		// TODO : the pom file is of interest, and should be 'written to'... requires the location of the file  	 
		Maybe<ArtifactDataResolution> optional = partResolver.resolvePart(artifactIdentification, PartIdentifications.pom, null);
		if (optional.isUnsatisfied()) {
			return optional.emptyCast();
		}
		ArtifactDataResolution artifactDataResolution = optional.get();
		//artifactDataResolution.writeTo( out);
		Resource resource = artifactDataResolution.getResource();
		
		Maybe<InputStream> maybeIn = artifactDataResolution.openStream();
		
		if (maybeIn.isUnsatisfied())
			return maybeIn.emptyCast();
		
		try (InputStream stream = new BufferedInputStream(maybeIn.get(), IOTools.SIZE_64K)) {
			Maybe<Object> declaredArtifactMaybe = marshaller.unmarshallReasoned( stream);
			if (declaredArtifactMaybe.isUnsatisfied()) {
				final String reasonText;
				
				if (resource instanceof FileResource) {
					FileResource fileResource = (FileResource)resource;
					reasonText = "Failed to read DeclaredArtifact for " + artifactIdentification.asString() + " from file " + fileResource.getPath();
				}
				else {
					reasonText = "Failed to read DeclaredArtifact for " + artifactIdentification.asString();
				}
				
				return Reasons.build(DeclaredArtifactReadError.T) //
						.text(reasonText) //
						.cause(declaredArtifactMaybe.whyUnsatisfied()).toMaybe();
			}
			
			DeclaredArtifact declaredArtifact = (DeclaredArtifact) declaredArtifactMaybe.get();
			declaredArtifact.setResource(resource);
			return Maybe.complete(declaredArtifact);
		}
		catch (Exception e) {
			throw Exceptions.uncheckedAndContextualize(e,  "error while resolving [" + artifactIdentification.asString() + "]", RuntimeException::new);
		}	
	}

}
