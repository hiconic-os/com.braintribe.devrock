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
