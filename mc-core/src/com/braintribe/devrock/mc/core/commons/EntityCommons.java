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
package com.braintribe.devrock.mc.core.commons;

import java.util.function.Predicate;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

/**
 * collection of functions centered on {@link GenericEntity} handling, especially 
 * absent values (used by settings/pom compiler)
 * @author pit
 *
 */
public class EntityCommons {


	/**
	 * creates an {@link GenericEntity} of {@link EntityType} with all properties initialized as absent
	 * @param type - the {@link EntityType} of the {@link GenericEntity} to create
	 * @return - an instance of the {@link EntityType} with all properties 'absented'  
	 */
	public static <T extends GenericEntity> T createWithAbsentProperties(EntityType<T> type) {
		T entity = type.create();
		for (com.braintribe.model.generic.reflection.Property property : type.getProperties()) {
			property.setAbsenceInformation(entity, GMF.absenceInformation());
		}		
		return entity;
	}
	
	/**
	 * @param owner - the {@link GenericEntity} to check 
	 * @param propertyName - the name of the property to check 
	 * @return - true if the property is absent
	 */
	public static boolean isAbsent( GenericEntity owner, String propertyName) {
		com.braintribe.model.generic.reflection.Property property = owner.entityType().getProperty( propertyName);
		return property.isAbsent(owner);
	}
	
	/**
	 * sets owner's property to value if owner's property is absent
	 * @param owner - the {@link GenericEntity} to set the {@link Property}
	 * @param propertyName - the name of the {@link Property}
	 * @param value - the value to set 
	 */
	public static void setIfNotAbsent( GenericEntity owner, String propertyName, Object value) {
		com.braintribe.model.generic.reflection.Property property = owner.entityType().getProperty( propertyName);
		if (property.isAbsent(owner)) {
			property.set(owner, value);			
		}
	}
	
	/**
	 * sets an absent property in target with the property's value of source 
	 * @param source - the {@link GenericEntity} to get the {@link Property}'s value from 
	 * @param target - the {@link GenericEntity} to set the {@link Property}
	 * @param propertyName - the name of the {@link Property}
	 */
	public static void setIfNotAbsent( GenericEntity source, GenericEntity target, String propertyName) {
		com.braintribe.model.generic.reflection.Property property = target.entityType().getProperty( propertyName);
		if (property.isAbsent(target) && !property.isAbsent(source)) {
			property.set(target, property.get(source));			
		}
	}
	
	/**
	 * merges two {@link GenericEntity} : if target's property is absent, it is taken from source 
	 * @param source - the {@link GenericEntity} to be used as source 
	 * @param target - the {@link GenericEntity} that is the target 
	 */
	public static void mergeIfAbsentInTarget( GenericEntity source, GenericEntity target) {
		if (source.entityType() != target.entityType()) {
			throw new IllegalStateException("source entity type and target entity do not match");
		}
		for (com.braintribe.model.generic.reflection.Property property : source.entityType().getProperties()) {
			if (property.isAbsent(target)) {
				Object value = property.get(source);
				property.set(target, value);
			}
		}
	}
	/**
	 * merges two {@link GenericEntity} : if source's property isn't absent, it is transferred to target 
	 * @param source - the {@link GenericEntity} to be used as source 
	 * @param target - the {@link GenericEntity} that is the target 
	 */

	public static void mergeIfNotAbsentInSource( GenericEntity source, GenericEntity target) {
		mergeIfNotAbsentInSource(source, target, p -> false);
	}
	
	/**
	 * merges two {@link GenericEntity} : if source's property isn't absent, it is transferred to target 
	 * @param source - the {@link GenericEntity} to be used as source 
	 * @param target - the {@link GenericEntity} that is the target 
	 * @param propertyExclusionFilter - the filter which can exclude property from the merge
	 */
	
	public static void mergeIfNotAbsentInSource( GenericEntity source, GenericEntity target, Predicate<Property> propertyExclusionFilter) {
		if (source.entityType() != target.entityType()) {
			throw new IllegalStateException("source entity type and target entity do not match");
		}
		
		for (com.braintribe.model.generic.reflection.Property property : source.entityType().getProperties()) {
			if (propertyExclusionFilter.test(property))
				continue;
				
			if (!property.isAbsent(source)) {
				Object value = property.get(source);
				property.set(target, value);
			}
		}
	}
}
