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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.testing.category.KnownIssue;
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
public class JsResolverTest_COREDR26 implements LauncherTrait, HasCommonFilesystemNode{
	private static boolean skipTests = false;
	private static String msg = "JsResolverTest_COREDR26 : skipped test for now as they might block JUnit suite processing";
	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/jsresolving/COREDR-26");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	private File initial = new File( input, "initial");

	private File initialJsRepository = new File( initial, "js-repository");
	private File initialJsProjects = new File( initial, "projects");
	
	private File target = new File( output, "target");
	private File projects = new File( output, "projects");
	private File jsRepository = new File( repo, "js-repository");
	private File settings = new File( input, "settings");
	
	private File settingsFile = new File( settings, "basic-settings.xml");
	private String terminal = "test.js:terminal#1.0";
	
	private File repoletInput = new File( input, "input");
	
	private static final boolean PREFER_MIN_OVER_PRETTY = true;
	private static final boolean USE_SYMBOLIC_LINKS = true;
	private static final boolean SUPPORT_LOCALS = true;
	
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
		TestUtils.ensure(target);
		TestUtils.ensure(jsRepository);
		TestUtils.ensure( repo);
		TestUtils.ensure( projects);
	
		if (initialJsRepository.exists()) {
			TestUtils.copy(initialJsRepository, jsRepository);
		}
	
		if (initialJsProjects.exists()) {
			TestUtils.copy( initialJsProjects, projects);
		}
		runBefore(launcher);
	}
	
	@After
	public void after() {
		runAfter( launcher);
	}
	
	// local mode : maps artifact name to "min exists" and "pretty exists"
	Map<String, Pair<Boolean,Boolean>> jsRepositoryExpectations;	
	{
		jsRepositoryExpectations = new HashMap<>();
		jsRepositoryExpectations.put( "test.js.js-1.0", Pair.of( true, false));
		jsRepositoryExpectations.put( "test.js.jsa-1.0", Pair.of( true, true));
		jsRepositoryExpectations.put( "test.js.jsb-1.0", Pair.of( true, false));
		jsRepositoryExpectations.put( "test.js.jsc-1.0", Pair.of( true, false));		
	}
	// remote mode : maps artifact name to "min exists" and "pretty exists"
	Map<String, Pair<Boolean,Boolean>> remoteJsRepositoryExpectations;	
		{
			remoteJsRepositoryExpectations = new HashMap<>();
			remoteJsRepositoryExpectations.put( "test.js.js-1.0", Pair.of( true, false));
			remoteJsRepositoryExpectations.put( "test.js.jsa-1.0", Pair.of( true, true));
			remoteJsRepositoryExpectations.put( "test.js.jsb-1.0", Pair.of( true, false));
			remoteJsRepositoryExpectations.put( "test.js.jsc-1.0", Pair.of( true, false));
			remoteJsRepositoryExpectations.put( "test.js.projectA-1.0.1", Pair.of( true, false));
			remoteJsRepositoryExpectations.put( "test.js.projectB-1.0.1", Pair.of( true, false));
		}

	// local: terminal's lib entries set to use 'min' 
	Map<String,Pair<String,String>> localLibEntryExpectations;
	{
		localLibEntryExpectations = new HashMap<>();
		localLibEntryExpectations.put( "test.js.js-1.0", Pair.of( "test.js.js-1.0", "min"));
		localLibEntryExpectations.put( "test.js.jsa-1.0", Pair.of( "test.js.jsa-1.0", "min"));
		localLibEntryExpectations.put( "test.js.jsb-1.0", Pair.of( "test.js.jsb-1.0", "min"));
		localLibEntryExpectations.put( "test.js.jsc-1.0", Pair.of( "test.js.jsc-1.0", "min"));
		localLibEntryExpectations.put( "test.js.projectA-1.0~", Pair.of( "projectA", "src"));	
		localLibEntryExpectations.put( "test.js.projectB-1.0~", Pair.of( "projectB", "src"));
	}
	// local : terminal's lib entries set to use 'pretty'
	Map<String,Pair<String,String>> localPrettyLibEntryExpectations;
	{
		localPrettyLibEntryExpectations = new HashMap<>();
		localPrettyLibEntryExpectations.put( "test.js.js-1.0", Pair.of( "test.js.js-1.0", "min"));
		localPrettyLibEntryExpectations.put( "test.js.jsa-1.0", Pair.of( "test.js.jsa-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.jsb-1.0", Pair.of( "test.js.jsb-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.jsc-1.0", Pair.of( "test.js.jsc-1.0", "pretty"));
		localPrettyLibEntryExpectations.put( "test.js.projectA-1.0~", Pair.of( "projectA", "src"));
		localPrettyLibEntryExpectations.put( "test.js.projectB-1.0~", Pair.of( "projectB", "src"));
	}
	// remote : terminal's lib entries set to use min 	
	Map<String,Pair<String,String>> remoteLibEntryExpectations;
	{
		remoteLibEntryExpectations = new HashMap<>();
		remoteLibEntryExpectations.put( "test.js.js-1.0", Pair.of( "test.js.js-1.0", "min"));
		remoteLibEntryExpectations.put( "test.js.jsa-1.0", Pair.of( "test.js.jsa-1.0", "min"));
		remoteLibEntryExpectations.put( "test.js.jsb-1.0", Pair.of( "test.js.jsb-1.0", "min"));
		remoteLibEntryExpectations.put( "test.js.jsc-1.0", Pair.of( "test.js.jsc-1.0", "min"));
		remoteLibEntryExpectations.put( "test.js.projectA-1.0~", Pair.of( "test.js.projectA-1.0.1", "min"));
		remoteLibEntryExpectations.put( "test.js.projectB-1.0~", Pair.of( "test.js.projectB-1.0.1", "min"));
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
				File minFolder = new File( file, "min");
				Assert.assertTrue( "expected folder [" + minFolder.getAbsolutePath() + "], but it doesn't", minFolder.exists());
			}
			if (found.second) { // pretty
				File minFolder = new File( file, "pretty");
				Assert.assertTrue( "expected folder [" + minFolder.getAbsolutePath() + "], but it doesn't", minFolder.exists());
			}			
		}
		List<String> missing = new ArrayList<>( expectations.keySet());
		missing.removeAll( matching);
		
		Assert.assertTrue("missing in js-repository [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue("excess in js-repository [" + collate( excess) + "]", excess.size() == 0);
		
	}
	
	/**
	 * @param working - the {@link File} that points to the working folder
	 * @param jsRepository - the {@link File} that points to the js-repository
	 * @param TERMINAL - the {@link String} that contains the name of the terminal
	 * @param expectations - a {@link Map} containing the expectations to verify the working folder with 
	 */
	private void validateWorkingFolder( File target, File project, File jsRepository, Map<String,Pair<String,String>> expectations, boolean symbolicLinks) {
				
		File [] files = target.listFiles();
		List<String> matching = new ArrayList<>();
		List<String> excess = new ArrayList<>();

		for (File file : files) {
			String name = file.getName();
			
			// handle self link here : always a symbolic link
			if (name.equals( "_src")) {			
				try {
					Path link = Files.readSymbolicLink( file.toPath());
					File found = new File( target, link.toString());
					System.out.println("found _src pointing to [" + found + "]");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
			Pair<String,String> linkTarget = expectations.get( name); 
			if (linkTarget == null) {
				excess.add( name);
				continue;
			}			
			matching.add( name);			

			if (symbolicLinks) {
				try {
					Path link = Files.readSymbolicLink( file.toPath());
	
					if (linkTarget.second.equalsIgnoreCase("src")) { 
						File found = new File( target, link.toString());
						Path foundX = found.toPath();
						Path foundy = foundX.toRealPath();
						String foundPath = foundy.toString();
						File expected = new File( project,linkTarget.first + "/" + linkTarget.second);
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
			else {
				String jsProject = linkTarget.first;				
				String jsProjectContent = linkTarget.second;
				File origin = new File( jsRepository, jsProject + "/" + jsProjectContent);
				if (!origin.exists()) {
					origin = new File( project, jsProject + "/" + jsProjectContent);
				}
				Assert.assertTrue("target of [" + file.getAbsolutePath() + "] doesn't exist", origin.exists());
				
				Map<String,String> originMap = extractContent( origin);
				Map<String,String> copiedMap = extractContent( file);								
				
				Assert.assertTrue("origin has another size as copy", originMap.size() == copiedMap.size());
				
				for (Map.Entry<String, String> entry : originMap.entrySet()) {
					String originName = entry.getKey();
					String originHash = entry.getValue();
					String copiedHash = copiedMap.get( originName);
					
					Assert.assertTrue("[" + originName + "] not copied", copiedHash != null);
					Assert.assertTrue("copy of [" + originName + "] differs", originHash.equals(copiedHash));
				}				
			}
		}
		List<String> missing = new ArrayList<>( expectations.keySet());
		missing.removeAll( matching);
		Assert.assertTrue("missing in target folder [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue("excess in target folder [" + collate( excess) + "]", excess.size() == 0);
		
		
	}
	
		
	private Map<String, String> extractContent(File directory) {
		Map<String,String> result = new HashMap<>();
		File [] files = directory.listFiles();
		
		for (File file : files) {
			result.put( file.getName(), TestUtils.generateHash(file, "md5"));
		}		
		return result;
	}
	
	/**
	 * @param projectDirectory - the directory of the project to process
	 */
	private void test(Collection<String> terminals, File targetDirectory, File projectsDirectory, File settings, File m2Repository, boolean useSymbolicLinks, boolean preferMinOverPretty, boolean localProjectSupport, Map<String, Pair<Boolean,Boolean>> jsRepositoryExpectations, Map<String,Pair<String,String>> libEntryExpectations) {
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		ove.setEnv( "ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settingsFile.getAbsolutePath());
		ove.setEnv( "port", "" + launcher.getAssignedPort());
		ove.setEnv("M2_REPO", repo.getAbsolutePath());

		AggregatorWireTerminalModule<JsResolverContract> wireTerminalModule = new AggregatorWireTerminalModule<>(JsResolverWireModule.INSTANCE);
		wireTerminalModule.addModule(MavenConfigurationWireModule.INSTANCE);
		
		if (localProjectSupport) {
			CodebaseRepositoryModule codebaseModule = new CodebaseRepositoryModule(projectsDirectory, "${artifactId}");
			wireTerminalModule.addModule(codebaseModule);
		}
		
		try (WireContext<JsResolverContract> wireContext = Wire.contextBuilder(wireTerminalModule) //
				.bindContract(VirtualEnvironmentContract.class, () -> ove) //
				.build()) {
			
			JsLibraryLinker linker = wireContext.contract().jsLibraryLinker();
			
			JsLibraryLinkingContext linkingContext = JsLibraryLinkingContext.build().useSymbolikLinks(useSymbolicLinks).libraryCacheFolder(jsRepository).preferPrettyOverMin(!preferMinOverPretty).done();
			
			List<CompiledTerminal> compiledTerminals = terminals.stream().map(CompiledTerminal::parse).collect(Collectors.toList());
			
			linker.linkLibraries(linkingContext, compiledTerminals, targetDirectory);
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		}

		validateJsRepositoryFolder(jsRepository, jsRepositoryExpectations);
		validateWorkingFolder( targetDirectory, projectsDirectory, jsRepository, libEntryExpectations, useSymbolicLinks);
	}

	/**
	 *  test: symbolic links on, minOverPretty on, local project support off
	 */
	@Test
	public void testRemoteLink() {
		if (skipTests) {
			System.out.println( msg);
			return;
		}
		test( Collections.singletonList(terminal), target, projects, settingsFile, repo, USE_SYMBOLIC_LINKS, PREFER_MIN_OVER_PRETTY, !SUPPORT_LOCALS, remoteJsRepositoryExpectations, remoteLibEntryExpectations);		
	}
	
	/**
	 *  test: symbolic links off, minOverPretty on, local project support on
	 */
	@Test
	public void testLocalCopy() {
		if (skipTests) {
			System.out.println( msg);
			return;
		}
		test( Collections.singletonList(terminal), target, projects, settingsFile, repo, !USE_SYMBOLIC_LINKS, PREFER_MIN_OVER_PRETTY, SUPPORT_LOCALS, jsRepositoryExpectations, localLibEntryExpectations);		
	}
	
	/**
	 * test: symbolic links on, minOverPretty on, local project support on
	 */
	@Test
	public void testLocalLink() {
		if (skipTests) {
			System.out.println( msg);
			return;
		}
		test( Collections.singletonList(terminal), target, projects, settingsFile, repo, USE_SYMBOLIC_LINKS, PREFER_MIN_OVER_PRETTY, SUPPORT_LOCALS, jsRepositoryExpectations, localLibEntryExpectations);		
	}
	
	/**
	 * test: symbolic links off, minOverPretty on, local project support off  
	 */
	@Test
	public void testRemoteCopy() {
		if (skipTests) {
			System.out.println( msg);
			return;
		}
		test( Collections.singletonList(terminal), target, projects, settingsFile, repo, !USE_SYMBOLIC_LINKS, PREFER_MIN_OVER_PRETTY, !SUPPORT_LOCALS, remoteJsRepositoryExpectations, remoteLibEntryExpectations);		
	}
	
}
