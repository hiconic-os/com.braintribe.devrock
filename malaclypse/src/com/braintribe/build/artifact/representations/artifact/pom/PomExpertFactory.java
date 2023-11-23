// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.codec.sax.SaxArtifactPomExpertRegistry;
import com.braintribe.build.artifact.representations.artifact.pom.codec.stax.StaxArtifactPomExpertRegistry;
import com.braintribe.build.artifact.representations.artifact.pom.codec.stax.staged.StagedStaxArtifactPomExpertRegistry;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationBroadcaster;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactory;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheInstance;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Solution;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;


public class PomExpertFactory implements PomReaderNotificationBroadcaster, PomReaderNotificationListener {
	private Supplier<DependencyResolver> dependencyResolverFactory;	
	private SaxArtifactPomExpertRegistry saxRegistry;
	private boolean enforceParentResolving = true;
	private Set<PomReaderNotificationListener> listeners;
	private VirtualPropertyResolver externalPropertyResolver;
	private CacheFactory cacheFactory;
	private ArtifactPomReader reader;
	private MavenSettingsReader settingsReader;
	private boolean detectParentLoops = false;
	private boolean identifyArtifactOnly = false;
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	private StaxArtifactPomExpertRegistry staxRegistry;
	private StagedStaxArtifactPomExpertRegistry stagedStaxRegistry;
	private boolean pomReaderLeniency = false;
	
	
	public void setReaderLeniency(boolean leniency) {
		pomReaderLeniency = leniency;
	}
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	@Required @Configurable
	public void setCacheFactory(CacheFactory factory) {
		this.cacheFactory = factory;
	}
	
	@Configurable
	public void setExternalPropertyResolver(VirtualPropertyResolver externalPropertyResolver) {
		this.externalPropertyResolver = externalPropertyResolver;
	}
	
	@Configurable
	public void setEnforceParentResolving(boolean enforceParentResolving) {
		this.enforceParentResolving = enforceParentResolving;
	}
	
	@Configurable
	public void setIdentifyArtifactOnly(boolean identifyArtifactOnly) {
		this.identifyArtifactOnly = identifyArtifactOnly;
	}
		
	@Configurable @Required
	public void setDependencyResolverFactory(Supplier<DependencyResolver> dependencyResolverFactory) {	
		this.dependencyResolverFactory = dependencyResolverFactory;		
	}
	
	@Configurable @Required
	public void setSettingsReader(MavenSettingsReader settingsReader) {
		this.settingsReader = settingsReader;
	}
	
	@Configurable
	public void setDetectParentLoops(boolean detectParentLoops) {
		this.detectParentLoops = detectParentLoops;
	}
	
	public void forceNewPomReaderInstance() {
		reader = null;
	}
	
	private SaxArtifactPomExpertRegistry getSaxRegistry() {
		if (saxRegistry != null)
			return saxRegistry;
		synchronized( this) {
			if (saxRegistry == null)				
				saxRegistry = new SaxArtifactPomExpertRegistry();
		}
		return saxRegistry;
	}
	
	private StaxArtifactPomExpertRegistry getStaxRegistry() {
		if (staxRegistry != null)
			return staxRegistry;
		synchronized( this) {
			if (staxRegistry == null)				
				staxRegistry = new StaxArtifactPomExpertRegistry();
		}
		return staxRegistry;
	}
	
	private StagedStaxArtifactPomExpertRegistry getStagedStaxxRegistry() {
		if (stagedStaxRegistry != null)
			return stagedStaxRegistry;
		synchronized( this) {
			if (stagedStaxRegistry == null)				
				stagedStaxRegistry = new StagedStaxArtifactPomExpertRegistry();
		}
		return stagedStaxRegistry;
	}
	
	private Object initializingMonitor = new Object();
		
	public ArtifactPomReader getReader() {		
		
		if (reader != null) {
			return reader;
		}
		
		synchronized (initializingMonitor) {
			if (reader != null) {
				return reader;
			}
	
			ArtifactPomReader _reader = new ArtifactPomReader();
			_reader.setVirtualEnvironment(virtualEnvironment);
			// set both registry, may drop one later
			_reader.setStaxRegistry( getStaxRegistry());
			_reader.setSaxRegistry( getSaxRegistry());
			_reader.setStagedStaxRegistry(getStagedStaxxRegistry());
	
			try {
				_reader.setCache(cacheFactory.get());
			} catch (RuntimeException e) {
				_reader.setCache( new CacheInstance());
			}
			
			_reader.setLeniency(pomReaderLeniency);
			_reader.setDetectParentLoops(detectParentLoops);
			_reader.setIdentifyArtifactOnly(identifyArtifactOnly);
			
			_reader.setSettingsExpert(settingsReader);		
			try {
				_reader.setDependencyResolver(dependencyResolverFactory.get());
			} catch (RuntimeException e) {
				throw new RuntimeException("cannot retrieve dependency resolver", e);
			}
			_reader.setEnforceParentResolving( enforceParentResolving);
			_reader.setExternalPropertyResolverOverride(externalPropertyResolver);
			_reader.addListener(this);
			reader = _reader;
		}
	
	
		return reader;
	}

	@Override
	public void addListener(PomReaderNotificationListener listener) {	
		synchronized (this) {
			if (listeners == null) {
				listeners = new HashSet<PomReaderNotificationListener>();			
			}
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(PomReaderNotificationListener listener) {
		synchronized (this) {
			if (listeners == null)
				return;
			listeners.remove(listener);
			if (listeners.isEmpty()) 
				listeners = null;
		}
	}

	@Override
	public void acknowledgeReadErrorOnFile(String walkScopeId, String location, String reason) {
		synchronized (this) {
			if (listeners == null)
				return;
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeReadErrorOnFile(walkScopeId, location, reason);
			}
		}
		
	}

	@Override
	public void acknowledgeReadErrorOnArtifact(String walkScopeId, Artifact artifact, String reason) {
		synchronized (this) {
			if (listeners == null)
				return;
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeReadErrorOnArtifact(walkScopeId, artifact, reason);
			}
		}
	}

	@Override
	public void acknowledgeReadErrorOnString(String walkScopeId, String contents, String reason) {
		synchronized (this) {
			if (listeners == null)
				return;
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeReadErrorOnString(walkScopeId, contents, reason);
			}
		}
	}

	@Override
	public void acknowledgeVariableResolvingError(String walkScopeId, Artifact artifact, String expression) {
		synchronized (this) {
			if (listeners == null)
				return;
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeVariableResolvingError(walkScopeId, artifact, expression);
			}
		}
	}

	@Override
	public void acknowledgeSolutionAssociation(String walkScopeId, String location, Artifact artifact) {
		synchronized (this) {
			if (listeners == null)
				return;
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeSolutionAssociation(walkScopeId, location, artifact);
			}		
		}
	}

	@Override
	public void acknowledgeParentAssociation(String walkScopeId, Artifact child, Solution parent) {
		synchronized (this) {
			if (listeners == null)
				return;
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeParentAssociation(walkScopeId, child, parent);
			}		
		}
	}
	
	

	@Override
	public void acknowledgeParentAssociationError(String walkScopeId, Artifact child, String groupId, String artifactId, String version) {
		synchronized (this) {
			if (listeners == null)
				return;
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeParentAssociationError(walkScopeId, child, groupId, artifactId, version);
			}			
		}
		
	}

	@Override
	public void acknowledgeImportAssociation(String walkScopeId, Artifact requestingSolution, Solution requestedSolution) {
		synchronized (this) {
			if (listeners == null)
				return;
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeImportAssociation(walkScopeId, requestingSolution, requestedSolution);
			}		
		}		
	}

	@Override
	public void acknowledgeImportAssociationError(String walkScopeId, Artifact requestingSolution, String groupId, String artifactId, String version) {
		synchronized (this) {
			if (listeners == null)
				return;
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeImportAssociationError(walkScopeId, requestingSolution, groupId, artifactId, version);
			}			
		}
	}
	
	
}
