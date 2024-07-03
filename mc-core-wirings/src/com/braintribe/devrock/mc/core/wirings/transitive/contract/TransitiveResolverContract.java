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
package com.braintribe.devrock.mc.core.wirings.transitive.contract;

import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.wire.api.space.WireSpace;

/**
 * top tier 
 * @author pit
 *
 */
public interface TransitiveResolverContract extends WireSpace {
	/**
	 * @return - the {@link TransitiveDependencyResolver}
	 */
	TransitiveDependencyResolver transitiveDependencyResolver();
	/**
	 * @return - the {@link ArtifactDataResolverContract} as configured by the {@link TransitiveResolverContract}
	 */
	ArtifactDataResolverContract dataResolverContract();
}
