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
