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
