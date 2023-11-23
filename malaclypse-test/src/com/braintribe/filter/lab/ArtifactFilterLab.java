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
package com.braintribe.filter.lab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.exception.Exceptions;
import com.braintribe.filter.test.wire.ArtifactFilteringTestModule;
import com.braintribe.filter.test.wire.FilteringTestConfigurationSpace;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.IOTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * test bed for COREDR-136
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class ArtifactFilterLab implements LauncherTrait {

	
	private File contents = new File( "res/filter3");
	private File input = new File( contents, "input");
	private File output = new File( contents, "output");

	private File initial = new File( input, "initial");
		
	private File repository = new File( output, "repo");
		
	private File settings = new File( input, "settings");
	
	
	
	private Launcher launcher;	
	{
		launcher = Launcher.build()
						.repolet()
							.name( "archive")
							.descriptiveContent()
								.descriptiveContent( archiveInput( "archive"))
							.close()
						.close()
					.done();
	}
	
	protected RepoletContent archiveInput(String key) {		
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input, key + ".definition.yaml"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");			
		}
		return null;
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
	
	List<Solution> defaultExpectations = new ArrayList<>();
	{
		defaultExpectations.add( NameParser.parseCondensedSolutionName("com.braintribe.devrock.test:t#1.0.1"));
		defaultExpectations.add( NameParser.parseCondensedSolutionName("com.braintribe.devrock.test:a#1.0.2"));
		defaultExpectations.add( NameParser.parseCondensedSolutionName("com.braintribe.devrock.test:b#1.0.2"));
		defaultExpectations.add( NameParser.parseCondensedSolutionName("com.braintribe.devrock.test:local#1.0.1-pc"));
	}
	
	List<Solution> filteredExpectations = new ArrayList<>();
	{
		filteredExpectations.add( NameParser.parseCondensedSolutionName("com.braintribe.devrock.test:t#1.0.1"));
		filteredExpectations.add( NameParser.parseCondensedSolutionName("com.braintribe.devrock.test:a#1.0.1"));
		filteredExpectations.add( NameParser.parseCondensedSolutionName("com.braintribe.devrock.test:b#1.0.1"));
		filteredExpectations.add( NameParser.parseCondensedSolutionName("com.braintribe.devrock.test:local#1.0.1-pc"));
	}
	
	
	private void run( File settings, File filter, Collection<Solution> expectations) {
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		String portAsString = Integer.toString(launcher.getAssignedPort());
		ove.addEnvironmentOverride( "port", portAsString);
		ove.addEnvironmentOverride("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		if (filter != null) {
			// need to patch the yaml..
			File nFilter = new File( output, "filter.yaml");
			try {
				String contents = IOTools.slurp(filter, "UTF-8");
				String replace = contents.replace("${env.port}", portAsString);
				IOTools.spit( nFilter, replace, "UTF-8", false);
				ove.addEnvironmentOverride("DEVROCK_REPOSITORY_CONFIGURATION", nFilter.getAbsolutePath());
			} catch (IOException e) {
				throw new IllegalStateException("cannot prime filter");
			}			
		}
		
		
		
		FilteringTestConfigurationSpace testCfgSpace = new FilteringTestConfigurationSpace();
		testCfgSpace.setOverridableVirtualEnvironment(ove);		
		testCfgSpace.setLocalRepository( repository);
		
		ArtifactFilteringTestModule module = new ArtifactFilteringTestModule( testCfgSpace);		
		try (
				WireContext<BuildDependencyResolutionContract> wireContext = Wire.context( module)
			){
			
			Dependency dependency = NameParser.parseCondensedDependencyName("com.braintribe.devrock.test:t#1.0.1");			
			
			// execute build walk on terminal			
			Set<Solution> resolvedSolutions = wireContext.contract().buildDependencyResolver().resolve( dependency);
			if (expectations != null)
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
	public void defaultTest() {
		File settingsToUse = new File( settings, "settings.xml");
		run( settingsToUse, null, defaultExpectations);		
	}
	
	@Test
	public void filterTest() {
		File settingsToUse = new File( settings, "settings.xml");
		File filterToUse = new File( input, "repository-configuration.yaml");
		run( settingsToUse, filterToUse, filteredExpectations);		
	}
	
	// BEURK : just to finde the bug in the YAML file
	public void loadYamlTest() {
		YamlMarshaller marshaller = new YamlMarshaller();
		try (InputStream in = new FileInputStream( new File(input, "repository-configuration.yaml"))){
			marshaller.unmarshall(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
