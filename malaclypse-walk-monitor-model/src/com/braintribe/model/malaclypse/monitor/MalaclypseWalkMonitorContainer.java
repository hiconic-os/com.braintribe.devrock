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
package com.braintribe.model.malaclypse.monitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.generic.GenericEntity;


 public interface MalaclypseWalkMonitorContainer extends GenericEntity {
	
	 String getName();
	 void setName( String name);
	
	 Solution getMasterSolution();
	 void setMasterSolution( Solution solution);
	
	 Map<Dependency, DependencyListContainer> getResolvedDependencyClashesMap();
	 void setResolvedDependencyClashesMap( Map<Dependency, DependencyListContainer> map);
	
	 Map<Identification, SolutionListContainer> getResolvedSolutionClashesMap();
	 void setResolvedSolutionClashesMap(Map<Identification, SolutionListContainer> map);
	
	 Map<Solution, DeclarationListContainer> getDeclaredDependencyMap();
	 void setDeclaredDependencyMap(Map<Solution, DeclarationListContainer> map);
	
	 List<Solution> getClasspathSolutions();
	 void setClasspathSolutions( List<Solution> solutions);
	
	 List<Dependency> getUnresolvedDependencies();
	 void setUnresolvedDependencies( List<Dependency> dependencies);
	
	 List<Dependency> getUndeterminedDependencies();
	 void setUndeterminedDependencies( List<Dependency> dependencies);
	
	 List<Dependency> getRedeterminedDependencies();
	 void setRedeterminedDependencies( List<Dependency> dependencies);
	
	 List<Part> getUpdatedParts();
	 void setUpdatedParts( List<Part> parts);
	
	 List<Part> getMissingParts();
	 void setMissingParts( List<Part> parts);
	
	 List<Part> getMissingUpdateInformationParts();
	 void setMissingUpdateInformationParts( List<Part> parts);
	
	 List<PomParentDictionaryContainer> getParentDictionaries();
	 void setParentDictionaries( List<PomParentDictionaryContainer> dictionaries);
	
	 List<TraversingTraceTuple> getTraversingProtocol();
	 void setTraversingProtocol( List<TraversingTraceTuple> tuples);
	
	 String getUpdateProtocol();
	 void setUpdateProtocol( String protocol);
	

	 List<String> getMissingFiles();
	 void setMissingFiles( List<String> files);
		
	 List<RedirectionContainer> getRedirections();
	 void setRedirections( List<RedirectionContainer> redirections);
	
	void setMerges( Set<Dependency> merges);
	Set<Dependency> getMerges();
		
}
