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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.buildrange;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.transitive.BoundaryHit;
import com.braintribe.devrock.mc.api.transitive.BuildRange;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

/**
 * helper class to handle the boundaries within the tests 
 * @author pit
 *
 */
public class HitExpert {
	private Map<EqProxy<CompiledArtifactIdentification>, BoundaryHit> map = new HashMap<>();
	
	public HitExpert() {		
	}
	
	public HitExpert(CompiledArtifactIdentification cai, BoundaryHit hit) {
		if (cai != null) {
			map.put( HashComparators.compiledArtifactIdentification.eqProxy(cai), hit);
		}
	}
	
	@SuppressWarnings("unchecked")
	public HitExpert(Pair<CompiledArtifactIdentification, BoundaryHit> ... pairs) {
		Stream.of( pairs).forEach( p -> map.put( HashComparators.compiledArtifactIdentification.eqProxy(p.first), p.second));				
	}
	
	
	BoundaryHit hit(CompiledArtifactIdentification cai) {
		// if boundary floor is asked, return BoundaryHit.open
		if (cai.compareTo( BuildRange.boundaryFloor) == 0) {
			return BoundaryHit.open;
		}
		
		if (map.isEmpty()) {
			return BoundaryHit.none;			
		}
		
		EqProxy<CompiledArtifactIdentification> key = HashComparators.compiledArtifactIdentification.eqProxy(cai);
		BoundaryHit hit = map.get(key);
		if (hit != null) {
			return hit;
		}
		
		return BoundaryHit.none;
	}
}
