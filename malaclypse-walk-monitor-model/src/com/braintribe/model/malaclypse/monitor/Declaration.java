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
package com.braintribe.model.malaclypse.monitor;

import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.generic.GenericEntity;


public interface Declaration extends GenericEntity {

	public String getArtifactId();
	public void setArtifactId( String value);
	
	public String getGroupId();
	public void setGroupId( String value);
	
	public VersionRange getVersionRange();
	public void setVersionRange( VersionRange range);
	
	public String getScope();
	public void setScope( String value);
}
