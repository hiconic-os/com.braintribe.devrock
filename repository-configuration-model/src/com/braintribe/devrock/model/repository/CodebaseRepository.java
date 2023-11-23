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
package com.braintribe.devrock.model.repository;

import java.util.Set;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a repository containing the SOURCES (no .pom, yet pom.xml, no jars et al)
 * @author pit
 *
 */
public interface CodebaseRepository extends AbstractFileSystemRepository {

	final EntityType<CodebaseRepository> T = EntityTypes.T(CodebaseRepository.class);

	String template = "template"; 
	String archetypesIncludes = "archetypesIncludes";
	String archetypesExcludes = "archetypesExcludes";
	/**
	 * @return - the pattern that defines the structure of the repository 
	 */
	@Mandatory
	String getTemplate();
	void setTemplate(String template);
	
	Set<String> getArchetypesIncludes();
	void setArchetypesIncludes(Set<String> archetypesIncludes);
	
	Set<String> getArchetypesExcludes();
	void setArchetypesExcludes(Set<String> archetypesExcludes);
}
