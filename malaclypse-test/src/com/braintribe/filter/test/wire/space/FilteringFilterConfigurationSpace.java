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
package com.braintribe.filter.test.wire.space;

import java.util.function.Predicate;

import com.braintribe.build.artifacts.mc.wire.buildwalk.space.FilterConfigurationSpace;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class FilteringFilterConfigurationSpace extends FilterConfigurationSpace {

	@Override
	public Predicate<? super Dependency> dependencyFilter() {
		return d -> {
			if (d.getScope() != null) {
				if (d.getScope().equalsIgnoreCase("test"))
					return false;
			}
			return !d.getOptional(); 
		};
	}

	
}
