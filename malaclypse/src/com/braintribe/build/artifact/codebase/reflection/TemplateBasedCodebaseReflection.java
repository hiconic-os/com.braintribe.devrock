// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.codebase.reflection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import com.braintribe.build.artifact.representations.artifact.pom.marshaller.ArtifactPomMarshaller;
import com.braintribe.cfg.Configurable;
import com.braintribe.model.artifact.Solution;
import com.braintribe.utils.paths.PathCollectors;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.TemplateException;
import com.braintribe.utils.template.model.MergeContext;
import com.braintribe.utils.template.model.Sequence;
import com.braintribe.utils.template.model.TemplateNode;
import com.braintribe.utils.template.model.Variable;

public class TemplateBasedCodebaseReflection implements CodebaseReflection {
	private static final String ARTIFACT_ID = "artifactId";
	private static final String VERSION = "version";
	private static final String GROUP_ID = "groupId";
	private static final String GROUP_ID_EXP = "groupId.expanded";
	private Template template;
	private Template versionReflectionTemplate;
	private Template groupReflectionTemplate;
	private boolean templateReflectsVersions;
	private boolean reflectsGroupArtifacts;
	private File codebaseRoot;
	private LinkedHashMap<String, Variable> variableIndex;
	private String defaultVersion;
	private ArtifactPomMarshaller marshaller = new ArtifactPomMarshaller();
	private Map<String, List<String>> groupIdToVersionMap = new ConcurrentHashMap<>(); 
	
	@Configurable
	public void setDefaultVersion(String defaultVersion) {
		this.defaultVersion = defaultVersion;
	}
	
	public TemplateBasedCodebaseReflection(File codebaseRoot, String templateStr) {
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

	@Override
	public List<String> findVersions(String groupId, String artifactId) {
		
		if (templateReflectsVersions) {	
			List<File> versionDirectories = findFolders(versionReflectionTemplate, groupId, artifactId, null);
			if (versionDirectories == null) {
				// no directories found, no versions found - tell to use the delegate (if any) 
				return null;
			}
			List<String> versions = versionDirectories.stream().map( f -> f.getName()).collect( Collectors.toList());
			return versions;
		}
		else if (defaultVersion != null) {
			if (findArtifactInternal(groupId, artifactId, defaultVersion) != null) {
				return Collections.singletonList( defaultVersion);
			}
			else {
				return null;
			}
		}
		else {	
			List<String> versions = groupIdToVersionMap.get( groupId);
			if (versions != null) {
				return versions;
			}
			// 
			// quick fix.. in case of git, there is only one version there, and it must reflect major.minor from the parent 
			//
			String parentPath = PathCollectors.filePath.join( codebaseRoot.getAbsolutePath(), groupId, "Parent", "pom.xml");
			File pom = new File( parentPath);
			if (pom.exists()) {
				Solution parent;
				try {
					parent = marshaller.unmarshall( pom);
				} catch (XMLStreamException e) {
					throw new UnsupportedOperationException("version reflection failed to retrieve the version for artifact [" + groupId + ":" + artifactId);
				}
				String major = parent.getProperties().stream().filter( p -> {return p.getName().equalsIgnoreCase("major");}).findFirst().orElseThrow( () -> new IllegalStateException("no property [major] found in [" + parentPath + "]")).getRawValue();
				String minor = parent.getProperties().stream().filter( p -> {return p.getName().equalsIgnoreCase("minor");}).findFirst().orElseThrow( () -> new IllegalStateException("no property [minor] found  in [" + parentPath + "]")).getRawValue();
				
				versions = Collections.singletonList( major + "." + minor);
				groupIdToVersionMap.put(groupId, versions);
				return versions;
			}
			
			throw new UnsupportedOperationException("version reflection is not supported by the configured template");		
		}
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

}
