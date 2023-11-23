package com.braintribe.build.artifact.test.repolet;

import com.braintribe.cfg.Configurable;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class RavenhurstRepolet implements Repolet {
	public static final String MARKER_STOP = "rest/stop";
	public static final String MARKER_RESUME = "rest/resume";
	private static final String MARKER_RAVENHURST = "rest/changes";
	private String root ="contents";
	private String [] changes = new String [] {
			"com.braintribe.devrock.test.ravenhurst:a#1.0",			
			"com.braintribe.devrock.test.ravenhurst:b#1.0",
			/*
			"com.braintribe.devrock.test.ravenhurst:c",
			"com.braintribe.devrock.test.ravenhurst:test-terminal",
			*/
	};
	
	private enum State {stopped, running};
	private State currentState = State.running;
	
	@Configurable
	public void setRoot(String root) {
		this.root = root;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange, String path) throws Exception {
		if (path.length() == 0) {		
			exchange.setResponseCode(404);
			return;
		}			
		String subPath = path.substring( root.length() + 2);
		
		if ( subPath.startsWith( MARKER_RAVENHURST)) {
			switch (currentState) {
				case stopped: 
					exchange.setResponseCode( 404);
					break;				
				case running:
				default:
					// deliver update information
					exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/txt");
					StringBuffer buffer = new StringBuffer();
					for (String change : changes) {
						if (buffer.length() > 0) {
							buffer.append( "\n");
						}
						buffer.append( change);
					}
					exchange.getResponseSender().send( buffer.toString());				
					break;						
				}
		}
		else if (subPath.startsWith(MARKER_STOP)) {
			currentState = State.stopped;
			exchange.setResponseCode( 200);
			exchange.getResponseSender().send( "Stopped");
		}
		else if (subPath.startsWith( MARKER_RESUME)) {
			currentState = State.running;
			exchange.setResponseCode( 200);
			exchange.getResponseSender().send( "resume");
		}
		else {
			exchange.getResponseSender().send( "unknown : " + subPath);
			exchange.setResponseCode( 404);
		}
	}

}
