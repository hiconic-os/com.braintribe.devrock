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
package com.braintribe.devrock.mc.impl.js;

import java.io.File;
import java.util.Map;

import com.braintribe.devrock.mc.api.js.JsLibraryLinkingContext;
import com.braintribe.devrock.mc.api.js.JsLibraryLinkingContextBuilder;

public class BasicJsLibraryLinkingContext implements JsLibraryLinkingContextBuilder, JsLibraryLinkingContext {

	private boolean lenient = false;
	private boolean useSymbolikLinks;
	private boolean prettyOverMin;
	private File libraryCacheFolder;
	private Map<File, String> linkFolders;
	private String outputPrefix = "";

	@Override
	public boolean lenient() {
		return lenient;
	}

	@Override
	public JsLibraryLinkingContextBuilder lenient(boolean lenient) {
		this.lenient = lenient;
		return this;
	}

	@Override
	public boolean preferPrettyOverMin() {
		return prettyOverMin;
	}

	@Override
	public boolean useSymbolikLinks() {
		return useSymbolikLinks;
	}

	@Override
	public Map<File, String> linkFolders() {
		return linkFolders;
	}

	@Override
	public String outputPrefix() {
		return outputPrefix;
	}

	@Override
	public JsLibraryLinkingContextBuilder preferPrettyOverMin(boolean prettyOverMin) {
		this.prettyOverMin = prettyOverMin;
		return this;
	}

	@Override
	public JsLibraryLinkingContextBuilder useSymbolikLinks(boolean useSymbolikLinks) {
		this.useSymbolikLinks = useSymbolikLinks;
		return this;
	}

	@Override
	public File libraryCacheFolder() {
		return libraryCacheFolder;
	}

	@Override
	public JsLibraryLinkingContextBuilder libraryCacheFolder(File libraryCacheFolder) {
		this.libraryCacheFolder = libraryCacheFolder;
		return this;
	}

	@Override
	public JsLibraryLinkingContextBuilder linkFolders(Map<File, String> linkFolders) {
		this.linkFolders = linkFolders;
		return this;
	}

	@Override
	public JsLibraryLinkingContextBuilder outputPrefix(String outputPrefix) {
		this.outputPrefix = outputPrefix;
		return this;
	}

	@Override
	public JsLibraryLinkingContext done() {
		return this;
	}

}
