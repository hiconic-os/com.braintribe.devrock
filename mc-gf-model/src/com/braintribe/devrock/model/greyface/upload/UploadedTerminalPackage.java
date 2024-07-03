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
package com.braintribe.devrock.model.greyface.upload;

import java.util.List;

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.declared.License;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author pit
 *
 */
public interface UploadedTerminalPackage extends CompiledArtifactIdentification {
	
	EntityType<UploadedTerminalPackage> T = EntityTypes.T(UploadedTerminalPackage.class);

	/**
	 * @return - true if all parts have been sucessfully uploaded
	 */
	boolean getSuccess();
	void setSuccess(boolean value);

	/**
	 * @return - the {@link List} of parts that were uploaded
	 */
	List<UploadedPartPackage> getUploadedParts();
	void setUploadedParts(List<UploadedPartPackage> value);

	/**
	 * @return - the license taken
	 */
	License getLicense();
	void setLicense(License value);

	
}
