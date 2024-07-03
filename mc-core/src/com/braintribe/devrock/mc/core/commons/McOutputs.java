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
import static com.braintribe.console.ConsoleOutputs.brightBlue;
import static com.braintribe.console.ConsoleOutputs.configurableSequence;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;

public interface McOutputs {
	public static ConsoleOutput artifactIdentification(ArtifactIdentification artifact) {
		return groupAndArtifact(artifact.getGroupId(), artifact.getArtifactId());
	}
	
	public static ConsoleOutput groupAndArtifact(String groupId, String artifactId) {
		ConfigurableConsoleOutputContainer out = configurableSequence();
		
		if (groupId != null)
			out.append(brightBlack(groupId + ":"));
		
		out.append(text(artifactId));
		
		return out;
	}
	
	public static ConsoleOutput versionedArtifactIdentification(VersionedArtifactIdentification artifact) {
		VersionExpression expression = VersionExpression.parse(artifact.getVersion());
		
		if (expression instanceof Version)
			return artifact(artifact.getGroupId(), artifact.getArtifactId(), (Version)expression);
		else
			return dependency(artifact.getGroupId(), artifact.getArtifactId(), expression);
	}
	
	/** {@code groupId} and {@code version} are both optional. */
	public static ConsoleOutput dependency(String groupId, String artifactId, VersionExpression version) {
		return dependency(groupId, artifactId, version != null? version.asString(): null);
	}

	/** {@code groupId} and {@code version} are both optional. */
	public static ConsoleOutput dependency(String groupId, String artifactId, String version) {
		ConfigurableConsoleOutputContainer out = configurableSequence();
		
		out.append(groupAndArtifact(groupId, artifactId));
		
		if (version != null) {
			out //
			.append(brightBlack("#")) //
			.append(versionExpressionStr(version));
		}
		
		return out;
	}
	
	public static ConsoleOutput compiledDependencyIdentification(CompiledDependencyIdentification cdi) {
		return dependency(cdi.getGroupId(), cdi.getArtifactId(), cdi.getVersion());
	}
	
	public static ConsoleOutput compiledDependency(CompiledDependency cd) {
		ConfigurableConsoleOutputContainer out = configurableSequence();
		out.append(dependency(cd.getGroupId(), cd.getArtifactId(), cd.getVersion()));
		out.append("/");
		out.append(partIdentification(cd));
		
		return out;
	}
	
	public static ConsoleOutput analysisDependency(AnalysisDependency ad) {
		return dependency(ad.getGroupId(), ad.getArtifactId(), ad.getVersion());
	}
	
	public static ConsoleOutput analysisArtifact(AnalysisArtifact artifact) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();

		CompiledArtifact origin = artifact.getOrigin();
		
		if (origin != null)
			return artifact(groupId, artifactId, origin.getVersion()); 
		else
			return artifact(groupId, artifactId, artifact.getVersion()); 
	}
	
	public static ConsoleOutput compiledArtifactIdentification(CompiledArtifactIdentification artifact) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		
		return artifact(groupId, artifactId, artifact.getVersion());
	}
	
	public static ConsoleOutput partIdentification(PartIdentification part) {
		return classifierAndType(part.getClassifier(), part.getType());
	}
	
	public static ConsoleOutput classifierAndType(String classifier, String type) {
		ConfigurableConsoleOutputContainer out = ConsoleOutputs.configurableSequence();

		if (classifier != null)
			out.append(brightBlack(sequence(text(classifier), text(":"))));
		else
			out.append(brightBlack(":"));
		
		if (type != null)
			out.append(type);

		return out;
	}
	
	public static ConsoleOutput compiledPartIdentification(CompiledPartIdentification part) {
		return sequence(compiledArtifactIdentification(part), text("/"), partIdentification(part));
	}

	/** {@code groupId} and {@code version} are both optional. */
	public static ConsoleOutput artifact(String groupId, String artifactId, String version) {
		return artifact(groupId, artifactId, version != null? Version.parse(version): null);
	}
	
	/** {@code groupId} and {@code version} are both optional. */
	public static ConsoleOutput artifact(String groupId, String artifactId, Version version) {
		ConfigurableConsoleOutputContainer out = ConsoleOutputs.configurableSequence();
		
		out.append(groupAndArtifact(groupId, artifactId));
		
		if (version != null) {
			out //
			.append(brightBlack("#")) //
			.append(version(version));
		}
		
		return out;
	}
	
	public static ConsoleOutput version(Version version) {
		// if an anomalous expression has been stored, just return it
		String anomalousExpression = version.getAnonmalousExpression();
		if (anomalousExpression != null) {
			return green(text(anomalousExpression));
		}
	
		ConfigurableConsoleOutputContainer out = ConsoleOutputs.configurableSequence();
		
		out.append(Integer.toString(version.getMajor()));
		
		Integer minor = version.getMinor();
		Integer revision = version.getRevision();
		
		if (minor != null) {
			out.append(".");
			out.append(Integer.toString(minor));
			
			if (revision != null) {
				out.append(".");
				out.append(Integer.toString(revision));
			}
		}
		else {
			if (revision != null) {
				out.append( ".0.");
				out.append(Integer.toString(revision));
			}
		}
		
		String qualifier = version.getQualifier();
		if (qualifier != null) {
			out.append(brightBlack("-"));
			
			if (version.isPreliminary()) {
				out.append(yellow(qualifier));
			}
			else {
				out.append(qualifier);
			}
			
			int buildNumber = version.getBuildNumber(); // requires a qualifier
			if (buildNumber != 0) {
				out.append(brightBlack("-"));
				out.append(Integer.toString(buildNumber));
			}
		}
		
		String nonConform = version.getNonConform();
		if (nonConform != null) {
			out.append( nonConform);
		}
		
		return green(out);
	}
	
	public static ConsoleOutput versionExpression(VersionExpression expression) {
		return versionExpressionStr(expression.asString());
	}
	
	public static ConsoleOutput versionExpressionStr(String expression) {
		return brightBlue(expression);
	}
	
	public static ConsoleOutput versionStr(String version) {
		return version(Version.parse(version));
	}
	
}
