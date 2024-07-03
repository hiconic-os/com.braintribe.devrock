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

import com.braintribe.devrock.model.greyface.scan.PartPackage;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.time.TimeSpan;

/**
 * the uploaded part 
 * @author pit
 *
 */
public interface UploadedPartPackage extends PartPackage {
	
	EntityType<UploadedPartPackage> T = EntityTypes.T(UploadedPartPackage.class);

	/**
	 * @return - true if upload worked
	 */ 
	boolean getSuccess();
	void setSuccess(boolean value);

	/**
	 * @return - how long it took to upload
	 */
	TimeSpan getElapsedTime();
	void setElapsedTime(TimeSpan value);
	
}
