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
