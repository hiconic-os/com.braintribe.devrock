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
package com.braintribe.devrock.model.repolet.content;


import java.util.List;
import java.util.Map;

import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * an artifact within the {@link RepoletContent}
 * @author pit
 *
 */
public interface Artifact extends VersionedArtifactIdentification {	
	
	EntityType<Artifact> T = EntityTypes.T(Artifact.class);
	
	String parent = "parent";
	String dependencies = "dependencies";
	String managedDependencies = "managedDependencies";
	String properties = "properties";
	String partClassifiers = "partClassifiers";
	String packaging = "packaging";
	String versionOverride = "versionOverride";
	String order = "order";
	String redirection = "redirection";
	String parts = "parts";
	String disregardAsSolutionInResolution = "disregardAsSolutionInResolution";
	
	
	/**
	 * the validator uses the repolet content to compare to a anaysis-artifact-resolution. Now, parent and import
	 * declarations are required to validate parent/import structure, hence they must appear not only in the definition content,
	 * but also in the validation content. However, they should not appear in the solution list, *unless* its a an AAR from
	 * a TDR run. Using this flag, the two cases can be separated.
	 * @return - true if the artifact is not be searched in the solution list of the resolution is validated against
	 */
	boolean getDisregardAsSolutionInResolution();
	void setDisregardAsSolutionInResolution(boolean value);


	/**
	 * @return - the {@link VersionedArtifactIdentification} that stands for the parent
	 */
	VersionedArtifactIdentification getParent();
	void setParent(VersionedArtifactIdentification parent);

	/**
	 * @return - a {@link List} of {@link Dependency} that are the REAL dependencies
	 */
	List<Dependency> getDependencies();
	void setDependencies(List<Dependency> value);
	
	/**
	 * @return - a {@link List} of {@link Dependency} that are the MANAGED dependencies
	 */
	List<Dependency> getManagedDependencies();
	void setManagedDependencies(List<Dependency> value);
	
	/**
	 * @return - the single redirecting {@link Dependency}
	 */
	Dependency getRedirection();
	void setRedirection(Dependency value);


	/**
	 * @return - a {@link List} of properties
	 */
	List<Property> getProperties();
	void setProperties(List<Property> value);
 

	/**
	 * @return - the packaging of the {@link Artifact}, default is 'jar' 
	 */
	String getPackaging();
	void setPackaging(String value);

	/**
	 * @return - the expression to be used as version, default is declaration version (as in rule file)
	 */
	String getVersionOverride();
	void setVersionOverride(String value);
	
	/**
	 * @return - the build order
	 */
	Integer getOrder();
	void setOrder( Integer order);


	/**
	 * @return - a map of part-name to resource, which may be null
	 */
	Map<String, Resource> getParts();
	void setParts(Map<String, Resource> parts);
	
	static Artifact from(String expression) {
		VersionedArtifactIdentification vai = VersionedArtifactIdentification.parse(expression);
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId(vai.getGroupId());
		artifact.setArtifactId( vai.getArtifactId());
		artifact.setVersion( vai.getVersion());
		return artifact;
	}
}
