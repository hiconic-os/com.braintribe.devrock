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
package com.braintribe.test.framework;

import java.io.File;

import org.junit.Assert;
import org.w3c.dom.Document;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class TestUtil {

	public static void delete( File file) {
		if (file == null || file.exists() == false)
			return;
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				if (child.isDirectory()) {
					delete( child);
				} 
				child.delete();			
			}
		}
		
		file.delete();
		
	}

	public static void ensure(String checkdir) {
		File output = new File(checkdir);
		if (output.exists())
			delete( output);
		output.mkdirs();
	}
	public static void ensure(File output) {	
		if (output.exists())
			delete( output);
		output.mkdirs();
	}
	
	public static void clearUpdateFiles(File root, String [] branches) {		
		try {
			for (String branch : branches) {
				clearUpdateFiles( root, branch);
			}
		} catch (RepresentationException e) {
			Assert.fail("Cannot determine local repository to remove test branch");
		}
	}
	
	public static void clearUpdateFiles( File root, String branch) throws RepresentationException {
		File testBranch = new File (root, branch);
		if (testBranch.exists()) {
			clearUpdateFiles( testBranch);
		}
	}
	
	public static void clearUpdateFiles(File directory) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				clearUpdateFiles( file);
			}
			String name = file.getName();
			if (name.equals( ".updated.artifact.redeploy")) {
				file.delete();
				continue;
			}
			if (name.equalsIgnoreCase( ".updated.solution")) {
				file.delete();
				continue;
			}
		}
	}
	
	public static void clearArtifact( String url, Artifact artifact) {
		File location = new File(RepositoryReflectionHelper.getArtifactFilesystemLocation(url, artifact));
		delete(location);
	}

	public static void copyArtifact( String url, Artifact artifact, String sourceVersion, String targetVersion) throws Exception {
		File artifactLocation = new File(RepositoryReflectionHelper.getArtifactFilesystemLocation(url, artifact));
		File targetSolutionDirectory = new File( artifactLocation, targetVersion);
		targetSolutionDirectory.mkdir();
		
		File solutionLocation = new File( artifactLocation, sourceVersion);
		for (File file : solutionLocation.listFiles()) {
			String name = file.getName();
			if (!name.startsWith( artifact.getArtifactId() + "-")) {
				continue;
			}
			String extension = name.substring( name.lastIndexOf('.'));
			String newName = artifact.getArtifactId() + "-" + targetVersion + extension;			
			File target =  new File( targetSolutionDirectory, newName);
			FileTools.copyFile(file, target);
			if (extension.equalsIgnoreCase(".pom")) {
				try {
					Document document = DomParser.load().from(target);
					DomUtils.setElementValueByPath( document.getDocumentElement(), "version", targetVersion, false);
					DomParser.write().from(document).to(target);
				} catch (DomParserException e) {
					throw new RuntimeException(e);
				}
			}			
		}
		
	}
	
	public static void copy(File source, File target){
		try {
			FileTools.copyFileOrDirectory(source, target);
		} catch (Exception e) {
			throw new IllegalStateException("cannot copy [" + source.getAbsolutePath() + "] to [" + target.getAbsolutePath() + "]", e);
		}
	}
	
	public static void copy(File source, File targetDirectory, String name) {
		if (!source.exists()) {
			return;
		}
		File target = new File( targetDirectory, name);
		FileTools.copyFile(source, target);
	}
}
