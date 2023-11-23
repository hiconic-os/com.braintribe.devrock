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

