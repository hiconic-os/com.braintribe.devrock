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
package com.braintribe.model.malaclypse.cfg.preferences.ac;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.malaclypse.cfg.preferences.ac.container.DynamicContainerPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.profile.ProfilePreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.ravenhurst.RavenhurstPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.views.dependency.DependencyViewPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.gwt.GwtPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.mv.MavenPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SvnPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.tb.TbRunnerPreferences;


public interface ArtifactContainerPreferences extends StandardIdentifiable{
	
	final EntityType<ArtifactContainerPreferences> T = EntityTypes.T(ArtifactContainerPreferences.class);

	AntRunnerPreferences getAntRunnerPreferences();
	void setAntRunnerPreferences( AntRunnerPreferences runnerPreferences);
	
	GwtPreferences getGwtPreferences();
	void setGwtPreferences( GwtPreferences gwtPreferences);
	
	ClasspathPreferences getClasspathPreferences();
	void setClasspathPreferences( ClasspathPreferences classpathPreferences);
	
	QuickImportPreferences getQuickImportPreferences();
	void setQuickImportPreferences( QuickImportPreferences quickImportPreferences);
	
	RavenhurstPreferences getRavenhurstPreferences();
	void setRavenhurstPreferences( RavenhurstPreferences ravenhurstPreferences);
	
	DependencyViewPreferences getDependencyViewPreferences();
	void setDependencyViewPreferences( DependencyViewPreferences dependencyViewFilterPreferences);
	
	SvnPreferences getSvnPreferences();
	void setSvnPreferences( SvnPreferences svnPreferences);	
	
	ProfilePreferences getProfilePreferences();
	void setProfilePreferences( ProfilePreferences profilePreferences);
	
	MavenPreferences getMavenPreferences();
	void setMavenPreferences( MavenPreferences mavenPreferences);
	
	DynamicContainerPreferences getDynamicContainerPreferences();
	void setDynamicContainerPreferences( DynamicContainerPreferences dynamicContainerPreferences);

	TbRunnerPreferences getTbRunnerPreferences();
	void setTbRunnerPreferences( TbRunnerPreferences preferences);
		
}
