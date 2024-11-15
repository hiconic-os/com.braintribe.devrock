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
package com.braintribe.ts.sample;

import jsinterop.annotations.JsOptional;
import jsinterop.annotations.JsType;

@JsType(namespace = "hc.test")
public interface TsCustomInterface {

	static String STATIC_FIELD = "STATIC_FIELD";

	static String staticMethod() {
		return "staticMethod";
	}

	TsCustomInterface sameNs();

	TsOtherNamespaceInterface otherNs();

	TsEnum _enum();

	TsNativeGlobalNamespaceCustomName nativeGlobalNamespaceCustomName();

	TsNativeGlobalNamespace nativeGlobalNamespace();

	TsNativeCustomNamespace nativeCustomNamespace();

	TsNativeWithGenericsGlobalNamespace<String> nativeWithGenerics();

	void methodWithOptionalParams(int first, String second, @JsOptional String thirdOptional, String fourthOptional);

}
