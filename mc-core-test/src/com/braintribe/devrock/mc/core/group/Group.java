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
package com.braintribe.devrock.mc.core.group;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Group implements Comparable<Group> {
	public String name;
	public File folder;
	public Set<Group> dependers = new LinkedHashSet<>();
	public Set<Group> dependencies = new LinkedHashSet<>();
	public Set<String> groupReferences = new HashSet<>();
	public List<List<Group>> cycles = new ArrayList<>();
	
	public Group(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int compareTo(Group o) {
		return name.compareTo(o.name);
	}
}
