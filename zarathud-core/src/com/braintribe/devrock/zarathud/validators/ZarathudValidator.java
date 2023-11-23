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
