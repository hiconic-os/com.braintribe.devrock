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
package com.braintribe.test.multi.framework.fake.direct;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import com.braintribe.build.artifact.representations.artifact.maven.metadata.MavenMetaDataCodec;
import com.braintribe.build.artifact.representations.artifact.maven.metadata.MavenMetaDataProcessor;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClient;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessException;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.xml.parser.DomParser;

public class DirectRepositoryAccessClient extends AbstractDirectClientBase implements RepositoryAccessClient {

	private LockFactory lockFactory = new FilesystemSemaphoreLockFactory();

	public DirectRepositoryAccessClient(String key, boolean expansive, String ... artifacts) {
		super( key, expansive, artifacts);
	}
	
	

	@Override
	public boolean checkConnectivity(String location, Server server) {	
		return true;
	}



	@Override
	public List<String> extractFilenamesFromRepository(String location, Server server) throws RepositoryAccessException {		
		String [] locationSplit = location.split("/");
		int len = locationSplit.length;
		String groupCandiate = locationSplit[ len-3];
		String artifactCandidate = locationSplit[len-2];
		String versionCandidate = locationSplit[len-1];
		
		for (String artifactAsString : artifacts) {
			Artifact artifact = buildArtifact(artifactAsString);
			if (
					artifact.getGroupId().equalsIgnoreCase(groupCandiate) &&
					artifact.getArtifactId().equalsIgnoreCase(artifactCandidate) &&
					artifact.getVersion().equalsIgnoreCase(versionCandidate)
				) {
				String prefix = buildFilenamePrefix(artifactAsString);
				List<String> result = new ArrayList<String>();
				
				if (expansive) {
					for (String extension : expansive_extensions) {
						result.add( prefix + extension);
						result.add( prefix + extension + ".md5");
						result.add( prefix + extension + ".sha1");
					}
				}
				else {
					for (String extension : restricted_extensions) {
						result.add( prefix + extension);
						result.add( prefix + extension + ".md5");
						result.add( prefix + extension + ".sha1");
					}
				}	
				result.add( location + "/maven-metadata.xml");
				result.add( location + "/maven-metadata.xml.md5");
				result.add( location + "/maven-metadata.xml.sha1");
				return result;
			}
		}
		return null;
	}

	@Override
	public List<String> extractVersionDirectoriesFromRepository(String location, Server server) throws RepositoryAccessException {
		String [] locationSplit = location.split("/");
		int len = locationSplit.length;
		String groupCandiate = locationSplit[ len-2];
		String artifactCandidate = locationSplit[len-1];
		List<String> versions = new ArrayList<String>();
		for (String artifactAsString : artifacts) {
			Artifact artifact = buildArtifact(artifactAsString);
			if (
					artifact.getGroupId().equalsIgnoreCase(groupCandiate) &&
					artifact.getArtifactId().equalsIgnoreCase(artifactCandidate)
				) {
				versions.add( artifact.getVersion());
			}
		}
		return versions;		
	}

	@Override
	public File extractFileFromRepository(String source, String target, Server server) throws RepositoryAccessException {
		System.out.println("delivering [" + source + "] from [" + getKey() + "] to [" + target + "]");
		String name = source.substring( source.lastIndexOf('/')+1);		
		String contents = "fake content for [" + name + "]";		
		if (name.equalsIgnoreCase("maven-metadata.xml")) {
			MavenMetaData metadata = extractMavenMetaData(lockFactory, source, server);
			try {
				contents = buildMavenMetaDataStringRepresentation(metadata);
			} catch (Exception e) {
				throw new RepositoryAccessException(e);
			}
		}
		else {
			if (name.endsWith( ".pom")) {
				try {
					contents = extractPom(source);
				} catch (IOException e) {
					throw new RepositoryAccessException( "cannot produce pom file contents for ["+ target + "] from [" + source + "] ");
				}
			}
		}
		File file = new File( target);
		try {
			IOTools.spit( file, contents, "UTF-8", false);
		} catch (IOException e) {
			throw new RepositoryAccessException( "cannot produce fake file for ["+ target + "] from [" + source + "] ");
		}	
		return file;
	}

	@Override
	public String extractFileContentsFromRepository(String source, Server server) throws RepositoryAccessException {
		String name = source.substring( source.lastIndexOf('/')+1);
		String contents = "fake content for [" + name + "]";
		if (name.equalsIgnoreCase("maven-metadata.xml")) {
			MavenMetaData metadata = extractMavenMetaData(lockFactory, source, server);
			try {
				contents = buildMavenMetaDataStringRepresentation(metadata);
			} catch (Exception e) {
				throw new RepositoryAccessException(e);
			}
		}
		else if (name.endsWith( ".pom")) {
			try {
				contents = extractPom(source);
			} catch (IOException e) {
				throw new RepositoryAccessException( "cannot produce pom file contents from [" + source + "] ");
			}
		}		
		return contents;
	}

	
	@Override
	public MavenMetaData extractMavenMetaData(LockFactory lockFactory, String source, Server server) throws RepositoryAccessException {
		// check what form of meta data we must return
		for (String artifactAsString : artifacts) {
			Artifact artifact = buildArtifact(artifactAsString);
			 
			String grpPathPart = artifact.getGroupId().replace('.',  '/');
			String identificationPattern = RepositoryReflectionHelper.safelyCombineUrlPathParts( grpPathPart, artifact.getArtifactId(), "maven-metadata.xml");
			String solutionPattern = RepositoryReflectionHelper.safelyCombineUrlPathParts( grpPathPart, artifact.getArtifactId(), artifact.getVersion(), "maven-metadata.xml");
			
		// a) identification
			if (source.endsWith(identificationPattern)) {
				return buildArtifactMavenMetaData(artifact);
			}
		// b) solution
			if (source.endsWith( solutionPattern)) {
				return buildSolutionMavenMetaData(artifact);
			}
		}
		
		throw new RepositoryAccessException("no matching maven metadata pattern");
	}

	@Override
	public Integer uploadFile(Server server, File source, String target) throws RepositoryAccessException {	
		return null;
	}

	@Override
	public Map<File, Integer> uploadFile(Server server, Map<File, String> sourceToTargetMap) throws RepositoryAccessException {
		return null;
	}
	
	private MavenMetaData buildSolutionMavenMetaData(Artifact artifact) throws RepositoryAccessException {		
		try {
			MavenMetaData metaData = MavenMetaDataProcessor.createMetaData( RepositoryReflectionHelper.solutionFromArtifact(artifact));			
			return metaData;
		} catch (Exception e) {
			throw new RepositoryAccessException(e);
		} 
	}
	
	private MavenMetaData buildArtifactMavenMetaData(Artifact identification) throws RepositoryAccessException {
		MavenMetaData metaData = null;
		for (String artifactAsString : artifacts) {
			Artifact suspect = buildArtifact(artifactAsString);		
			if (
					suspect.getGroupId().equalsIgnoreCase( identification.getGroupId()) &&
					suspect.getArtifactId().equalsIgnoreCase( identification.getArtifactId())
				) {								
				try {
					metaData = MavenMetaDataProcessor.addSolution(metaData, RepositoryReflectionHelper.solutionFromArtifact( suspect));
				} catch (Exception e) {
					throw new RepositoryAccessException(e);
				} 
			}		
		}
		return metaData;
	}
	
	private String buildMavenMetaDataStringRepresentation( MavenMetaData metaData) throws Exception {
		MavenMetaDataCodec codec = new MavenMetaDataCodec();
		Document mavenMetaDataDoc = codec.encode(metaData);
		return DomParser.write().from(mavenMetaDataDoc).to();
	}
	
		
}
