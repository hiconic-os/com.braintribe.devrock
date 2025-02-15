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

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.utils.FileTools;

public class GroupReleasing {

	protected static final String[] activeNatures = { //
			"TribefireModule", //
			"TribefireWebPlatform", //
			"ModelPriming", //
			"PrimingModule", //
			"CoreWebContext" //
	};

	protected static final List<String> ignoredParentDeps = asList( //
			"com.braintribe.common", //
			"com.braintribe.wire" //
	);

	private final DeclaredArtifactMarshaller marshaller = new DeclaredArtifactMarshaller();

	private final Map<String, Group> groups = new LinkedHashMap<>();
	private final File folder;
	private final List<SortedSet<Group>> bulks = new ArrayList<>();

	private boolean hasCycles;

	// ######################################################################

	public static void main(String[] args) {
		new GroupReleasing(new File("C:\\devrock-sdk\\env\\release\\git")).process();
	}

	// ######################################################################

	public GroupReleasing(File folder) {
		this.folder = folder;
	}

	private void process() {
		analyzeDependencies();
		analyzeCycles();

		if (hasCycles)
			return;

		analyzeBulks();
		printBulks();
	}

	private void analyzeDependencies() {
		analyzeGroups(folder);
	}

	private void analyzeGroups(File groupsParent) {
		for (File groupDir : groupsParent.listFiles())
			if (groupDir.isDirectory())
				analyzeGroupDir(groupDir);

		for (Group group : groups.values()) {
			for (String groupName : group.groupReferences) {
				if (groupName.equals(group.name))
					continue;

				Group dependency = groups.get(groupName);

				if (dependency == null) {
					if (isHiconicGroupName(groupName))
						System.err.println("missing group: " + groupName + ", referenced from: " + group.name);
					continue;
				}

				dependency.dependers.add(group);
				group.dependencies.add(dependency);
			}
		}
	}

	private void analyzeGroupDir(File groupDir) {
		String groupId = groupDir.getName();
		Group group = acquireGroup(groupId);
		group.folder = groupDir;

		Set<String> groupPropsInParent = new HashSet<>();

		for (File artifactDir : groupDir.listFiles()) {
			if (!artifactDir.isDirectory())
				continue;

			File pomFile = new File(artifactDir, "pom.xml");
			if (!pomFile.exists())
				continue;

			if (isPassiveAsset(artifactDir)) {
				// System.out.println("skipped passive asset: " + artifactFolder.getName());
				continue;
			}

			String artifactId = artifactDir.getName();

			DeclaredArtifact declaredArtifact = FileTools.read(pomFile).fromInputStream(in -> marshaller.unmarshall(in));

			List<DeclaredDependency> dependencies = declaredArtifact.getDependencies();

			for (DeclaredDependency declaredDependency : dependencies) {
				String groupRef = declaredDependency.getGroupId();
				group.groupReferences.add(groupRef);
			}

			if (artifactId.equals("parent")) {
				for (String pName : declaredArtifact.getProperties().keySet()) {
					if (!pName.startsWith("V."))
						continue;

					String refGroup = pName.substring(2);
					if (isHiconicGroupName(refGroup))
						groupPropsInParent.add(refGroup);
				}
			}
		}

		groupPropsInParent.removeAll(group.groupReferences);
		groupPropsInParent.removeAll(ignoredParentDeps);
		groupPropsInParent.remove(groupId);
		if (!groupPropsInParent.isEmpty())
			System.err.println("Not needed variables in parent for broup: " + groupId + ".  Variables for: " + groupPropsInParent);
	}

	private boolean isPassiveAsset(File artifactFolder) {
		File assetManFile = new File(artifactFolder, "asset.man");
		if (!assetManFile.exists())
			return false;

		String content = FileTools.read(assetManFile).asString();

		for (String nature : activeNatures) {
			if (content.contains(nature))
				return false;
		}

		return true;
	}

	private boolean isHiconicGroupName(String group) {
		return group.contains(".braintribe.") || group.contains(".tribefire.") || group.contains(".hiconic.");
	}

	private Group acquireGroup(String name) {
		return groups.computeIfAbsent(name, Group::new);
	}

	private boolean analyzeCycles() {
		for (Group group : groups.values()) {
			Deque<Group> stack = new ArrayDeque<>();
			Set<Group> visited = newSet();
			analyzeCycles(group, group, stack, visited);
		}

		for (Group group : groups.values()) {
			if (!group.cycles.isEmpty()) {
				hasCycles = true;
				System.out.println("Cycle detected for group " + group.name);
				for (List<Group> cycle : group.cycles) {
					System.out.println("  " + cycle);
				}
			}
		}

		return hasCycles;
	}

	private void analyzeCycles(Group curGroup, Group group, Deque<Group> stack, Set<Group> visited) {
		stack.push(group);
		try {
			if (curGroup == group && !visited.isEmpty()) {
				ArrayList<Group> cycle = new ArrayList<>(stack);
				Collections.reverse(cycle);
				curGroup.cycles.add(cycle);
				return;
			}

			if (!visited.add(group))
				return;

			for (Group dependency : group.dependencies) {
				analyzeCycles(curGroup, dependency, stack, visited);
			}
		} finally {
			stack.pop();
		}
	}

	private void printBulks() {
		int i = 0;
		for (Set<Group> bulk : bulks) {
			System.out.println("Bulk " + ++i);
			for (Group group : bulk) {
				System.out.println("  " + group.name);
			}
		}
	}

	private void analyzeBulks() {
		Set<Group> processed = new HashSet<>();
		Set<Group> toProcess = new HashSet<>(groups.values());

		while (!toProcess.isEmpty()) {
			SortedSet<Group> detected = new TreeSet<>();

			for (Group group : toProcess) {
				if (processed.containsAll(group.dependencies)) {
					detected.add(group);
				}
			}

			if (detected.isEmpty()) {
				bulks.add(new TreeSet<>(toProcess));
				break;
			} else {
				toProcess.removeAll(detected);
				processed.addAll(detected);
				bulks.add(detected);
			}
		}
	}

	private void printDependencies(Group group, Set<Group> visited) {
		if (!visited.add(group))
			return;

		for (Group dependency : group.dependencies) {
			printDependencies(dependency, visited);
		}

		System.out.println(group.name);
	}

}
