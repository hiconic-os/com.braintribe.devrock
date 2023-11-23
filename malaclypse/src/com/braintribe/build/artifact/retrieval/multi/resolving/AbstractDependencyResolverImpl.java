// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationListener;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.version.Version;

/**
 * an abstract implementation of the {@link DependencyResolver}, made for local repositories, <br/>
 * comes in two flavours :<br/>
 * {@link LocalRepositoryDependencyResolverImpl} - local repo <br/> 
 * {@link WorkingCopyDependencyResolverImpl} - working copy <br/>
 * @author pit
 *
 */
public abstract class AbstractDependencyResolverImpl implements DependencyResolver, 
																ChainableDependencyResolver,
																CacheAwareDependencyResolver,
																RedirectionAwareDependencyResolver,
																DependencyResolverNotificationListener {
	
	private Set<DependencyResolverNotificationListener> listeners = new HashSet<>();	
	protected LocalRepositoryLocationProvider locationExpert;
	protected DependencyResolver delegate;
	protected LocalRepositoryLocationProvider delegateLocationExpert;
	private PomExpertFactory pomExpertFactory;
	private ArtifactPomReader reader;
		
	@Override
	public void setPomExpertFactory(PomExpertFactory factory) {
		this.pomExpertFactory = factory;
	}
	
	@Configurable
	public void setPomReader( ArtifactPomReader reader) {
		this.reader = reader;
	}
	 @Configurable @Required
	public void setLocalRepositoryLocationProvider(LocalRepositoryLocationProvider expert) {
		this.locationExpert = expert;		
	}

	@Override
	public void addListener(DependencyResolverNotificationListener listener) {
		synchronized( listeners) {
			listeners.add( listener);
			
			if (delegate != null) {
				delegate.addListener(listener);
			}
		}
	}

	@Override
	public void removeListener(DependencyResolverNotificationListener listener) {
		synchronized( listeners) {
			listeners.remove(listener);
			
			if (delegate != null) {
				delegate.removeListener(listener);
			}
		}
	}

	@Override
	public void clearCache() {
	}
	
	
	@Override
	public void setDelegate(DependencyResolver delegate) {
		this.delegate =delegate;		
	}
	
	@Configurable
	public void setDelegateLocationExpert(LocalRepositoryLocationProvider delegateLocationExpert) {
		this.delegateLocationExpert = delegateLocationExpert;
	}
	
	/**
	 * returns different file name (*.pom for local, pom.xml for working copy)
	 * @param pomPart - the {@link Part} that reflects the pom 
	 * @return - the file name of the part
	 * @throws ResolvingException -
	 */
	protected abstract String getPomName(Part pomPart) throws ResolvingException;
	
	/**
	 * returns the different directory name (full version in local, only major, minor for working copy)
	 * @param pomPart - the {@link Part} whose location we need 
	 * @return - the path to the file
	 * @throws ResolvingException -
	 */
	protected abstract String getPartLocation( Part pomPart) throws ResolvingException;

	@Override
	public Part resolvePom(String walkScopeId, Identification id, Version version) throws ResolvingException {
		Part part = ArtifactProcessor.createPartFromIdentification(id, version, PartTupleProcessor.createPomPartTuple());		
		return resolvePomPart( walkScopeId, part);
	}

	@Override
	public Part resolvePomPart(String walkScopeId, Part pomPart) throws ResolvingException {
		try {								
			String partLocation = getPartLocation( pomPart);
			File file = partLocation != null ? new File(partLocation) : null;
			if (file != null && file.exists()) {		
				if (reader == null) {
					reader = pomExpertFactory.getReader();
				}
				
				Solution solution;
				String pomFilePath = file.getAbsolutePath();
				try {
					solution = reader.redirected( walkScopeId, pomFilePath);
				} catch (PomReaderException e) {
					String msg="cannot check for redirection for [" + pomFilePath + "]";
					throw new ResolvingException(msg, e);
				}
				pomPart.setLocation( pomFilePath);			
				if (solution == null) {
					return pomPart;
				}
				else {
					acknowledgeRedirection( walkScopeId, pomPart, solution);					
					return resolvePom(walkScopeId, solution, solution.getVersion());
				}
			}
			else {
					if (delegate != null) {
						pomPart = delegate.resolvePomPart(walkScopeId, pomPart);					
					}
					else {
						pomPart.setLocation(null);
					}
					return pomPart;
			}								
		} catch (Exception e) {
			String msg="cannot find local file location for pom part ("+e.getMessage()+")";
			throw new ResolvingException(msg, e);
		} 
	}

	@Override
	public Set<Solution> resolveTopDependency(String walkScopeId, Dependency dependency) throws ResolvingException {		
				
			if (delegate != null) {
				return delegate.resolveTopDependency(walkScopeId, dependency);				
			}
			else {
				throw new ResolvingException("a delegate is required");
			}
						
	}
	
	

	@Override
	public Set<Solution> resolveDependency(String walkScopeId, Dependency dependency) throws ResolvingException {	
		return resolveTopDependency(walkScopeId, dependency);
	}

	@Override
	public Set<Solution> resolveMatchingDependency(String walkScopeId, Dependency dependency) throws ResolvingException {
		if (delegate != null) {
			return delegate.resolveMatchingDependency(walkScopeId, dependency);				
		}
		else {
			throw new ResolvingException("a delegate is required");
		}
	}

	@Override
	public void acknowledgeRedirection(String walkScopeId, Part source, Solution target) {
		// listener : redirect 
		synchronized( listeners) {
			for (DependencyResolverNotificationListener listener : listeners) {
				listener.acknowledgeRedirection( walkScopeId, source, target);
			}
		}		
	}

	
	
}
