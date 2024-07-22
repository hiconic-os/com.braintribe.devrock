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
package com.braintribe.model.artifact.analysis;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * defines the two possible strategies to resolve clashes:
 * {@link #firstOccurrence} means resolving by dependency order of the clashing dependencies,
 * {@link #highestVersion} means resolving by the highest version of the clashing dependencies
 *  
 * @author pit
 *
 */
public enum ClashResolvingStrategy implements EnumBase<ClashResolvingStrategy> {
		
	firstOccurrence, highestVersion;
	
	EnumType T = EnumTypes.T(ClashResolvingStrategy.class);

	@Override
	public EnumType<ClashResolvingStrategy> type() {
		return T;
	}

	
}

