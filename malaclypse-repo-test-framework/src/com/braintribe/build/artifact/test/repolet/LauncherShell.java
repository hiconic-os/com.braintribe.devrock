// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.archives.ArchivesException;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;

public class LauncherShell {
	
	public enum RepoType { singleZip, switchZip, multiZip, ravenhurst}
	private Undertow server;
	private int port = -1;
	 	 
	 public static void main(final String[] args) {
		 LauncherShell shell = new LauncherShell( NetworkTools.getUnusedPortInRange(8080, 8100));
		shell.launch( args, RepoType.singleZip);
    }
	 
	 	
	 
	 public LauncherShell(int port) {
		 this.port = port;
	}
	 
	 public void setPort(int port) {
		this.port = port;
	}
	 
	 public int getPort() {
		return port;
	}
	 
	 public List<Repolet> launch(final String[] args, RepoType repoType) {
		List<Repolet> result = new ArrayList<Repolet>();
		for (String arg : args) {				
			String host = "localhost";
			if (port < 0) {
				port = NetworkTools.getUnusedPortInRange(8080, 8100);
			}
			System.out.println("port="+port);
			
			String [] repolets = arg.split( ";");
			Map<String, Repolet> prefixToRepoletMap = new HashMap<String, Repolet>( repolets.length);
			for (String repolet : repolets) {
				try {
					String [] data = repolet.split(",");
					String prefix = data[0];
					
					switch ( repoType) {
						case multiZip: {
							throw new IllegalArgumentException(" multi zip repos are not supported yet");
						}
						case ravenhurst: {
							RavenhurstRepolet ravenhurstRepolet = new RavenhurstRepolet();
							ravenhurstRepolet.setRoot(prefix);
							prefixToRepoletMap.put(prefix, ravenhurstRepolet);
							result.add(ravenhurstRepolet);
							break;
						}
						case switchZip :{
							ZipBasedSwitchingRepolet zipRepolet = new ZipBasedSwitchingRepolet();
							String [] values = data[1].split( "\\|");
							File [] files = new File[ values.length];
							for (int i = 0; i < values.length; i++) {
								files[i] = new File( values[i]);
							}
							zipRepolet.setContent(files);
							zipRepolet.setRoot(prefix);
							prefixToRepoletMap.put(prefix, zipRepolet);
							result.add(zipRepolet);
							break;
						}
						default:  {
							ZipBasedRepolet zipRepolet = new ZipBasedRepolet();
							zipRepolet.setContent( new File( data[1]));
							zipRepolet.setRoot(prefix);
							prefixToRepoletMap.put(prefix, zipRepolet);
							result.add(zipRepolet);
							break;
						}
					}								
					
				} catch (ArchivesException e) {
					System.err.println("cannot configure repolet from [" + repolet + "]");
				}
			}
			if (prefixToRepoletMap.size() > 0) {
				RedirectionPathHandler handler = new RedirectionPathHandler();
				handler.setContextToHandlerMap(prefixToRepoletMap);
				launch( host, port, handler);
			}								
		}	
		return result;
    }
	 
	 public Map<String, Repolet> launch(Map<String, RepoType> args) {
				
			String host = "localhost";
			if (port < 0) {
				port = NetworkTools.getUnusedPortInRange(8080, 8100);
			}
			
			Map<String, Repolet> prefixToRepoletMap = new HashMap<String, Repolet>( args.size());
		
			for (Entry<String, RepoType> entry : args.entrySet()) {				
				String [] data = entry.getKey().split(",");
				String prefix = data[0];
				
				
				switch ( entry.getValue()) {
					case multiZip: {
						throw new IllegalArgumentException(" multi zip repos are not supported yet");
					}
						
					case switchZip :{
						ZipBasedSwitchingRepolet zipRepolet = new ZipBasedSwitchingRepolet();
						String [] values = data[1].split( "\\|");
						File [] files = new File[ values.length];
						for (int i = 0; i < values.length; i++) {
							files[i] = new File( values[i]);
						}
						try {
							zipRepolet.setContent(files);
						} catch (ArchivesException e) {
							throw new RuntimeException("cannot setup switching repolet", e);
						}
						zipRepolet.setRoot(prefix);
						prefixToRepoletMap.put(prefix, zipRepolet);						
						break;
					}
					default:  {
						ZipBasedRepolet zipRepolet = new ZipBasedRepolet();
						try {
							zipRepolet.setContent( new File( data[1]));
						} catch (ArchivesException e) {
							throw new RuntimeException("cannot setup standard repolet", e);
						}
						zipRepolet.setRoot(prefix);
						prefixToRepoletMap.put(prefix, zipRepolet);
						break;
					}
				}									
									
			}	
			if (prefixToRepoletMap.size() > 0) {
				RedirectionPathHandler handler = new RedirectionPathHandler();
				handler.setContextToHandlerMap(prefixToRepoletMap);
				launch( host, port, handler);
			}								
			return prefixToRepoletMap;
	    }

	
	 
	private void launch(String host, int port, HttpHandler handler) {
		try {
			
			server = Undertow.builder()
					.addHttpListener( port, host)
					.setHandler( handler)				      
					.build();
			server.start();
		} catch (Exception e) {
			System.err.println("Cannot launch redirection handler at [" + host + ":" + port + "]");
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		server.stop();
	}
}
