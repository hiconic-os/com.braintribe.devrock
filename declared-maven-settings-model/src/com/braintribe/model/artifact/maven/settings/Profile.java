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
package com.braintribe.model.artifact.maven.settings;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;  


public interface Profile extends com.braintribe.model.generic.GenericEntity {
	
	final EntityType<Profile> T = EntityTypes.T(Profile.class);

	String activation = "activation";	
	String pluginRepositories = "pluginRepositories";
	String properties = "properties";
	String repositories = "repositories";

	void setActivation(com.braintribe.model.artifact.maven.settings.Activation value);
	com.braintribe.model.artifact.maven.settings.Activation getActivation();


	void setPluginRepositories(List<Repository> value);
	List<Repository> getPluginRepositories();

	void setProperties(List<Property> value);
	List<Property> getProperties();

	void setRepositories(List<Repository> value);
	List<Repository> getRepositories();

}
