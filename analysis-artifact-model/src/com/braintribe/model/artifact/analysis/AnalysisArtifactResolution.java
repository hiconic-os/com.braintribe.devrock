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
import java.util.Set;

import com.braintribe.devrock.model.mc.cfg.origination.HasOrigination;
import com.braintribe.gm.model.reason.HasFailure;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface AnalysisArtifactResolution extends HasFailure, HasOrigination {
	EntityType<AnalysisArtifactResolution> T = EntityTypes.T(AnalysisArtifactResolution.class);
	
	String terminals = "terminals";
	String solutions = "solutions";
	String clashes = "clashes";
	String unresolvedDependencies = "unresolvedDependencies";
	String incompleteArtifacts = "incompleteArtifacts";

	List<AnalysisTerminal> getTerminals();
	void setTerminals(List<AnalysisTerminal> terminals);
	
	List<AnalysisArtifact> getSolutions();
	void setSolutions(List<AnalysisArtifact> solutions);

	List<DependencyClash> getClashes();
	void setClashes(List<DependencyClash> clashes);
	
	Set<AnalysisDependency> getUnresolvedDependencies();
	void setUnresolvedDependencies(Set<AnalysisDependency> unresolvedDependencies);
	
	Set<AnalysisDependency> getFilteredDependencies();
	void setFilteredDependencies(Set<AnalysisDependency> unresolvedDependencies);
	
	Set<AnalysisArtifact> getIncompleteArtifacts();
	void setIncompleteArtifacts(Set<AnalysisArtifact> incompleteArtifacts);
	
}
