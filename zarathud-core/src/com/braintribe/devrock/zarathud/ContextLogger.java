// ============================================================================
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
package com.braintribe.devrock.zarathud;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.cfg.Required;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.zarathud.ComparisonContext;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.AnnotationEntity;
import com.braintribe.model.zarathud.data.ClassEntity;
import com.braintribe.model.zarathud.data.EnumEntity;
import com.braintribe.model.zarathud.data.InterfaceEntity;

/**
 * helper class to format messages for the {@link ComparisonContext}<br/>
 * 
 * @author pit
 *
 */
public abstract class ContextLogger {
	
	private Map<String, String> descToSimpleTypeSignatureMap = new HashMap<String,String>();
	protected BasicPersistenceGmSession session;
	
	@Required
	public void setSession(BasicPersistenceGmSession session) {
		this.session = session;
	}	
	
	public ContextLogger() {
		// simplest types
		descToSimpleTypeSignatureMap.put("I", "java/lang/Integer");
		descToSimpleTypeSignatureMap.put("J", "java/lang/Long");
		
		descToSimpleTypeSignatureMap.put("F", "java/lang/Float");
		descToSimpleTypeSignatureMap.put("D", "java/lang/Double");
		
		descToSimpleTypeSignatureMap.put("Z", "java/lang/Boolean");
		descToSimpleTypeSignatureMap.put("B", "java/lang/Byte");

	}
	/**
	 * generates a symbolic name from the {@link AbstractEntity} 
	 * @param entity - the {@link AbstractEntity} to get the string from 
	 * @return - a {@link String} that reflects the symbolic type 
	 */
	protected String zarathudEntityToType( AbstractEntity entity) {
		if (entity instanceof EnumEntity)
			return "enum";
		if (entity instanceof AnnotationEntity)
			return "annotation";
		if (entity instanceof ClassEntity) 
			return "class";
		if (entity instanceof InterfaceEntity)
			return "interface";
		return "<unknown>";
	}
	
	
	protected String getSimpleTypeForDesc(String desc) {
		String retval = descToSimpleTypeSignatureMap.get( desc);
		if (retval == null)
			return desc;
		return retval;
	}
}
