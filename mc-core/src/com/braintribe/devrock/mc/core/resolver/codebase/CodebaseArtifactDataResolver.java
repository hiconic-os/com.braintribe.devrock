// ============================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
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
package com.braintribe.devrock.mc.core.resolver.codebase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.api.repository.CodebaseReflection;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.core.commons.McConversions;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.resolver.BasicArtifactDataResolution;
import com.braintribe.devrock.mc.core.resolver.BasicVersionInfo;
import com.braintribe.devrock.model.mc.reason.UnresolvedPart;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.version.Version;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.TemplateException;
import com.braintribe.utils.template.model.MergeContext;
import com.braintribe.utils.template.model.Sequence;
import com.braintribe.utils.template.model.TemplateNode;
import com.braintribe.utils.template.model.Variable;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

public class CodebaseArtifactDataResolver implements ArtifactDataResolver, InitializationAware, CodebaseReflection {
	
	private LoadingCache<EqProxy<ArtifactIdentification>, CodebaseArtifacts> codebaseArtifactsMap;

	private static final String ARTIFACT_ID = "artifactId";
	private static final String VERSION = "version";
	private static final String GROUP_ID = "groupId";
	private static final String GROUP_ID_EXP = "groupId.expanded";
	private static final Logger logger = Logger.getLogger(CodebaseArtifactDataResolver.class);
	
	private String repositoryId;
	private Template template;
	private Template versionReflectionTemplate;
	private Template groupReflectionTemplate;
	private boolean templateReflectsVersions;
	private boolean reflectsGroupArtifacts;
	private File codebaseRoot;
	private LinkedHashMap<String, Variable> variableIndex;
	private String defaultVersion;
	private DeclaredArtifactMarshaller marshaller = new DeclaredArtifactMarshaller();
	private Map<String, List<String>> groupIdToVersionMap = new ConcurrentHashMap<>(); 
	private Set<String> archetypesIncludes = Collections.emptySet();
	private Set<String> archetypesExcludes = Collections.emptySet();
	
	@Configurable
	public void setTemplate(Template template) {
		this.template = template;
	}
	
	@Configurable
	public void setArchetypesIncludes(Set<String> archetypesIncludes) {
		this.archetypesIncludes = archetypesIncludes;
	}
	
	@Configurable
	public void setArchetypesExcludes(Set<String> archetypesExcludes) {
		this.archetypesExcludes = archetypesExcludes;
	}
	
	@Configurable
	public void setDefaultVersion(String defaultVersion) {
		this.defaultVersion = defaultVersion;
	}
	
	private static class CodebaseArtifacts {
		ArtifactIdentification identification;
		Map<EqProxy<Version>, CodebaseArtifact> artifacts = new HashMap<>();
		
		
		LazyInitialized<Resource> mavenMetaDataResource = new LazyInitialized<>(this::buildMavenMetaDataResource);
		
		private Resource buildMavenMetaDataResource() {
			MavenMetaData mavenMetaData = MavenMetaData.T.create();
			mavenMetaData.setGroupId(identification.getGroupId());
			mavenMetaData.setArtifactId(identification.getArtifactId());
			
			List<Version> versions = getVersions();
			
			Versioning versioning = Versioning.T.create();
			versioning.setVersions(versions);
			
			versioning.setLastUpdated(McConversions.formatMavenMetaDataDate(new Date()));
			
			if (!versions.isEmpty()) {
				Version latest = versions.get(versions.size() - 1);
				versioning.setLatest(latest);
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			
			DeclaredMavenMetaDataMarshaller.INSTANCE.marshall(baos, mavenMetaData);
			
			byte[] byteArray = baos.toByteArray();
			
			Resource resource = Resource.createTransient(() -> new ByteArrayInputStream(byteArray));
			resource.setName("maven-metadata.xml");
			resource.setFileSize((long)byteArray.length);
			resource.setMimeType("text/xml");

			return resource; 
		}
		
		public List<Version> getVersions() {
			return artifacts.keySet().stream().<Version>map(EqProxy::get).sorted(Version::compareTo).collect(Collectors.toList());
		}
	}
	
	private static class CodebaseArtifact {
		CompiledArtifactIdentification identification;
		File folder;
	}
	
	@Override
	public void postConstruct() {
		if (templateReflectsVersions) {
			codebaseArtifactsMap = Caffeine.newBuilder().build(this::buildEmptyCodebaseArtifacts);
			codebaseArtifactsMap.putAll(scanCodebaseArtifacts(codebaseRoot));
		}
		else {
			codebaseArtifactsMap = Caffeine.newBuilder().build(this::findCodebaseArtifacts);
		}
	}

	private Map<EqProxy<ArtifactIdentification>, CodebaseArtifacts> scanCodebaseArtifacts(File folder) {
		Set<File> pomFiles = new HashSet<>();
		
		scanPomFiles(codebaseRoot, pomFiles);
		
		Map<EqProxy<ArtifactIdentification>, CodebaseArtifacts> codebaseArtifactsMap = new HashMap<>();
		
		for (File pomFile: pomFiles) {
			Maybe<CompiledArtifact> artifactPotential = DeclaredArtifactIdentificationExtractor.extractMinimalArtifact(pomFile);
			Maybe<CompiledArtifactIdentification> identificationPotential = DeclaredArtifactIdentificationExtractor.extractIdentification(pomFile);
			
			if (identificationPotential.isSatisfied()) {
				CompiledArtifact compiledArtifact= artifactPotential.get();
				
				boolean include = true;
				String archetype = compiledArtifact.getProperties().get("archetype");
				
				if (!archetypesIncludes.isEmpty()) {
					include = archetypesIncludes.contains(archetype);
				}
				if (!archetypesExcludes.isEmpty()) {
					include = !archetypesExcludes.contains(archetype);
				}
				
				if (!include)
					continue;
				
				EqProxy<ArtifactIdentification> key = HashComparators.artifactIdentification.eqProxy(compiledArtifact);
				CodebaseArtifacts codebaseArtifacts = codebaseArtifactsMap.computeIfAbsent(key, k -> {
					CodebaseArtifacts cas = new CodebaseArtifacts();
					cas.identification = ArtifactIdentification.from(compiledArtifact);
					return cas;
				});
				
				CodebaseArtifact codebaseArtifact = new CodebaseArtifact();
				codebaseArtifact.folder = pomFile.getParentFile();
				codebaseArtifact.identification = compiledArtifact;
				codebaseArtifacts.artifacts.put(HashComparators.version.eqProxy(compiledArtifact.getVersion()), codebaseArtifact);
			}
			else {
				logger.warn("Could not extract version from pom.xml [" + pomFile + "]. This may lead to an unresolvable codebase artifact");
			}
		}

		return codebaseArtifactsMap;
	}
	
	private void scanPomFiles(File folder, Set<File> pomFiles) {
		File pomCandidate = new File(folder, "pom.xml");
		
		if (pomCandidate.exists()) {
			pomFiles.add(pomCandidate);
			return;
		}
		
		File[] foundFiles = folder.listFiles();
		// NPE if no files found in folder averted
		if (foundFiles != null) {
			for (File file: foundFiles) {
				if (file.isDirectory())
					scanPomFiles(file, pomFiles);
			}
		}
	}

	private CodebaseArtifacts findCodebaseArtifacts(EqProxy<ArtifactIdentification> artifactIdentificationProxy) {
		ArtifactIdentification artifactIdentification = artifactIdentificationProxy.get();
		String groupId = artifactIdentification.getGroupId();
		String artifactId = artifactIdentification.getArtifactId();
		
		MergeContext mergeContext = buildMergeContext(groupId, artifactId, null);
		
		String normalizedEvaluatedPath = template.merge(mergeContext);
		String path = normalizedEvaluatedPath.replace('/', File.separatorChar);
		
		File codebaseSubFolder = new File(codebaseRoot, path);
		
		Map<EqProxy<ArtifactIdentification>, CodebaseArtifacts> codebaseMap = scanCodebaseArtifacts(codebaseSubFolder);
		
		return codebaseMap.computeIfAbsent(artifactIdentificationProxy, this::buildEmptyCodebaseArtifacts);
	}
	
	private CodebaseArtifacts buildEmptyCodebaseArtifacts(EqProxy<ArtifactIdentification> artifactIdentificationProxy) {
		CodebaseArtifacts codebaseArtifacts = new CodebaseArtifacts();
		codebaseArtifacts.identification = artifactIdentificationProxy.get();
		return codebaseArtifacts;
	}
	
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}
	
	@Override
	public List<VersionInfo> getVersions(ArtifactIdentification artifactIdentification) {
		EqProxy<ArtifactIdentification> key = HashComparators.artifactIdentification.eqProxy(artifactIdentification);
		
		CodebaseArtifacts codebaseArtifacts = codebaseArtifactsMap.get(key);
		
		List<VersionInfo> versionInfos = codebaseArtifacts.getVersions().stream() //
			.map(v -> new BasicVersionInfo(v, Collections.singletonList(repositoryId))) //
			.collect(Collectors.toList());
		
		return versionInfos;
	}
	
	@Override
	public Maybe<List<VersionInfo>> getVersionsReasoned(ArtifactIdentification artifactIdentification) {
		return Maybe.complete(getVersions(artifactIdentification));
	}

	@Override
	public Maybe<ArtifactDataResolution> resolvePart(CompiledArtifactIdentification artifactIdentification, PartIdentification partIdentification, Version partVersionOverride) {
		if (!HashComparators.partIdentification.compare(PartIdentifications.pom, partIdentification))
			return TemplateReasons.build(UnresolvedPart.T).enrich(r -> r.setPart(PartIdentification.from(partIdentification))).toMaybe();
		
		EqProxy<ArtifactIdentification> key = HashComparators.artifactIdentification.eqProxy(artifactIdentification);
		
		CodebaseArtifacts codebaseArtifacts = codebaseArtifactsMap.get(key);
		
		CodebaseArtifact codebaseArtifact = codebaseArtifacts.artifacts.get(HashComparators.version.eqProxy(artifactIdentification.getVersion()));
		
		if (codebaseArtifact == null)
			return Reasons.build(UnresolvedPart.T)
							.enrich(r -> r.setArtifact(artifactIdentification))
							.enrich(r -> r.setPart(partIdentification))
							.toMaybe();
		
		File pomFile = new File(codebaseArtifact.folder, "pom.xml"); 
		
		BasicArtifactDataResolution res = new BasicArtifactDataResolution();
		res.setRepositoryId(repositoryId);
		FileResource resource = FileResource.T.create();
		resource.setPath(pomFile.getAbsolutePath());
		resource.setName(pomFile.getName());
		resource.setFileSize(pomFile.length());			
		res.setResource(resource);
		return Maybe.complete(res);
	}

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(ArtifactIdentification artifactIdentification) {
		EqProxy<ArtifactIdentification> key = HashComparators.artifactIdentification.eqProxy(artifactIdentification);
		
		CodebaseArtifacts codebaseArtifacts = codebaseArtifactsMap.get(key);
		if (codebaseArtifacts.getVersions().isEmpty())
			return Reasons.build(NotFound.T).toMaybe();
		
		BasicArtifactDataResolution resolution = new BasicArtifactDataResolution(codebaseArtifacts.mavenMetaDataResource.get());
		resolution.setRepositoryId(repositoryId);
		
		return Maybe.complete(resolution);
	}

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(CompiledArtifactIdentification identification) {
		return Reasons.build(NotFound.T).toMaybe();
	}
	
	public CodebaseArtifactDataResolver(File codebaseRoot, String templateStr) {
		this.codebaseRoot = Objects.requireNonNull(codebaseRoot, () -> "codebaseRoot must not be null");
		try {
			template = Template.parse(templateStr);
			
			initializeVariableIndex(templateStr, template);
			
			if (templateReflectsVersions) {
				versionReflectionTemplate = buildTrimmedTemplate(VERSION);
			}
			
			if (reflectsGroupArtifacts) {
				groupReflectionTemplate = buildTrimmedTemplate(ARTIFACT_ID);
			}
			
			
		} catch (Exception e) {
			throw new IllegalArgumentException("Error while parsing template string", e);
		}
	}

	private Template buildTrimmedTemplate(String atVariable) {
		Sequence sequence = new Sequence();
		
		template.getRootNode().walk(n -> {
			if (n instanceof Sequence)
				return true;
			
			if (n instanceof Variable) {
				Variable variable = (Variable)n;
				if (variable.getVariableName().equals(atVariable))
				return false;
			}
			
			
			sequence.add(n);
			
			return true;
		});
		
		return new Template(sequence);
	}

	private void initializeVariableIndex(String templateStr, Template template) {
		TemplateNode rootNode = template.getRootNode();
		List<Variable> variables = new ArrayList<>();
		rootNode.collectVariables(variables);
		
		variableIndex = new LinkedHashMap<>();
		
		for (Variable variable: variables) {
			switch (variable.getVariableName()) {
				case GROUP_ID:
				case GROUP_ID_EXP:
				case ARTIFACT_ID:
				case VERSION:
					break;
				default:
					throw new IllegalArgumentException("Template " + templateStr + " contains unsupported variable: " + variable.getVariableName());
			}

			variableIndex.put(variable.getVariableName(), variable);
		}
		
		if (!variables.isEmpty() && variables.get(variables.size() - 1).getVariableName().equals(ARTIFACT_ID)) {
			reflectsGroupArtifacts = true;
		}

		templateReflectsVersions = variableIndex.containsKey(VERSION);
	}

	@Override
	public List<File> findGroupArtifacts(String groupId, String version) {
		if (!reflectsGroupArtifacts)
			throw new UnsupportedOperationException("group artifacts reflection is not supported by the configured template");
		
		return findFolders(groupReflectionTemplate, groupId, null, version);
	}

	private List<File> findFolders(Template template, String groupId, String artifactId, String version) {
		try {
			MergeContext mergeContext = buildMergeContext(groupId, artifactId, version);
			
			String normalizedEvaluatedPath = template.merge(mergeContext);
			String path = normalizedEvaluatedPath.replace('/', File.separatorChar);
			
			File file = new File(codebaseRoot, path);
			
			return Files.list(file.toPath()).filter(Files::isDirectory).map(Path::toFile).collect(Collectors.toList());
			
		} catch (TemplateException e) {
			throw new IllegalStateException("Error while evaluating template", e);
		} catch (IOException e) {
			// no directories found is a valid state for this resolver, it just means that the delegate must take over  : pit
			//throw new UncheckedIOException(e);
			return null;
		}
	}
	
	private MergeContext buildMergeContext(String groupId, String artifactId, String version) {
		MergeContext mergeContext = new MergeContext();
		mergeContext.setVariableProvider(name -> {
			switch (name) {
				case GROUP_ID:
					return groupId;
				case GROUP_ID_EXP:
					return groupId.replace('.', File.separatorChar);
				case ARTIFACT_ID:
					return artifactId;
				case VERSION:
					return version;
				default:
					throw new IllegalStateException("Template is in an unexpected state");
			}
		});
		return mergeContext;
	}

	private File findArtifactInternal(String groupId, String artifactId, String version) {
		MergeContext mergeContext = buildMergeContext(groupId, artifactId, version);
		
		try {
			String normalizedEvaluatedPath = template.merge(mergeContext);
			String path = normalizedEvaluatedPath.replace('/', File.separatorChar);
			
			File file = new File(codebaseRoot, path);
			
			if (file.exists())
				return file;
			else
				return null;
			
		} catch (TemplateException e) {
			throw new IllegalStateException("Error while evaluating template", e);
		}
	}
	
	@Override
	public File findArtifact(String groupId, String artifactId, String version) {
		return findArtifactInternal(groupId, artifactId, version);		
/*		
		if (templateReflectsVersions) {
		}
		else if (defaultVersion != null) {
			return findArtifactInternal(groupId, artifactId, defaultVersion);
		}
		else {		
			throw new UnsupportedOperationException("version lookup is not supported by the configured template");
		}
	*/	
	}

	@Override
	public File findArtifact(String groupId, String artifactId) {
		if (templateReflectsVersions)
			throw new UnsupportedOperationException("version lookup is not supported by the configured template");
		
		return findArtifactInternal(groupId, artifactId, null);
	}

	@Override
	public boolean reflectsVersions() {
		
		return templateReflectsVersions || defaultVersion != null;
	}
	
	@Override
	public boolean reflectsGroupArtifacts() {
		return reflectsGroupArtifacts;
	}

	@Override
	public List<PartReflection> getPartsOf(CompiledArtifactIdentification compiledArtifactIdentification) {	
		// can only contain the pom 
		File artifactDirectory = findArtifact( compiledArtifactIdentification.getGroupId(), compiledArtifactIdentification.getArtifactId(), compiledArtifactIdentification.getVersion().asString());
		if (artifactDirectory == null || !artifactDirectory.exists()) {
			return Collections.emptyList();
		}
		File pom = new File( artifactDirectory, "pom.xml");
		if (!pom.exists()) {
			return Collections.emptyList();
		}
		
		PartReflection pf = PartReflection.create(null, "pom", repositoryId);
		return Collections.singletonList( pf);
	}

	@Override
	public Maybe<ArtifactDataResolution> getPartOverview( CompiledArtifactIdentification compiledArtifactIdentification) {
		return Reasons.build(NotFound.T).toMaybe();
	}
	
}
