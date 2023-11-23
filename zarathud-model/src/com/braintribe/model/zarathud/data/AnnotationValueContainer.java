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

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface AnnotationValueContainer extends GenericEntity {

	final EntityType<AnnotationValueContainer> T = EntityTypes.T(AnnotationValueContainer.class);

	AnnotationValueContainerType getContainerType();
	void setContainerType( AnnotationValueContainerType type);
	
	AnnotationEntity getAnnotation();
	void setAnnotation( AnnotationEntity annotation);
	
	List<AnnotationValueContainer> getChildren();
	void setChildren( List<AnnotationValueContainer> containers);
	
	// simple types
	String getSimpleStringValue();
	void setSimpleStringValue(String value);
	
	Integer getSimpleIntegerValue();
	void setSimpleIntegerValue(Integer value);
	
	Long getSimpleLongValue();
	void setSimpleLongValue(Long value);
	
	Float getSimpleFloatValue();
	void setSimpleFloatValue(Float value);
	
	Double getSimpleDoubleValue();
	void setSimpleDoubleValue(Double value);
	
	Boolean getSimpleBooleanValue();
	void setSimpleBooleanValue(Boolean value);
	
	Date getSimpleDateValue();
	void setSimpleDateValue(Date value);
}
