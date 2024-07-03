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

import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.builder.cfg.LauncherCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.RepoletCfg;

/**
 * a builder context for a launcher configuration 
 * @author pit
 *
 */
public class LauncherCfgBuilderContext implements RepoletCfgConsumer {
	private LauncherCfg cfg;
	
	/**
	 * @return - a fresh {@link LauncherCfgBuilderContext}
	 */
	public static LauncherCfgBuilderContext build() {
		return new LauncherCfgBuilderContext();
	}

	/**
	 * basic constructor
	 */
	public LauncherCfgBuilderContext() {
		cfg = new LauncherCfg();
	}
	@Override
	public void accept(RepoletCfg cfg) {
		this.cfg.getRepoletCfgs().add(cfg);
	}

	/**
	 * @return - a fresh {@link RepoletContext}
	 */
	public RepoletContext<LauncherCfgBuilderContext> repolet() {
		return new RepoletContext<>(this);
	}
	
	/**
	 * @param port - the port
	 * @return - the current {@link LauncherCfgBuilderContext}
	 */
	public LauncherCfgBuilderContext port( int port) {
		cfg.setPort(port);
		return this;
	}
	
	/**
	 * @return - a fully configured {@link Launcher}
	 */
	public Launcher done() {
		return Launcher.launcher(cfg);
	}
}
