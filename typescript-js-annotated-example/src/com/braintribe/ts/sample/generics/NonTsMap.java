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
package com.braintribe.ts.sample.generics;

import java.util.Collection;
import java.util.Set;

/**
 * This type is here to see that sub-types that rename/specify the K/V type parameters are handled properly.
 * <p>
 * This cannot be a JS type, so it's methods are not inherited in the generated TypeScript.
 * 
 * @author peter.gazdik
 */
public interface NonTsMap<K, V> {

	Set<K> nonTsMapKeys();

	Collection<V> nonTsMapValues();

}
