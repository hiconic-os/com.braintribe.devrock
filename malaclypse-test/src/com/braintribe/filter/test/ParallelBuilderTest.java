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
package com.braintribe.filter.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.exception.Exceptions;
import com.braintribe.filter.test.wire.ArtifactFilteringTestModule;
import com.braintribe.filter.test.wire.FilteringTestConfigurationSpace;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class ParallelBuilderTest implements LauncherTrait {

	private String walkScopeId = UUID.randomUUID().toString();
	private File contents = new File( "res/filter2");
	private File input = new File( contents, "input");
	private File output = new File( contents, "output");

	private File initial = new File( input, "initial");
		
	private File repository = new File( output, "repo");
	
	private File repoletAContents = new File( input, "archive");
	
	private File settings = new File( input, "settings");
	
	private File terminalPom = new File( repository, "com/braintribe/devrock/test/terminal/1.0.1/terminal-1.0.1.pom");
	
	private Launcher launcher;	
	{
		launcher = Launcher.build()
						.repolet()
							.name( "archive")
							.filesystem()
								.filesystem( repoletAContents)
							.close()
						.close()						
					.done();
	}
	
	private Collection<Solution> expectedAllMatchingSolutions;
	{
		expectedAllMatchingSolutions = new ArrayList<>();
		expectedAllMatchingSolutions.add( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test:a#1.0"));
		expectedAllMatchingSolutions.add( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test:b#1.0"));
		expectedAllMatchingSolutions.add( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test:c#1.0"));
		expectedAllMatchingSolutions.add( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test:import#1.0"));
		expectedAllMatchingSolutions.add( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test:parent#1.0"));
	}
	
	private Collection<Solution> expectedNonLocalMatchingSolutions;
	{
		expectedNonLocalMatchingSolutions = new ArrayList<>();
		expectedNonLocalMatchingSolutions.add( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test:a#1.0.1"));
		expectedNonLocalMatchingSolutions.add( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test:b#1.0.1"));
		expectedNonLocalMatchingSolutions.add( NameParser.parseCondensedSolutionName( "com.braintribe.devrock.test:c#1.0.1"));
	}
	
	@Before
	public void runBefore() {
		TestUtil.ensure(output); 		
		TestUtil.copy(initial, repository);
		
		launcher.launch();
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	
	private void run( File settings, File filter, File pomFile, Collection<Solution> expectations) {
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		ove.addEnvironmentOverride( "port", Integer.toString(launcher.getAssignedPort()));
		ove.addEnvironmentOverride("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		if (filter != null) {
			ove.addEnvironmentOverride("DEVROCK_REPOSITORY_CONFIGURATION", filter.getAbsolutePath());
		}
		
		FilteringTestConfigurationSpace testCfgSpace = new FilteringTestConfigurationSpace();
		testCfgSpace.setOverridableVirtualEnvironment(ove);		
		testCfgSpace.setLocalRepository( repository);
		
		ArtifactFilteringTestModule module = new ArtifactFilteringTestModule( testCfgSpace);		
		try (
				WireContext<BuildDependencyResolutionContract> wireContext = Wire.context( module)
			){					
			Solution terminalSolution = wireContext.contract().pomReader().readPom( walkScopeId, pomFile);
			
			// execute build walk on terminal
			List<Dependency> dependencies = terminalSolution.getDependencies();
			Set<Solution> resolvedSolutions = wireContext.contract().buildDependencyResolver().resolve( dependencies);
			validate( resolvedSolutions, expectations);												
		}		
		 catch (Exception e) {
				throw Exceptions.unchecked(e, "cannot execute test", IllegalStateException::new);
		}
		
	
	}

	private void validate(Set<Solution> resolvedSolutions, Collection<Solution> expectations) {		
		List<String> resolvedSolutionNames = resolvedSolutions.stream().map( s -> NameParser.buildName(s)).collect( Collectors.toList());
		List<String> expectedSolutionNames = expectations.stream().map( s -> NameParser.buildName(s)).collect( Collectors.toList());
		
		List<String> matched = new ArrayList<String>();
		List<String> missing = new ArrayList<String>();
		for (String expected : expectedSolutionNames) {
			if (resolvedSolutionNames.contains( expected)) {
				matched.add( expected);
			}
			else {
				missing.add( expected);
			}
		}
		List<String> excess = new ArrayList<>( resolvedSolutionNames);
		excess.removeAll( matched);
		
		Assert.assertTrue("missing [" + missing.stream().collect( Collectors.joining(",")) + "]", missing.size() == 0);
		Assert.assertTrue("unexpected [" + excess.stream().collect( Collectors.joining(",")) + "]", excess.size() == 0);
	}
	
	@Test
	public void allMatchingTest() {
		File settingsToUse = new File( settings, "settings.xml");
		run( settingsToUse, null, terminalPom, expectedAllMatchingSolutions);		
	}
	

	
}
