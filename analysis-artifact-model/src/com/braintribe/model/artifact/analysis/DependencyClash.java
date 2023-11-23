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
