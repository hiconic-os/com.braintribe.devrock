// ============================================================================
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
package com.braintribe.ts.sample;

import java.util.List;
import java.util.Map;

import com.braintribe.ts.sample.nointerop.NoInterop;

import jsinterop.annotations.JsType;

@JsType(namespace = "$tf.test")
public interface TsGenericsInMethods {

	List<String> listString();

	List<? extends TsGenericsInMethods> listProducer();

	List<? extends List<? extends TsGenericsInMethods>> listOfListsProducer();

	void listConsumer(List<? super TsGenericsInMethods> list);

	Map<String, Integer> mapStringInteger();

	<R extends TsGenericsInMethods> R genericMethod_Simple();

	<R extends Iterable<?> & TsGenericsInMethods> R genericMethod_MultiExtends();

	<R extends NoInterop & Map<?, ?>> R genericMethod_MultiExtends2();

	<R extends NoInterop & Map<? extends TsGenericsInMethods, ?>> List<R> genericMethod_MultiExtends3();

	<K extends TsGenericsInMethods, V extends List<String>> Map<K, V> genericMethod(K k, V v);

	<E> List<E> genericMethod_NonJsParam(E e1, E e2);

}
