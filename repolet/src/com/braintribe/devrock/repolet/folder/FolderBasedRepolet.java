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
package com.braintribe.devrock.repolet.folder;

import java.io.File;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

import io.undertow.server.HttpServerExchange;

/**
 * an {@link AbstractFolderBasedRepolet} with a stable content
 * @author pit
 *
 */
public class FolderBasedRepolet extends AbstractFolderBasedRepolet {	
	private File content;

	@Required @Configurable
	public void setContent(File content) {
		this.content = content;
	}
	
	@Override
	protected File getContent() {
		return content;
	}

	@Override
	protected void processUpdate(HttpServerExchange exchange) {
		throw new UnsupportedOperationException( "[" + this.getClass().getName() + "] doesn't support content updates");
		
	}
	
}
