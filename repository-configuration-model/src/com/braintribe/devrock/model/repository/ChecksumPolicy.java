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
package com.braintribe.devrock.model.repository;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * determines what should be done during downloads <br/>
 * {@link ChecksumPolicy#ignore} - no check, no nothing (default) <br/>
 * {@link ChecksumPolicy#warn} - check and issue a warning via logging <br/>
 * {@link ChecksumPolicy#fail} - check and throw an exception if no match<br/>
 *
 * @author pit
 *
 */
public enum ChecksumPolicy implements EnumBase {
	ignore,
	warn,
	fail;

	public final static EnumType T = EnumTypes.T(ChecksumPolicy.class);

	@Override
	public EnumType type() {
		return T;
	}

}
