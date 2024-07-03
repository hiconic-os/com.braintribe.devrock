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
//@SelectiveInformation("while probing at ${timestamp}, at least one repository had issues")
public interface ProbingFailed extends McReason {
		
	final EntityType<ProbingFailed> T = EntityTypes.T(ProbingFailed.class);
	
	String timestamp = "timestamp";
	
	@Initializer("now()")
	Date getTimestamp();
	void setTimestamp(Date value);

	

}
