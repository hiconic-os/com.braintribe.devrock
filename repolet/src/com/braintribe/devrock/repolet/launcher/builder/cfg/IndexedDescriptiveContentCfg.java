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
package com.braintribe.devrock.repolet.launcher.builder.cfg;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.devrock.model.repolet.content.RepoletContent;

public class IndexedDescriptiveContentCfg {
	private String initial;	
	private Map<String, RepoletContent> keyToContent = new HashMap<>();
	
	/**
	 * @return - the initial key
	 */
	public String getInitial() {
		return initial;
	}

	/**
	 * @param initial - the initial key 
	 */
	public void setInitial(String initial) {
		this.initial = initial;
	}

	/**
	 * @return - a {@link Map} of key to filesystem
	 */
	public Map<String, RepoletContent> getKeyToContent() {
		return keyToContent;
	}

	/**
	 * @param keyToFile - a {@link Map} of key to filesystem
	 */
	public void setKeyToContent(Map<String, RepoletContent> keyToFile) {
		this.keyToContent = keyToFile;
	}
}
