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
package com.braintribe.devrock.mc.api.js;

import java.io.File;
import java.util.Map;

import com.braintribe.devrock.mc.impl.js.BasicJsLibraryLinkingContext;

public interface JsLibraryLinkingContext {
	boolean lenient();
	boolean preferPrettyOverMin();
	boolean useSymbolikLinks();
	File libraryCacheFolder();
	Map<File, String> linkFolders();

	/** Prefix for to use for any console output, typically just indentation from the parent task. */
	String outputPrefix();

	static JsLibraryLinkingContextBuilder build() {
		return new BasicJsLibraryLinkingContext();
	}
}
