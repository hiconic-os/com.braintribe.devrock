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
