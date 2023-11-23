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
package com.braintribe.ts.sample.statics;

import java.util.Collection;
import java.util.List;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;

/**
 * @author peter.gazdik
 */
public class HasStaticMembers {

	@JsProperty(namespace = "$tf.test.static")
	public static String STATIC_STRING;

	@JsMethod(namespace = "$tf.test.static")
	public static void run() {
		// no op
	}

	@JsMethod(namespace = "$tf.test.static", name = "jsRun")
	public static void hasDifferentJsName() {
		// no op
	}

	@JsMethod(namespace = "$tf.test.static")
	public static String getStaticString() {
		return STATIC_STRING;
	}

	@JsMethod(namespace = "$tf.test.static")
	public static <T extends Collection<?>> T getStaticAutoCast() {
		return null;
	}

	@JsMethod(namespace = "$tf.test.static")
	public static <T extends Collection<?>> List<T> asList() {
		return null;
	}

	@JsMethod(namespace = "$tf.test.static")
	public static List<String> getStaticListString() {
		return null;
	}

	@JsMethod(namespace = "$tf.test.static", name = "hasParams")
	public static String hasParameters(Integer i, int ii) {
		return i + " " + ii;
	}
}
