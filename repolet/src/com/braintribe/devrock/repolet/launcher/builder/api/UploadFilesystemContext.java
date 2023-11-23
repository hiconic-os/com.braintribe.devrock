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

import com.braintribe.devrock.repolet.launcher.builder.cfg.FilesystemCfg;


/**
 * a builder context for the {@link FilesystemCfg} 
 * @author pit
 *
 * @param <T>
 */
public class UploadFilesystemContext<T extends UploadFilesystemCfgConsumer> {
	private FilesystemCfg cfg;
	private T consumer;
	
	public UploadFilesystemContext( T consumer) {
		this.consumer = consumer;
		cfg = new FilesystemCfg();
	}
	
	/**
	 * @param filesystem - a directory
	 * @return - itself
	 */
	public UploadFilesystemContext<T> filesystem( File filesystem) {		
		cfg.setFilesystem(filesystem);
		return this;		
	}
	
	public T close() {
		consumer.acceptForUpload(cfg);
		return consumer;
	}
}
