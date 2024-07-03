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
package com.braintribe.model.artifact.info;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;

/**
 * common abstract base for the {@link LocalRepositoryInformation} and the {@link RemoteRepositoryInformation}
 * @author pit
 *
 */
@Abstract
public interface RepositoryInformation extends GenericEntity {
	
	/**
	 * @return
	 */
	String getUrl();
	void setUrl( String url);

	List<PartInformation> getPartInformation();
	void setPartInformation( List<PartInformation> partInformation);
	

}
