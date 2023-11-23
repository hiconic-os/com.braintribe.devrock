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
