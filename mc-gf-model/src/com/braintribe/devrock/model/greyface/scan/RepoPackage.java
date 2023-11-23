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
package com.braintribe.devrock.model.greyface.scan;

import java.util.Date;
import java.util.List;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.declared.License;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a {@link RepoPackage} represents an artifact in a remote repository
 * 
 * @author pit
 *
 */
public interface RepoPackage extends CompiledArtifactIdentification {
	
	EntityType<RepoPackage> T = EntityTypes.T(RepoPackage.class);

	/**
	 * @return - the owning {@link Repository} 
	 */
	Repository getRepository();
	void setRepository(Repository repository);

	/**
	 * @return - the {@link List} of {@link PartPackage}, i.e. its parts 
	 */
	List<PartPackage> getParts();
	void setParts(List<PartPackage> value);
	
	/**
	 * @return - the license as declared (via parent or directly)
	 */
	License getLicense();
	void setLicense(License value);

	/**
	 * @return - the {@link Date} as specified in the maven-metadata.xml 
	 */
	Date getUploadDate();
	void setUploadDate(Date value);


}
