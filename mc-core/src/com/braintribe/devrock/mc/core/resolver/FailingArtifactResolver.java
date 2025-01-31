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
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.Version;

public class FailingArtifactResolver implements ReflectedArtifactResolver {

	private Reason reason;
	
	public FailingArtifactResolver(Reason reason) {
		this.reason = reason;
	}
	
	@Override
	public Maybe<List<VersionInfo>> getVersionsReasoned(ArtifactIdentification artifactIdentification) {
		return reason.asMaybe();
	}

	@Override
	public List<VersionInfo> getVersions(ArtifactIdentification artifactIdentification) {
		return getVersionsReasoned(artifactIdentification).get();
	}

	@Override
	public Maybe<ArtifactDataResolution> resolvePart(CompiledArtifactIdentification identification,
			PartIdentification partIdentification, Version partVersionOverride) {
		return reason.asMaybe();
	}
	
	@Override
	public List<PartReflection> getAvailablePartsOf(CompiledArtifactIdentification compiledArtifactIdentification) {
		return Collections.emptyList();
	}
	
	@Override
	public Maybe<List<PartReflection>> getAvailablePartsOfReasoned(
			CompiledArtifactIdentification compiledArtifactIdentification) {
		return Maybe.complete(Collections.emptyList());
	}

}
