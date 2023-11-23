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
package com.braintribe.model.typescript.model.eval;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * For this case, when the eval method returns two different types {@link GenericEntity} in {@link TsEvalA} and {@link TsEvalB} returns itself, Java
 * is smart enough to pick the most specific type (TsEvalB), but TypeScript is not. Therefore we have to render it on this level.
 * 
 * The implementation can assume there is a single most specific sub-type, as it would be illegal Java code otherwise.
 * 
 * @author peter.gazdik
 */
public interface TsEvalABB extends TsEvalA, TsEvalBB {

	EntityType<TsEvalABB> T = EntityTypes.T(TsEvalABB.class);

	// This is the code that we don't have to write in Java, but we have to put it in the generated TypeScript
	// @Override
	// EvalContext<? extends TsEvalB> eval(Evaluator<ServiceRequest> evaluator);

}
