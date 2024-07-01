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
package com.braintribe.devrock.zarathud.comparator;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.devrock.zarathud.ContextLogger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.zarathud.ComparisonContext;
import com.braintribe.model.zarathud.ComparisonContextMessage;
import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.AccessModifier;
import com.braintribe.model.zarathud.data.AnnotationEntity;
import com.braintribe.model.zarathud.data.ClassEntity;
import com.braintribe.model.zarathud.data.EnumEntity;
import com.braintribe.model.zarathud.data.InterfaceEntity;
import com.braintribe.model.zarathud.data.MethodEntity;

public class ComparisonContextLogger extends ContextLogger {
	
	
	private ComparisonContext comparisonContext;
	
	public void setComparisonContext(ComparisonContext comparisonContext) {
		this.comparisonContext = comparisonContext;
	}
	
	
	/**
	 * logs no matching types 
	 * @param base - the base {@link AbstractEntity}
	 * @param candidate - the candidate {@link AbstractEntity}
	 */
	public void logTypeNotMatching( AbstractEntity base, AbstractEntity candidate) {
		String msg="candidate [" + base.getName() + "] is type [" + zarathudEntityToType( candidate) +"] instead of [" + zarathudEntityToType(base) + "] as expected";
		addMessage( base, candidate, msg);
	
	}
	
	/**
	 * logs missing entity in the candidate 
	 * @param base - the missing {@link AbstractEntity}
	 */
	public void logMissingEntityInCandidate( AbstractEntity base) {
		String msg="candidate doesn't contain a entity [" + base.getName() + "] of type [" + zarathudEntityToType( base) +"]";
		addMessage( base, null, msg);
	
	}
	
	/**
	 * logs a missing enum value
	 * @param base - the base {@link EnumEntity}
	 * @param candidate - the candidate {@link EnumEntity}
	 * @param value - the missing value 
	 */
	public void logMissingEnumValue( EnumEntity base, EnumEntity candidate, String value) {
		String msg ="candidate for [enum] named + [" + base.getName() + "] doesn't contain [" + value + "]";
		addMessage( base, candidate, msg);
	}
	
	/**
	 * logs a missing method 
	 * @param base - the {@link MethodEntity} that's missing in the candidate
	 */
	public void logMissingMethod( MethodEntity base, AbstractClassEntity candidate) {
		String msg ="candidate's [" + base.getOwner().getName() + "] doesn't contain required base method [" + base.getName() + ":" + base.getDesc() + "]";
		addMessage( base.getOwner(), candidate, msg);
	}
	
	/**
	 * logs a mismatched {@link AccessModifier}
	 * @param base - the {@link MethodEntity} base 
	 * @param candidate - the {@link MethodEntity} candidate 
	 */
	public void logMismatchedAccessModifier( MethodEntity base, MethodEntity candidate) {
		String msg ="candidate's [" + base.getOwner().getName() + "] method [" + base.getName() + ":" + base.getDesc() + "] has access modifier [" + candidate.getAccessModifier().toString() + "], expected is [" + base.getAccessModifier().toString() + "]";
		addMessage( base.getOwner(), candidate.getOwner(), msg);	
	}
	
	/**
	 * logs a missing exception (i.e candidate doesn't throw an exception that base declared)
	 * @param base - the base {@link MethodEntity}
	 * @param exception - the exception as {@link ClassEntity}
	 */
	public void logMissingException(MethodEntity base, MethodEntity candidate, ClassEntity exception) {
		String msg ="candidate's [" + base.getOwner().getName() + "] method [" + base.getName() + ":" + base.getDesc() + "] doesn't throw [" + exception.getName() + "]";
		addMessage( base.getOwner(), candidate.getOwner(),  msg);			
	}
	
	/**
	 * logs an additional exception that the candidate throws, but the base doesn't  
	 * @param method - the base {@link MethodEntity}
	 * @param exception - the exception as a {@link ClassEntity}
	 */
	public void logAdditionalException( AbstractClassEntity base, AbstractClassEntity candidate, MethodEntity method, ClassEntity exception) {
		String msg ="candidate's [" + method.getOwner().getName() + "] method [" + method.getName() + ":" + method.getDesc() + "] introduces exception [" + exception.getName() + "]";
		addMessage( base, candidate, msg);							
	}
	
	/**
	 * logs a missing annotation (i.e. candidate doesn't have an annotation that base declared)
	 * @param base - the owner in the base a {@link GenericEntity}, can by a {@link MethodEntity} or a {@link AbstractClassEntity}
	 * @param annotation - the {@link AnnotationEntity} that's missing 
	 */
	public void logMissingAnnotations( GenericEntity base, GenericEntity candidate, AnnotationEntity annotation) {
		if (base instanceof MethodEntity) {
			MethodEntity method = (MethodEntity) base;
			String msg ="candidate's [" + method.getOwner().getName() + "] method [" + method.getName() + ":" + method.getDesc() + "] doesn't show annotation [" + annotation.getName() + "]";
			addMessage( method.getOwner(), ((MethodEntity)candidate).getOwner(), msg);					
		} else
			if (base instanceof ClassEntity) {
				ClassEntity classEntity = (ClassEntity) base;
				String msg = "candidate's class [" + classEntity.getName() + "] doesn't show annotation [" + annotation.getName() + "]";
				addMessage( classEntity, ((ClassEntity) candidate), msg);
			} else 
				if (base instanceof InterfaceEntity) {
					InterfaceEntity interfaceEntity = (InterfaceEntity) base;
					String msg = "candidate's interface [" + interfaceEntity.getName() + "] doesn't show annotation [" + annotation.getName() + "]";
					addMessage( interfaceEntity, ((InterfaceEntity) candidate), msg);
				}			
	}
	
	/**
	 * logs annotations that the candidate introduces 
	 * @param base - the base as a {@link GenericEntity}, can be a {@link MethodEntity} or a {@link AbstractClassEntity}
	 * @param annotation - the {@link AnnotationEntity} the is introduced by the candidate 
	 */
	public void logAdditionalAnnotation( GenericEntity base, GenericEntity candidate, AnnotationEntity annotation) {
		if (base instanceof MethodEntity) {
			MethodEntity method = (MethodEntity) base;
			String msg ="candidate's [" + method.getOwner().getName() + "] method [" + method.getName() + ":" + method.getDesc() + "] introduces annotation [" + annotation.getName() + "]";
			addMessage( method.getOwner(), ((MethodEntity)candidate).getOwner(), msg);				
		} else
			if (base instanceof ClassEntity) {
				ClassEntity classEntity = (ClassEntity) base;
				String msg = "candidate's class [" + classEntity.getName() + "] introduces annotation [" + annotation.getName() + "]";
				addMessage( classEntity, ((ClassEntity) candidate), msg);
			} else 
				if (base instanceof InterfaceEntity) {
					InterfaceEntity interfaceEntity = (InterfaceEntity) base;
					String msg = "candidate's interface [" + interfaceEntity.getName() + "] introduces annotation [" + annotation.getName() + "]";
					addMessage( interfaceEntity, ((InterfaceEntity) candidate), msg);
				}	
	}
	
	
	private void addMessage( AbstractEntity focus1, AbstractEntity focus2, String msg) {
		ComparisonContextMessage message = session != null ? session.create( ComparisonContextMessage.T) : ComparisonContextMessage.T.create();
		message.setFirstFocus(focus1);
		message.setSecondFocus(focus2);
		message.setMessage(msg);
		comparisonContext.getMessages().add(message);
	}	

	public static String [] dumpToString(ComparisonContext context) {
		List<String> result = new ArrayList<String>( );
		List<ComparisonContextMessage> messages = context.getMessages();
		if (messages == null || messages.size() == 0)
			return result.toArray( new String[0]);
		
		for (ComparisonContextMessage message : messages) {
			result.add( message.getMessage());
		}
		return result.toArray( new String[0]);
	}
}
