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
import com.braintribe.model.artifact.meta.Snapshot;
import com.braintribe.model.artifact.meta.Versioning;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.test.multi.framework.SnapshotTuple;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.xml.parser.DomParser;

public class DirectSnapshotRepositoryAccessClient extends AbstractDirectSnapshotClientBase implements RepositoryAccessClient {
	private LockFactory lockFactory = new FilesystemSemaphoreLockFactory();

	

	public DirectSnapshotRepositoryAccessClient(String key, boolean expansive, SnapshotTuple... snapshotTuples) {
		super(key, expansive, snapshotTuples);
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
		List<String> result = new ArrayList<String>();
		
		SnapshotTuple tuple = identifyTuple( tuples, groupCandiate, artifactCandidate, versionCandidate);
		if (tuple == null) {
			throw new RepositoryAccessException( "cannot identify tuple from [" + location + "]");
		}
		for (int i = 0; i < tuple.getBuilds().length; i++) {
			String prefix = buildFilenamePrefix( tuple, i);								
			if (expansive) {
				for (String extension : expansive_extensions) {
					result.add( prefix + extension);
				}
			}
			else {
				for (String extension : restricted_extensions) {
					result.add( prefix + extension);
				}
			}	
			result.add( location + "/maven-metadata.xml");
			result.add( location + "/maven-metadata.xml.md5");
			result.add( location + "/maven-metadata.xml.sha1");
		}
		return result;
	}			

	@Override
	public List<String> extractVersionDirectoriesFromRepository(String location, Server server) throws RepositoryAccessException {
		String [] locationSplit = location.split("/");
		int len = locationSplit.length;
		String groupCandiate = locationSplit[ len-2];
		String artifactCandidate = locationSplit[len-1];
		List<String> versions = new ArrayList<String>();
		for (SnapshotTuple tuple : tuples) { 
			String artifactAsString = tuple.getArtifact();
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
		File file = new File( target);
		String contents = "fake content for [" + name + "]";
		if (name.equalsIgnoreCase("maven-metadata.xml")) {
			MavenMetaData metaData = extractMavenMetaData(lockFactory, source, server);
			contents = buildMavenMetaDataStringRepresentation(metaData);					
		}		
		else if (name.endsWith(".pom")) {
			try {
				contents = extractPom(source);
			} catch (IOException e) {
				throw new RepositoryAccessException( "cannot produce pom file for ["+ target + "] from [" + source + "] ");
			}
		}
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
			MavenMetaData metaData = extractMavenMetaData(lockFactory, source, server);
			contents = buildMavenMetaDataStringRepresentation(metaData);
		}
		else if (name.endsWith(".pom")) {
			try {
				contents = extractPom(source);
			} catch (IOException e) {
				throw new RepositoryAccessException( "cannot produce pom file for [" + source + "] ");
			}
		}
		return contents;
	}
	

	@Override
	public MavenMetaData extractMavenMetaData(LockFactory lockFactory, String source, Server server) throws RepositoryAccessException {
		// check what form of meta data we must return
		for (SnapshotTuple tuple : tuples) {
			Artifact artifact = buildArtifact( tuple.getArtifact());
			 
			String grpPathPart = artifact.getGroupId().replace('.',  '/');
			String identificationPattern = RepositoryReflectionHelper.safelyCombineUrlPathParts( grpPathPart, artifact.getArtifactId(), "maven-metadata.xml");
			String solutionPattern = RepositoryReflectionHelper.safelyCombineUrlPathParts( grpPathPart, artifact.getArtifactId(), artifact.getVersion(), "maven-metadata.xml");
			
		// a) identification
			if (source.endsWith(identificationPattern)) {
				return buildArtifactMavenMetaData( artifact);
			}
		// b) solution
			if (source.endsWith( solutionPattern)) {
				return buildSolutionMavenMetaData(tuple);
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

	private MavenMetaData buildSolutionMavenMetaData( SnapshotTuple tuple) throws RepositoryAccessException {
		try {
			MavenMetaData metaData = MavenMetaDataProcessor.createMetaData( RepositoryReflectionHelper.solutionFromArtifact( buildArtifact( tuple.getArtifact())));
			Versioning versioning = Versioning.T.create();
			Snapshot snapshot = Snapshot.T.create();
			int max = -1;
			int maxI = 0;
			for (int i = 0; i < tuple.getBuilds().length; i++) {
				int build = tuple.getBuilds()[i];
				if (build > max) {
					max = build;
					maxI = i;
				}
			}
			snapshot.setBuildNumber( tuple.getBuilds()[maxI]);
			snapshot.setTimestamp( tuple.getTimestamps()[maxI]);
			versioning.setSnapshot(snapshot);
			versioning.setLastUpdated( tuple.getTimestamps()[maxI]);
			metaData.setVersioning(versioning);			
			return metaData;
		} catch (Exception e) {
			throw new RepositoryAccessException(e);
		}			
	}
		
	
	private String buildMavenMetaDataStringRepresentation( MavenMetaData metaData) throws RepositoryAccessException{
		MavenMetaDataCodec codec = new MavenMetaDataCodec();
		try {
			Document mavenMetaDataDoc = codec.encode(metaData);
			String contents = DomParser.write().from(mavenMetaDataDoc).to();
			return contents;
		} catch (Exception e) {
			throw new RepositoryAccessException(e);
		}		 
	}	
	
	private MavenMetaData buildArtifactMavenMetaData(Artifact identification) throws RepositoryAccessException {
		MavenMetaData metaData = null;
		for (SnapshotTuple tuple : tuples) {
			Artifact suspect = buildArtifact( tuple.getArtifact());		
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
	
	
}
