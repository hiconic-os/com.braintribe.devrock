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
		Maybe<InputStream> inMaybe = resolution.openStream();
		
		if (inMaybe.isUnsatisfiedBy(NotFound.T))
			return Maybe.complete(Collections.emptyList());
		
		if (inMaybe.isUnsatisfied())
			return inMaybe.whyUnsatisfied().asMaybe();
		
		try (InputStream in = inMaybe.get()) {
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
		catch (Exception e) {
			throw Exceptions.unchecked(e, "error while processing metadata for [" + artifactIdentification.asString() + "] from [" + resolution.repositoryId() + "]");
		}
	}
	
	@Override
	default List<VersionInfo> getVersions(ArtifactIdentification artifactIdentification) {
		return getVersionsReasoned(artifactIdentification).get();
	}

	
}
