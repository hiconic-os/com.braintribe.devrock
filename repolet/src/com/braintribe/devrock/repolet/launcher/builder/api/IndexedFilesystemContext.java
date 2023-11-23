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

import java.io.File;

import com.braintribe.devrock.repolet.launcher.builder.cfg.IndexedFilesystemCfg;

/**
 * a builder context for indexed file systems
 * @author pit
 *
 * @param <T> : an extension of the {@link IndexedFilesystemConsumer}
 */
public class IndexedFilesystemContext<T extends IndexedFilesystemConsumer> {

	private IndexedFilesystemCfg cfg;
	private T consumer;
	
	public IndexedFilesystemContext( T consumer) {
		this.consumer = consumer;
		cfg = new IndexedFilesystemCfg();
	}
	
	public IndexedFilesystemContext<T> initialIndex( String key) {		
		cfg.setInitial(key);
		return this;		
	}
	
	/**
	 * @param filesystem - a directory
	 * @return - itself
	 */
	public IndexedFilesystemContext<T> filesystem( String key, File filesystem) {		
		cfg.getKeyToFile().put(key, filesystem);
		return this;		
	}
	
	public T close() {
		consumer.accept(cfg);
		return consumer;
	}
}
