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
package com.braintribe.devrock.repolet.descriptive;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.common.RepoletCommons;
import com.braintribe.devrock.repolet.content.ContentGenerator;
import com.braintribe.devrock.repolet.generator.RepoletContentGenerator;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.xml.parser.DomParserException;
import com.braintribe.web.velocity.renderer.VelocityTemplateRendererException;


public class Navigator {
	private ContentGenerator generator = new ContentGenerator();
	private String root;
	private Map<String,NavigatorNode> nodes = new HashMap<>();
	private RepoletContent content;
	private NavigatorNode mainNode = new NavigatorNode();
	{
		nodes.put("", mainNode);
	}
	
	public void setRoot(String root) {
		this.root = root;
	}

	class NodeComparator implements Comparator<Pair<String,String>> {

		@Override
		public int compare(Pair<String, String> o1, Pair<String, String> o2) {
			return o1.first.compareTo( o2.first);
		}
		
	}
	
	class NavigatorNode  {		
		NavigatorNode parent;
		Set<NavigatorNode> children = new HashSet<>();
		String value;
		String name;
		Artifact artifact;
		String partClassifier;
		Resource resource;
		String firstLevelMetadata;
		String secondLevelMetadata;
		DateTime timestamp;
		String buildNumber;
		
		
		public Map<String,String> writeTo(String path, OutputStream out){	
			
			OutputStream stream = out;
			Map<String,MessageDigest> digests = new HashMap<>();
			for (Entry<String, Pair<String,String>> entry : RepoletCommons.hashAlgToHeaderKeyAndExtension.entrySet()) {
				try {
					MessageDigest digest = MessageDigest.getInstance( entry.getKey());
					DigestOutputStream digestStream = new DigestOutputStream(stream, digest);
					stream = digestStream;
					digests.put( entry.getValue().first, digest);
				} catch (NoSuchAlgorithmException e) {
					throw Exceptions.unchecked(e, "cannot produce output for node [" + value + "] at [" + path + "]", IllegalStateException::new);
				}		
			}
			
			if (artifact != null) {
				// dump listing of artifact
				List<Pair<String,String>> files = new ArrayList<>();
				for (NavigatorNode node : children) {				
					files.add( Pair.of( node.name, node.name));
				}
				files.sort( new NodeComparator());
				try {
					String rendered = generator.descriptiveRender(root, path + "/", files);
					try {
						stream.write( rendered.getBytes());
					} catch (IOException e) {
						throw Exceptions.unchecked(e, "cannot render output for node [" + value + "] at [" + path + "]", IllegalStateException::new);
					}
				} catch (VelocityTemplateRendererException e) {
					throw Exceptions.unchecked(e, "cannot render output for node [" + value + "] at [" + path + "]", IllegalStateException::new);
				}
				
				
			}
			else if (partClassifier != null) {		
				if (partClassifier.equals(":pom")) {
					RepoletContentGenerator.writePomToStream(parent.artifact, stream);
				}
				else {
					if (resource != null) {
						try {
							IOTools.pump(resource.openStream(), stream, IOTools.SIZE_64K);
						} catch (IOException e) {
							throw Exceptions.unchecked(e, "cannot render output for node [" + value + "] at [" + path + "] as pumping from resource failed", IllegalStateException::new);
						}
					}
				}
			}
			else if (firstLevelMetadata != null) {			
				List<Artifact> artifacts = content.getArtifacts().stream().filter( a -> firstLevelMetadata.equals( a.getGroupId() + ":" + a.getArtifactId())).collect(Collectors.toList());
				// produce metadata 
				try {					
					RepoletContentGenerator.writeMetadataToStream(ArtifactIdentification.parse( firstLevelMetadata), artifacts, stream);
				} catch (DomParserException e) {
					throw Exceptions.unchecked(e, "cannot render output for node [" + value + "] at [" + path + "]", IllegalStateException::new);
				}
			}
			else if (secondLevelMetadata != null) {
				try {
					Artifact artifact = parent.artifact;
					List<String> files = parent.children.stream().filter( s -> !s.name.endsWith("maven-metadata.xml")).map( s -> s.name).collect(Collectors.toList());
					RepoletContentGenerator.writeSecondLevelMetadataToStream(artifact, files, timestamp, buildNumber, stream);
				} catch (DomParserException e) {
					throw Exceptions.unchecked(e, "cannot render output for node [" + value + "] at [" + path + "]", IllegalStateException::new);
				}
			}
			else {
				// listing of directories
				List<Pair<String,String>> files = new ArrayList<>();
				for (NavigatorNode node : children) {		
					files.add( Pair.of( "/" + root + path + "/" + node.name + "/", node.name));
				}
				files.sort(new NodeComparator());
				try {
					
					String rendered = generator.descriptiveRender(root, path, files);
					try {
						stream.write( rendered.getBytes());
					} catch (IOException e) {
						throw Exceptions.unchecked(e, "cannot render output for node [" + value + "] at [" + path + "]", IllegalStateException::new);
					}
				} catch (VelocityTemplateRendererException e) {
					e.printStackTrace();
				}				
			}
			
			try {
				stream.flush();
				stream.close();
			} catch (IOException e) {
				throw Exceptions.unchecked(e, "cannot render output for node [" + value + "] at [" + path + "]", IllegalStateException::new);
			}
			
			Map<String,String> hashes = new HashMap<>();
			for (Map.Entry<String, MessageDigest> entry : digests.entrySet()) {
				hashes.put( entry.getKey(), StringTools.toHex( entry.getValue().digest()));
			}		
			return hashes;
		}
		
		
	}
	
	private NavigatorNode acquireArtifactNode( Artifact artifact) {
		String path = artifact.getGroupId().replace('.', '/') + "/" + artifact.getArtifactId() + "/" + artifact.getVersion() + "/";
		int i = path.indexOf( '/');
		NavigatorNode current = null;
		boolean first = true;
		while (i > 0) {
			String key = "/" + path.substring(0, i);
			NavigatorNode node = nodes.computeIfAbsent(key, k -> {
				NavigatorNode c = new NavigatorNode();				
				c.value = k;			
				c.name= k.substring( k.lastIndexOf( '/')+1);
				return c;
			});
			if (current != null) {
				node.parent = current;
				current.children.add(node);
			}
			
			if (first) {
				mainNode.children.add(node);
				first = false;
			}
			current = node;
			
			i = path.indexOf('/', i+1);				
		}
		//
		current.artifact = artifact;
		
		return current;
	}
	/**
	 * @param content
	 */
	public Navigator( RepoletContent content) {
		this.content = content;
		DateTime timestamp = new DateTime();
		String buildNumber = "10815";
			
		for (Artifact artifact : content.getArtifacts()) {			
			boolean isSnapshot = artifact.getVersion().endsWith( "SNAPSHOT");
			
			NavigatorNode artifactNode = acquireArtifactNode(artifact);
			String path = artifact.getGroupId().replace('.', '/') + "/" + artifact.getArtifactId() + "/" + artifact.getVersion() + "/";						
		
			// node for first level metadata 			
			String parentKey = "/" + artifact.getGroupId().replace('.', '/') + "/" + artifact.getArtifactId();
			String metaKey = parentKey + "/maven-metadata.xml";			
			NavigatorNode metaNode = new NavigatorNode();
			metaNode.firstLevelMetadata = artifact.getGroupId() + ":" + artifact.getArtifactId();
			metaNode.value = metaKey;
			metaNode.name="maven-metadata.xml";			
			NavigatorNode parentNode = nodes.get( parentKey);
			parentNode.children.add(metaNode);
			nodes.put( metaKey, metaNode);
					
			// node for pom part  
			NavigatorNode pomNode = new NavigatorNode();
			
			String pomName;
			if (!isSnapshot) {
				pomName = artifact.getArtifactId() + "-" + artifact.getVersion() + ".pom";
			}
			else {
				pomName = RepoletContentGenerator.produceSnapshotName(artifact, timestamp, buildNumber, PartIdentification.create("pom"));
			}
			String pomKey = "/" + path + pomName;
			pomNode.partClassifier = ":pom";
			pomNode.parent = artifactNode;
			pomNode.name= pomName;
			pomNode.value = pomKey;
			artifactNode.children.add( pomNode);
			nodes.put( pomKey, pomNode);
			
			// node for second level metadata
			if (isSnapshot) {
				NavigatorNode metadataNode = new NavigatorNode();				
				String metadataKey = "/" + path + "maven-metadata.xml";
				metadataNode.secondLevelMetadata = artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + artifact.getVersion();
				metadataNode.parent = artifactNode;
				metadataNode.name=  "maven-metadata.xml";
				metadataNode.value = metadataKey;
				metadataNode.timestamp = timestamp;
				metadataNode.buildNumber = buildNumber;
				artifactNode.children.add( metadataNode);
				nodes.put( metadataKey, metadataNode);
			}
			
			
			for (Map.Entry<String,Resource> entry : artifact.getParts().entrySet()) {
				NavigatorNode node = new NavigatorNode();
				
				PartIdentification pi = PartIdentification.parse(entry.getKey());
				
				
				String name = null;
				
				if (!isSnapshot) {
					
					if (pi.getClassifier() == null) {
						name = artifact.getArtifactId() + "-" + artifact.getVersion() + "." + pi.getType();					
					}
					else {
						name = artifact.getArtifactId() + "-" + artifact.getVersion() + "-" + pi.getClassifier() + "." + pi.getType();
					}
				}
				else {					
					name = RepoletContentGenerator.produceSnapshotName( artifact, timestamp, buildNumber, pi);
				}
				String key = "/" + path + name;
				node.partClassifier = pi.asString();
				node.value = key;
				node.name= name;
				node.resource = entry.getValue();
				node.parent = artifactNode;				
				artifactNode.children.add( node);
				nodes.put(key, node);
			}
			
			
			
		}
	}
	
	public NavigatorNode getNode( String path) {
		return nodes.get( path);						
	}

	public void removeNode(String subPath) {
		NavigatorNode node = nodes.get( subPath);
		if (node == null)
			return;
		NavigatorNode parent = node.parent;
		parent.children.remove( node);		
		nodes.remove(subPath);		
		
		if (parent.children.size() == 0) {
			removeNode( parent.value);
		}
	}
		
	public synchronized void addPart(String subPath, File file) {
		Pair<String,String> details = RepoletCommons.extractArtifactExpression(subPath);
		
		NavigatorNode node = nodes.get( subPath);
		if (node == null) {
			Artifact artifact = Artifact.from( details.first);						
			node = acquireArtifactNode(artifact);
		}
		boolean found = false;
		for (NavigatorNode partNode : node.children) {
			if (partNode.name.equalsIgnoreCase( details.second)) {
				partNode.resource = Resource.createTransient( () -> new FileInputStream(file));
				found = true;
				break;
			}
		}
		if (!found) {
			NavigatorNode partNode = new NavigatorNode();
			partNode.name = details.second;
			partNode.value = subPath;
			partNode.resource = Resource.createTransient( () -> new FileInputStream(file));
			partNode.parent = node;
			node.children.add( partNode);
			nodes.put( subPath, partNode);
		}
		
	}
	public List<Artifact> getKnownArtifacts() {
		return nodes.values().stream().filter( n -> n.artifact != null).map( n -> n.artifact).collect(Collectors.toList());
	}
	
}
