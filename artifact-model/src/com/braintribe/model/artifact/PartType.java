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
package com.braintribe.model.artifact;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * describes symbolic names for certain common {@link PartTuple}, such as <br>
 * JAR, JAVADOC, POM, SOURCES, <br/>
 * EXCLUSIONS, <br/>
 * META, GLOBAL_META, MD5, SHA1, ANT, ASC, <br/>
 * PROJECT <br/>
 * see {@link PartTuple} and Malaclypse's PartTupleProcessor and KnownPartTuples for details.
 * 
 * @author Pit
 *
 */
public enum PartType implements EnumBase<PartType> {
	JAR,
	JAVADOC,
	META,
	GLOBAL_META,
	POM,
	SOURCES,
	PROJECT,
	MD5,
	SHA1,
	EXCLUSIONS,
	ANT,
	ASC,
	MODEL,
	_UNKNOWN_;

	public static final EnumType<PartType> T = EnumTypes.T(PartType.class);

	@Override
	public EnumType<PartType> type() {
		return T;
	}

}
