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
