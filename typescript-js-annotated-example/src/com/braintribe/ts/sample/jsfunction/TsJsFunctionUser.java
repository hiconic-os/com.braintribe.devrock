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
package com.braintribe.ts.sample.jsfunction;

import java.util.Map;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@JsType(namespace = "$tf.test")
public interface TsJsFunctionUser<X> {

	void apply(TsJsFunction function);

	@JsFunction
	@FunctionalInterface
	public interface TsJsFunctionWithGenerics<T, R> {
		R consume(T t);
	}

	void applyWithGenerics(TsJsFunctionWithGenerics<String, Integer> fun);

	void applyWithGenericsOfClass(TsJsFunctionWithGenerics<X, String> fun);

	<A, B> Map<A, B> applyWithGenericsOfMethod(TsJsFunctionWithGenerics<A, B> fun);

	@SuppressWarnings("unused")
	static <A, B> Map<A, B> staticWithGenericsOfMethod(TsJsFunctionWithGenerics<A, B> fun) {
		return null;
	}
}
