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
package com.braintribe.devrock.model.mc.reason.configuration;

import java.util.Date;

import com.braintribe.devrock.model.mc.reason.McReason;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * umbrella reason for repository configuration
 * 
 * @author pit
 *
 */
@SelectiveInformation("while probing at ${timestamp}, at least one repository had issues")
public interface ProbingFailed extends McReason {
		
	final EntityType<ProbingFailed> T = EntityTypes.T(ProbingFailed.class);
	
	String timestamp = "timestamp";
	
	@Initializer("now()")
	Date getTimestamp();
	void setTimestamp(Date value);

	

}
