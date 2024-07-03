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
