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
 * a validator that checks if all direct dependencies are also declared as direct dependencies 
 * 
 * @author pit
 *
 */
public class DependencyDeclarationValidator implements ZarathudValidator {

	@Override
	public boolean isValid(Artifact artifact) {
		// TODO Auto-generated method stub
		return false;
	}

}
