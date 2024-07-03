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
package com.braintribe.devrock.model.mc.cfg.origination.resolution;

import java.util.Date;

import com.braintribe.devrock.model.mc.cfg.origination.Origination;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("Reverse dependency lookup result created at ${date} for ${identification}")
public interface ReverseDependencyLookupResult extends Origination {
	
	EntityType<ReverseDependencyLookupResult> T = EntityTypes.T(ReverseDependencyLookupResult.class);
	
	
	String date = "date";
	String identification= "identification";

	
	// date
	Date getDate();
	void setDate(Date value);
	
	// terminal
	ArtifactIdentification getIdentification();
	void setIdentification(ArtifactIdentification value);


}
