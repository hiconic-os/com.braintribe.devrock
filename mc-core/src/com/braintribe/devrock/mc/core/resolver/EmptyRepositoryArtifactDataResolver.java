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

import java.util.Collections;
import java.util.List;

import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.model.mc.reason.UnresolvedPart;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.Version;

/**
 * an {@link ArtifactDataResolver} that doesn't resolve squat
 * @author pit
 *
 */
public class EmptyRepositoryArtifactDataResolver implements ArtifactDataResolver {
	
	public static final EmptyRepositoryArtifactDataResolver instance = new EmptyRepositoryArtifactDataResolver();

	@Override
	public Maybe<ArtifactDataResolution> resolvePart(CompiledArtifactIdentification identification, PartIdentification partIdentification, Version partVersionOverride) {
		return Reasons.build(UnresolvedPart.T)
				.enrich( r -> r.setArtifact(identification))
				.enrich(r -> r.setPart(partIdentification))
				.toMaybe();
	}

	@Override
	public Maybe<List<VersionInfo>> getVersionsReasoned(ArtifactIdentification artifactIdentification) {
		return Reasons.build(NotFound.T).toMaybe();
	}
	
	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(ArtifactIdentification identification) {
		return Reasons.build(NotFound.T).toMaybe();
	}

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(CompiledArtifactIdentification identification) {
		return Reasons.build(NotFound.T).toMaybe();
	}

	@Override
	public List<VersionInfo> getVersions(ArtifactIdentification artifactIdentification) {	
		return Collections.emptyList();
	}

	@Override
	public Maybe<ArtifactDataResolution> getPartOverview( CompiledArtifactIdentification compiledArtifactIdentification) {
		return Reasons.build(NotFound.T).toMaybe();
	}

	@Override
	public List<PartReflection> getPartsOf(CompiledArtifactIdentification compiledArtifactIdentification) {	
		return Collections.emptyList();
	}
	
	
	
	

	
}
