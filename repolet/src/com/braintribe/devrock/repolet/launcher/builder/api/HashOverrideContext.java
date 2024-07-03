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
