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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.utils.FileTools;

public class GroupMigration {
	private static List<String> gitIngores = Arrays.asList(".gradle");
	private static List<String> obsoletes = Arrays.asList("Jenkinsfile", "MIGRATION.md", "build.xml");
	private Map<String, Group> groups = new LinkedHashMap<>();
	private File folder;
	private List<SortedSet<Group>> bulks = new ArrayList<>();
	private static final String[] natures = {"TribefireModule", "TribefireWebPlatform", "ModelPriming", "PrimingModule", "CoreWebContext"};
	
	public GroupMigration(File folder) {
		this.folder = folder;
	}

	private Group acquireGroup(String name) {
		return groups.computeIfAbsent(name, Group::new);
	}

	private void process() {
		analyzeDependencies();
		boolean hasCycles = analyzeCycles();
		
		if (hasCycles)
			return;
		
		if (false) {
			enrichGitIgnores();
			enrichFiles();
			cleanupObsoleteFiles();
		}
		
		analyzeBulks();
		printBulks();
	}

	private boolean analyzeCycles() {
		
		for (Group group: groups.values()) {
			Deque<Group> stack = new ArrayDeque<>();
			Set<Group> visited = new HashSet<>();
			analyzeCycles(group, group, stack, visited);
		}
		
		boolean hasCycles = false;
		
		for (Group group: groups.values()) {
			if (!group.cycles.isEmpty()) {
				hasCycles = true;
				System.out.println("Cycle detected for group " + group.name);
				for (List<Group> cycle: group.cycles) {
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
			
			for (Group dependency: group.dependencies) {
				analyzeCycles(curGroup, dependency, stack, visited);
			}
		}
		finally {
			stack.pop();
		}
	}

	private void printBulks() {
		int i = 0;
		for (Set<Group> bulk: bulks) {
			System.out.println("Bulk " + ++i);
			for (Group group: bulk) {
				System.out.println("  " + group.name);
			}
		}
	}

	private void cleanupObsoleteFiles() {
		for (Group group: groups.values()) {
			for (String obsolete: obsoletes) {
				File file = new File(group.folder, obsolete);
				
				if (file.exists())
					file.delete();
			}
		}
	}

	private void enrichFiles() {
		File sourceFolder = new File("res/migration/group-injections");
		
		for (Group group: groups.values()) {
			try {
				FileTools.copyDirectory(sourceFolder, group.folder);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private void enrichGitIgnores() {
		for (Group group: groups.values()) {
			patchGitIgnores(group.folder);
		}
	}
	
	private void patchGitIgnores(File folder) {
		File gitIgnoreFile = new File(folder, ".gitignore");
		
		Set<String> ignores = new LinkedHashSet<>();
		
		if (gitIgnoreFile.exists())
			ignores.addAll(FileTools.read(gitIgnoreFile).asLines());
		
		ignores.addAll(gitIngores);
		
		FileTools.write(gitIgnoreFile).lines(ignores);
	}

	private void analyzeBulks() {
		Set<Group> processed = new HashSet<>();
		Set<Group> toProcess = new HashSet<>(groups.values());
		
		while (!toProcess.isEmpty()) {
			SortedSet<Group> detected = new TreeSet<>();

			for (Group group: toProcess) {
				if (processed.containsAll(group.dependencies)) {
					detected.add(group);
				}
			}
			
			if (detected.isEmpty()) {
				bulks.add(new TreeSet<>(toProcess));
				break;
			}
			else {
				toProcess.removeAll(detected);
				processed.addAll(detected);
				bulks.add(detected);
			}
		}
	}

	private void printDependencies(Group group, Set<Group> visited) {
		if (!visited.add(group))
			return;
		
		for (Group dependency: group.dependencies) {
			printDependencies(dependency, visited);
		}
		
		System.out.println(group.name);
	}

	private void analyzeDependencies() {
		DeclaredArtifactMarshaller marshaller = new DeclaredArtifactMarshaller();
		
		for (File file: folder.listFiles()) {
			if (file.isDirectory()) {
				for (File artifactFolder: file.listFiles()) {
					if (artifactFolder.isDirectory()) {
						File pomFile = new File(artifactFolder, "pom.xml");
						
						if (!pomFile.exists()) 
							continue;
						
						if (isPassiveAsset(artifactFolder)) {
							// System.out.println("skipped passive asset: " + artifactFolder.getName());
							continue;
						}
						
						try (InputStream in = new FileInputStream(pomFile)) {
							
							DeclaredArtifact declaredArtifact = marshaller.unmarshall(in);
							String groupId = declaredArtifact.getGroupId();
							
							if (groupId == null) {
								VersionedArtifactIdentification parentReference = declaredArtifact.getParentReference();
								
								if (parentReference == null) {
									System.out.println("skipping broken artifact: " + pomFile);
									continue;
								}
								
								groupId = parentReference.getGroupId();
							}
							
							Group group = acquireGroup(groupId);
							group.folder = file;
							
							List<DeclaredDependency> dependencies = declaredArtifact.getDependencies();
							
							for (DeclaredDependency declaredDependency : dependencies) {
								String groupRef = declaredDependency.getGroupId();
								group.groupReferences.add(groupRef);
							}
						}
						catch (IOException e) {
							throw new UncheckedIOException(e);
						}
						
					}
				}
			}
		}
		
		for (Group group: groups.values()) {
			for (String groupName: group.groupReferences) {
				if (!groups.containsKey(groupName))
					continue;
				
				if (groupName.equals(group.name))
					continue;
				
				Group dependency = acquireGroup(groupName);
				dependency.dependers.add(group);
				group.dependencies.add(dependency);
			}
		}
	}
	
	private boolean isPassiveAsset(File artifactFolder) {
		File assetManFile = new File(artifactFolder, "asset.man");
		if (!assetManFile.exists())
			return false;
		
		String content = FileTools.read(assetManFile).asString();
		
		for (String nature: natures) {
			if (content.contains(nature))
				return false;
		}
		
		return true;
	}

	public static void main(String[] args) {
		new GroupMigration(new File("C:\\devrock-sdk\\env\\migration\\git\\")).process();
		//new GroupMigration(new File("C:\\devrock-sdk\\env\\cicd\\git\\initial\\extracted")).process();
	}

}

