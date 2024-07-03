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
package com.braintribe.devrock.mc.core.configuration;

import java.util.Date;

import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationResolving;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.reason.TemplateReasons;

/**
 * helper class to deal with origination reasons
 * @author pit
 *
 */
public class OriginationPreparation {

	/**
	 * @param repositoryConfiguration - the {@link RepositoryConfiguration}
	 * @return - the {@link RepositoryConfigurationResolving} 
	 */
	public static Reason acquireOrigination( RepositoryConfiguration repositoryConfiguration) {
		Reason origination = repositoryConfiguration.getOrigination();
		if (origination != null) {
			return origination;
		}
		else {
			// TODO: what should in there? 			
			Date now = new Date();
			origination = TemplateReasons.build( RepositoryConfigurationResolving.T) //
								.assign(RepositoryConfigurationResolving::setTimestamp, now) //
								.assign(RepositoryConfigurationResolving::setAgent, "internal agent") //
								.toReason();
		}
		return origination;
	}
}
