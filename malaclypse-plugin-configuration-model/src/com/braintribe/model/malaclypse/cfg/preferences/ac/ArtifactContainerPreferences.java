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
