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

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface AbstractClassEntity extends AbstractEntity {

	final EntityType<AbstractClassEntity> T = EntityTypes.T(AbstractClassEntity.class);

	
	boolean getParameterNature();
	void setParameterNature( boolean parameterNature);
	
	boolean getArrayNature();
	void setArrayNature( boolean arrayNature);
	
	boolean getTwoDimensionality();
	void setTwoDimensionality( boolean twoDimensionality);
	
	Boolean getGenericNature();
	void setGenericNature( Boolean value);
	
	Set<MethodEntity> getMethods();
	void setMethods( Set<MethodEntity> entries);
	
	Set<AnnotationEntity> getAnnotations();
	void setAnnotations( Set<AnnotationEntity> annotations);
	
	List<AbstractEntity> getParameterization();
	void setParameterization( List<AbstractEntity> parameterization);
}
