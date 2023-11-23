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

import com.braintribe.devrock.repolet.launcher.builder.cfg.HashCfg;

public class HashOverrideContext<T extends HashOverrideConsumer> {
	private T consumer;
	private HashCfg cfg;

	public HashOverrideContext(T consumer, String node) {
		this.consumer = consumer;
		this.cfg = new HashCfg( node);
	}
	
	public HashOverrideContext<T> hash( String key, String value) {
		cfg.getHashes().put(key, value);
		return this;
	}
	
	public HashOverrideContext<T> noHeaderSupport() {
		cfg.setNoHeaders(true);
		return this;
	}
	
	public HashOverrideContext<T> noHeaderSupport(boolean noHeaderSupport) {
		cfg.setNoHeaders( noHeaderSupport);
		return this;
	}
	
	
	public T close() {
		this.consumer.accept(cfg);
		return this.consumer;
	}
	
}
