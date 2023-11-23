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
