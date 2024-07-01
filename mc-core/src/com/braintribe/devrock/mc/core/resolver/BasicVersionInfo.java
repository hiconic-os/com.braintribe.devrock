// ============================================================================
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
package com.braintribe.devrock.mc.core.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.model.version.Version;

/**
 * an implementation of the {@link VersionInfo}
 * @author pit/dirk
 *
 */
public class BasicVersionInfo implements VersionInfo {
	private Version version;
	private List<String> repositoryIds;
	private Integer dominancePos;
	
	/**
	 * @param version - the version 
	 * @param repositoryIds - a {@link List} of repository ids
	 */
	public BasicVersionInfo(Version version, List<String> repositoryIds) {
		super();
		this.version = version;
		this.repositoryIds = repositoryIds;
	}
	
	public BasicVersionInfo(Version version) {
		this(version, new ArrayList<>());
	}

	@Override
	public Version version() {
		return version;
	}

	@Override
	public List<String> repositoryIds() {
		return repositoryIds;
	}
	
	@Override
	public Integer dominancePos() {
		return dominancePos;
	}
	
	public void setDominancePos(Integer dominancePos) {
		this.dominancePos = dominancePos;
	}
	
	/**
	 * @param repositoryId - the repository id to add to list
	 */
	public void add( String repositoryId) {
		repositoryIds.add( repositoryId);
	}
	
	public String asString() {
		StringBuilder builder = new StringBuilder();
		builder.append( version().asString());
		builder.append( '[');
		builder.append( repositoryIds().stream().collect(Collectors.joining(",")));
		builder.append (']');
		return builder.toString();
	}

}
