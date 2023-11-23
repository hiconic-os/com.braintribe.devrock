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
