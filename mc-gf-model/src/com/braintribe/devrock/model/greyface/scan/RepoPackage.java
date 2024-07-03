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
