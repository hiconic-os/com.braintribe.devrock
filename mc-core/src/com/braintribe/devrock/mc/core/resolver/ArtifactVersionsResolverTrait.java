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
package com.braintribe.devrock.mc.core.resolver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactMetaDataResolver;
import com.braintribe.devrock.mc.api.resolver.ArtifactVersionsResolver;
import com.braintribe.devrock.model.mc.reason.MalformedMavenMetadata;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependencyVersion;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.version.Version;

public interface ArtifactVersionsResolverTrait extends ArtifactVersionsResolver,ArtifactMetaDataResolver {
	
	@Override
	default Maybe<List<VersionInfo>> getVersionsReasoned(ArtifactIdentification artifactIdentification) {
		Maybe<ArtifactDataResolution> resolveMetadata = resolveMetadata(artifactIdentification);
		
		if (resolveMetadata.isUnsatisfied()) {
			Reason reason = resolveMetadata.whyUnsatisfied();
			
			if (reason instanceof NotFound) {
				return Maybe.complete(Collections.emptyList());
			}
			else
				return reason.asMaybe();
		}
		
		ArtifactDataResolution resolution = resolveMetadata.get();
		try (InputStream in = resolution.getResource().openStream() ) {
			Maybe<MavenMetaData> mdMaybe = (Maybe<MavenMetaData>)(Maybe<?>) DeclaredMavenMetaDataMarshaller.INSTANCE.unmarshallReasoned(in);
			if (mdMaybe.isUnsatisfied())
				return TemplateReasons.build(MalformedMavenMetadata.T).assign(MalformedMavenMetadata::setMetadata, artifactIdentification.asString()).cause(mdMaybe.whyUnsatisfied()).toMaybe();

			MavenMetaData md = mdMaybe.get();
			
			Versioning versioning = md.getVersioning();
			if (versioning == null) {
				return Maybe.complete(Collections.emptyList());
			}
			List<VersionInfo> versions = new ArrayList<>();
			for (Version version : versioning.getVersions()) {
				VersionInfo versionInfo = new VersionInfo() {
					
					@Override
					public Version version() {
						return version;
					}					
					@Override
					public List<String> repositoryIds() {
						return Collections.singletonList(resolution.repositoryId());
					}
				};
				versions.add( versionInfo);
			}
			// ascending sort 
			versions.sort( (v1,v2) -> v1.version().compareTo(v2.version()));
			return Maybe.complete(versions);
		}
		catch (NoSuchElementException e) {
			return Maybe.complete(Collections.emptyList());
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "error while processing metadata for [" + artifactIdentification.asString() + "] from [" + resolution.repositoryId() + "]");
		}
						
	}
	
	@Override
	default List<VersionInfo> getVersions(ArtifactIdentification artifactIdentification) {
		return getVersionsReasoned(artifactIdentification).get();
	}

	
}
