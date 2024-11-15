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
package com.braintribe.ts.sample.clazz;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@JsType(namespace = "hc.test")
public class TsClass implements TsInterface1, TsInterface2 {

	@JsIgnore
	public TsClass(String s) {
		this(s, null);
	}

	@SuppressWarnings("unused")
	public TsClass(String s, TsClass ts) {
		throw new UnsupportedOperationException("Method 'TsClass.TsClass' is not supported!");
	}

}
