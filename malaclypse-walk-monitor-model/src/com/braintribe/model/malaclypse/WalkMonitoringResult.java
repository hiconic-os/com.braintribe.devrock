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
