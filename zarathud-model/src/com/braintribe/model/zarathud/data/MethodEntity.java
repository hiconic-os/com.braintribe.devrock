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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface MethodEntity extends GenericEntity{
	
	final EntityType<MethodEntity> T = EntityTypes.T(MethodEntity.class);

	String getName();
	void setName( String name);
	
	String getSignature();
	void setSignature( String signature);
	
	String getDesc();
	void setDesc( String desc);
	
	Set<ClassEntity> getExceptions();
	void setExceptions( Set<ClassEntity> exceptions);
	
	AccessModifier getAccessModifier();
	void setAccessModifier( AccessModifier modifier);
	
	boolean getAbstractNature();
	void setAbstractNature( boolean value);
	
	boolean getStaticNature();
	void setStaticNature( boolean value);
	
	boolean getSynchronizedNature();
	void setSynchronizedNature( boolean value);
	
	Set<AnnotationEntity> getAnnotations();
	void setAnnotations( Set<AnnotationEntity> value);
	
	AbstractClassEntity getOwner();
	void setOwner( AbstractClassEntity owner);
	
	AbstractEntity getReturnType();
	void setReturnType( AbstractEntity desc);	
	
	List<AbstractEntity> getArgumentTypes();
	void setArgumentTypes( List<AbstractEntity> types);
			
}
