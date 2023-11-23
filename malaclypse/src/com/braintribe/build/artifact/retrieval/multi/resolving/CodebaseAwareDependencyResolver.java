// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.codebase.reflection.CodebaseReflection;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationListener;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;

/**
 * @author pit
 *
 */
public class CodebaseAwareDependencyResolver implements DependencyResolver, 
																			ChainableDependencyResolver,
																			RedirectionAwareDependencyResolver, 
																			DependencyResolverNotificationListener {
	
	private static Logger log = Logger.getLogger(CodebaseAwareDependencyResolver.class);
	private DependencyResolver delegate;
	private Set<DependencyResolverNotificationListener> listeners;
	private PomExpertFactory pomExpertFactory;
	
	private CodebaseReflection codebaseReflection;
	
	@Configurable @Required
	public void setCodebaseReflection(CodebaseReflection codebaseReflection) {
		this.codebaseReflection = codebaseReflection;
	}
	

	
	@Override
	public void addListener(DependencyResolverNotificationListener listener) {
		if (listeners == null) {
			listeners = new HashSet<DependencyResolverNotificationListener>();
		}
		listeners.add(listener);
	}

	@Override
	public void removeListener(DependencyResolverNotificationListener listener) {
		listeners.remove(listener);
		if (listeners.isEmpty())
			listeners = null;
	}
		

	@Override
	public void setPomExpertFactory(PomExpertFactory factory) {
		this.pomExpertFactory = factory;
		
	}

	@Override
	public Part resolvePom(String walkScopeId, Identification id, Version version) throws ResolvingException {
		Part pomPart = Part.T.create();
		pomPart.setGroupId( id.getGroupId());
		pomPart.setArtifactId(id.getArtifactId());
		pomPart.setVersion( version);
		pomPart.setType( PartTupleProcessor.createPomPartTuple());		
		return resolvePomPart(walkScopeId, pomPart);	
	}
	
	private File extractPomFile(String groupId, String artifactId, String version) {		
		File artifactHome = codebaseReflection.findArtifact(groupId, artifactId, version);
		File pom = new File( artifactHome, "pom.xml");
		if (!pom.exists()) {
			return null;
		}
		return pom;
	}

	@Override
	public Part resolvePomPart(String walkScopeId, Part pomPart) throws ResolvingException {
		File pomFile = extractPomFile(pomPart.getGroupId(), pomPart.getArtifactId(), VersionProcessor.toString( pomPart.getVersion()));
		if (pomFile == null) {
			if (delegate != null) {
				return delegate.resolvePomPart(walkScopeId, pomPart);
			}
		} 
		else {			
			Solution solution;
			String pomFilePath = pomFile.getAbsolutePath();
			try {
				solution = pomExpertFactory.getReader().redirected( walkScopeId, pomFilePath);
			} catch (PomReaderException e) {
				String msg="cannot check for redirection for [" + pomFilePath + "]";
				throw new ResolvingException(msg, e);
			}
			pomPart.setLocation( pomFilePath);			
			if (solution == null) {
				return pomPart;
			}
			else {
				acknowledgeRedirection(walkScopeId, pomPart, solution);					
				return resolvePom( walkScopeId, solution, solution.getVersion());
			}
		}	
		pomPart.setLocation(null);		
		return pomPart;
	}
	
	private List<String> getDependencyVersionsFromFilesystem( File dependencyHome, String artifactId) {
		File [] versions = dependencyHome.listFiles( new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				return false;
			}
		});
		List<String> result = new ArrayList<String>();
		for (File version : versions) {
			File artifact = new File( version, artifactId);
			if (artifact.exists()) {
				result.add( version.getName());
			}
		}
		return result;
	}
	
	

	@Override
	public Set<Solution> resolveDependency(String walkScopeId, Dependency dependency) throws ResolvingException {	
		return resolveTopDependency(walkScopeId, dependency);
	}



	@Override
	public Set<Solution> resolveMatchingDependency(String walkScopeId, Dependency dependency) throws ResolvingException {
		return resolveDependency(walkScopeId, dependency, false);
	}

	@Override
	public Set<Solution> resolveTopDependency(String walkScopeId, Dependency dependency) throws ResolvingException {
		return resolveDependency(walkScopeId, dependency, true);
	}
		

	
	private Set<Solution> resolveDependency(String walkScopeId, Dependency dependency, boolean topOnly) throws ResolvingException {
		// find directory 
		List<String> versions = extractVersionsForDependency(walkScopeId, dependency);
		if (versions == null || versions.isEmpty()) {
			if (delegate != null) {
				return delegate.resolveTopDependency(walkScopeId, dependency);
			}
			else {
				return Collections.emptySet();
			}
		
		}
		
		VersionRange range = dependency.getVersionRange();
		List<Solution> solutions = new ArrayList<Solution>();		
		for (String sVersion : versions) {
			Version version = VersionProcessor.createFromString( sVersion);
			if (VersionRangeProcessor.matches(range, version)) {
				// as we have only the major.minor versions on the directory level, we must determine the 
				// revision for the actual version. 		
				File artifactHome = codebaseReflection.findArtifact(dependency.getGroupId(), dependency.getArtifactId(), sVersion);
				if (artifactHome == null) {
					throw new ResolvingException("cannot determine revision of [" + dependency.getGroupId() + ":" + dependency.getArtifactId() + "#" + sVersion +"] as it doesnt' exist");
				}
				File pomFile = new File( artifactHome, "pom.xml"); 
				try {
					Solution solution = pomExpertFactory.getReader().readPom(walkScopeId, pomFile);
					if (VersionRangeProcessor.matches(range, solution.getVersion())) {
						solutions.add( solution);
					}
					else {
						log.warn("no match on actual version [" + NameParser.buildName(solution) + "], guessed version [" + sVersion + "]");
					}
				} catch (PomReaderException e) {
					throw new ResolvingException("cannot determine revision of [" + pomFile.getAbsolutePath() + "]", e);
					
				}
			}
		}
		
	
		// sort ascending acc version
		solutions.sort( new Comparator<Solution>() {

			@Override
			public int compare(Solution o1, Solution o2) {			
				return VersionProcessor.compare(o1.getVersion(), o2.getVersion());
			}
		});
		
		if (topOnly && !solutions.isEmpty()) { 
			// take the highest from the list
			Solution m = solutions.get( solutions.size()-1);
			Set<Solution> result = new HashSet<>();
			result.add( m);
			return result;
		}
			
		if (delegate != null) {
			// add the single one that the delegate may deliver
			
			Set<Solution> delegatesSolutions;
			if (topOnly) 
				delegatesSolutions = delegate.resolveTopDependency(walkScopeId, dependency);
			else
				delegatesSolutions = delegate.resolveMatchingDependency(walkScopeId, dependency);
			
			if (!topOnly && delegatesSolutions != null && !delegatesSolutions.isEmpty()) {
				solutions.addAll(delegatesSolutions);									
			}
		}
	
		// hashset expected 
 		return new HashSet<>(solutions);
	}

	private void addToSolutionSetIfNotPresent(Set<Solution> solutions, Set<Solution> delegatesSolutions) {
		List<String> existingVersions = solutions.stream().map( s -> VersionProcessor.toString( s.getVersion())).collect( Collectors.toList());
		for (Solution delegateSolution : delegatesSolutions) {
			String version = VersionProcessor.toString( delegateSolution.getVersion());
			if (!existingVersions.contains(version)) {
				solutions.add(delegateSolution);
			}
		}		
	}



	private List<String> extractVersionsForDependency(String walkScopeId, Dependency dependency) {
		List<String> versions = codebaseReflection.findVersions(dependency.getGroupId(), dependency.getArtifactId());
						
		if (versions == null || versions.size() == 0) {
			return null;
		}
		return versions;
	}

	@Override
	public void setDelegate(DependencyResolver delegate) {
		this.delegate = delegate;
	}
	
	
	@Override
	public void acknowledgeRedirection(String walkScopeId, Part source, Solution target) { 
		if (listeners != null) {
			for (DependencyResolverNotificationListener listener : listeners) {
				listener.acknowledgeRedirection( walkScopeId, source, target);
			}
		}		
	}
	
	
	
}
