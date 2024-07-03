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
package com.braintribe.devrock.zarathud.validators;

import com.braintribe.model.zarathud.data.Artifact;

/**
 * basic interface for validators 
 * 
 * @author pit
 *
 */
public interface ZarathudValidator {

	/**
	 * validates an {@link Artifact} - which must contain the extraction data
	 * @param artifact - the {@link Artifact} with full attached data 
	 * @return - true if it passes the validation, false otherwise
	 */
	public boolean isValid( Artifact artifact);
}
