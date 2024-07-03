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
package com.braintribe.devrock.mc.api.commons;

import java.util.List;

import com.braintribe.model.version.Version;

/**
 * contains information about the {@link Version} of an artifact and a list of the ids of repositories that cao
 * could provide it
 * @author pit / dirk
 *
 */
public interface VersionInfo extends Comparable<VersionInfo>{
	Version version();
	List<String> repositoryIds();
	
	default Integer dominancePos() { return null; }
	
	@Override
	default int compareTo(VersionInfo o) {
		return this.version().compareTo(o.version());		
	}
}
