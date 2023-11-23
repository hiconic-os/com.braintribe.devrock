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
package com.braintribe.artifacts.test.walk.downloads;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.ClasspathResolvers;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ConfigurableClasspathResolverExternalSpace;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.Scopes;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.filter.test.wire.ArtifactFilteringTestModule;
import com.braintribe.filter.test.wire.FilteringTestConfigurationSpace;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.util.Lists;

@Category(KnownIssue.class)
public class SecondLevelMetadataDownloadTest implements LauncherTrait {
	protected static final String GRP_PATH = "com/braintribe/devrock/test";
	
	protected File content = new File("res/secondLevelMetadataDownloadTest");
	protected File output = new File( content, "output");
	protected File repo = new File( output, "repo");
	
	protected File input = new File( content, "input");
	protected File initial = new File( input, "initial");
	protected File settings = new File( input, "settings.xml");

	
	private File contentFile = new File( input, "content.definition.txt");
	
	private String terminalOne = "com.braintribe.devrock.test:t#1.0.1";
	private String terminalTwo = "com.braintribe.devrock.test:t#1.0.2";
	
	private RepoletContent repoContent;
	{
		try {
			repoContent = RepositoryGenerations.parseConfigurationFile(contentFile);
		} catch (Exception e) {
			throw new IllegalStateException("can't read [" + contentFile.getAbsolutePath() + "]");		
		}
	}
	
	private Launcher launcher; 
	{
		launcher = Launcher.build()
				.repolet()
				.name("archive")
					.descriptiveContent()
						.descriptiveContent( repoContent)
					.close()
				.close()
			.done();
	}

	
	private List<Pair<File,Boolean>> mavenMetaDataFilesToExistForA = new ArrayList<>();
	{
		// first level 
		mavenMetaDataFilesToExistForA.add( Pair.of( new File( repo, GRP_PATH + "/a/maven-metadata-archive.xml"), true));
		mavenMetaDataFilesToExistForA.add( Pair.of( new File( repo, GRP_PATH + "/b/maven-metadata-archive.xml"), true));
		mavenMetaDataFilesToExistForA.add( Pair.of( new File( repo, GRP_PATH + "/c/maven-metadata-archive.xml"), true));
		
		// second level
		mavenMetaDataFilesToExistForA.add( Pair.of( new File( repo, GRP_PATH + "/a/1.0.1/maven-metadata-archive.xml"), true));
		mavenMetaDataFilesToExistForA.add( Pair.of( new File( repo, GRP_PATH + "/b/1.0.1/maven-metadata-archive.xml"), true));
		mavenMetaDataFilesToExistForA.add( Pair.of( new File( repo, GRP_PATH + "/c/1.0.1/maven-metadata-archive.xml"), true));
		
						
	} 
	@Before
	public void runBefore() {
		TestUtil.ensure(repo);
		if (initial.exists()) {
			TestUtil.copy(initial, repo);
		}
		launcher.launch();
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	

	
	protected OverrideableVirtualEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvironmentOverrides(overrides);						
		}
		ove.addEnvironmentOverride("repo", repo.getAbsolutePath());
		ove.addEnvironmentOverride("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		ove.addEnvironmentOverride( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;
		
	}
	
	
	protected WireContext<ClasspathResolverContract> getClasspathWalkContext(VirtualEnvironment ove) {
		ConfigurableClasspathResolverExternalSpace cfg = new ConfigurableClasspathResolverExternalSpace();
		
		cfg.setScopes( Scopes.compileScopes());
		cfg.setSkipOptional(true);
		
		cfg.setResolvingInstant( ResolvingInstant.posthoc);
		
		
		cfg.setVirtualEnvironment(ove);
		
		WireContext<ClasspathResolverContract> context = ClasspathResolvers.classpathResolverContext( b -> {  
			b.bindContract(ClasspathResolverExternalContract.class, cfg);	
		});
		
		return context;		
	}

	
	protected WireContext<ClasspathResolverContract> getClasspathWalkContext(Map<String,String> overrides) {
		ConfigurableClasspathResolverExternalSpace cfg = new ConfigurableClasspathResolverExternalSpace();
		
		cfg.setScopes( Scopes.compileScopes());
		cfg.setSkipOptional(false);
		
		cfg.setResolvingInstant( ResolvingInstant.posthoc);
		
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		ove.setEnvironmentOverrides(overrides);
		
		cfg.setVirtualEnvironment(ove);
			
		WireContext<ClasspathResolverContract> context = ClasspathResolvers.classpathResolverContext( b -> {  
			b.bindContract(ClasspathResolverExternalContract.class, cfg);	
		});
		
		return context;		
	}
	
	
	protected Collection<Solution> runClasspathResolutionTest(String terminal) {
		try {
			Map<String,String> overrides = new HashMap<>();
			overrides.put("repo", repo.getAbsolutePath());
			overrides.put("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
			overrides.put( "port", Integer.toString( launcher.getAssignedPort()));
			
			WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( overrides);			
			
			WalkerContext walkerContext = new WalkerContext();
			
			Walker walker = classpathWalkContext.contract().walker( walkerContext);
			
			String walkScopeId = UUID.randomUUID().toString();
			
			Solution terminalSolution = NameParser.parseCondensedSolutionName( terminal);
			
			Collection<Solution> collection = walker.walk( walkScopeId, terminalSolution);
			return collection;
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(terminal + ": exception thrown : " + e.getLocalizedMessage());
		} 
		return null;
	}
	
	/**
	 * tests the parallel build resolver
	 */
	
	protected Set<Solution> runBuildPathResolutionTest(String terminal, boolean execeptionExpected) {
		FilteringTestConfigurationSpace testCfgSpace = new FilteringTestConfigurationSpace();
		testCfgSpace.setOverridableVirtualEnvironment( buildVirtualEnvironement(null));		
		testCfgSpace.setLocalRepository( repo);
		
		boolean exceptionThrown = false;
		Set<Solution> resolvedSolutions =null;
		ArtifactFilteringTestModule module = new ArtifactFilteringTestModule( testCfgSpace);		
		try (
				WireContext<BuildDependencyResolutionContract> wireContext = Wire.context( module)
			){									
			List<Dependency> dependencies = Lists.list( NameParser.parseCondensedDependencyName(terminal));
			resolvedSolutions = wireContext.contract().buildDependencyResolver().resolve( dependencies);
			System.out.println( "found [" + resolvedSolutions.size() + "] solutions");
		}		
		 catch (Exception e) {
			 System.out.println("Exception of type [" + e.getClass().getName() + "] thrown, message : [" + e.getMessage() + "]");
			exceptionThrown = true;
		}
		Assert.assertTrue( "excepted an exception, but nothing was thrown", execeptionExpected && exceptionThrown == true);
		return resolvedSolutions;
	}
	
	@Test
	public void runClasspathTest() {
		Collection<Solution> classpathResolution = runClasspathResolutionTest(terminalOne);
		Assert.assertTrue("unexpected empty resolution result", classpathResolution != null);
		// validate resolution 
		// validate maven meta data
		for (Pair<File, Boolean> pair : mavenMetaDataFilesToExistForA) {
			File file = pair.first;
			boolean mustBePresent = pair.second;
					
			if (mustBePresent) {
				Assert.assertTrue("file [" + file.getAbsolutePath() + "] doesn't exist, but should",  file.exists());
			}
			else {
				Assert.assertTrue("file [" + file.getAbsolutePath() + "] exist, but shouldn't",  !file.exists());
			}			
		}
	}
}
