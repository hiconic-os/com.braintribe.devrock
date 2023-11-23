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
package com.braintribe.devrock.repolet.descriptive;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.model.repolet.content.RepoletContent;

import io.undertow.server.HttpServerExchange;

public class DescriptionBasedRepolet extends AbstractDescriptionBasedRepolet {
	private RepoletContent repoletContent;
	private Navigator navigator;
	
	@Configurable @Required
	public void setContent(RepoletContent content) {
		this.repoletContent = content;
		this.navigator = new Navigator(repoletContent);
	}

	@Override
	public void setRoot(String root) {	
		super.setRoot(root);
		this.navigator.setRoot(root);
	}

	protected Navigator getNavigator() {
		return navigator;
	}
	
	@Override
	protected void processUpdate(HttpServerExchange exchange) {
		throw new UnsupportedOperationException( "[" + this.getClass().getName() + "] doesn't support content updates");
		
	}
}
