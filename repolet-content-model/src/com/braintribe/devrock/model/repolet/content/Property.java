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
package com.braintribe.devrock.model.repolet.content;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a simple name <-> value pair, a property as in the properties sections
 * @author pit
 *
 */
public interface Property extends GenericEntity {
	
	EntityType<Property> T = EntityTypes.T(Property.class);
	String name = "name";
	String value = "value";

	/**
	 * @return - name of the property 
	 */
	String getName();
	void setName(String name);
	
	/**
	 * @return - value of the property
	 */
	String getValue();
	void setValue(String value);
	
	
	static Property create( String name, String value) {
		Property property = Property.T.create();
		property.setName(name);
		property.setValue(value);
		return property;
	}
}
