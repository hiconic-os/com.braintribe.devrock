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
package com.braintribe.devrock.mc.core.configuration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;

/**
 * an {@link ArtifactChangesTreeNode} stores - starting from the root - several folders and files. it is used
 * to efficiently clear files in the local repository if referenced by RH responses.
 * @author pit / dirk
 *
 */
public class ArtifactChangesTreeNode {
	private File file;
	private Map<String, ArtifactChangesTreeNode> subNodes;
	private String name;
	private ArtifactChangesNodeType nodeType;

	public ArtifactChangesTreeNode(String name) {
		this.name = name;
	}
	
	@Configurable @Required
	public void setPath(File path) {
		this.file = path;
	}
	@Configurable @Required
	public void setNodeType(ArtifactChangesNodeType nodeType) {
		this.nodeType = nodeType;
	}
	
	private void addSubNode( ArtifactChangesTreeNode subNode) {
		if (subNodes == null) {
			subNodes = new LinkedHashMap<>();
		}
		subNodes.put( subNode.name, subNode);
	}
	
	/**
	 * @param paths - a list of path elements 
	 * @param repositoryId - the associated repository id
	 */
	public void addPath( List<String> paths, String repositoryId) {
		if (paths.isEmpty())
			return;
		
		String first = paths.get(0);
		
		if (subNodes == null) {
			subNodes = new LinkedHashMap<>();
		}
		ArtifactChangesTreeNode subNode = null;
		
		subNode = subNodes.get( first);
		if (subNode == null) {
			subNode = createNode(paths, repositoryId, first, first);
			subNodes.put( first, subNode);			
		}		
		else {
			attachDropfilesToNode(subNode, repositoryId, subNode.nodeType);					
		}
		/*
		
		ArtifactChangesTreeNode subNode = subNodes.computeIfAbsent( first, k -> {
			return createNode(paths, repositoryId, first, k);
		});
		*/
		if (subNode.nodeType != ArtifactChangesNodeType.deadNode) {
			subNode.addPath(paths.subList(1, paths.size()), repositoryId);
		}
		
	}
	
	private Pair<File, ArtifactChangesNodeType> determineNodeType( List<String> paths) {
		File universalPath = new File( file, paths.get(0));
		if (universalPath.exists()) {				
			switch (paths.size()) {
				case 2 : 
					return Pair.of( universalPath, ArtifactChangesNodeType.artifactFolder);									
				case 1:
					return Pair.of( universalPath, ArtifactChangesNodeType.versionFolder);								
				default:					
					return Pair.of( universalPath, ArtifactChangesNodeType.normalFolder);
					
			}
		}
		else {
			return Pair.of( universalPath, ArtifactChangesNodeType.deadNode);			
		}
	}

	private ArtifactChangesTreeNode createNode(List<String> paths, String repositoryId, String first, String name) {
			
		Pair<File, ArtifactChangesNodeType> pair = determineNodeType(paths);	
		
		File determinedFilePath = pair.first;
		ArtifactChangesNodeType determinedNodeType = pair.second;
	
		ArtifactChangesTreeNode node = new ArtifactChangesTreeNode(name);
		node.setPath( determinedFilePath);
		node.setNodeType(determinedNodeType);
	
		attachDropfilesToNode( node, repositoryId, determinedNodeType);
		return node;
	}

	private void attachDropfilesToNode(ArtifactChangesTreeNode node, String repositoryId, ArtifactChangesNodeType determinedNodeType) {
		switch ( determinedNodeType) {
			case artifactFolder:
				node.addDropFile( "maven-metadata-" + repositoryId + ".xml");
				break;
			case versionFolder:
				node.addDropFile( "maven-metadata-" + repositoryId + ".xml");
				node.addDropFile( "part-availability-" + repositoryId + ".txt");
				node.addDropFile( "part-availability-" + repositoryId + ".artifactory.json");
				node.addDropFile( repositoryId + ".solution");
				break;
			default:
				break;
			}
	}

	/**
	 * @param dropFileName - the name of the file to drop 
	 */
	private void addDropFile( String dropFileName) {
		ArtifactChangesTreeNode node = new ArtifactChangesTreeNode(dropFileName);
		File path = new File( file, dropFileName);
		node.setPath( path);
		node.setNodeType( ArtifactChangesNodeType.dropfile);
		addSubNode(node);				
	}
	
	
	/**
	 * recursively traverses the nodes's subnodes and lets the consumer gobble the files to drop
	 * @param fileDropper - a {@link Consumer} for the file to drop 
	 */
	public void drop(Consumer<File> fileDropper) {
		switch (nodeType) {
			case dropfile:
				fileDropper.accept( file);
				//System.out.println("dropping [" + file.getAbsolutePath() + "]");
				break;
			/*
			case deadNode:
				System.out.println("dead node [" + file.getAbsolutePath() + "]");
				break;
			*/
			default:
				if (subNodes == null)
					return;
				for (ArtifactChangesTreeNode node : subNodes.values()) {
					node.drop( fileDropper);
				}				
				break;				
		}
													
	}
	
}	
