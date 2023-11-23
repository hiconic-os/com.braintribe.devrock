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
