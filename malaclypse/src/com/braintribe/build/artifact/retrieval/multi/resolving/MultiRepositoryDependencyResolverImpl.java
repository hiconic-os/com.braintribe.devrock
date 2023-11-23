// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;
 
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.ArtifactReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.SolutionReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationListener;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.ravenhurst.data.RepositoryRole;

public class MultiRepositoryDependencyResolverImpl implements DependencyResolver,
																CacheAwareDependencyResolver,
																RedirectionAwareDependencyResolver,														
																DependencyResolverNotificationListener {
	
	private static Logger log = Logger.getLogger(MultiRepositoryDependencyResolverImpl.class);	
	private PomExpertFactory pomExpertFactory;
	private ArtifactPomReader pomReader;
	private Map<Dependency, Set<Solution>> solutionCache = new HashMap<Dependency, Set<Solution>>();	
	private Set<DependencyResolverNotificationListener> listeners;
	
	private RepositoryReflection repositoryRegistry;

	@Override @Configurable @Deprecated
	public void setPomExpertFactory(PomExpertFactory factory) {
		pomExpertFactory = factory;	
	}
	
	@Override @Required @Configurable
	public void setPomReader(ArtifactPomReader pomReader) {
		this.pomReader = pomReader;
	}
	
	@Configurable @Required
	public void setRepositoryRegistry(RepositoryReflection repositoryRegistry) {
		this.repositoryRegistry = repositoryRegistry;
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
			

	private Set<Solution> checkCache( Dependency dependency) {
		for (Dependency suspect : solutionCache.keySet()) {
			if (ArtifactProcessor.coarseDependencyEquals(dependency, suspect)) {
				return solutionCache.get( suspect);
			}		
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.retrieval.multi.MultiRepositoryDependencyResolver#clearCache()
	 */
	@Override
	public void clearCache() {		
		solutionCache.clear();
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.retrieval.multi.MultiRepositoryDependencyResolver#resolvePom(com.braintribe.model.artifact.Identification, com.braintribe.model.artifact.version.Version)
	 */
	@Override
	public Part resolvePom(String walkScopeId, Identification id, Version version) throws ResolvingException {
		Part part = ArtifactProcessor.createPartFromIdentification(id, version, PartTupleProcessor.createPomPartTuple());		
		return resolvePomPart( walkScopeId, part);
	}
	
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.retrieval.multi.MultiRepositoryDependencyResolver#resolvePomPart(com.braintribe.model.artifact.Part)
	 */
	@Override
	public Part resolvePomPart(String walkScopeId, Part pomPart) throws ResolvingException {
		
		SolutionReflectionExpert solutionExpert;
		Solution parent = Solution.T.create();
		ArtifactProcessor.transferIdentification(parent, pomPart);
			
		try {
			solutionExpert = repositoryRegistry.acquireSolutionReflectionExpert( parent);
		} catch (RepositoryPersistenceException e1) {
			String msg = "cannot retrieve solution expert for [" + NameParser.buildName( parent) + "]";
			throw new ResolvingException(msg, e1);
		}
		String expectedPartName = NameParser.buildFileName(pomPart);
		
		RepositoryRole repositoryRole = RepositoryRole.release;
		if (expectedPartName.endsWith( "-SNAPSHOT.pom")) {
			repositoryRole = RepositoryRole.snapshot;
		}		
		File file;
		//synchronized (this) { 
			try {
				file = solutionExpert.getPart(pomPart, expectedPartName, repositoryRole);
			} catch (RepositoryPersistenceException e1) {
				String msg="cannot retrieve file [" + expectedPartName + "]";
				throw new ResolvingException(msg, e1);
			}
		//}
		if (file != null) {			
			Solution solution;
			String pomFilePath = file.getAbsolutePath();
			try {
				solution = acquirePomReader().redirected( walkScopeId, pomFilePath);
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
		else {
			pomPart.setLocation(null);
			return pomPart;
		}			
		
	}

	private ArtifactPomReader acquirePomReader() {
		if (pomReader != null) {
			return pomReader;
		}
		if (pomExpertFactory != null) {
			pomReader = pomExpertFactory.getReader();					
			return pomReader;
		}
		throw new IllegalStateException("either a pom reader instance or its factory instance is required");
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
	

	/**
	 * actually resolve the dependency  
	 * @param walkScopeId - the walk's id
	 * @param dependency - the {@link Dependency}
	 * @param top - true if only the topmost (release & snapshot), false  all matching solutions
	 * @return - a {@link Set} of {@link Solution}
	 */
	private Set<Solution> resolveDependency(String walkScopeId, Dependency dependency, boolean top) {
		VersionRange dependencyRange = dependency.getVersionRange();
		
		Set<Solution> solutions = checkCache( dependency);
		if (
				(solutions != null) &&
				(solutions.size() > 0)
			){			
			if (log.isDebugEnabled()) {
				log.debug("Found cached solutions for dependency [" + NameParser.buildName(dependency, dependency.getVersionRange()) + "]");
			}
			return solutions;
		}
		
		
		RepositoryRole repositoryRole = RepositoryRoleExtractor.getRelevantRole(dependency);
		ArtifactReflectionExpert artifactExpert;
		try {
			artifactExpert = repositoryRegistry.acquireArtifactReflectionExpert(dependency);
		} catch (RepositoryPersistenceException e1) {
			String msg="cannot extract artifact expert for [" + NameParser.buildName((Identification) dependency);
			throw new ResolvingException(msg, e1);
		}
		List<Version> versions;
		try {
			// TODO : pass the range - so in case of a non-interval range, the expert may return a fake 
			versions = artifactExpert.getVersions(repositoryRole, dependencyRange);
		} catch (RepositoryPersistenceException e1) {
			String msg="cannot extract available versions for [" + NameParser.buildName(dependency);
			throw new ResolvingException(msg, e1);
		}
		
		Iterator<Version> iterator = versions.iterator();
		while (iterator.hasNext()) {
			Version version = iterator.next();
			if (!VersionRangeProcessor.matches(dependencyRange, version)) {
				iterator.remove();
			}			
		}
		
		if (solutions == null) {
			solutions = new HashSet<Solution>();
		}
		if (versions.size() == 0)
			return solutions;
		
		// 
		// only use the highest version 
		//
		if (top && versions.size() > 1 ) {
			versions = pruneVersionList( versions);			
		}
		
		for (Version version : versions) {
			Part part = Part.T.create();
			part.setGroupId( dependency.getGroupId());
			part.setArtifactId(dependency.getArtifactId());
			part.setType( PartTupleProcessor.createPomPartTuple());
			
			part.setVersion(version);
			Part resolved = resolvePomPart( walkScopeId, part);
			if (resolved != null && resolved.getLocation() != null) {
				Solution solution = Solution.T.create();
				ArtifactProcessor.transferIdentification(solution, part);
				solution.setVersion( part.getVersion());
				solution.getParts().add( resolved);
				solutions.add(solution);
			}
		}
		
		return solutions;
	}
	
	/**
	 * makes sure that only the highest version is used, both for release and snapshots
	 * @param versions - the version list as returned from the {@link ArtifactReflectionExpert}
	 * @return - a reduced list with only the highest version (single if only one kind of version)
	 */
	private List<Version> pruneVersionList(List<Version> versions) {
		List<Version> releaseVersions = new ArrayList<>();
		List<Version> snapshotVersions = new ArrayList<>();
		
		for (Version version : versions) {
			String versionAsString = VersionProcessor.toString(version);
			if (versionAsString.toUpperCase().endsWith( "SNAPSHOT")) {
				snapshotVersions.add(version);
			}
			else {
				releaseVersions.add(version);
			}
		}
		releaseVersions.sort( VersionProcessor.comparator);
		snapshotVersions.sort(VersionProcessor.comparator);
		List<Version> result = new ArrayList<>();
		
		int numReleases = releaseVersions.size();
		if (numReleases > 0) {
			result.add( releaseVersions.get( numReleases -1));
		}
		int numSnapshots = snapshotVersions.size();
		if (numSnapshots > 0) {
			result.add( snapshotVersions.get( numSnapshots -1));
		}
		
		
		return result;
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
