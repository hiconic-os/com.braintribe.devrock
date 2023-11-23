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
