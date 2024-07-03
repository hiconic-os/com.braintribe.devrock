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
package com.braintribe.devrock.repolet.launcher.builder.api;

import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.launcher.builder.cfg.IndexedDescriptiveContentCfg;

public class IndexedDescriptiveContentContext<T extends IndexedDescriptiveContentConsumer> {
	private IndexedDescriptiveContentCfg cfg;
	private T consumer;
	
	public IndexedDescriptiveContentContext( T consumer) {
		this.consumer = consumer;
		cfg = new IndexedDescriptiveContentCfg();
	}
	
	/**
	 * @param key - the initial key (activates the associated indexed {@link RepoletContent} at startup
	 * @return - itself
	 */
	public IndexedDescriptiveContentContext<T> initialIndex( String key) {		
		cfg.setInitial(key);
		return this;		
	}
	/**
	 * @param key - the index's key
	 * @param content - the indexed {@link RepoletContent}
	 * @return - itself
	 */
	public IndexedDescriptiveContentContext<T> descriptiveContent( String key, RepoletContent content) {		
		cfg.getKeyToContent().put(key, content);
		return this;		
	}
	
	public T close() {
		consumer.accept(cfg);
		return consumer;
	}
}
