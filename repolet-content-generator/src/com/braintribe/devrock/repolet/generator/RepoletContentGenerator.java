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
package com.braintribe.devrock.repolet.generator;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.devrock.model.repolet.content.Dependency;
import com.braintribe.devrock.model.repolet.content.Property;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.parser.RepoletContentParser;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * content generator for the descriptive repolet
 * @author pit
 *
 */
public class RepoletContentGenerator {
	public static RepoletContentGenerator INSTANCE = new RepoletContentGenerator( false);
	public static RepoletContentGenerator HASH_PRODUCING_INSTANCE = new RepoletContentGenerator( true);
	protected static DateTimeFormatter timeFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
	private YamlMarshaller marshaller = new YamlMarshaller();
	private boolean produceHashesAsFiles;
	
	public RepoletContentGenerator( boolean produceHashes) {
		this.produceHashesAsFiles = produceHashes;
	}
	
	static final HashingComparator<ArtifactIdentification> artifactIdentification = EntityHashingComparator
			.build( ArtifactIdentification.T)
			.addField( ArtifactIdentification.groupId)
			.addField( ArtifactIdentification.artifactId)
			.done();

	private static void setIdentification( Element parent, VersionedArtifactIdentification vai) {
		setIdentification(parent, vai, false);
	}
	private static void setIdentification( Element parent, VersionedArtifactIdentification vai, boolean partially) {	
		String groupId = vai.getGroupId();
		if (groupId != null && !groupId.isEmpty()) {
			DomUtils.setElementValueByPath(parent, "groupId", groupId, true);
		}	
		String artifactId = vai.getArtifactId();
		if (artifactId == null || artifactId.isEmpty()) {
			if (!partially) {
				throw new IllegalStateException("artifact id must always be defined");
			}
		}
		DomUtils.setElementValueByPath(parent, "artifactId", artifactId, true);
		String version = vai.getVersion();
		if (version != null && !version.isEmpty()) {
			DomUtils.setElementValueByPath(parent, "version", version, true);
		}
	}

	private File generateGroupFolder( File folder, String grp) {
		String [] grps = grp.split("\\.");
		File parent = folder;
		for (String fgrp : grps) {
			parent = new File( parent, fgrp);
		}
		return parent;
	}

	
	/**
	 * read an expressive file and generate content from it
	 * @param target 
	 * @param source
	 */
	public void generateExpressive(File target, File source) {
		if (!source.exists()) {
			throw new IllegalStateException("file [" + source.getAbsolutePath() + "] doesn't exist");					
		}
		try (InputStream in = new FileInputStream(source)) {
			RepoletContent content = RepoletContentParser.INSTANCE.parse(in);			
			generate(target, content);
		}
		catch (Exception e) {
			throw new IllegalStateException("file [" + source.getAbsolutePath() + "] cannot be read");
		}
	}
	
	/**
	 * read a generic yaml file and generate content from it
	 * @param target
	 * @param source
	 */
	public void generateMarshalled(File target, File source) {
		if (!source.exists()) {
			throw new IllegalStateException("file [" + source.getAbsolutePath() + "] doesn't exist");					
		}
		try (InputStream in = new FileInputStream(source)) {
			RepoletContent content = (RepoletContent) marshaller.unmarshall(in);
			generate(target, content);
		}
		catch (Exception e) {
			throw new IllegalStateException("file [" + source.getAbsolutePath() + "] cannot be read");
		}
	}
	
	/**
	 * generate the content into the folder
	 * @param target - the output folder
	 * @param content - the {@link RepoletContent} to transfer
	 * @param repoId - null for a standard remote repo, otherwise the name of the repo (for local repository contents)
	 */
	public void generate(File target, RepoletContent content) {
		Map<String, File> artifactIdToFolder = new HashMap<>();	
		
		for (Artifact artifact : content.getArtifacts()) {		
			// directories
			File groupFolder = generateGroupFolder(target, artifact.getGroupId());
			File artifactFolder = new File( groupFolder, artifact.getArtifactId());			
			artifactIdToFolder.put( artifact.getArtifactId(), artifactFolder);
			
			File versionFolder = new File( artifactFolder, artifact.getVersion());
			versionFolder.mkdirs();

			// save pom
			String filePrefix = artifact.getArtifactId() + "-" + artifact.getVersion();

			Map<String, OutputStreamer> parts = new LinkedHashMap<>();
			
			// pom part
			parts.put(":pom", o -> writePomToStream(artifact, o));
			
			// meaty parts
			for (Map.Entry<String, Resource> entry: artifact.getParts().entrySet()) {
				
				Resource resource = entry.getValue();
				if (resource != null) {
					parts.put(entry.getKey(), resource::writeToStream);
				}
			}
			
			// transfer parts and generate hashes
			for (Map.Entry<String, OutputStreamer> entry : parts.entrySet()) {
				String partClassifier = entry.getKey();
				OutputStreamer outputStreamer = entry.getValue();
				String partName = filePrefix;
				int p = partClassifier.indexOf( ':');				
				if (p >= 0) {
					String c = partClassifier.substring(0, p);
					if (c.length() > 0) {
						partName += "-" + c;
					}
					partName = partName + "." + partClassifier.substring( p+1);
				}
				else {
					partName = partName + "." + partClassifier;
				}
				
				File partFile = new File( versionFolder, partName);
				
				Map<String, String> hashes = new HashMap<>();
				try (OutputStream out = new BufferedOutputStream(new FileOutputStream(partFile))){
					hashes = writeToStreamAndGenerateHashes(outputStreamer, out);
				} catch (IOException e) {
					throw new IllegalStateException( "cannot create part file [" + partFile.getAbsolutePath() + "]", e);
				}				
				// 
				if (produceHashesAsFiles) {
					for (Map.Entry<String, String> hashEntry: hashes.entrySet()) {
						File partHashFile = new File( versionFolder, partName + "." + hashEntry.getKey());
						try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(partHashFile)), "US-ASCII")) {
							writer.write(hashEntry.getValue());
						} catch (IOException e) {
							throw new IllegalStateException( "cannot create part file [" + partFile.getAbsolutePath() + "]", e);
						}
					}
				}
			}			
		}		
		
		writeMetadata(content, artifactIdToFolder);
	}
	
	/**
	 * create a pom and write it to the stream 
	 * @param artifact - the {@link Artifact}
	 * @param bout - the {@link OutputStream}
	 * @throws DomParserException
	 */
	public static void writePomToStream(Artifact artifact, OutputStream out) {
		try {
			Document pom = createPom(artifact);		
			DomParser.write().setIndent(true).from( pom).to( out);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while writing pom for: " + artifact);
		}
	}
	
	public Map<String,String> writeToStreamAndGenerateHashes(OutputStreamer streamer, OutputStream out) {
		DigestOutputStream md5Stream = null;
		DigestOutputStream sha1Stream = null;
		DigestOutputStream sha256Stream = null;
		
		try {
			md5Stream = new DigestOutputStream(out, MessageDigest.getInstance("md5"));
			sha1Stream = new DigestOutputStream( md5Stream, MessageDigest.getInstance("sha1"));
			sha256Stream = new DigestOutputStream( sha1Stream, MessageDigest.getInstance("SHA-256"));						
			
			streamer.writeTo(sha256Stream);
			
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot determine hash of pom while writing to stream", IllegalStateException::new);
		}
		finally {
			try {
				if (sha256Stream != null) {
					sha256Stream.close();
				}
				if (sha1Stream != null) {
					sha1Stream.close();
				}
				if (md5Stream != null) {
					md5Stream.close();
				}			
			}
			catch (Exception e) {
				throw Exceptions.unchecked(e, "cannot close hashes digest stream while writing pom to stream", IllegalStateException::new);
			}
		}
		
		Map<String, String> result = new HashMap<>();
		result.put( "md5", StringTools.toHex( md5Stream.getMessageDigest().digest()));
		result.put( "sha1", StringTools.toHex( sha1Stream.getMessageDigest().digest()));
		result.put( "sha256", StringTools.toHex( sha256Stream.getMessageDigest().digest()));
		
		return result;
	}

	/**
	 * @param content
	 * @param artifactIdToFolder
	 * @return
	 */
	private Map<String,String> writeMetadata(RepoletContent content, Map<String, File> artifactIdToFolder) {
		Map<EqProxy<ArtifactIdentification>,List<Artifact>> artifactsPerName = new HashMap<>();
		for (Artifact artifact : content.getArtifacts()) {
					
			List<Artifact> artifacts = artifactsPerName.computeIfAbsent( artifactIdentification.eqProxy( artifact), (k) -> new ArrayList<>());
			artifacts.add(artifact);
		}
		for (Map.Entry<EqProxy<ArtifactIdentification>, List<Artifact>> entry : artifactsPerName.entrySet()) {
			ArtifactIdentification ai = entry.getKey().get();
			List<Artifact> artifacts = entry.getValue();
			File artifactFolder = artifactIdToFolder.get( ai.getArtifactId());
			
			String metaDataFileName = (content.getRepositoryId() != null) ? "maven-metadata-" + content.getRepositoryId() + ".xml" : "maven-metadata.xml";
			File metadataFile = new File( artifactFolder, metaDataFileName);
		
			Map<String, String> hashes;
			try (
					BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream( metadataFile))
				){				
				hashes = writeMetadataToStream(ai, artifacts, bout);
			} catch (Exception e) {
				throw new IllegalStateException( "cannot write metadata file [" + metadataFile.getAbsolutePath() + "]", e);
			}
			
			if (produceHashesAsFiles) {
				for (Map.Entry<String, String> hashEntry: hashes.entrySet()) {
					File partHashFile = new File( artifactFolder, metaDataFileName + "." + hashEntry.getKey());
					try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(partHashFile)), "US-ASCII")) {
						writer.write(hashEntry.getValue());
					} catch (IOException e) {
						throw new IllegalStateException( "cannot create part file [" + metadataFile.getAbsolutePath() + "]", e);
					}
				}
			}

		}
		return null;
	}

	/**
	 * writes a maven-metadata file into the passed output stream 
	 * @param ai - the common {@link ArtifactIdentification}
	 * @param artifacts - a {@link List} of specific {@link Artifact}
	 * @param bout - the {@link OutputStream}
	 * @throws DomParserException
	 */
	public static Map<String,String> writeMetadataToStream(ArtifactIdentification ai, List<Artifact> artifacts, OutputStream bout) throws DomParserException {
		Document metadata = DomParser.create().makeItSo();
		Element metadataE = metadata.createElement("metadata");
		metadata.appendChild(metadataE);
		DomUtils.setElementValueByPath(metadataE, "groupId", ai.getGroupId(), true);
		DomUtils.setElementValueByPath(metadataE, "artifactId", ai.getArtifactId(), true);
		
		Element versioningE = createElement(metadataE, "versioning");
		DomUtils.setElementValueByPath( versioningE, "lastUpdated", timeFormat.print( new DateTime()), true);
		Element versionsE = createElement(versioningE, "versions");
		
		for (Artifact artifact : artifacts) {
			String version = artifact.getVersion();
			Element versionE = createElement(versionsE, "version");
			versionE.setTextContent( version);
		}
		
		DigestOutputStream md5Stream = null;
		DigestOutputStream sha1Stream = null;
		DigestOutputStream sha256Stream = null;
		try {
			md5Stream = new DigestOutputStream(bout, MessageDigest.getInstance("md5"));
			sha1Stream = new DigestOutputStream( md5Stream, MessageDigest.getInstance("sha1"));
			sha256Stream = new DigestOutputStream( sha1Stream, MessageDigest.getInstance("SHA-256"));						
					
					
			DomParser.write().setIndent(true).from(metadata).to( sha256Stream);
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot determine hash of pom while writing to stream", IllegalStateException::new);
		}
		finally {
			try {
				if (sha256Stream != null) {
					sha256Stream.close();
				}
				if (sha1Stream != null) {
					sha1Stream.close();
				}
				if (md5Stream != null) {
					md5Stream.close();
				}			
			}
			catch (Exception e) {
				throw Exceptions.unchecked(e, "cannot close hashes digest stream while writing pom to stream", IllegalStateException::new);
			}
		}
		
		Map<String, String> result = new HashMap<>();
		result.put( "md5", StringTools.toHex( md5Stream.getMessageDigest().digest()));
		result.put( "sha1", StringTools.toHex( sha1Stream.getMessageDigest().digest()));
		result.put( "sha256", StringTools.toHex( sha256Stream.getMessageDigest().digest()));
	
		return result;
			
	}
	
	public static Map<String,String> writeSecondLevelMetadataToStream(Artifact artifact, List<String> files, DateTime timestamp, String buildNumber, OutputStream stream) throws DomParserException {
		// 
		Document metadata = DomParser.create().makeItSo();
		Element metadataE = metadata.createElement("metadata");
		metadata.appendChild(metadataE);
		DomUtils.setElementValueByPath(metadataE, "groupId", artifact.getGroupId(), true);
		DomUtils.setElementValueByPath(metadataE, "artifactId", artifact.getArtifactId(), true);
		DomUtils.setElementValueByPath(metadataE, "version", artifact.getVersion(), true);
		
		// create contents
		// versioning
		Element versioningE = createElement(metadataE, "versioning");
		
		String lastUpdated = timeFormat.print( timestamp);
		String dateAsString = lastUpdated.substring(0, 8);
		String timeAsString = lastUpdated.substring( 8);
		
		
		DomUtils.setElementValueByPath( versioningE, "lastUpdated", lastUpdated, true);
		
		Element snapshotE = createElement(metadataE, "snapshot");
		versioningE.appendChild(snapshotE);
				
		DomUtils.setElementValueByPath(snapshotE, "timestamp", dateAsString + "." + timeAsString, true);
		DomUtils.setElementValueByPath(snapshotE, "buildNumber", buildNumber, true);
		
		Element snapshotVersionsE = createElement(metadataE, "snapshotVersions");
		versioningE.appendChild( snapshotVersionsE);
		String prefix = artifact.getArtifactId();
		int len = prefix.length();
		
		for (String file : files) {
			String remainder = file.substring(len + 1);
			String type;
			String classifier = null;

			int pBuildNumber = remainder.lastIndexOf(buildNumber);
			String afterBuildNumber = remainder.substring(pBuildNumber + buildNumber.length());
			int p = afterBuildNumber.indexOf( '-');
			
			if (p < 0) {
				type = afterBuildNumber.substring( afterBuildNumber.lastIndexOf('.') + 1);				
			}
			else {						
				int c = afterBuildNumber.indexOf( '.', p);				
				classifier = afterBuildNumber.substring( p+1, c);
				type = afterBuildNumber.substring( c+1);
			}
			
			Element snapshotVersionE = createElement(snapshotVersionsE, "snapshotVersion");
			snapshotVersionsE.appendChild(snapshotVersionE);
			
			if (classifier != null && classifier.length() > 0) {
				DomUtils.setElementValueByPath(snapshotVersionE, "classifier", classifier, true);
			}
			DomUtils.setElementValueByPath(snapshotVersionE, "extension", type, true);			
			String value = produceSnapshotVersion(artifact, timestamp, buildNumber);
			DomUtils.setElementValueByPath(snapshotVersionE, "value", value, true);
			DomUtils.setElementValueByPath(snapshotVersionE, "updated", timeFormat.print( timestamp), true);
			
			
		}
		
				
		DigestOutputStream md5Stream = null;
		DigestOutputStream sha1Stream = null;
		DigestOutputStream sha256Stream = null;
		try {
			md5Stream = new DigestOutputStream(stream, MessageDigest.getInstance("md5"));
			sha1Stream = new DigestOutputStream( md5Stream, MessageDigest.getInstance("sha1"));
			sha256Stream = new DigestOutputStream( sha1Stream, MessageDigest.getInstance("SHA-256"));						
					
					
			DomParser.write().setIndent(true).from(metadata).to( sha256Stream);
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot determine hash of pom while writing to stream", IllegalStateException::new);
		}
		finally {
			try {
				if (sha256Stream != null) {
					sha256Stream.close();
				}
				if (sha1Stream != null) {
					sha1Stream.close();
				}
				if (md5Stream != null) {
					md5Stream.close();
				}			
			}
			catch (Exception e) {
				throw Exceptions.unchecked(e, "cannot close hashes digest stream while writing pom to stream", IllegalStateException::new);
			}
		}
		
		Map<String, String> result = new HashMap<>();
		result.put( "md5", StringTools.toHex( md5Stream.getMessageDigest().digest()));
		result.put( "sha1", StringTools.toHex( sha1Stream.getMessageDigest().digest()));
		result.put( "sha256", StringTools.toHex( sha256Stream.getMessageDigest().digest()));
	
		return result;
		
	}
	
	private static Element createElement( Element parent, String tag) {
		Element element = parent.getOwnerDocument().createElement(tag);
		parent.appendChild(element);
		return element;
	}
	
	private static Element createExclusion( Element parent, String exclusion) {
		Element exclusionE = createElement(parent, "exclusion");
		if (exclusion.length() > 1) {					
			String [] values = exclusion.split(":");
			if (values[0].length() > 0) 
				DomUtils.setElementValueByPath(exclusionE, "groupId", values[0], true);
			if (values[1].length() > 0)
				DomUtils.setElementValueByPath(exclusionE, "artifactId", values[1], true);
		}		
		return exclusionE;
	}
	
	/**
	 * creates a fully fledged pom for the passed {@link Artifact}
	 * @param artifact
	 * @return
	 */
	private static Document createPom(Artifact artifact) {
		try {
			
			
			Document pom = DomParser.create().makeItSo();
			Element projectE = pom.createElement( "project");
			pom.appendChild(projectE);			
			projectE.setAttribute("xmlns", "http://maven.apache.org/POM/4.0.0");
			projectE.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
			projectE.setAttribute("xsi:schemaLocation","http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
			// identification 
			setIdentification( projectE, artifact);
			// if override's set, override it 
			if (artifact.getVersionOverride() != null) {
				DomUtils.setElementValueByPath( projectE, "version", artifact.getVersionOverride(), true);
			}
			
			if (artifact.getPackaging() != null) {
				DomUtils.setElementValueByPath(projectE, "packaging", artifact.getPackaging(), true);
			}
		
			// parent
			VersionedArtifactIdentification parent = artifact.getParent();
			if (parent != null) {
				Element parentE = DomUtils.getElementByPath(projectE, "parent", true);				
				setIdentification( parentE, parent);
			}
			// properties		
			List<Property> properties = artifact.getProperties();
			if (properties.size() > 0) {
				Element propertiesE = DomUtils.getElementByPath( projectE, "properties", true);
				for (Property property : properties) {					
					DomUtils.setElementValueByPath( propertiesE,  property.getName(), property.getValue(), true);
				}
			}
			// dependency management
			List<Dependency> managedDependencies = artifact.getManagedDependencies();
			if (managedDependencies.size() > 0) {
				Element dependenciesE = DomUtils.getElementByPath( projectE, "dependencyManagement/dependencies", true);
				for (Dependency dependency : managedDependencies) {
					
					Element dependencyE = createElement( dependenciesE, "dependency");
									
					
					setIdentification(dependencyE,  dependency);

					// scope
					String scope = dependency.getScope();
					if (scope != null && scope.length() > 0) {
						DomUtils.setElementValueByPath(dependencyE, "scope", scope, true);
					}
					// type
					String type = dependency.getType();
					if (type != null && type.length() > 0) {
						DomUtils.setElementValueByPath(dependencyE, "type", type, true);
					}
					// classifier
					String classifier = dependency.getClassifier();
					if (classifier != null && classifier.length() > 0) {
						DomUtils.setElementValueByPath(dependencyE, "classifier", classifier, true);
					}			
					// optional
					if (dependency.getOptional()) {
						DomUtils.setElementValueByPath(dependencyE, "optional", "true", true);
					}
				}		
			}
			
			// relocation / redirect
			Dependency redirect = artifact.getRedirection();
			if (redirect != null) {
				Element relocationE = DomUtils.getElementByPath( projectE, "distributionManagement/relocation", true);
				setIdentification(relocationE, redirect, true);
			}
				
			
			// dependencies
			List<Dependency> dependencies = artifact.getDependencies();
			if (dependencies.size() > 0) {
				Element dependenciesE = DomUtils.getElementByPath( projectE, "dependencies", true);
				for (Dependency dependency : dependencies) {
					
					Element dependencyE = createElement( dependenciesE, "dependency");
									
					
					setIdentification(dependencyE,  dependency);
					
					String scope = dependency.getScope();
					if (scope != null && scope.length() > 0) {
						DomUtils.setElementValueByPath(dependencyE, "scope", scope, true);
					}
					String type = dependency.getType();
					if (type != null && type.length() > 0) {
						DomUtils.setElementValueByPath(dependencyE, "type", type, true);
					}
					
					String classifier = dependency.getClassifier();
					if (classifier != null && classifier.length() > 0) {
						DomUtils.setElementValueByPath(dependencyE, "classifier", classifier, true);
					}
					
					// optional
					if (dependency.getOptional()) {
						DomUtils.setElementValueByPath(dependencyE, "optional", "true", true);
					}
					
					List<String> exclusions = dependency.getExclusions();
					if (exclusions.size() > 0) {
						Element exclusionsE = createElement( dependencyE, "exclusions");
						for (String exclusion : exclusions) {
							createExclusion(exclusionsE, exclusion);							
						}
					}
					Map<String,String> pis = dependency.getProcessingInstructions();
					if (pis != null && !pis.isEmpty()) {
						for (Map.Entry<String, String> entry : pis.entrySet()) {
							String key = entry.getKey();
							String value = entry.getValue();
							if (key.equals("tag") && value.contains(",")) {
								String [] tags = value.split(",");
								for (String tag : tags) {
									ProcessingInstruction processingInstruction = pom.createProcessingInstruction( key, tag.trim());
									dependencyE.appendChild( processingInstruction);		
								}
							}
							else {
								ProcessingInstruction processingInstruction = pom.createProcessingInstruction( entry.getKey(), entry.getValue());
								dependencyE.appendChild( processingInstruction);
							}
						}
					}
				}					
			}
			return pom;
			
			
		} catch (DomParserException e) {
			throw new IllegalStateException( "cannot load pom template", e);
		}
	}
	public static String produceSnapshotName(Artifact artifact, DateTime timestamp, String buildNumber, PartIdentification pi) {
		if (pi.getClassifier() == null) {		
			return artifact.getArtifactId() + "-" + produceSnapshotVersion(artifact, timestamp, buildNumber) + "." + pi.getType();
		}
		else {
			return artifact.getArtifactId() + "-" + produceSnapshotVersion(artifact, timestamp, buildNumber) + "-" + pi.getClassifier() + "." + pi.getType();
		}
	}
	
	public static String produceSnapshotVersion(Artifact artifact, DateTime timestamp, String buildNumber) {
		int p = artifact.getVersion().indexOf( "-SNAPSHOT");
		String versionPrefix = artifact.getVersion().substring(0, p);
		String lastUpdated = timeFormat.print( timestamp);
		String dateAsString = lastUpdated.substring(0, 8);
		String timeAsString = lastUpdated.substring( 8);
		
		return versionPrefix + "-" + dateAsString + "." + timeAsString + "-" + buildNumber;
	}
}
