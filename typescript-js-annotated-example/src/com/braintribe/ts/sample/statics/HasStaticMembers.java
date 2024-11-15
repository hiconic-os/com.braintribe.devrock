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
package com.braintribe.ts.sample.statics;

import java.util.Collection;
import java.util.List;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;

/**
 * @author peter.gazdik
 */
public class HasStaticMembers {

	@JsProperty(namespace = "hc.test.static")
	public static String STATIC_STRING;

	@JsMethod(namespace = "hc.test.static")
	public static void run() {
		// no op
	}

	@JsMethod(namespace = "hc.test.static", name = "jsRun")
	public static void hasDifferentJsName() {
		// no op
	}

	@JsMethod(namespace = "hc.test.static")
	public static String getStaticString() {
		return STATIC_STRING;
	}

	@JsMethod(namespace = "hc.test.static")
	public static <T extends Collection<?>> T getStaticAutoCast() {
		return null;
	}

	@JsMethod(namespace = "hc.test.static")
	public static <T extends Collection<?>> List<T> asList() {
		return null;
	}

	@JsMethod(namespace = "hc.test.static")
	public static List<String> getStaticListString() {
		return null;
	}

	@JsMethod(namespace = "hc.test.static", name = "hasParams")
	public static String hasParameters(Integer i, int ii) {
		return i + " " + ii;
	}
}
