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
package com.braintribe.devrock.mc.core.compiled;

import java.util.function.Consumer;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EssentialTypes;

public class ReasonCollectingAccessor<E extends GenericEntity> {
	private Consumer<Reason> reasonCollector;
	private E entity;
	
	public ReasonCollectingAccessor(E entity, Consumer<Reason> reasonCollector) {
		super();
		this.entity = entity;
		this.reasonCollector = reasonCollector;
	}

	public <R> Maybe<R> get(ReasonedAccessor<E, R> accessor) {
		Maybe<R> maybe = accessor.get(entity);
		
		if (maybe.isUnsatisfied()) {
			reasonCollector.accept(maybe.whyUnsatisfied());
			
			if (accessor.property().getType() == EssentialTypes.TYPE_STRING) {
				R result = (R)"<n/a>";
				return Maybe.complete(result);
			}
		}
		
		return maybe;
	}
}
