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
