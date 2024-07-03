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
package com.braintribe.model.malaclypse.cfg.preferences.ac.qi;

import java.util.Map;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * AC preferences related to quick import feature 
 * @author pit
 *
 */
public interface QuickImportPreferences extends StandardIdentifiable{
	
	final EntityType<QuickImportPreferences> T = EntityTypes.T(QuickImportPreferences.class);
	String localOnlyNature = "localOnlyNature";
	String alternativeUiNature = "alternativeUiNature";
	String attachToCurrentProject = "attachToCurrentProject";
	String importAction = "importAction";
	String expectedNumberOfSources = "expectedNumberOfSources";
	String filterOnWorkingSet = "filterOnWorkingSet";
	String lastDependencyCopyMode = "lastDependencyCopyMode";
	String lastDependencyPasteMode = "lastDependencyPasteMode";
	String archetypeToAssetMap = "archetypeToAssetMap";
	String primeWithSelection = "primeWithSelection";

	boolean getLocalOnlyNature();
	void setLocalOnlyNature( boolean nature);
	
	boolean getAlternativeUiNature();
	void setAlternativeUiNature(boolean nature);
	
	boolean getAttachToCurrentProject();
	void setAttachToCurrentProject( boolean attach);
	
	QuickImportAction getImportAction();
	void setImportAction( QuickImportAction importAction);
	
	int getExpectedNumberOfSources();
	void setExpectedNumberOfSources( int numSources);
	
	boolean getFilterOnWorkingSet();
	void setFilterOnWorkingSet( boolean filterOnWorkingSet);
	
	VersionModificationAction getLastDependencyCopyMode();
	void setLastDependencyCopyMode( VersionModificationAction action);
	
	VersionModificationAction getLastDependencyPasteMode();
	void setLastDependencyPasteMode( VersionModificationAction action);
	
	Map<String,String> getArchetypeToAssetMap();
	void setArchetypeToAssetMap( Map<String,String> archetypeToAssetMap);
	
	boolean getPrimeWithSelection();
	void setPrimeWithSelection(boolean primeWithSelection);

	
}
