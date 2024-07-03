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
package com.braintribe.ts.sample.jsfunction;

import jsinterop.annotations.JsFunction;

/**
 * @author peter.gazdik
 */
@JsFunction
@FunctionalInterface
public interface TsJsFunction {

	int convertStringToInt(String s);

	// To test that default method is ignored
	default Integer sqrDefault(Integer n) {
		return n * n;
	}

	// To test that static method is ignored
	static Integer sqrStatic(Integer n) {
		return n * n;
	}

}
