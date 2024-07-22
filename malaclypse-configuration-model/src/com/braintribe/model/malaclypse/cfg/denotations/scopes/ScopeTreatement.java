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
package com.braintribe.model.malaclypse.cfg.denotations.scopes;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * well, what can you do with scopes? Either include them or exclude them (filter them out)
 * @author pit
 *
 */
public enum ScopeTreatement implements EnumBase<ScopeTreatement> {
	INCLUDE, EXCLUDE;
	
	
	final EnumType<ScopeTreatement> T = EnumTypes.T(ScopeTreatement.class);

	@Override
	public EnumType<ScopeTreatement> type() {
		return T;
	}
}
