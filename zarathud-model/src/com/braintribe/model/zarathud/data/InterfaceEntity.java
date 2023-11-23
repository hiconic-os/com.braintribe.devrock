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
package com.braintribe.model.zarathud.data;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface InterfaceEntity extends AbstractClassEntity {
	
	final EntityType<InterfaceEntity> T = EntityTypes.T(InterfaceEntity.class);

	Set<InterfaceEntity> getSuperInterfaces();
	void setSuperInterfaces( Set<InterfaceEntity> entries);
	
	Set<InterfaceEntity> getSubInterfaces();
	void setSubInterfaces( Set<InterfaceEntity> subInterfaces);
	
	Set<ClassEntity> getImplementingClasses();
	void setImplementingClasses( Set<ClassEntity> implementingClasses);
}
