// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
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
package com.braintribe.devrock.mc.core.commons;

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledSolution;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.declared.License;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.lcd.StringTools;

public class ArtifactTreePrinter {
	protected static final String entryEdge = "\u2500\u2500\u2500";
	protected static final String edge = "\u251C\u2500\u2500";
	protected static final String lastEdge = "\u2514\u2500\u2500";
	protected static final String connector = "\u2502  ";
	protected static final String empty = "   ";
//	protected static final String check = "\u221a";
//	protected static final String mark = "\u2666";
	
	private boolean outputParts;
	private boolean outputLicense;
	
	private Map<EqProxy<License>, String> licenseIndex = new LinkedHashMap<>();
	
	private int licenseCounter = 1;

	public void setOutputParts(boolean outputParts) {
		this.outputParts = outputParts;
	}
	
	public void setOutputLicense(boolean outputLicense) {
		this.outputLicense = outputLicense;
	}
	
	static final HashingComparator<License> licenseComparator = EntityHashingComparator
			.build(License.T)
			.addField(License.name)
			.addField(License.url)
			.done();

	
	private String getLicenseReference(License license) {
		return licenseIndex.computeIfAbsent(licenseComparator.eqProxy(license), k -> "L" + licenseCounter++);
	}
	
	private Collection<License> licensesOf(AnalysisArtifact artifact) {
		Set<License> licenses = getDeclaredLicenses(artifact);
		
		if (!licenses.isEmpty())
			return licenses;
		
		Part jarPart = artifact.getParts().get(":jar");
		
		if (jarPart != null) {
			Resource resource = jarPart.getResource();
			if (resource instanceof FileResource) {
				FileResource fileResource = (FileResource)resource;
				try {
					ZipFile zipFile = new ZipFile(fileResource.getPath());
					
					for (String candidate: new String[]{"META-INF/LICENSE.txt", "META-INF/LICENSE"}) {
						ZipEntry entry = zipFile.getEntry(candidate);
						
						if (entry != null) {
							String name = readFirstTextBlock(() -> zipFile.getInputStream(entry));
							License license = License.T.create();
							license.setName(name);
							
							return Collections.singleton(license);
						}
					}
					
				} catch (IOException e) {
					// ignore
				}
			}
		}
		
		return Collections.emptySet();
	}
	
	private Set<License> getDeclaredLicenses(AnalysisArtifact artifact) {
		Set<EqProxy<License>> licenses = new HashSet<>();
		
		CompiledArtifact compiledArtifact = artifact.getOrigin();
		
		while (compiledArtifact != null) {
			
			for (License license: compiledArtifact.getOrigin().getLicenses()) {
				licenses.add(licenseComparator.eqProxy(license));
			}
			
			CompiledArtifact parent = Optional.ofNullable(compiledArtifact.getParentSolution()).map(CompiledSolution::getSolution).orElse(null);
			
			if (parent != null)
				compiledArtifact = parent;
			else
				compiledArtifact = null;
		}
		
		return licenses.stream().map(EqProxy::get).collect(Collectors.toSet());
	}

	private static String readFirstTextBlock(InputStreamProvider isProvider) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(isProvider.openInputStream(), "ISO-8859-1"))) {
			boolean readingCaption = false;
			StringBuilder builder = new StringBuilder();
			while (true) {
				String line = in.readLine().trim();
				
				if (line == null)
					return builder.toString();
				
				if (readingCaption) {
					if (!line.isEmpty()) { 
						builder.append(" ");
						builder.append(line);
					}
					else 
						return builder.toString();
				}
				else {
					if (!line.isEmpty()) { 
						builder.append(line);
						readingCaption = true;
					}
				}
			}
		} catch (Exception e) {
			return "";
		}
	}

	public void printDependencyTree(AnalysisArtifactResolution resolution) {
		
		StringBuilder padding = new StringBuilder();
		ConfigurableConsoleOutputContainer outputs = ConsoleOutputs.configurableSequence();
		Set<AnalysisArtifact> visited = new HashSet<>();

		for (AnalysisTerminal terminal: resolution.getTerminals()) {
			outputs.append(brightBlack(entryEdge));
			if (terminal instanceof AnalysisDependency) {
				AnalysisDependency terminalDependency = (AnalysisDependency)terminal;
				printDependencyTree(terminalDependency, terminalDependency.getSolution(), outputs, padding, false, visited);
			}
			else {
				AnalysisArtifact terminalArtifact = (AnalysisArtifact)terminal;
				printDependencyTree(null, terminalArtifact, outputs, padding, false, visited);
			}
		}
		
		if (outputLicense) {
			outputs.append("\nDetected Licenses\n");
			
			for (Map.Entry<EqProxy<License>, String> entry: licenseIndex.entrySet()) {
				License license = entry.getKey().get();
				String reference = entry.getValue();
				
				outputs.append(ConsoleOutputs.brightBlue(reference));
				outputs.append(": ");
				outputs.append(license.getName());

				String url = license.getUrl();
				if (url != null) {
					outputs.append(" ");
					outputs.append(ConsoleOutputs.yellow(url));
				}
				
				outputs.append("\n");
			}
		}
		
		ConsoleOutputs.println(outputs);
	}

	protected void printDependencyTree(AnalysisDependency dependency, AnalysisArtifact artifact, ConfigurableConsoleOutputContainer outputs, StringBuilder padding, boolean hasSuccessors, Set<AnalysisArtifact> visited) {
		if (artifact == null) {
			ConfigurableConsoleOutputContainer outputDependency = outputDependency(dependency);
			outputDependency.append(ConsoleOutputs.cyan(" -> "));
			outputDependency.append(ConsoleOutputs.brightRed("unresolved"));
			outputs.append(outputDependency);
			outputs.append("\n");
			return;
		}
		
		boolean alreadyTraversed = !visited.add(artifact);
		
		outputs.append(artifactNameColoredByType(dependency, artifact, alreadyTraversed)).append("\n");
		
		if (alreadyTraversed)
			return;

		List<AnalysisDependency> dependencies = artifact.getDependencies();
		
		int dependencyCount = dependencies.size();
		
		int i = 0;
		for (AnalysisDependency artifactDependency: dependencies) {
			boolean hasChildSuccessors = i++ < (dependencyCount - 1);
			if (hasSuccessors) {
				// has successive siblings
				padding.append(connector);
			}
			else {
				// last dependency
				padding.append(empty);
			}
			
			outputs.append(brightBlack(padding.toString())).append(brightBlack(hasChildSuccessors? edge: lastEdge));
			printDependencyTree(artifactDependency, artifactDependency.getSolution(), outputs, padding, hasChildSuccessors, visited);
			
			padding.setLength(padding.length() - 3);
		}
	}

	protected ConsoleOutput artifactNameColoredByType(AnalysisDependency dependency, AnalysisArtifact artifact, boolean alreadyTraversed) {

		ConfigurableConsoleOutputContainer solutionOutput = ConsoleOutputs.configurableSequence();
		
		if (dependency != null) {
			solutionOutput.append(outputDependency(dependency));
			solutionOutput.append(ConsoleOutputs.cyan(" -> "));
			solutionOutput.append(outputArtifact(dependency, artifact));
		}
		else {
			solutionOutput.append(outputArtifact(artifact));
		}
		
		if (outputParts) {
			if (!artifact.getParts().isEmpty()) {
				String partNames = artifact.getParts().values().stream() //
						.map(p -> PartIdentification.asString(p)).sorted().collect(Collectors.joining(", ", " (", ")"));
				solutionOutput.append(text(partNames));
			}
		}
		
		if (outputLicense) {
			Collection<License> licenses = licensesOf(artifact);
			if (licenses.isEmpty() && !"pom".equals(artifact.getPackaging())) {
				solutionOutput.append(ConsoleOutputs.brightRed(" missing license"));
			}
			else {
				solutionOutput.append(brightBlack(" licenses: "));
				int l = 0;
				for (License license: licenses) {
					if (l++ > 0)
						solutionOutput.append(", ");
					
					String reference = getLicenseReference(license);
					solutionOutput.append(ConsoleOutputs.brightBlue(reference));
				}
			}
		}

		if (alreadyTraversed) {
			solutionOutput.append(ConsoleOutputs.yellow(" already traversed"));
		}

		ConfigurableConsoleOutputContainer detailInfo = ConsoleOutputs.configurableSequence();

		solutionOutput.append(brightBlack(detailInfo));
		

		//detailInfo.append(" -> ").append(ConsoleOutputs.cyan(artifact.getNature().entityType().getShortName())).append(brightBlack(" -> "));

		return solutionOutput;
	}

	public ConfigurableConsoleOutputContainer outputDependency(AnalysisDependency dependency) {
		String groupId = dependency.getGroupId();
		String artifactId = dependency.getArtifactId();
		String version = dependency.getVersion();
		String type = PartIdentification.asString(dependency);
		String scope = dependency.getScope();
		
		return outputArtifact(groupId, artifactId, version, scope, type, dependency.hasFailed());
	}
	
	public ConfigurableConsoleOutputContainer outputArtifact(AnalysisArtifact artifact) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		String packaging = artifact.getPackaging();
		
		return outputArtifact(groupId, artifactId, version, null, packaging, artifact.hasFailed());
	}
	
	public ConfigurableConsoleOutputContainer outputArtifact(Artifact artifact) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		String packaging = artifact.getPackaging();
		
		return outputArtifact(groupId, artifactId, version, null, packaging, false);
	}
	
	public ConfigurableConsoleOutputContainer outputDependency(CompiledDependencyIdentification dependency) {
		String groupId = dependency.getGroupId();
		String artifactId = dependency.getArtifactId();
		String version = dependency.getVersion().asString();
		String type = null;
		String scope = null;
		if (dependency instanceof CompiledDependency) {
			CompiledDependency compiledDependency = (CompiledDependency)dependency;
			scope = compiledDependency.getScope();
			type = PartIdentification.asString(compiledDependency);
		}
		
		return outputArtifact(groupId, artifactId, version, scope, type, false);
	}
	
	public ConfigurableConsoleOutputContainer outputArtifact(CompiledArtifact artifact) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion().asString();
		String packaging = artifact.getPackaging();
		
		return outputArtifact(groupId, artifactId, version, null, packaging, false);
	}
	
	public ConfigurableConsoleOutputContainer outputArtifact(AnalysisDependency dependency, AnalysisArtifact artifact) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		String packaging = artifact.getPackaging();
		
		if (groupId.equals(dependency.getGroupId())) {
			groupId = null;
		}
		
		if (!artifact.hasFailed() && artifactId.equals(dependency.getArtifactId())) {
			artifactId = null;
		}
		
		return outputArtifact(groupId, artifactId, version, null, packaging, artifact.hasFailed());
	}
	
	/** {@code groupId}, {@code artifactId} and {@code version} are all optional. */
	public ConfigurableConsoleOutputContainer outputArtifact(String groupId, String artifactId, String version, String scope, String type, boolean error) {
		ConfigurableConsoleOutputContainer configurableSequence = ConsoleOutputs.configurableSequence();

		if (groupId != null)
			configurableSequence.append(brightBlack(groupId + ":"));

		if (artifactId != null)
			configurableSequence.append(text(artifactId));

		if (!StringTools.isEmpty(version))
			configurableSequence //
					.append(brightBlack("#")) //
					.append(isSnapshot(version) ? yellow(version) : green(version));
		
		if (scope != null)
			configurableSequence.append(brightBlack(" " + scope));
		
		if (type != null) 
			configurableSequence.append(brightBlack(" " + type));

		return configurableSequence;
	}

	protected boolean isSnapshot(String version) {
		return version.endsWith("-pc") || version.endsWith("-SNAPSHOT");
	}
}
