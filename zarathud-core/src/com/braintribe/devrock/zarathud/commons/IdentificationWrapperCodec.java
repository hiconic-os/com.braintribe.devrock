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
package com.braintribe.devrock.zarathud.commons;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.Identification;

/**
 * wrapper codec for the {@link CodingSet} or {@link CodingMap} based structures with {@link Identification} 
 * @author Pit
 *
 */
public class IdentificationWrapperCodec extends HashSupportWrapperCodec<Identification> {
	
	public IdentificationWrapperCodec() {
		super(true);
	}

	@Override
	protected int entityHashCode(Identification e) {
		return (e.getGroupId() + ":" + e.getArtifactId()).hashCode();
	}

	@Override
	protected boolean entityEquals(Identification e1, Identification e2) {
		if (!e1.getGroupId().equalsIgnoreCase(e2.getGroupId()))
			return false;
		if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
			return false;
		return true;
	}

}
