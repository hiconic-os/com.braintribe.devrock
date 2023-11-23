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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.js;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.js.JsLibraryLinker;
import com.braintribe.devrock.mc.api.js.JsLibraryLinkingContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.codebase.CodebaseRepositoryModule;
import com.braintribe.devrock.mc.core.wirings.js.JsResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.js.contract.JsResolverContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * tests the JsResolver :
 * - support for local artifacts/projects (using parent of working folder as base) or only remote artifacts
 * - support to switch between the 'pretty' (unpacked standard zip) or 'min' (unpacked min:zip)
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class JsResolverTest_COREDR19 implements LauncherTrait, HasCommonFilesystemNode {
	private static boolean skipTests = false;
	private static String msg = "JsResolverTest_COREDR19 : skipped test for now as they might block JUnit suite processing";
	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/jsresolving/COREDR-19");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	
	private File initial = new File( input, "initial");

	private File initialWorking = new File( initial, "working");
	private File initialJsRepository = new File( initial, "js-repository");
	
	private File working = new File( output, "working");	
	private File jsRepository = new File( repo, "js-repository");
	private File settings = new File( input, "settings");
	
	private File settingsFile = new File( settings, "basic-settings.xml");
	
	private File repoletInput = new File( input, "input");
	private YamlMarshaller marshaller = new YamlMarshaller();
	
	private Launcher launcher = Launcher.build()
			.repolet()
				.name("archive")				
				.filesystem()
					.filesystem( repoletInput)
				.close()
			.close()			
		.done();	
	
	@Before 
	public void before() {		
		TestUtils.ensure(working);
		TestUtils.ensure(jsRepository);
		TestUtils.ensure( repo);
		
		TestUtils.copy(initialWorking, working);
		if (initialJsRepository.exists()) {
			TestUtils.copy(initialJsRepository, jsRepository);
		}
		runBefore(launcher);
	}
	
	@After
	public void after() {
		runAfter( launcher);
	}
	
	// local mode : maps artifact name to "min exists" and "pretty exists"
	Map<String, Pair<Boolean,Boolean>> jsRepositoryMinExpectations;	
	{
		jsRepositoryMinExpectations = new HashMap<>();
		jsRepositoryMinExpectations.put( "test.js.js-1.0", Pair.of( true, false));
		jsRepositoryMinExpectations.put( "test.js.jsa-1.0", Pair.of( true, false));
		jsRepositoryMinExpectations.put( "test.js.jsb-1.0", Pair.of( true, false));
		jsRepositoryMinExpectations.put( "test.js.jsc-1.0", Pair.of( true, false));		
	}
	
	// local mode : maps artifact name to "min exists" and "pretty exists"
		Map<String, Pair<Boolean,Boolean>> jsRepositoryPrettyExpectations;	
		{
			jsRepositoryPrettyExpectations = new HashMap<>();
			jsRepositoryPrettyExpectations.put( "test.js.js-1.0", Pair.of( false, true));
			jsRepositoryPrettyExpectations.put( "test.js.jsa-1.0", Pair.of( false, true));
			jsRepositoryPrettyExpectations.put( "test.js.jsb-1.0", Pair.of( false, true));
			jsRepositoryPrettyExpectations.put( "test.js.jsc-1.0", Pair.of( false, true));		
		}
	
	// remote mode : maps artifact name to "min exists" and "pretty exists"
	Map<String, Pair<Boolean,Boolean>> remoteJsRepositoryMinExpectations;	
		{
			remoteJsRepositoryMinExpectations = new HashMap<>();
			remoteJsRepositoryMinExpectations.put( "test.js.js-1.0", Pair.of( true, false));
			remoteJsRepositoryMinExpectations.put( "test.js.jsa-1.0", Pair.of( true, false));
			remoteJsRepositoryMinExpectations.put( "test.js.jsb-1.0", Pair.of( true, false));
			remoteJsRepositoryMinExpectations.put( "test.js.jsc-1.0", Pair.of( true, false));
			remoteJsRepositoryMinExpectations.put( "test.js.projectA-1.0.1", Pair.of( true, false));
			remoteJsRepositoryMinExpectations.put( "test.js.projectB-1.0.1", Pair.of( true, false));
		}

	// local: terminal's lib entries set to use 'min' 
	Map<String,Pair<String,String>> localMinLibEntryExpectations;
	{
		localMinLibEntryExpectations = new HashMap<>();
		localMinLibEntryExpectations.put( "test.js.js-1.0", Pair.of( "test.js.js-1.0", "min"));
		localMinLibEntryExpectations.put( "test.js.jsa-1.0", Pair.of( "test.js.jsa-1.0", "min"));
		localMinLibEntryExpectations.put( "test.js.jsb-1.0", Pair.of( "test.js.jsb-1.0", "min"));
		localMinLibEntryExpectations.put( "test.js.jsc-1.0", Pair.of( "test.js.jsc-1.0", "min"));
		localMinLibEntryExpectations.put( "test.js.projectA-1.0~", Pair.of( "projectA", "src"));
		localMinLibEntryExpectations.put( "test.js.projectB-1.0~", Pair.of( "projectB", "src"));
		localMinLibEntryExpectations.put( "_src", Pair.of( "terminal", "src"));		
	}
	// local : terminal's lib entries set to use 'pretty'
	Map<String,Pair<String,String>> localPrettyLibEntryExpectations;
	{
		localPrettyLibEntryExpectations = new HashMap<>();
		localPrettyLibEntryExpectations.put( "test.js.js-1.0", Pair.of( "test.js.js-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.jsa-1.0", Pair.of( "test.js.jsa-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.jsb-1.0", Pair.of( "test.js.jsb-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.jsc-1.0", Pair.of( "test.js.jsc-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.projectA-1.0~", Pair.of( "projectA", "src"));
		localPrettyLibEntryExpectations.put( "test.js.projectB-1.0~", Pair.of( "projectB", "src"));
		localPrettyLibEntryExpectations.put( "_src", Pair.of( "terminal", "src"));		
	}
	// remote : terminal's lib entries set to use min 	
	Map<String,Pair<String,String>> remoteMinLibEntryExpectations;
	{
		remoteMinLibEntryExpectations = new HashMap<>();
		remoteMinLibEntryExpectations.put( "test.js.js-1.0", Pair.of( "test.js.js-1.0", "min"));
		remoteMinLibEntryExpectations.put( "test.js.jsa-1.0", Pair.of( "test.js.jsa-1.0", "min"));
		remoteMinLibEntryExpectations.put( "test.js.jsb-1.0", Pair.of( "test.js.jsb-1.0", "min"));
		remoteMinLibEntryExpectations.put( "test.js.jsc-1.0", Pair.of( "test.js.jsc-1.0", "min"));
		remoteMinLibEntryExpectations.put( "test.js.projectA-1.0~", Pair.of( "test.js.projectA-1.0.1", "min"));
		remoteMinLibEntryExpectations.put( "test.js.projectB-1.0~", Pair.of( "test.js.projectB-1.0.1", "min"));
		remoteMinLibEntryExpectations.put( "_src", Pair.of( "terminal", "src"));		
	}
	
	// remote : terminal's lib entries set to use pretty 	
		Map<String,Pair<String,String>> remotePrettyLibEntryExpectations;
		{
			remotePrettyLibEntryExpectations = new HashMap<>();
			remotePrettyLibEntryExpectations.put( "test.js.js-1.0", Pair.of( "test.js.js-1.0", "min"));
			remotePrettyLibEntryExpectations.put( "test.js.jsa-1.0", Pair.of( "test.js.jsa-1.0", "min"));
			remotePrettyLibEntryExpectations.put( "test.js.jsb-1.0", Pair.of( "test.js.jsb-1.0", "min"));
			remotePrettyLibEntryExpectations.put( "test.js.jsc-1.0", Pair.of( "test.js.jsc-1.0", "min"));
			remotePrettyLibEntryExpectations.put( "test.js.projectA-1.0~", Pair.of( "test.js.projectA-1.0.1", "min"));
			remotePrettyLibEntryExpectations.put( "test.js.projectB-1.0~", Pair.of( "test.js.projectB-1.0.1", "min"));
			remotePrettyLibEntryExpectations.put( "_src", Pair.of( "terminal", "src"));		
		}
	

	/**
	 * @param names - a {@link List} of {@link String} to collate to a single string 
	 * @return
	 */
	private String collate( List<String> names) {
		return names.stream().collect(Collectors.joining(","));
	}

	/**
	 * @param jsRepository - the {@link File} pointing to the js-repository folder 
	 * @param expectations - a {@link Map} containing the expectations to verify the js-repository folder against
	 */
	private void validateJsRepositoryFolder( File jsRepository, Map<String, Pair<Boolean,Boolean>> expectations) {
		File [] files = jsRepository.listFiles( new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		Assert.assertTrue("jsrepository [" + jsRepository.getAbsolutePath() + "] is empty. Does it exist?", files != null);
		
		List<String> matching = new ArrayList<>();
		List<String> excess = new ArrayList<>();
		for (File file : files) {
			String name = file.getName();
			Pair<Boolean,Boolean> found = expectations.get(name);
			if (found == null) {
				excess.add( name);
				continue;
			}
			matching.add( name);
			if (found.first) { // min
				File folder = new File( file, "min");
				Assert.assertTrue( "expected folder [" + folder.getAbsolutePath() + "] to exist, but it doesn't", folder.exists());
			}
			if (found.second) { // pretty
				File folder = new File( file, "pretty");
				Assert.assertTrue( "expected folder [" + folder.getAbsolutePath() + "] to exist, but it doesn't", folder.exists());
			}			
		}
		List<String> missing = new ArrayList<>( expectations.keySet());
		missing.removeAll( matching);
		
		Assert.assertTrue("missing [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue("excess [" + collate( excess) + "]", excess.size() == 0);
		
	}
	
	/**
	 * @param working - the {@link File} that points to the working folder
	 * @param jsRepository - the {@link File} that points to the js-repository
	 * @param terminal - the {@link String} that contains the name of the terminal
	 * @param expectations - a {@link Map} containing the expectations to verify the working folder with 
	 */
	private void validateWorkingFolder( File working, File jsRepository, String terminal, Map<String,Pair<String,String>> expectations) {
		File terminalFolder = new File( working, terminal);
		File libFolder = new File( terminalFolder, "lib");
		File [] files = libFolder.listFiles();
		List<String> matching = new ArrayList<>();
		List<String> excess = new ArrayList<>();

		for (File file : files) {
			String name = file.getName();
			Pair<String,String> linkTarget = expectations.get( name); 
			if (linkTarget == null) {
				excess.add( name);
				continue;
			}			
			matching.add( name);			

			try {
				Path link = Files.readSymbolicLink( file.toPath());

				if (linkTarget.second.equalsIgnoreCase("src")) { 
					File found = new File( libFolder, link.toString());
					Path foundX = found.toPath();
					Path foundy = foundX.toRealPath();
					String foundPath = foundy.toString();
					File expected = new File( working,linkTarget.first + "/" + linkTarget.second);
					String expectedPath = expected.toPath().toRealPath().toFile().getAbsolutePath();
					Assert.assertTrue("expected [" + expectedPath + "] but found [" + foundPath +"]",  expectedPath.equals( foundPath));					
				}
				else {
					Path real = link.toRealPath();
					File found = real.toFile();
					String foundPath = found.getAbsolutePath();
					File expected = new File( jsRepository,linkTarget.first + "/" + linkTarget.second);
					String expectedPath = expected.toPath().toRealPath().toFile().getAbsolutePath();
					Assert.assertTrue("expected [" + expectedPath + "] but found [" + foundPath +"]",  expectedPath.equals( foundPath));
				}
				
			} catch (IOException e) {
				//e.printStackTrace();
				Assert.fail( name + ": cannot read symbolic link [" + file.getAbsolutePath() + "] as " + e.getMessage());
			}			
		}
		List<String> missing = new ArrayList<>( expectations.keySet());
		missing.removeAll( matching);
		Assert.assertTrue("missing [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue("excess [" + collate( excess) + "]", excess.size() == 0);
		
		
	}
	
	/**
	 * @param projectDirectory - the directory of the project to process
	 */
	private void test(File workingDirectory, String terminal,boolean useSymbolicLinks, boolean preferMinOverPretty, boolean localProjectSupport, Map<String, Pair<Boolean,Boolean>> jsRepositoryExpectations, Map<String,Pair<String,String>> libEntryExpectations) {
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		ove.setEnv( "ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settingsFile.getAbsolutePath());
		ove.setEnv( "port", "" + launcher.getAssignedPort());
		ove.setEnv("M2_REPO", repo.getAbsolutePath());

		File projectDirectory = new File( workingDirectory, terminal);
		
		AggregatorWireTerminalModule<JsResolverContract> wireTerminalModule = new AggregatorWireTerminalModule<>(JsResolverWireModule.INSTANCE);
		wireTerminalModule.addModule(MavenConfigurationWireModule.INSTANCE);
		
		if (localProjectSupport) {
			CodebaseRepositoryModule codebaseModule = new CodebaseRepositoryModule(workingDirectory, "${artifactId}");
			wireTerminalModule.addModule(codebaseModule);
		}
		
		try (WireContext<JsResolverContract> wireContext = Wire.contextBuilder(wireTerminalModule) //
				.bindContract(VirtualEnvironmentContract.class, () -> ove) //
				.build()) {
			
			JsLibraryLinker linker = wireContext.contract().jsLibraryLinker();
			
			JsLibraryLinkingContext linkingContext = JsLibraryLinkingContext.build() //
					.lenient(true) //
					.libraryCacheFolder(jsRepository) //					
					.preferPrettyOverMin(!preferMinOverPretty) //
					.useSymbolikLinks(useSymbolicLinks)
					.linkFolders(Collections.singletonMap( new File( projectDirectory, "src"), "_src"))
					.done();
			
			linker.linkLibraries(linkingContext, projectDirectory);
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
		
		validateJsRepositoryFolder(jsRepository, jsRepositoryExpectations);
		validateWorkingFolder(working, jsRepository, terminal, libEntryExpectations);
	}

	/**
	 * tests 'local mode' with preferences set to 'min' 
	 */
	@Test
	public void testLocal() {
		if (skipTests) {
			System.out.println( msg);
			return;
		}
		test( working, "terminal", true, true, true, jsRepositoryMinExpectations, localMinLibEntryExpectations);		
	}
	
	/**
	 * tests 'local mode' with preferences set to 'pretty'
	 */
	@Test
	public void testPrettyLocal() {
		if (skipTests) {
			System.out.println( msg);
			return;
		}
		test( working, "terminal", true, false, true, jsRepositoryPrettyExpectations, localPrettyLibEntryExpectations);		
	}
	/**
	 * tests 'remote mode' with preferences set to 'min'
	 */
	@Test
	public void testRepoOnly() {
		if (skipTests) {
			System.out.println( msg);
			return;
		}
		test( working, "terminal", true, true, false, remoteJsRepositoryMinExpectations, remoteMinLibEntryExpectations);		
	}
	
	Map<Pair<String, String>, Pair<String, Boolean>> touchedExpectations = new HashMap<>();
	{
		touchedExpectations.put( Pair.of("test.js:jsc#1.0", "test.js.jsc-1.0"), Pair.of( "jsc-1.0.js.zip", true));
		touchedExpectations.put( Pair.of("test.js:jsa#1.0", "test.js.jsa-1.0"), Pair.of( "jsa-1.0.js.zip", false));
	}
		
	/**
	 * tests 'remote mode' with preferences set to 'min'
	 */
	// TODO : perhaps eventually find out the logic of this .touched file.. it's not there anymore, so this test is useless
	//@Test
	public void testRepoInstalled() {
		if (skipTests) {
			System.out.println( msg);
			return;
		}
		// initial run 
		test( working, "terminal", true, true, false, remoteJsRepositoryMinExpectations, remoteMinLibEntryExpectations);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// store date from artifact
		Map<String, Date> touchDates = new HashMap<>();
		for (Map.Entry<Pair<String,String>,Pair<String, Boolean>> entry : touchedExpectations.entrySet()) {
			
			Pair<String, String> artifactIdentificationPair = entry.getKey();
			File folder = new File( jsRepository, artifactIdentificationPair.second + "/pretty");		
			Pair<String, Boolean> touchedDataPair = entry.getValue();
			
			String touchFileName = touchedDataPair.first.endsWith( "min.js.zip") ? "min" : "pretty";
			File file = new File( folder.getParentFile(), touchFileName + ".touched");
			
			try (InputStream in = new FileInputStream( file)) {
				Date date = (Date) marshaller.unmarshall(in);
				touchDates.put(touchedDataPair.first, date);
				if (touchedDataPair.second) {
					// copy file 
					VersionedArtifactIdentification s = VersionedArtifactIdentification.parse( artifactIdentificationPair.first);					
					File sourceFile = UniversalPath.from(input).push( s.getGroupId(), ".").push( s.getArtifactId()).push(s.getVersion()).push( touchedDataPair.first).toFile();
					File targetFile = UniversalPath.from(repo).push( s.getGroupId(), ".").push( s.getArtifactId()).push(s.getVersion()).push( touchedDataPair.first).toFile();
					targetFile.delete();
					Files.copy( sourceFile.toPath(), targetFile.toPath());
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				Assert.fail("cannot read / touch [" +  file.getAbsolutePath() + "]");
			}
		}				
		// second run 
		test( working, "terminal", true, true, false, remoteJsRepositoryMinExpectations, remoteMinLibEntryExpectations);
		
		// compare date from artifact
		for (Map.Entry<Pair<String,String>,Pair<String, Boolean>> entry : touchedExpectations.entrySet()) {
			
			Pair<String, String> artifactIdentificationPair = entry.getKey();
			File folder = new File( jsRepository, artifactIdentificationPair.second + "/pretty");		
			Pair<String, Boolean> touchedDataPair = entry.getValue();
			String touchFileName = touchedDataPair.first.endsWith( "min.zip") ? "min" : "pretty";
			File file = new File( folder.getParentFile(), touchFileName + ".touched");
			try (InputStream in = new FileInputStream( file)) {
				Date date = (Date) marshaller.unmarshall(in);
				Date storedDate = touchDates.get( touchedDataPair.first);
				
				if (touchedDataPair.second) {
					Assert.assertTrue("expected [" + file.getAbsolutePath() + "] to have changed", date.compareTo(storedDate) != 0);												
				}
				else {
					Assert.assertTrue("expected [" + file.getAbsolutePath() + "] not to have changed", date.compareTo(storedDate) == 0);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				Assert.fail("cannot read / touch [" +  file.getAbsolutePath() + "]");
			}
		}				
		
	}
}
