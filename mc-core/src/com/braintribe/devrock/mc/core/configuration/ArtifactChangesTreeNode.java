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
