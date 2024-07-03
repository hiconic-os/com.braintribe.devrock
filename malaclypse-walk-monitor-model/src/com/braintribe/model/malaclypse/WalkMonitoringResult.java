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
package com.braintribe.model.malaclypse;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;
import com.braintribe.model.malaclypse.container.DependencyContainer;
import com.braintribe.model.malaclypse.container.SolutionContainer;
import com.braintribe.model.malaclypse.container.TraversingEvent;


public interface WalkMonitoringResult extends GenericEntity {
	
	final EntityType<WalkMonitoringResult> T = EntityTypes.T(WalkMonitoringResult.class);
	
	void setTimestamp( Date date);
	Date getTimestamp();
	
	void setDurationInMs( long millis);
	long getDurationInMs();
	
	void setTerminal( Solution solution);
	Solution getTerminal();
	
	void setSolutions( List<Solution> solutions);
	List<Solution> getSolutions();
	
	void setDependencyClashes( Map<Dependency, DependencyContainer> clashes);
	Map<Dependency, DependencyContainer> getDependencyClashes();	
	
	void setSolutionClashes( Map<Solution, SolutionContainer> clashes);
	Map<Solution, SolutionContainer> getSolutionClashes();
	
	void setUnresolvedDependencies( Set<Dependency> unresolved);
	Set<Dependency> getUnresolvedDependencies();
	
	void setDependencyReassignments( Map<Dependency,Dependency> reassignment);
	Map<Dependency, Dependency> getDependencyReassignments();
	
	
	void setUndeterminedDependencies( Set<Dependency> undetermined);
	Set<Dependency> getUndeterminedDependencies();
	
	void setTraversingEvents( List<TraversingEvent> events);
	List<TraversingEvent> getTraversingEvents();
	
	void setRedirectionMap( Map<Part, Solution> redirectionMap);
	Map<Part, Solution> getRedirectionMap();
	
	void setParentAssociationMap( Map<Artifact, Solution> parentAssociationMap);
	Map<Artifact, Solution> getParentAssociationMap();
	
	void setParentContainerAssociationMap( Map<Artifact, ParentContainer> parentAssociationMap);
	Map<Artifact, ParentContainer> getParentContainerAssociationMap();

	
	void setMergedDependencies( Set<Dependency> mergedDependencies);
	Set<Dependency> getMergedDependencies();
	
	void setWalkDenotationType( WalkDenotationType denotationType);
	WalkDenotationType getWalkDenotationType();
	
}
