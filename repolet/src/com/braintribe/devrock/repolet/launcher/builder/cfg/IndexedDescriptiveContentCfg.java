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
