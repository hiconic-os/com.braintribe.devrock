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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;  

  
public interface Activation extends com.braintribe.model.generic.GenericEntity {
	
	final EntityType<Activation> T = EntityTypes.T(Activation.class); 
	
	public static final String activeByDefault = "activeByDefault";
	public static final String file = "file";	
	public static final String jdk = "jdk";
	public static final String os = "os";
	public static final String property = "property";

	void setActiveByDefault(java.lang.Boolean value);
	java.lang.Boolean getActiveByDefault();

	void setFile(com.braintribe.model.artifact.maven.settings.ActivationFile value);
	com.braintribe.model.artifact.maven.settings.ActivationFile getFile();
	
	void setJdk(java.lang.String value);
	java.lang.String getJdk();

	void setOs(com.braintribe.model.artifact.maven.settings.ActivationOS value);
	com.braintribe.model.artifact.maven.settings.ActivationOS getOs();

	void setProperty(com.braintribe.model.artifact.maven.settings.ActivationProperty value);
	com.braintribe.model.artifact.maven.settings.ActivationProperty getProperty();

}
