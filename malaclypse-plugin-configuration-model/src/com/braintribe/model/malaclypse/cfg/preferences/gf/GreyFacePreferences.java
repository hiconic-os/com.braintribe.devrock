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
package com.braintribe.model.malaclypse.cfg.preferences.gf;

import java.util.List;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;


public interface GreyFacePreferences extends StandardIdentifiable{
	
	final EntityType<GreyFacePreferences> T = EntityTypes.T(GreyFacePreferences.class);
	
	String sourceRepositories = "sourceRepositories";
	String tempDirectory = "tempDirectory";
	String excludeOptionals = "excludeOptionals";
	String excludeTest = "excludeTest";
	String excludeExisting = "excludeExisting";
	String overwrite = "overwrite";
	String repair = "repair";
	String purgePoms = "purgePoms";
	String applyCompileScope = "applyCompileScope";
	String enforceLicenses = "enforceLicenses";
	String validatePoms = "validatePoms";
	String lastSelectedTargetRepo = "lastSelectedTargetRepo";

	List<RemoteRepository> getSourceRepositories();
	void setSourceRepositories( List<RemoteRepository> repositories);
	
	String getTempDirectory();
	void setTempDirectory( String tmp);
	
	boolean getExcludeOptionals();
	void setExcludeOptionals( boolean excludeOptionals);
	
	boolean getExcludeTest();
	void setExcludeTest( boolean excludeTests);
	
	boolean getExcludeExisting();
	void setExcludeExisting( boolean excludeExisting);
	
	boolean getOverwrite();
	void setOverwrite( boolean overwrite);
	
	boolean getRepair();
	void setRepair( boolean repair);
	
	boolean getPurgePoms();
	void setPurgePoms( boolean purgePoms);
	
	boolean getApplyCompileScope();
	void setApplyCompileScope( boolean applyCompileScope);
	
	boolean getEnforceLicenses();
	void setEnforceLicenses( boolean enforce);
	
	boolean getValidatePoms();
	void setValidatePoms( boolean validate);
	
	String getLastSelectedTargetRepo();
	void setLastSelectedTargetRepo( String repoid);
		
	//
	// simulation settings
	//
	boolean getFakeUpload();
	void setFakeUpload( boolean fakeUpload);
	
	String getFakeUploadTarget();
	void setFakeUploadTarget( String fakeUploadTarget);
	
	boolean getAsyncScanMode();
	void setAsyncScanMode( boolean asyncScanMode);
	
	boolean getSimulateErrors();
	void setSimulateErrors( boolean simulateErrors);
	
	
	
	
}
