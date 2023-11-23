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
