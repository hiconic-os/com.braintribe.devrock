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
package tribefire.cortex.asset.resolving.ng.impl;

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.Version;

public abstract class ArtifactOutputs {

	public static ConsoleOutputContainer solution(AnalysisArtifact solution) {
		String groupId = solution.getGroupId();
		String artifactId = solution.getArtifactId();

		return solution(groupId, artifactId, solution.getOrigin().getVersion());
	}
	
	public static ConfigurableConsoleOutputContainer solution(CompiledArtifactIdentification artifact) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		
		return solution(groupId, artifactId, artifact.getVersion());
	}
	
	public static ConfigurableConsoleOutputContainer part(CompiledPartIdentification part) {
		return solution(part) // 
			.append(" ") //
			.append(PartIdentification.asString(part)); //
	}

	/** {@code groupId} and {@code version} are both optional. */
	public static ConfigurableConsoleOutputContainer solution(String groupId, String artifactId, String version) {
		return solution(groupId, artifactId, version != null? Version.parse(version): null);
	}
	
	/** {@code groupId} and {@code version} are both optional. */
	public static ConfigurableConsoleOutputContainer solution(String groupId, String artifactId, Version version) {
		ConfigurableConsoleOutputContainer configurableSequence = ConsoleOutputs.configurableSequence();
		
		if (groupId != null)
			configurableSequence.append(brightBlack(groupId + ":"));
		
		configurableSequence.append(text(artifactId));
		
		if (version != null) {
			configurableSequence //
			.append(brightBlack("#")) //
			.append(version(version));
		}

		return configurableSequence;
	}

	public static ConsoleOutput version(Version version) {
		String versionAsStr = version.asString();
		return version.isPreliminary() ? yellow(versionAsStr) : green(versionAsStr);
	}

	public static ConsoleOutput version(String version) {
		return version.endsWith("-pc") ? yellow(version) : green(version);
	}
	
	public static ConsoleOutput fileName(String fileName) {
		return yellow(fileName);
	}

}
