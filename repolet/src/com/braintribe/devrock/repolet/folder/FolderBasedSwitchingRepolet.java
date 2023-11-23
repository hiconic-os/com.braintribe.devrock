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
package com.braintribe.devrock.repolet.folder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.logging.Logger;
import com.braintribe.utils.archives.ArchivesException;

import io.undertow.server.HttpServerExchange;

/**
 * an {@link AbstractFolderBasedRepolet} that can switch its content on request.
 * @author pit
 *
 */
public class FolderBasedSwitchingRepolet extends AbstractFolderBasedRepolet {
	private static Logger log = Logger.getLogger(FolderBasedSwitchingRepolet.class);
	private static final String SWITCH_PARAMETER = "key=";
	private Map<String,File> contents = new HashMap<>();
	private File current;	
	private String initialIndex;
	
	public void setInitialIndex(String initial) {
		this.initialIndex = initial;		
	}
	
	public void setContent( Map<String,File> contents) {
		this.contents.putAll(contents);
	}
	
	@Override
	protected File getContent() {
		if (current == null) {
			current = this.contents.get( initialIndex);
		}
		if (current == null) {
			throw new IllegalStateException("no data found for initial index [" + initialIndex + "]");
		}
		return current;
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
		File file = this.contents.get(key);
		if (file == null) {
			String msg = "no content found for key [" + key + "]. Not switching";
			log.warn(msg);
			exchange.getResponseSender().send( msg);
		}
		else {
			current = file;
			String msg = "switched content to key [" + key + "] to [" + current.getAbsolutePath() + "]";
			log.info(msg);
			exchange.getResponseSender().send( msg);
		}
	}	

}
