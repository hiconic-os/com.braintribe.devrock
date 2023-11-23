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

}
