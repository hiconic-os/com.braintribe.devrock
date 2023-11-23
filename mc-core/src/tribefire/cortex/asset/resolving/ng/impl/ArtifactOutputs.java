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
