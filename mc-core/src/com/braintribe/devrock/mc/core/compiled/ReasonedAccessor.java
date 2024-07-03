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
package com.braintribe.devrock.mc.core.compiled;

import com.braintribe.devrock.model.mc.reason.UndefinedProperty;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.value.UnsatisfiedValue;
import com.braintribe.model.generic.value.ValueDescriptor;

public interface ReasonedAccessor<E extends GenericEntity, R> {
	Maybe<R> get(E entity);
	Property property();
	
	
	static <E extends GenericEntity, R> ReasonedAccessor<E, R> build(EntityType<E> type, String propertyName) {
		Property property = type.getProperty(propertyName);
		return new ReasonedAccessorImpl<>(property);
	}
}

class ReasonedAccessorImpl<E extends GenericEntity, R> implements ReasonedAccessor<E, R>{
	private Property property;
	
	public ReasonedAccessorImpl(Property property) {
		super();
		this.property = property;
	}
	
	@Override
	public Maybe<R> get(E e) {
		Object vdCandidate = property.getDirectUnsafe(e);
		
		if (VdHolder.isVdHolder(vdCandidate)) {
			ValueDescriptor valueDescriptor = VdHolder.getValueDescriptorIfPossible(vdCandidate);
			if (valueDescriptor instanceof UnsatisfiedValue) {
				UnsatisfiedValue unsatisfiedValue = (UnsatisfiedValue)valueDescriptor;
				return undefinedProperty(property, unsatisfiedValue.getWhy()).asMaybe();
			}
			else {
				return undefinedProperty(property).asMaybe();
			}
		}
		
		return Maybe.complete(property.get(e));
	}
	
	@Override
	public Property property() {
		return property;
	}
	
	static UndefinedProperty undefinedProperty(Property property) {
		return undefinedProperty(property, null);
	}
	
	static UndefinedProperty undefinedProperty(Property property, Reason cause) {
		UndefinedProperty undefined = Reasons.build(UndefinedProperty.T).text("Undefined property: " + property.getName()).toReason();
		if (cause != null)
			undefined.getReasons().add(cause);
			
		return undefined;
	}

}