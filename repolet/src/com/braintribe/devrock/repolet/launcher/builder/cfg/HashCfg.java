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

public class HashCfg {
	private String node;
	private Map<String,String> hashes = new HashMap<>();
	private boolean noHeaders;
	
	public HashCfg(String node) {
		this.node = node;
	}

	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}

	public Map<String, String> getHashes() {
		return hashes;
	}
	public void setHashes(Map<String, String> hashes) {
		this.hashes = hashes;
	}

	public boolean getNoHeaders() {
		return noHeaders;
	}
	public void setNoHeaders(boolean noHeaders) {
		this.noHeaders = noHeaders;
	}	
	
	
}
