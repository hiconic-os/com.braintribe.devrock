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
package com.braintribe.test.framework.artifactBuilder;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;



/**
 * simple artifact generator 
 * creates one or more simple artifacts, based on the templates in "res/builder/artifact"<br/>
 * format is a follows : <br/>
 * {@code <groupId>:<artifactId>#<version>[,<version>,...,<version>]}<br/>
 * {@code [<groupId>:<artifactId>#<version>[,<version>,....,<version>]}<br/>
 * or<br/>
 * 
 * {@code -f <file name>}<br/> 
 * where file is formatted as above<br/>
 * 
 * note: dependencies must managed manually :-)
 * 
 * @author pit
 *
 */
public class TestArtifactBuilder {
	private File resDir = new File("res/builder/artifact");

	private void copyFiles(File sourceDir, File targetDir) {
		File [] sources = sourceDir.listFiles();
		if (sources != null) {
			for (File source : sources) {
				File target = new File( targetDir, source.getName());
				if (source.isDirectory()) {
					target.mkdirs();
					copyFiles( source, target);
				}
				else {
					FileTools.copyFile(source, target);
				}
			}
		}
	}
	
	private void adaptPomFile( Artifact artifact, File target) {
		try {
			Document doc = DomParser.load().from( target);
			Element parent = doc.getDocumentElement();
			DomUtils.setElementValueByPath(parent, "groupId", artifact.getGroupId(), false);
			DomUtils.setElementValueByPath(parent, "artifactId", artifact.getArtifactId(), false);
			DomUtils.setElementValueByPath(parent, "version", VersionProcessor.toString(artifact.getVersion()), false);
			DomParser.write().from(doc).to(target);
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail( "cannot manipulate document as " + e.getMessage());
		} 
		
	}
	
	private void adaptBuildFile( Artifact artifact, File target){
		try {
			Document doc = DomParser.load().from( target);
			Element parent = doc.getDocumentElement();
			parent.setAttribute( "name", artifact.getArtifactId());
			DomParser.write().from(doc).to(target);
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail( "cannot manipulate document as " + e.getMessage());
		} 		
	}
	
	private void adaptProjectFile( Artifact artifact, File target){
		try {
			Document doc = DomParser.load().from( target);
			Element parent = doc.getDocumentElement();			
			DomUtils.setElementValueByPath(parent, "name", artifact.getArtifactId() + "-" + VersionProcessor.toString( artifact.getVersion()), false);
			DomParser.write().from(doc).to(target);
		} catch (DomParserException e) {
			e.printStackTrace();
			Assert.fail( "cannot manipulate document as " + e.getMessage());
		} 
	}
	
	public void createTestArtifact( Artifact artifact, File targetRoot){
		try {
			String targetName = NameParser.buildPartialWorkingCopyPath(artifact, artifact.getVersion(), targetRoot.getAbsolutePath());
			File target = new File( targetName);
			target.mkdirs();
			copyFiles(resDir,  target);
			adaptPomFile( artifact, new File( targetName, "pom.xml"));
			adaptBuildFile( artifact, new File( targetName, "build.xml"));
			adaptProjectFile( artifact, new File( targetName, ".project"));
		} catch (NameParserException e) {			
			e.printStackTrace();
			Assert.fail( "cannot determine target location " + e.getMessage());
		}
		
	}

	public static void main( String [] args) {
		TestArtifactBuilder builder = new TestArtifactBuilder();
		for (String arg : args) {
			arg = arg.trim();
			if (arg.startsWith( "-f")) {
				arg = arg.substring(3).trim();
				try {
					String contents = IOTools.slurp( new File( arg), "UTF-8");
					String [] contentArgs = contents.split( System.lineSeparator());
					for (String cArg : contentArgs) {
						builder.parse( cArg.trim());
					}
				} catch (IOException e) {
					Assert.fail( "cannot read parameter file [" + arg +"] as "+ e.getMessage());
				}
			}
			else {
				builder.parse(arg);
			}
			
		}
	}

	private void parse(String arg) {
		int ai = arg.indexOf(":");
		int vi = arg.indexOf( "#");
		String groupId = arg.substring(0,  ai);
		String artifactId = arg.substring( ai+1, vi);
		String versionSuspect = arg.substring(vi+1);
		String [] versions = versionSuspect.split(",");
		for (String version : versions) {
			try {
				Artifact artifact = Artifact.T.create();
				artifact.setGroupId(groupId);
				artifact.setArtifactId(artifactId);
				artifact.setVersion( VersionProcessor.createFromString(version));
				createTestArtifact(artifact, new File(System.getenv( "BT__ARTIFACTS_HOME")));					
			} catch (VersionProcessingException e) {
				Assert.fail( "cannot parse version from [" + arg + "] as " + e.getMessage());
			}
		}
	}

}
