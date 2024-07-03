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
package com.braintribe.devrock.zarathud.validator;

import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.MethodEntity;

/**
 * tuple that wraps corresponding getter/setter methods
 *  
 * @author pit
 */
public class AccessTuple {

	private static  final String setterPrefix = "set";
	private static  final String getterPrefix = "get";
	
	private AbstractEntity owner;
	
	private MethodEntity getter;
	private MethodEntity setter;
	private String suffix;
	
	public MethodEntity getGetter() {
		return getter;
	}	
	public MethodEntity getSetter() {
		return setter;
	}
	
	public AbstractEntity getOwner() {
		return owner;
	}	
	public String getSuffix() {
		return suffix;
	}
	
	/**
	 * setups an {@link AccessTuple} build trying to aggregate a {@link MethodEntity}, returns: <br/>
	 * true : was able to fit the {@link MethodEntity} into the {@link AccessTuple}<br/>
	 * false : wasn't able to fit (not matching per name or already positioned)<br/>
	 * null : not fitting the match at all (no getter / setter)
	 * @param method - {@link MethodEntity}
	 * @return - see above 
	 */
	public Boolean assign( MethodEntity method) {
		String name = method.getName();
		if (getter != null) {						
			// must be a setter 
			if (name.startsWith( setterPrefix) == false) {
				return false;				 
			}
			// must have the same suffix
			if (name.substring( setterPrefix.length()).equalsIgnoreCase( suffix) == false)
				return false;
			setter = method;
			return true;
		}
		if (setter != null) {
			// must be a setter 
			if (name.startsWith( getterPrefix) == false) {
				return false;				 
			}
			// must have the same suffix
			if (name.substring( getterPrefix.length()).equalsIgnoreCase( suffix) == false)
				return false;
			getter = method;
			return true;
		}	
		if (name.startsWith(setterPrefix)) {
			setter = method;
			suffix = name.substring( setterPrefix.length());
			owner = method.getOwner();
			return true;
		}
		if (name.startsWith(getterPrefix)) {
			getter = method;
			suffix = name.substring( getterPrefix.length());
			owner = method.getOwner();
			return true;
		}
		return null;		
	}
	
}
