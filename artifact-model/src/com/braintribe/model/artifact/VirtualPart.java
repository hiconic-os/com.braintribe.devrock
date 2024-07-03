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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.meta.data.MetaData;

/**
 * virtual parts are like file based parts, yet attached directly to a {@link HasMetaData} entity <br/>
 * 
 * @author pit
 *
 */
public interface VirtualPart extends MetaData {
	
	final EntityType<VirtualPart> T = EntityTypes.T(VirtualPart.class);

	/**
	 * the type of the {@link VirtualPart}, expressed as a {@link PartTuple}
	 * @return - the {@link PartTuple}
	 */
	PartTuple getType();
	void setType( PartTuple type);
	
	/**
	 * the content of the {@link VirtualPart} as a {@link String}
	 * @return
	 */
	String getPayload();
	void setPayload( String payload);
}
