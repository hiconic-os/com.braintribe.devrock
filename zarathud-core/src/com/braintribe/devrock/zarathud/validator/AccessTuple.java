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
