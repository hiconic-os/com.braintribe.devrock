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
package com.braintribe.devrock.mc.api.transitive;

/**
 * path elements - to be able to generate a meaningful traversing protocol
 * @author pit / dirk
 *
 */
public interface ResolutionPathElement {
	/**
	 * @return - the {@link ResolutionPathElement} that is the ancestor of this one
	 */
	ResolutionPathElement getParent();
	
	/**
	 * @return - the string representation 
	 */
	String asString();
	
	/**
	 * @return - the path string representation
	 */
	String asPathString();
}
