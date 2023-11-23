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
package com.braintribe.devrock.repolet.launcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.devrock.mc.api.event.EntityEventListener;
import com.braintribe.devrock.mc.api.event.EventContext;
import com.braintribe.devrock.mc.api.event.EventHub;
import com.braintribe.devrock.model.repolet.event.launcher.OnRepoletLaunchedEvent;
import com.braintribe.devrock.model.repolet.event.launcher.OnRepoletLaunchingEvent;
import com.braintribe.devrock.repolet.AbstractRepolet;
import com.braintribe.devrock.repolet.Repolet;
import com.braintribe.devrock.repolet.descriptive.DescriptionBasedRepolet;
import com.braintribe.devrock.repolet.descriptive.DescriptionBasedSwitchingRepolet;
import com.braintribe.devrock.repolet.folder.FolderBasedRepolet;
import com.braintribe.devrock.repolet.folder.FolderBasedSwitchingRepolet;
import com.braintribe.devrock.repolet.launcher.builder.api.LauncherCfgBuilderContext;
import com.braintribe.devrock.repolet.launcher.builder.cfg.DescriptiveContentCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.FilesystemCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.IndexedDescriptiveContentCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.IndexedFilesystemCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.LauncherCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.RepoletCfg;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.util.network.NetworkTools;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;

/**
 * cfg driven launcher for repolets. Use the build function to set one up 
 * 
 * @author pit
 *
 */
public class Launcher extends EventHub implements EntityEventListener<GenericEntity> {
	private static Logger log = Logger.getLogger(Launcher.class);
	private LauncherCfg cfg;
	private Undertow server;
	private String host = "localhost";
	private String url = "http://" + host + ":${env.port}/";
	private Map<String,String> launchedRepolets;
	private boolean isRunning = false;

	public static LauncherCfgBuilderContext build() {
		return LauncherCfgBuilderContext.build();
	}
	/**
	 * @param cfg - the {@link LauncherCfg} to use
	 * @return - the {@link Launcher}
	 */
	public static Launcher launcher(LauncherCfg cfg) {
		Launcher launcher = new Launcher();
		launcher.setLauncherCfg( cfg);
		return launcher;
	}

	/**
	 * @param cfg - the {@link LauncherCfg}
	 */
	private void setLauncherCfg(LauncherCfg cfg) {
		this.cfg = cfg;		
	}
	
	/**
	 * @return - {@link LauncherCfg}
	 */
	public LauncherCfg getLaunchedCfg() {
		return cfg;
	}
	
	/**
	 * @return - a {@link Map} of repolet name (id) and its actual URL
	 */
	public Map<String, String> getLaunchedRepolets() {
		return launchedRepolets;
	}
	
	public RepoletCfg getCfgOfRepoletPerName(String name) {
		for (RepoletCfg rCfg: cfg.getRepoletCfgs()) {
			if (rCfg.getName().equals( name))
				return rCfg;
		}
		return null;
	}
	
	/**
	 * @return - a {@link Map} of the repolet's name to the actual URL of it
	 */
	public Map<String,String> launch() {
		
		OnRepoletLaunchingEvent launchingEvent = OnRepoletLaunchingEvent.T.create();
		
		sendEvent( launchingEvent);
		
		
		// no port declared, get's auto-determined
		if (cfg.getPort() < 0) {
			cfg.setPort(NetworkTools.getUnusedPortInRange(8080, 8100));
		}
		String activeUrl = url.replace( "${env.port}", Integer.toString( cfg.getPort()));
		
		Map<String, Repolet> prefixToRepoletInstanceMap = new HashMap<>( cfg.getRepoletCfgs().size());
		Map<String, String> prefixToUrlMap = new HashMap<>(cfg.getRepoletCfgs().size());
		
		for (RepoletCfg rCfg : cfg.getRepoletCfgs()) {
			List<IndexedFilesystemCfg> indexedFilesystems = rCfg.getIndexedFilesystems();
			if (indexedFilesystems != null && indexedFilesystems.size() > 1) {
				log.warn("currently only one indexed file system per repolet is supported. Only the first one is used, others are ignored");
			}
			List<FilesystemCfg> filesystems = rCfg.getFilesystems();		
			if (filesystems != null && filesystems.size() > 1) {
				log.warn("currently only one file system per repolet is supported. Only the first one is used, others are ignored");
			}
			
			List<DescriptiveContentCfg> descriptiveContentDescriptions = rCfg.getDescriptiveContentDescriptions();
			if (descriptiveContentDescriptions != null && descriptiveContentDescriptions.size() > 1) {
				log.warn("currently only one descriptive content per repolet is supported. Only the first one is used, others are ignored");
			}
			
			List<IndexedDescriptiveContentCfg> indexedDescriptiveContentDescriptions = rCfg.getIndexedDescriptiveContentDescriptions();
			if (indexedDescriptiveContentDescriptions != null && indexedDescriptiveContentDescriptions.size() > 1) {
				log.warn("currently only one indexed descriptive content per repolet is supported. Only the first one is used, others are ignored");
			}
			
			
	
			AbstractRepolet repolet;
			if (indexedFilesystems != null && indexedFilesystems.size()>0) {
				IndexedFilesystemCfg indexedFilesystem = indexedFilesystems.get(0);
				repolet = new FolderBasedSwitchingRepolet();				
				((FolderBasedSwitchingRepolet) repolet).setInitialIndex(indexedFilesystem.getInitial());							
				((FolderBasedSwitchingRepolet) repolet).setContent( indexedFilesystem.getKeyToFile());
			} 
			else if (filesystems != null && filesystems.size() > 0){
				FilesystemCfg fCfg = filesystems.get(0);
				repolet = new FolderBasedRepolet();				
				((FolderBasedRepolet) repolet).setContent( fCfg.getFilesystem());
				((FolderBasedRepolet) repolet).setUseExternalHashData( fCfg.getUseExternalHashes());
			}	
			else if (indexedDescriptiveContentDescriptions != null && indexedDescriptiveContentDescriptions.size() > 0) {
				repolet = new DescriptionBasedSwitchingRepolet();
				((DescriptionBasedSwitchingRepolet)repolet).setContent( indexedDescriptiveContentDescriptions.get(0).getKeyToContent());
				((DescriptionBasedSwitchingRepolet)repolet).setInitialIndex( indexedDescriptiveContentDescriptions.get(0).getInitial());
			}
			else if ( descriptiveContentDescriptions != null && descriptiveContentDescriptions.size()> 0){
				repolet = new DescriptionBasedRepolet();
				((DescriptionBasedRepolet)repolet).setContent( descriptiveContentDescriptions.get(0).getRepoletContent());
			}
			else {
				throw new UnsupportedOperationException("not implemented yet");
			}			
			
			repolet.setRoot( rCfg.getName());
			repolet.setActualPort( cfg.getPort());				
			repolet.setChangesUrl(rCfg.getChangesUrl());
			repolet.setRestApiUrl(rCfg.getRestApiUrl());
			repolet.setServerIdentification( rCfg.getServerIdentification());
			repolet.setDateToResponseMap( rCfg.getDateToContentFile());
			repolet.setHashOverrides( rCfg.getHashOverrides());
			repolet.setHashesInHeader(rCfg.getNoHashesInHeader());
			repolet.setUploadReturnCodeOverrides( rCfg.getUploadReturnValuesOverride());
			
			if (rCfg.getOverridingReponseCode() != null) {
				repolet.setOverridingReponseCode( rCfg.getOverridingReponseCode());
			}
			
			repolet.setListener( this);
			
			FilesystemCfg uploadFilesystem = rCfg.getUploadFilesystem();
			if (uploadFilesystem != null) {
				repolet.setUploadContent( uploadFilesystem.getFilesystem());
			}
			
			
			prefixToRepoletInstanceMap.put( rCfg.getName(), repolet);				
			prefixToUrlMap.put( rCfg.getName(), activeUrl + rCfg.getName());
			
		}		
		// setup the redirection handler & launch the stuff... 
		if (prefixToRepoletInstanceMap.size() > 0) {
			RedirectionPathHandler handler = new RedirectionPathHandler();
			handler.setContextToHandlerMap(prefixToRepoletInstanceMap);
			launch( host, cfg.getPort(), handler);			
		}
		launchedRepolets = prefixToUrlMap;
		
		OnRepoletLaunchedEvent onLaunched = OnRepoletLaunchedEvent.T.create();
		onLaunched.setLaunchedRepoletUrls(launchedRepolets);
		sendEvent(onLaunched);
		
		// 
		isRunning = true;
		return prefixToUrlMap;		
	}
	
	
	/**
	 * launches a repolet 
	 * @param host - the host (localhost)
	 * @param port - the port as determined
	 * @param handler - the {@link HttpHandler}, i.e. the repolet
	 */
	private void launch(String host, int port, HttpHandler handler) {
		try {
			
			server = Undertow.builder()
					.addHttpListener( port, host)
					.setHandler(e -> e.dispatch(handler))				      
					.build();
			server.start();
		} catch (Throwable e) {
			String msg = "Cannot launch redirection handler at [" + host + ":" + port + "]";
			log.error( msg, e);
			throw new IllegalStateException(msg, e);
		}
	}
	
	/**
	 * stops the server 
	 */
	public void shutdown() {
		server.stop();
		isRunning = false;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public int getAssignedPort() {
		if (cfg == null)
			throw new IllegalStateException("no configuration present");
		return cfg.getPort();	
	}
	@Override
	public void onEvent(EventContext eventContext, GenericEntity event) {
		sendEvent(event);		
	}
	
	

	
}
