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

import java.util.HashMap;
import java.util.Map;

import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.logging.Logger;
import com.braintribe.utils.archives.ArchivesException;

import io.undertow.server.HttpServerExchange;

public class DescriptionBasedSwitchingRepolet extends AbstractDescriptionBasedRepolet {
	private static Logger log = Logger.getLogger(DescriptionBasedSwitchingRepolet.class);
	private static final String SWITCH_PARAMETER = "key=";
	private String initialIndex;
	private Map<String,Navigator> indexToNavigator = new HashMap<>();
	private String current;

	public void setInitialIndex(String initial) {
		this.initialIndex = initial;
		this.current = initial;
	}
	
	public void setContent( Map<String,RepoletContent> contents) {
		for (Map.Entry<String, RepoletContent> entry : contents.entrySet()) {
			Navigator navigator = new Navigator( entry.getValue());
			if (root != null) {
				navigator.setRoot(root);
			}
			indexToNavigator.put( entry.getKey(), navigator);
		}		
	}
	
	

	@Override
	public void setRoot(String root) {		
		super.setRoot(root);
		for (Navigator navigator : indexToNavigator.values()) {
			navigator.setRoot(root);
		}
	}

	@Override
	protected Navigator getNavigator() {		
		return indexToNavigator.get( current);
	}

	@Override
	protected void processUpdate(HttpServerExchange exchange) {
		String queryString = exchange.getQueryString();
		int p = queryString.indexOf( SWITCH_PARAMETER);
		if (p <0) {
			switchContents( exchange, initialIndex);
		}
		else {
			String key = queryString.substring(p + SWITCH_PARAMETER.length());
			switchContents(exchange, key);
		}	
	}

	/**
	 * increases the current index by one and switches the content to the respective archive
	 * @param exchange 
	 * @throws ArchivesException
	 */
	private void switchContents(HttpServerExchange exchange, String key) throws ArchivesException {
		Navigator navigator = this.indexToNavigator.get(key);
		if (navigator == null) {
			String msg = "no content found for key [" + key + "]. Not switching";
			log.warn(msg);
			exchange.getResponseSender().send( msg);
		}
		else {
			current = key;
			String msg = "switched content to key [" + key + "] to [" + current + "]";
			log.info(msg);
			exchange.getResponseSender().send( msg);
		}
		// reset already-dumped marker 
		dumpedContentAlreadyOnce = false;
	}	
}
