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
package com.braintribe.model.artifact.analysis;

import java.util.List;
import java.util.Map;

import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a clash, i.e. contradictions in artifact versions encountered during traversion
 * @author pit/dirk
 *
 */
public interface DependencyClash extends ArtifactIdentification {
	
	EntityType<DependencyClash> T = EntityTypes.T(DependencyClash.class);

	final String solution = "solution";
	final String selectedDependency = "selectedDependency";
	final String involvedDependencies = "involvedDependencies";
	final String replacedSolutions = "replacedSolutions";
	
	/**
	 * @return - the {@link AnalysisArtifact} winning the clash
	 */
	AnalysisArtifact getSolution();
	void setSolution(AnalysisArtifact solution);
	
	/**
	 * @return - the {@link AnalysisDependency} that was chosen
	 */
	AnalysisDependency getSelectedDependency();
	void setSelectedDependency(AnalysisDependency selectedDependency);
	
	/**
	 * @return - a {@link List} of all {@link AnalysisDependency} that were involved in the clash
	 */
	List<AnalysisDependency> getInvolvedDependencies();
	void setInvolvedDependencies(List<AnalysisDependency> involvedDependencies);
	
	/** 
	 * @return - a {@link Map} that correlates the losing dependencies with their losing artifact
	 */
	Map<AnalysisDependency, AnalysisArtifact> getReplacedSolutions();
	void setReplacedSolutions(Map<AnalysisDependency, AnalysisArtifact> omittedSolutions);
}
