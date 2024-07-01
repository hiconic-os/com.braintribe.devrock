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
package com.braintribe.ts.sample;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Blob {
	protected Blob() {
		// To avoid constructor call in Java
	}

	public String type;
	public double size;

	public final native Blob slice(double start, double end, String mimeType);

	@JsOverlay
	public final Blob slice(int start, int end) {
		return slice(start, end, null);
	}

	@JsOverlay
	public final Blob slice(long start, long end) {
		return slice(start, end, null);
	}

	@JsOverlay
	public final String type() {
		return type;
	}

	@JsOverlay
	public final long size() {
		return (long) size;
	}

}