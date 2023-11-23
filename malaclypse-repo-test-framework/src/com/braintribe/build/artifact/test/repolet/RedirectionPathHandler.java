// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet;

import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * simple redirector, i.e. is configured via a {@link Map} of Prefix to {@link Repolet}, and redirects the calls to the appropriate Repolet 
 * @author pit
 *
 */
public class RedirectionPathHandler implements HttpHandler {

	private Map<String, Repolet> contextToHandlerMap;
	
	@Configurable @Required
	public void setContextToHandlerMap(Map<String, Repolet> contextToHandlerMap) {
		this.contextToHandlerMap = contextToHandlerMap;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		String path = exchange.getRequestPath();
		for (Entry<String, Repolet> entry : contextToHandlerMap.entrySet()) {
			if (path.substring(1).startsWith( entry.getKey())) {							
				entry.getValue().handleRequest(exchange, path);
				return;
			}
		}
		// 
		exchange.setResponseCode( 404);
		exchange.getResponseSender().send("404 : no repolet found for [" + path + "]");
		exchange.endExchange();
		
	}
	
	
}
