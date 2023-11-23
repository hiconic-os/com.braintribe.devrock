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
package com.braintribe.ts.sample.clazz;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@JsType(namespace = "$tf.test")
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
