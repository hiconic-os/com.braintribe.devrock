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
package com.braintribe.devrock.mc.core.resolver.clashes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * helper class to detect clashing dependencies in the tree declared with the entry-point dependencies
 * @author pit / dirk
 *
 */
public class ClashDetector {

	private Map<EqProxy<ArtifactIdentification>, List<AnalysisDependency>> map = new HashMap<>();
	private Set<AnalysisArtifact> visited = new HashSet<>();
	
	private ClashDetector() {		
	}
	
	/**
	 * detects clashing dependencies in the tree 
	 * @param dependencies - the entry points of the tree
	 * @return - a {@link Map} of the clashing {@link ArtifactIdentification} with the involved {@link AnalysisDependency}
	 */
	public static Map<EqProxy<ArtifactIdentification>, List<AnalysisDependency>> detect( Iterable<AnalysisDependency> dependencies) {
		ClashDetector detector = new ClashDetector();		
		return detector.detectClashes( dependencies);		
	}

	private Map<EqProxy<ArtifactIdentification>, List<AnalysisDependency>> detectClashes( Iterable<AnalysisDependency> dependencies) {
		detectClashesRecursively(dependencies);
		Map<EqProxy<ArtifactIdentification>, List<AnalysisDependency>> result = new HashMap<>();
		
		for (Map.Entry<EqProxy<ArtifactIdentification>, List<AnalysisDependency>> entry : map.entrySet()) {
			List<AnalysisDependency> deps = entry.getValue();
			if (deps.size() > 1) {
				AnalysisArtifact lastSolution = null;
				boolean first = true;
				
				EqProxy<ArtifactIdentification> key = entry.getKey();
				for (AnalysisDependency dep : deps) {
					AnalysisArtifact solution = dep.getSolution();
					
					if (first) {
						lastSolution = solution;
						first = false;
					}
					
					if (solution != lastSolution) {
						result.put( key, deps);
						break;
					}
				}				
			}
		}
		return result;
	}
	
	private void detectClashesRecursively( AnalysisArtifact analysisArtifact) {
		if (!visited.add(analysisArtifact))
			return;		
		Iterable<AnalysisDependency> dependencies = analysisArtifact.getDependencies();
		detectClashesRecursively(dependencies);
	}

	private void detectClashesRecursively(Iterable<AnalysisDependency> dependencies) {
		for (AnalysisDependency dependency : dependencies) {
			AnalysisArtifact solution = dependency.getSolution();
			
			List<AnalysisDependency> occurrences = map.computeIfAbsent(HashComparators.artifactIdentification.eqProxy( dependency), k -> new ArrayList<>());
			occurrences.add(dependency);
			
			if (solution == null)
				continue;
			
			detectClashesRecursively(solution);
		}
	}
	
 
}
