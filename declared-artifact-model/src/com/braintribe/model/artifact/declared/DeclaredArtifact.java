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
package com.braintribe.model.artifact.declared;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * represents an artifact in a low level model, very closely to the POM 
 * 
 * @author pit
 *
 */
public interface DeclaredArtifact extends VersionedArtifactIdentification {
	String name = "name";
	String description = "description";
	String packaging = "packaging";
	String resource = "resource";
	String properties = "properties";
	String parentReference = "parentReference";
	String relocationReference = "relocationReference";
	String dependencies = "dependencies";
	String dependencyManagement = "dependencyManagement";
	String licenses = "licenses";
	
	EntityType<DeclaredArtifact> T = EntityTypes.T(DeclaredArtifact.class);
	
	/**
	 * @return - the name of the project
	 */
	String getName();
	void setName(String name);
	
	/**
	 * @return - the description of the project
	 */
	String getDescription();
	void setDescription(String value);

	/**
	 * @return - the packaging as declared 
	 */
	String getPackaging();
	void setPackaging( String packaging);
	

	/**
	 * @return - the pom as a resource
	 */
	Resource getResource();
	void setResource(Resource resource);
	
	/**
	 * @return - the 'raw' properties as declared in the properties section
	 */
	Map<String,String> getProperties();
	void setProperties( Map<String,String> properties);
	
	/**
	 * @return - the reference pointing to the parent (if any)
	 */
	VersionedArtifactIdentification getParentReference();
	void setParentReference( VersionedArtifactIdentification reference);
	
	/**
	 * @return - the reference pointing to the relocation (if any)
	 */
	/*
	VersionedArtifactIdentification getRelocationReference();
	void setRelocationReference( VersionedArtifactIdentification reference);
	*/
	/**
	 * @return - the dependencies of the artifact (if any)
	 */
	List<DeclaredDependency> getDependencies();
	void setDependencies( List<DeclaredDependency> dependencies);
	
	/**
	 * @return - the managed dependencies of the artifact (if any) 
	 */
	List<DeclaredDependency> getManagedDependencies();
	void setManagedDependencies( List<DeclaredDependency> dependencies);
	
	
	/**
	 * @return - the licenses as declared in the pom (if any)
	 */
	Set<License> getLicenses();
	void setLicenses( Set<License> licenses);
	
	DistributionManagement getDistributionManagement();
	void setDistributionManagement(DistributionManagement value);

}
