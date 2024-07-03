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
package com.braintribe.devrock.zarathud.extracter.filter;

import java.util.function.Predicate;

import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.Artifact;

public class StandardFilter implements Predicate<AbstractEntity> {
	
	private Artifact artifact;
	
	public StandardFilter(Artifact owner) {
		artifact = owner;
	}

	@Override
	public boolean test(AbstractEntity entity) {
		if (entity instanceof AbstractClassEntity) {
			if ( ((AbstractClassEntity) entity).getParameterNature() )
			return false;
		}
		return ArtifactMatcher.matchArtifactTo(artifact, entity.getArtifact());
	}

	
	
}
