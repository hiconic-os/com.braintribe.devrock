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
package com.braintribe.devrock.mc.core.wirings.js.contract;

import com.braintribe.devrock.mc.api.js.JsDependencyResolver;
import com.braintribe.devrock.mc.api.js.JsLibraryLinker;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.wire.api.space.WireSpace;

/**
 * the contract to get the {@link JsDependencyResolver}
 * @author pit / dirk
 *
 */
public interface JsResolverContract extends WireSpace {

	/**
	 * @return - a fully qualified {@link JsDependencyResolver}
	 */
	JsDependencyResolver jsResolver();
	
	JsLibraryLinker jsLibraryLinker();

	/**
	 * @return - the underlying {@link TransitiveResolverContract} as configured by the {@link JsResolverContract}
	 */
	TransitiveResolverContract transitiveResolverContract();
}

