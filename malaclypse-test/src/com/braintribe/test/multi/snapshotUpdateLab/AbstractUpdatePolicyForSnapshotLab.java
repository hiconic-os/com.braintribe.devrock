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
package com.braintribe.test.multi.snapshotUpdateLab;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.representations.artifact.maven.metadata.MavenMetaDataCodec;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.codec.CodecException;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.AbstractWalkLab;
import com.braintribe.test.multi.ClashStyle;
import com.braintribe.test.multi.WalkDenotationTypeExpert;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public abstract class AbstractUpdatePolicyForSnapshotLab extends AbstractWalkLab {
	private static LauncherShell launcherShell;
	protected static File localRepository = new File ("res/snapshotUpdateLab/contents/repo");
	private static File contents = new File( "res/snapshotUpdateLab/contents");
	private static final File [] data = new File[] { new File( contents, "archiveBase.zip"),
													 new File( contents, "archiveRelease.zip"), 
													 new File( contents, "archiveSnapshot.zip"),
													 new File( contents, "archiveSnapshotUpdate.zip"),
	};

	protected static Map<String, Repolet> launchedRepolets;	
	
	protected abstract String [] getResultsForFirstRun();
	protected abstract String [] getResultsForSecondRun();
	protected abstract void tweakEnvironment();


	protected static void before( File settings) {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		localRepository.mkdirs();
		int port = runBefore();
		
		// clean local repository
		TestUtil.delete(localRepository);
			
		// fire them up 
		launchRepolets( port);
	}

	private static void launchRepolets( int port) {
		Map<String, RepoType> map  = new HashMap<String, LauncherShell.RepoType>();
		map.put( "archive," + data[0].getAbsolutePath(), RepoType.singleZip);
		map.put( "release," + data[1].getAbsolutePath(), RepoType.singleZip);
		map.put( "snapshot," + data[2].getAbsolutePath() + "|" + data[3].getAbsolutePath(), RepoType.switchZip);
		launcherShell = new LauncherShell( port);
		launchedRepolets = launcherShell.launch( map);
	}
	
	@AfterClass
	public static void after() {
		runAfter();
		launcherShell.shutdown();
	}
	
	
	
	@Test
	public void testUpdate() {
		try {
			Solution terminal = Solution.T.create();			
			terminal.setGroupId( "com.braintribe.test.dependencies.repositoryRoleTest");
			terminal.setArtifactId( "RepositoryRoleTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			
			// first test: set with only #1.0 versions 
			String[] expectedNames = getResultsForFirstRun();
			
			Collection<Solution> result = test( "testupdate", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);
			
			// tweak environment
			tweakEnvironment();
			
			// second test: set with the #1.1 versions 
			expectedNames = getResultsForSecondRun();
							
			scope.getPersistenceRegistry().clear();
			repositoryRegistry.clear();
			
			result = test( "testupdate", terminal, WalkDenotationTypeExpert.buildCompileWalkDenotationType((ClashStyle.optimistic)), expectedNames,1,0);
			testPresence(result, localRepository);
			testUpdates(result, localRepository);			
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	
	protected void touchUpdateData(Identification identification, final String repoId, Date dateToSetAsLastAccess) {
		File location = new File( localRepository, identification.getGroupId().replace('.',  File.separatorChar) + File.separator + identification.getArtifactId());
		File [] files = location.listFiles( new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.equalsIgnoreCase( "maven-metadata-" + repoId + ".xml"))
					return true;
				return false;
			}
		});

		MavenMetaDataCodec mavenMetaDataCodec = new MavenMetaDataCodec();
		for (File file : files) {
			Document document;
			try {
				document = DomParser.load().from(file);
			} catch (DomParserException e) {
				Assert.fail("cannot read file [" + file.getAbsolutePath() + "]");
				return;
			}
			MavenMetaData metadata;
			try {
				metadata = mavenMetaDataCodec.decode(document);
			} catch (CodecException e) {
				Assert.fail("cannot decode file [" + file.getAbsolutePath() + "]");
				return;
			}
			metadata.getVersioning().setLastUpdated(dateToSetAsLastAccess);
			try {
				document = mavenMetaDataCodec.encode(metadata);
			} catch (CodecException e) {
				Assert.fail("cannot encode file [" + file.getAbsolutePath() + "]");
				return;
			}
			try {
				DomParser.write().from(document).to( file);
			} catch (DomParserException e) {
				Assert.fail("cannot write file [" + file.getAbsolutePath() + "]");
				return;
			}
		}
		
	}
}
