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

import java.util.ArrayList;
import java.util.List;

/**
 * a configuration of the launcher
 * @author pit
 *
 */
public class LauncherCfg {
	private int port = -1;
	private List<RepoletCfg> repoletCfgs = new ArrayList<>();
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public List<RepoletCfg> getRepoletCfgs() {
		return repoletCfgs;
	}
	public void setRepoletCfgs(List<RepoletCfg> repoletCfgs) {
		this.repoletCfgs = repoletCfgs;
	}
	
	
	
	
}
