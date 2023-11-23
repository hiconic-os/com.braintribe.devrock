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
