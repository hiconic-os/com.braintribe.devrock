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
package com.braintribe.model.processing.panther.depmgt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolverFactory;
import com.braintribe.build.artifact.retrieval.multi.resolving.ResolvingException;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationListener;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.model.processing.panther.SvnUtil;


public class PublishWalkDependencyResolver implements DependencyResolver, DependencyResolverFactory {
	
	private static Comparator<SourceArtifact> sourceArtifactComparator = Comparator
			.comparing(SourceArtifact::getGroupId)
			.thenComparing(SourceArtifact::getArtifactId)
			.thenComparing(SourceArtifact::getVersion);

	private File svnWalkCache;
	private NavigableSet<SourceArtifact> artifactsToBePublished;
	private Map<SourceArtifact, Solution> svnSolutionCache = new TreeMap<>(sourceArtifactComparator);
	private DependencyResolver delegate;
	
	public void addListener(DependencyResolverNotificationListener listener) {
		delegate.addListener(listener);
	}

	public void removeListener(DependencyResolverNotificationListener listener) {
		delegate.removeListener(listener);
	}

	
	public Part resolvePomPart(String walkScopeId, Part pomPart) throws ResolvingException {
		return delegate.resolvePomPart(walkScopeId, pomPart);
	}
/*
	public void setPomExpertFactory(PomExpertFactory factory) {
		delegate.setPomExpertFactory(factory);
	}
	
*/
	@Configurable @Required
	public void setDelegate(DependencyResolver delegate) {
		this.delegate = delegate;
	}
	
	@Configurable @Required
	public void setArtifactsToBePublished(Set<SourceArtifact> artifactsToBePublished) {
		this.artifactsToBePublished = new TreeSet<>(sourceArtifactComparator);
		this.artifactsToBePublished.addAll(artifactsToBePublished);
	}

	@Configurable @Required
	public void setSvnWalkCache(File svnWalkCache) {
		this.svnWalkCache = svnWalkCache;
	}
	
	
	@Override
	public Set<Solution> resolveMatchingDependency(String walkScopeId, Dependency dependency) throws ResolvingException {
		throw new UnsupportedOperationException("not implemented for this resolver");
	}

	@Override
	public Set<Solution> resolveTopDependency(String walkScopeId, Dependency dependency) throws ResolvingException {
		SourceArtifact artifactToBePublished = lookupArtifactToBePublished(dependency);
		if (artifactToBePublished != null) {
			try {
				return Collections.singleton(getSolutionFromSvn(artifactToBePublished));
			} catch (Exception e) {
				throw new ResolvingException("error while building solution from artifact to be published", e);
			}
		}
		else
			return delegate.resolveTopDependency(walkScopeId, dependency);
	}
	
	private Solution getSolutionFromSvn(SourceArtifact artifactToBePublished) throws Exception {
		Solution solution = svnSolutionCache.computeIfAbsent(artifactToBePublished, this::createSolutionFromSvn);
		return solution;
	}
	
	private Solution createSolutionFromSvn(SourceArtifact artifactToBePublished) {
		try {
			File file = svnExport(artifactToBePublished);
			
			try {
				return createSolution(artifactToBePublished, file);
			}
			finally {
				file.delete();
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File svnExport(SourceArtifact artifactToBePublished) throws IOException {
		String url = getSvnUrl(artifactToBePublished);
		File file = new File(svnWalkCache, artifactToBePublished.getArtifactId() + "-" + UUID.randomUUID().toString() + ".pom");
		
		SvnUtil.export(url, file);

		return file;
	}

	private String getSvnUrl(SourceArtifact sourceArtifact) {
		SourceRepository repository = sourceArtifact.getRepository();
		
		String base = repository.getRepoUrl();
		String path = sourceArtifact.getPath();
		
		String url = concat('/', base, path, "pom.xml");
		
		return url;
	}

	private Solution createSolution(SourceArtifact artifactToBePublished, File tempFile) throws Exception {
		String versionAsString = readActualVersion(artifactToBePublished, tempFile);
		
		File svnCachefile = getSvnCacheFile(artifactToBePublished.getGroupId(), artifactToBePublished.getArtifactId(), versionAsString);
		svnCachefile.getParentFile().mkdirs();
		Files.copy(tempFile.toPath(), svnCachefile.toPath());
		
		Version version = VersionProcessor.createFromString(versionAsString);
		
		Solution solution = Solution.T.create();
		solution.setGroupId(artifactToBePublished.getGroupId());
		solution.setVersion(version);
		solution.setArtifactId(artifactToBePublished.getArtifactId());
		
		Part part = Part.T.create();
		part.setGroupId(artifactToBePublished.getGroupId());
		part.setArtifactId(artifactToBePublished.getArtifactId());
		part.setVersion(version);
		part.setType(PartTupleProcessor.createPomPartTuple());
		part.setLocation(svnCachefile.getAbsolutePath());
		
		solution.getParts().add(part);
		return solution;
	}
	
	private File getSvnCacheFile(String groupId, String artifactId, String versionAsString) {
		String filePath = Stream
			.concat(Stream.of(groupId.split("\\.")), Stream.of(artifactId, versionAsString, artifactId))
			.collect(Collectors.joining(File.separator)) + "-" + versionAsString + ".pom";
		
		return new File(svnWalkCache, filePath);
	}

	private String readActualVersion(SourceArtifact artifactToBePublished, File file) throws FileNotFoundException, Exception {
		String revision = Poms.readProperties(file, (p,v) -> {
			if (p.equals("rev"))
				return v;
			else
				return null;
		});

		if (revision != null)
			return artifactToBePublished.getVersion() + '.' + revision;

		String version = Poms.readVersion(file);

		if (version == null)
			throw new VersionProcessingException("could not determine actual full version for: " + artifactToBePublished);
		
		return version;
	}

	private String concat(char separator, String... fragments) {
		StringBuilder builder = new StringBuilder();
		
		for (String fragment: fragments) {
			if (builder.length() > 0 && builder.charAt(builder.length() - 1) != separator) {
				builder.append(separator);
			}
			builder.append(fragment);
		}
		
		return builder.toString();
	}
	
	private SourceArtifact lookupArtifactToBePublished(SourceArtifact candidateArtifact) {
		SortedSet<SourceArtifact> subSet = artifactsToBePublished.subSet(candidateArtifact, true, candidateArtifact, true);
		
		return subSet.isEmpty()?
				null:
				subSet.first();
	}

	private SourceArtifact lookupArtifactToBePublished(Dependency dependency) {
		Version version = getSimplifiedVersion(dependency); 
		
		return lookupArtifactToBePublished(dependency, version);
	}

	private Version getSimplifiedVersion(Dependency dependency) {
		VersionRange versionRange = dependency.getVersionRange();
		
		Version version = versionRange.getInterval()? 
				versionRange.getMinimum(): 
				versionRange.getDirectMatch();
		return version;
	}

	@Override
	public Part resolvePom(String walkScopeId, Identification id, Version version) throws ResolvingException {
		if (lookupArtifactToBePublished(id, version) != null) {
			String versionAsString = VersionProcessor.toString(version);
			
			File file = getSvnCacheFile(id.getGroupId(), id.getArtifactId(), versionAsString);
			
			Part part = PartProcessor.createPartFromIdentification(id, version, PartTupleProcessor.createPomPartTuple());
			part.setLocation(file.getAbsolutePath());
			
			return part;
		}
		else
			return delegate.resolvePom(walkScopeId, id, version);
	}

	private SourceArtifact lookupArtifactToBePublished(Identification id, Version version) {
		SourceArtifact candidate = sourceArtifactFrom(id, version);
		return lookupArtifactToBePublished(candidate);
	}

	private SourceArtifact sourceArtifactFrom(Identification id, Version version) {
		SourceArtifact candidate = SourceArtifact.T.create();
		candidate.setGroupId(id.getGroupId());
		candidate.setArtifactId(id.getArtifactId());
		candidate.setVersion(VersionProcessor.toString(version));
		return candidate;
	}

	@Override
	public DependencyResolver get() throws RuntimeException {
		return this;
	}

}
