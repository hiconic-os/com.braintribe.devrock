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
package com.braintribe.devrock.model.mc.reason;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a reason to show variable resolving errors of the YAML marshallers
 * @author pit
 *
 */
@SelectiveInformation("Unresolved properties found while reading :'${file}'")
public interface UnresolvedPropertiesDuringConfigurationRead extends McReason {
	
	EntityType<UnresolvedPropertiesDuringConfigurationRead> T = EntityTypes.T(UnresolvedPropertiesDuringConfigurationRead.class);

	String file = "file";
	
	/**
	 * @return - the file that had unresolved properties while being parsed 
	 */
	String getFile();
	void setFile(String value);

}
