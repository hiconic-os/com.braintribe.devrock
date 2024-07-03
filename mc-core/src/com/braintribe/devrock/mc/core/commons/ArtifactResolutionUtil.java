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

import static com.braintribe.console.ConsoleOutputs.brightRed;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.TraversingContext;

public class ArtifactResolutionUtil {
	private static final ArtifactTreePrinter artifactTreePrinter = new ArtifactTreePrinter();

	/**
	 * Returns a copy of given {@link AnalysisArtifactResolution resolution} containing only resolution paths relevant for this resolution's
	 * {@link AnalysisArtifactResolution#getIncompleteArtifacts() incomplete artifacts} and
	 * {@link AnalysisArtifactResolution#getUnresolvedDependencies() unresolved dependencies}.
	 */
	public static AnalysisArtifactResolution trimToFailures(AnalysisArtifactResolution resolution) {
		return trim(resolution, resolution.getIncompleteArtifacts(), resolution.getUnresolvedDependencies());
	}

	/**
	 * Returns a copy of given {@link AnalysisArtifactResolution resolution} containing only resolution paths relevant for given
	 * {@link AnalysisArtifact artifacts} and {@link AnalysisDependency dependencies}. This can be used to trim the resolution to just the information
	 * relevant for given artifacts, e.g. those with some problems.
	 * 
	 * @see #trimToFailures(AnalysisArtifactResolution)
	 */
	public static AnalysisArtifactResolution trim( //
			AnalysisArtifactResolution resolution, Iterable<AnalysisArtifact> includedArtifacts, Iterable<AnalysisDependency> includedDeps) {

		return resolution.clone(new ResolutionTrimmingContext(includedArtifacts, includedDeps));
	}
	
	private static class ResolutionTrimmingContext extends StandardCloningContext implements Matcher {
		private final Set<ArtifactIdentification> included = new HashSet<>();
		
		public ResolutionTrimmingContext(Iterable<AnalysisArtifact> artifacts, Iterable<AnalysisDependency> deps) {
			setMatcher(this);

			collectIncludedArtifacts(artifacts);
			collectIncludedDeps(deps);
		}

		private void collectIncludedArtifacts(Iterable<AnalysisArtifact> artifacts) {
			for (AnalysisArtifact artifact : artifacts)
				collectIncluded(artifact);
		}

		private void collectIncludedDeps(Iterable<AnalysisDependency> deps) {
			for (AnalysisDependency dependency : deps)
				collectIncluded(dependency);
		}
		
		private void collectIncluded(AnalysisDependency dependency) {
			if (!included.add(dependency))
				return;
			
			AnalysisArtifact dependerArtifact = dependency.getDepender();
			if(dependerArtifact != null)
				collectIncluded(dependerArtifact);
		}
		
		private void collectIncluded(AnalysisArtifact artifact) {
			// TODO: revise this ...ok if a terminal itself has an issue in the CPR (here, it's used to get a jar) 
			// it is not properly collected here, because the terminal has depender (a dependency) but this 
			// dependency has no depender (artifact) hence this leads to a NPE. 			
			if (artifact == null)
				return;
			
			if (!included.add(artifact))
				return;

			collectIncludedDeps(artifact.getDependers());
		}

		@Override
		public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
				GenericEntity instanceToBeCloned, GenericEntity clonedInstance,
				AbsenceInformation sourceAbsenceInformation) {
			
			return !property.getName().equals("origin");
		}
		
		@Override
		public boolean matches(TraversingContext traversingContext) {
			Object value = traversingContext.getObjectStack().peek();
			
			if (value instanceof ArtifactIdentification) {
				return !included.contains(value);
			}
			
			return false;
		}
	}

	public static void printFailedResolution(AnalysisArtifactResolution resolution) {
		List<Reason> rootCauses = getRootCauses(resolution.getFailure());
	
		for (Reason reason: rootCauses) {
			
			ConsoleOutputs.println(sequence(
					brightRed("Error: "),
					text(reason.getText())
			));
		}
		
		ConsoleOutputs.println("\nDependency paths to the errors\n");
		printTrimmedResolution(resolution, resolution.getIncompleteArtifacts(), resolution.getUnresolvedDependencies());
	}

	public static void printTrimmedResolution( //
			AnalysisArtifactResolution resolution, Iterable<AnalysisArtifact> includedArtifacts, Iterable<AnalysisDependency> includedDeps) {
		
		AnalysisArtifactResolution trimmedResolution = trim(resolution, includedArtifacts, includedDeps);
		artifactTreePrinter.printDependencyTree(trimmedResolution);
	}

	public static void printDependencyTree(AnalysisArtifactResolution resolution) {
		artifactTreePrinter.printDependencyTree(resolution);
	}
	
	private static List<Reason> getRootCauses(Reason failure) {
		Set<Reason> visited = new HashSet<>();
		List<Reason> causes = new ArrayList<>();
		
		scanForRootCauses(failure, causes, visited);
		return causes;
	}

	private static void scanForRootCauses(Reason reason, List<Reason> causes, Set<Reason> visited) {
		if (!visited.add(reason))
			return;
		
		if (reason.getReasons().isEmpty()) {
			causes.add(reason);
			return;
		}
		
		for (Reason cause: reason.getReasons()) {
			scanForRootCauses(cause, causes, visited);
		}
	}

	public static ConfigurableConsoleOutputContainer outputTerminal(CompiledTerminal terminal) {
		if (terminal instanceof CompiledDependencyIdentification) {
			return outputDependency((CompiledDependencyIdentification) terminal);
		}
		else {
			return outputArtifact((CompiledArtifact) terminal);
		}
	}
	
	public static ConfigurableConsoleOutputContainer outputTerminal(AnalysisTerminal terminal) {
		if (terminal instanceof AnalysisDependency) {
			return outputDependency((AnalysisDependency) terminal);
		}
		else {
			return outputArtifact((AnalysisArtifact) terminal);
		}
	}
	
	public static ConfigurableConsoleOutputContainer outputDependency(AnalysisDependency dependency) {
		return artifactTreePrinter.outputDependency(dependency);
	}
	
	public static ConfigurableConsoleOutputContainer outputArtifact(AnalysisArtifact artifact) {
		return artifactTreePrinter.outputArtifact(artifact);
	}
	
	public static ConfigurableConsoleOutputContainer outputArtifact(Artifact artifact) {
		return artifactTreePrinter.outputArtifact(artifact);
	}
	
	public static ConfigurableConsoleOutputContainer outputDependency(CompiledDependencyIdentification dependency) {
		return artifactTreePrinter.outputDependency(dependency);
	}
	
	public static ConfigurableConsoleOutputContainer outputArtifact(CompiledArtifact artifact) {
		return artifactTreePrinter.outputArtifact(artifact);
	}
	
	public static ConfigurableConsoleOutputContainer outputArtifact(AnalysisDependency dependency, AnalysisArtifact artifact) {
		return artifactTreePrinter.outputArtifact(dependency, artifact);
	}
	
	/** {@code groupId}, {@code artifactId} and {@code version} are all optional. */
	public static ConfigurableConsoleOutputContainer outputArtifact(String groupId, String artifactId, String version, String scope, String type, boolean error) {
		return artifactTreePrinter.outputArtifact(groupId, artifactId, version, scope, type, error);
	}
}
