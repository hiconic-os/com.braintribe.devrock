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
package com.braintribe.devrock.mc.core.declared;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.devrock.model.mc.reason.DeclaredArtifactReadError;
import com.braintribe.devrock.model.mc.reason.MalformedArtifactDescriptor;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.model.MergeContext;

public class DeclaredArtifactIdentificationExtractor {
	public static Maybe<CompiledArtifact> extractMinimalArtifact(DeclaredArtifact artifact) {
		DeclaredArtifactPropertyResolver declaredArtifactPropertyResolver = new DeclaredArtifactPropertyResolver(artifact);
		
		String explicitVersion = artifact.getVersion();
		String explicitGroupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		
		VersionedArtifactIdentification ai = VersionedArtifactIdentification.create(explicitGroupId, artifactId, explicitVersion);
		
		try {
			
			Optional<VersionedArtifactIdentification> parentRef = Optional.ofNullable(artifact.getParentReference());
			
			final String version;
			
			if (explicitVersion != null) {
				version = declaredArtifactPropertyResolver.resolve(explicitVersion);
			}
			else {
				String parentVersionAsStr = parentRef.map(VersionedArtifactIdentification::getVersion).orElse(null);
				
				if (parentVersionAsStr != null) {
					VersionExpression parentVersion = VersionExpression.parse(parentVersionAsStr);
					
					if (parentVersion instanceof Version) {
						version = parentVersionAsStr;
					}
					else {
						return Reasons.build(MalformedArtifactDescriptor.T).text("version neither present nor derivable from parent in: " + ai.asString()).toMaybe();
					}
				}
				else {
					return Reasons.build(MalformedArtifactDescriptor.T).text("version not present in: " + ai.asString()).toMaybe();
				}
			}
			
			String groupId = explicitGroupId != null? declaredArtifactPropertyResolver.resolve(explicitGroupId): 
				parentRef.map(VersionedArtifactIdentification::getGroupId).orElse(null);
			
			if (artifactId == null || groupId == null || version == null)
				return Reasons.build(MalformedArtifactDescriptor.T).text("invalid artifact identification: " + ai.asString()).toMaybe();
			
			artifactId = declaredArtifactPropertyResolver.resolve(artifact.getArtifactId());
			
			CompiledArtifact compiledArtifact = CompiledArtifact.T.create();
			compiledArtifact.setGroupId(groupId);
			compiledArtifact.setArtifactId(artifactId);
			compiledArtifact.setVersion(Version.parse(version));
			compiledArtifact.setPackaging(artifact.getPackaging());
			compiledArtifact.getProperties().putAll(artifact.getProperties());
			
			return Maybe.complete(compiledArtifact);
		} catch (Exception e) {
			return InternalError.from(e, "invalid artifact identification: " + ai.asString()).asMaybe();
		}
		
	}

	public static Maybe<CompiledArtifactIdentification> extractIdentification(DeclaredArtifact artifact) {
		// TODO why can't this method simply return this artifactPotential?
		Maybe<CompiledArtifact> artifactPotential = extractMinimalArtifact(artifact);
		
		if (artifactPotential.isUnsatisfied()) {
			return Maybe.empty(artifactPotential.whyUnsatisfied());
		}
		return artifactPotential.cast();

		// Original code was losing groupId information 

		// CompiledArtifact compiledArtifact = artifactPotential.get();
		//
		// return Potential.fill(CompiledArtifactIdentification.from(artifact, compiledArtifact.getVersion()));
	}

	public static Maybe<DeclaredArtifact> readArtifact(InputStreamProvider streamProvider, Object from) {
		try (InputStream in = new BufferedInputStream(streamProvider.openInputStream())) {
			return readArtifact(in, from);
		}
		catch (IOException e) {
			return InternalError.from(e, "Error while reading declared artifact from: " + from).asMaybe();
		}
	}
	
	public static Maybe<DeclaredArtifact> readArtifact(InputStream in, Object from) {
		try {
			Maybe<Object> declaredArtifactMaybe = DeclaredArtifactMarshaller.INSTANCE.unmarshallReasoned(in);
			
			if (declaredArtifactMaybe.isUnsatisfied()) {
				String text = from != null? //
						"Failed to read DeclaredArtifact from " + from: //
						"Failed to read DeclaredArtifact"; 
						
				return Reasons.build(DeclaredArtifactReadError.T) //
						.text(text) //
						.cause(declaredArtifactMaybe.whyUnsatisfied()).toMaybe();
			}
			
			return declaredArtifactMaybe.cast();
		}
		catch (Exception e) {
			return InternalError.from(e, "Error while reading declared artifact from: " + from).asMaybe();
		}
	}
	
	public static Maybe<CompiledArtifact> extractMinimalArtifact(InputStreamProvider streamProvider, Object from) {
		Maybe<DeclaredArtifact> declaredArtifactMaybe = readArtifact(streamProvider, from);
		
		if (declaredArtifactMaybe.isUnsatisfied()) {
			return Maybe.empty(declaredArtifactMaybe.whyUnsatisfied());
		}

		DeclaredArtifact declaredArtifact = declaredArtifactMaybe.get();
		Maybe<CompiledArtifact> potential = extractMinimalArtifact(declaredArtifact);
		
		if (potential.isSatisfied())
			return potential;
		
		Reason cause = potential.whyUnsatisfied();
		return Reasons.build(cause.entityType()).text("Malformed artifact descriptor read from: " + from).cause(cause).toMaybe();
	}
	
	public static Maybe<CompiledArtifact> extractMinimalArtifact(InputStreamProvider streamProvider) {
		return extractMinimalArtifact(streamProvider, "unidentified input stream");
	}
	
	
	public static Maybe<CompiledArtifact> extractMinimalArtifact(File file) {
		return extractMinimalArtifact(() -> new FileInputStream(file), file);
	}
	
	public static Maybe<CompiledArtifact> extractMinimalArtifact(Resource resource) {
		return extractMinimalArtifact(resource::openStream, resource);
	}

	
	public static Maybe<CompiledArtifactIdentification> extractIdentification(InputStreamProvider streamProvider, Object from) {
		Maybe<DeclaredArtifact> declaredArtifactMaybe = readArtifact(streamProvider, from);
		
		if (declaredArtifactMaybe.isUnsatisfied()) {
			return Maybe.empty(declaredArtifactMaybe.whyUnsatisfied());
		}
		
		return extractIdentification(declaredArtifactMaybe.get());
	}
	
	public static Maybe<CompiledArtifactIdentification> extractIdentification(InputStreamProvider streamProvider) {
		return extractIdentification(streamProvider, "unidentified input stream");
	}
	
	
	public static Maybe<CompiledArtifactIdentification> extractIdentification(File file) {
		return extractIdentification(() -> new FileInputStream(file), file);
	}
	
	public static Maybe<CompiledArtifactIdentification> extractIdentification(Resource resource) {
		return extractIdentification(resource::openStream, resource);
	}
	
	private static class DeclaredArtifactPropertyResolver {
		private final DeclaredArtifact artifact;

		public DeclaredArtifactPropertyResolver(DeclaredArtifact artifact) {
			super();
			this.artifact = artifact;
		}
		
		public String resolve(String s) {
			if (s == null || s.isEmpty())
				throw new IllegalArgumentException("cannot resolve null value");
			
			MergeContext mergeContext = new MergeContext();
			mergeContext.setVariableProvider(this::resolveProperty);
			Template template = Template.parse(s);
			return template.merge(mergeContext);
		}
		
		private String resolveProperty(String property) {
			String value = artifact.getProperties().get(property);
			
			return value != null? resolve(value): null;
		}
	}

}