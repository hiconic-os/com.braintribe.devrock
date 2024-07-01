// ============================================================================
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
