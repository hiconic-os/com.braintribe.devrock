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
package com.braintribe.model.ravenhurst.interrogation;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface RavenhurstBundle extends GenericEntity {

	final EntityType<RavenhurstBundle> T = EntityTypes.T(RavenhurstBundle.class);

	void setProfileId(String id);
	String getProfileId();

	void setRepositoryId(String id);
	String getRepositoryId();

	void setRepositoryUrl(String id);
	String getRepositoryUrl();

	void setDate(Date date);
	Date getDate();

	void setUpdateIntervalForRelease(int interval);
	int getUpdateIntervalForRelease();

	void setUpdateIntervalForSnapshot(int interval);
	int getUpdateIntervalForSnapshot();

	void setFailOnCrcMismatchForRelease(boolean fail);
	boolean getFailOnCrcMismatchForRelease();

	void setFailOnCrcMismatchForSnapshot(boolean fail);
	boolean getFailOnCrcMismatchForSnapshot();

	void setRelevantForRelease(boolean use);
	boolean getRelevantForRelease();

	void setRelevantForSnapshot(boolean use);
	boolean getRelevantForSnapshot();

	void setRavenhurstClientKey(String key);
	String getRavenhurstClientKey();

	void setRepositoryClientKey(String key);
	String getRepositoryClientKey();

	void setRavenhurstRequest(RavenhurstRequest request);
	RavenhurstRequest getRavenhurstRequest();

	void setRavenhurstResponse(RavenhurstResponse reponses);
	RavenhurstResponse getRavenhurstResponse();

	void setDynamicRepository(boolean dynamic);
	boolean getDynamicRepository();

	void setTrustworthyRepository(boolean trustworthy);
	boolean getTrustworthyRepository();

	void setWeaklyCertified(boolean lenient);
	boolean getWeaklyCertified();

	void setListingLenient(boolean lenient);
	boolean getListingLenient();

	@Initializer("false")
	void setInaccessible(boolean inaccessible);
	boolean getInaccessible();

	void setCrcValidationLevelForRelease(CrcValidationLevelForBundle validationLevel);
	CrcValidationLevelForBundle getCrcValidationLevelForRelease();

	void setCrcValidationLevelForSnapshot(CrcValidationLevelForBundle validationLevel);
	CrcValidationLevelForBundle getCrcValidationLevelForSnapshot();

	void setIndexFilterExpression(String expression);
	String getIndexFilterExpression();
}
