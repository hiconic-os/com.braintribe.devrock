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
package com.braintribe.test.multi.realRepoWalk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.cache.PartCacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.coding.IdentificationWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.coding.SolutionWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricherFactory;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricherFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.enriching.RelevantPartTupleFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.ResolvingException;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.build.artifact.walk.multi.WalkException;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifact.walk.multi.WalkerFactory;
import com.braintribe.build.artifact.walk.multi.WalkerFactoryImpl;
import com.braintribe.build.artifact.walk.multi.clash.ClashResolverFactoryImpl;
import com.braintribe.build.artifact.walk.multi.exclusion.ExclusionControlFactory;
import com.braintribe.build.artifact.walk.multi.scope.ScopeControlFactory;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.logging.LoggerInitializer;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.ExclusionControlDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.ScopeControlDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDomain;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverByDepthDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverByIndexDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.InitialDependencyClashPrecedence;
import com.braintribe.model.malaclypse.cfg.denotations.clash.OptimisticClashResolverDenotationType;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.MagicScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.ScopeTreatement;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;

// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================
@Category(SpecialEnvironment.class)
public class RealRepoWalkLab {
	

	private static RepositoryReflectionImpl repositoryRegistry;
	private static MavenSettingsReader mavenSettingsReader;	
	private enum ScopeKind {compile, launch};	
	private enum ClashStyle { optimistic, index, depth};
	
	@BeforeClass
	public static void before() {
		LoggerInitializer loggerInitializer = new LoggerInitializer();
		try {					
			loggerInitializer.setLoggerConfigUrl( new File("res/logger.properties").toURI().toURL());		
			loggerInitializer.afterPropertiesSet();
		} catch (Exception e) {		
			e.printStackTrace();
		}
		
		MavenSettingsExpertFactory mavenSettingsfactory = new MavenSettingsExpertFactory();
	
		RavenhurstScopeImpl scope = new RavenhurstScopeImpl();
		mavenSettingsReader = mavenSettingsfactory.getMavenSettingsReader();
		scope.setReader(mavenSettingsReader);
		
		Set<String> inhibitList = new HashSet<String>();		
		scope.setInhibitedRepositoryIds(inhibitList);
		repositoryRegistry = new RepositoryReflectionImpl();
		repositoryRegistry.setInterrogationClientFactory( new RepositoryInterrogationClientFactoryImpl());				
		repositoryRegistry.setAccessClientFactory( new RepositoryAccessClientFactoryImpl());
		repositoryRegistry.setRavenhurstScope(scope);
		repositoryRegistry.setMavenSettingsReader(mavenSettingsReader);
		repositoryRegistry.setArtifactFilterExpertSupplier( scope);
		repositoryRegistry.setLocalRepositoryLocationProvider( mavenSettingsReader);
		
		// unpack the corrupt files (from res/walk/corrupt/corrupt.zip)
		try {
			File target = buildWorkingCopyDirectory( "com.braintribe.test.dependencies");
			if (!target.exists()) {
				target.mkdirs();
			}
			Archives.zip().from( new File( "res/walk/corrupt/corrupt.zip")).unpack( target);
		} catch (ArchivesException e) {
			throw new IllegalArgumentException("Cannot unpack corrupt.zip");
		}													
	}

	@AfterClass
	public static void after() {
		repositoryRegistry.closeContext();
		// delete the contents of the corrupt files 
		File target = buildWorkingCopyDirectory( "com.braintribe.test.dependencies.corrupt");
		TestUtil.delete(target);
		target.delete();
	}
	
	private static File buildWorkingCopyDirectory( String pathAsGroup) {
		String rootAsString = System.getenv( "BT__ARTIFACTS_HOME");
		if (rootAsString == null) {
			throw new IllegalArgumentException("Variable BT__ARTIFACTS_HOME must be set for the tests");
		}
		File root = new File(rootAsString);
		if (!root.exists()) {
			throw new IllegalArgumentException("The directory [" + root.getAbsolutePath() + "] must exist for the tests");
		}
		String path = pathAsGroup.replace( '.', File.separatorChar);
		return new File( root, path);				
	}
	
	
	
	
	/**
	 * build the {@link WalkerFactory} 
	 * @param walkDomain - the {@link WalkDomain} sets what {@link DependencyResolver} to use 
	 * @return - the configured {@link WalkerFactory}
	 */
	private WalkerFactory buildWalkFactory() {
		
		Monitor monitor = new Monitor();
		monitor.setVerbosity( true);
		
		PomExpertFactory pomExpertFactory = new PomExpertFactory();
		pomExpertFactory.addListener(monitor);
		pomExpertFactory.setCacheFactory( new CacheFactoryImpl());
		pomExpertFactory.setSettingsReader(mavenSettingsReader);
		pomExpertFactory.setDetectParentLoops(false);
		
		ConfigurableDependencyResolverFactoryImpl resolverFactory = new ConfigurableDependencyResolverFactoryImpl( monitor);
		resolverFactory.setLocalRepositoryLocationProvider(mavenSettingsReader);
		resolverFactory.setPomExpertFactory(pomExpertFactory);
		resolverFactory.setRepositoryRegistry( repositoryRegistry);
		
		pomExpertFactory.setDependencyResolverFactory(resolverFactory);
		
		MultiRepositorySolutionEnricherFactory enricherFactory = new MultiRepositorySolutionEnricherFactoryImpl();
		enricherFactory.setRepositoryRegistry( repositoryRegistry);
		RelevantPartTupleFactoryImpl partTupleFactoryImpl = new RelevantPartTupleFactoryImpl();
		partTupleFactoryImpl.setRelevantPartTuples( new PartTuple [] {PartTupleProcessor.createJarPartTuple(), PartTupleProcessor.createPomPartTuple()});
		enricherFactory.setRelevantPartTupleFactory( partTupleFactoryImpl);
		enricherFactory.setPartCacheFactory( new PartCacheFactoryImpl());
		
		
		WalkerFactory walkerFactory = new WalkerFactoryImpl();
		walkerFactory.setClashResolverFactory( new ClashResolverFactoryImpl());
		walkerFactory.setScopeControl( new ScopeControlFactory());
		walkerFactory.setExclusionControlFactory( new ExclusionControlFactory());
		walkerFactory.setMultiRepositoryDependencyResolverFactory( resolverFactory);
		walkerFactory.setSolutionEnricherFactory( enricherFactory);
		walkerFactory.setPomExpertFactory( pomExpertFactory);
		
		return walkerFactory;
	}	
	

	
	private ClashResolverDenotationType buildMavenClashResolverDenotationType( InitialDependencyClashPrecedence initialPrecedence, boolean merge) {		
		return buildClashResolverDenotationType( ClashStyle.depth, initialPrecedence, ResolvingInstant.posthoc, merge);
	}
	
	private ClashResolverDenotationType buildOptimisticClashResolverDenotationType( boolean merge) {		
		return buildClashResolverDenotationType( ClashStyle.optimistic, InitialDependencyClashPrecedence.pathIndex, ResolvingInstant.posthoc, merge);
	}
	
	private ClashResolverDenotationType buildClashResolverDenotationType( ClashStyle clashStyle, InitialDependencyClashPrecedence initialPrecedence, ResolvingInstant resolvingInstant, boolean merge) {
		ClashResolverDenotationType clashResolvingType;
		switch (clashStyle) {
			case depth:
				clashResolvingType = ClashResolverByDepthDenotationType.T.create();
				break;
			case index:
				clashResolvingType = ClashResolverByIndexDenotationType.T.create();
				((ClashResolverByIndexDenotationType) clashResolvingType).setResolvingInstant(resolvingInstant);
				break;
			case optimistic:	
			default:
				clashResolvingType = OptimisticClashResolverDenotationType.T.create();
				break;		
		}
		if (initialPrecedence != null)
			clashResolvingType.setInitialClashPrecedence( initialPrecedence);
		
		clashResolvingType.setOmitDependencyMerging( !merge);
		return clashResolvingType;
	}
	
	private WalkDenotationType buildCompileWalkDenotationType(ClashStyle clashStyle, boolean mergeDependencies) {		
		return buildCompileWalkDenotationType( buildClashResolverDenotationType(clashStyle, InitialDependencyClashPrecedence.pathIndex, ResolvingInstant.adhoc, mergeDependencies), ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true);
	}
	
	
	/**
	 * build a {@link WalkDenotationType} 
	 * @param scopeLevel - {@link ScopeKind}, compile or provided 
	 * @param walkDomain - {@link WalkDomain}, repository or workingcopy 
	 * @param walkKind - {@link WalkKind}, classpath or build order (latter not supported) 
	 * @param skipOptional - true 
	 * @return - {@link WalkDenotationType} configured
	 */
	private WalkDenotationType buildCompileWalkDenotationType(ClashResolverDenotationType clashResolvingType, ScopeKind scopeLevel, WalkDomain walkDomain, WalkKind walkKind, boolean skipOptional, DependencyScope ... additionalIncludedScopes) {
		WalkDenotationType walkType = WalkDenotationType.T.create();
		walkType.setClashResolverDenotationType( clashResolvingType);
		
		ExclusionControlDenotationType exclusionControlType = ExclusionControlDenotationType.T.create();		
		walkType.setExclusionControlDenotationType( exclusionControlType);
		
		ScopeControlDenotationType scopeControlType = ScopeControlDenotationType.T.create();
		scopeControlType.setSkipOptional( skipOptional);		
		
		MagicScope scope;
		switch (scopeLevel) {
			case compile:
			default:
				scope = createClasspathMagicScope();
			break;
			case launch:
				scope = createRuntimeMagicScope();
			break;
		}
		scopeControlType.getScopes().add( scope);
		
		if (additionalIncludedScopes != null) {
			for (DependencyScope dependencyScope : additionalIncludedScopes) {
				scopeControlType.getScopes().add(dependencyScope);
			}
		}

		walkType.setScopeControlDenotationType( scopeControlType);
		walkType.setWalkDomain(walkDomain);
		walkType.setWalkKind(walkKind);
		return walkType;
	}

	
	/**
	 * creates a magic scope for compile 
	 * @return - {@link MagicScope}
	 */
	private MagicScope createClasspathMagicScope() {
		MagicScope classpathScope = MagicScope.T.create();
		classpathScope.setName("classpathScope");
		
		DependencyScope compileScope = DependencyScope.T.create();
		compileScope.setName("compile");
		compileScope.setScopeTreatement( ScopeTreatement.INCLUDE);
		classpathScope.getScopes().add( compileScope);
		
		DependencyScope providedScope = DependencyScope.T.create();
		providedScope.setName("provided");
		providedScope.setScopeTreatement( ScopeTreatement.INCLUDE);		
		classpathScope.getScopes().add( providedScope);
		
		return classpathScope;
	}
	
	private MagicScope createRuntimeMagicScope() {
		MagicScope runtimeScope = MagicScope.T.create();
		runtimeScope.setName("runtimeScope");
		
		DependencyScope compileScope = DependencyScope.T.create();
		compileScope.setName("compile");
		compileScope.setScopeTreatement( ScopeTreatement.INCLUDE);
		runtimeScope.getScopes().add( compileScope);
		
		DependencyScope providedScope = DependencyScope.T.create();
		providedScope.setName("runtime");
		providedScope.setScopeTreatement( ScopeTreatement.INCLUDE);		
		runtimeScope.getScopes().add( providedScope);
		
		return runtimeScope;
	}
	
	
	public Collection<Solution> test(String walkScopeId, Solution terminal, WalkDenotationType denotationType, String [] expectedNames, int repeat, int threshold) throws Exception {
		
			WalkerFactory walkFactory = buildWalkFactory();
			Walker walker = walkFactory.apply( denotationType);

			Collection<Solution> solutions = null;
			long sum = 0;
			for (int i = 0; i < repeat; i++) {
				long before = System.nanoTime();
				solutions = walker.walk( walkScopeId, terminal);
				long after = System.nanoTime();
				if (i >= threshold) {
					sum += (after-before);
				}
			}
			
			long average = sum / (repeat-threshold);
			System.out.println("walking took [" + (average / 1E6) + "]ms");
			
			if (expectedNames == null) {
				// no specified, list them
				System.out.println("found [" + solutions.size() + "] solutions");
				for (Solution solution : solutions) {
					System.out.println( "\t" + NameParser.buildName(solution));
				}
			}
			else  if (expectedNames.length == 0) {
				Assert.assertTrue("solutions returned", solutions == null || solutions.size() == 0);
			}
			else {
				Assert.assertTrue("No solutions returned", solutions != null && solutions.size() > 0);
			}			
			
			List<Solution> sorted = new ArrayList<Solution>( solutions);
			Collections.sort( sorted, new Comparator<Solution>() {

				@Override
				public int compare(Solution arg0, Solution arg1) {
					return arg0.getArtifactId().compareTo( arg1.getArtifactId());					
				}
				
			});			
			if (expectedNames == null)
				return solutions;
			
			boolean result = listDiscrepancies(sorted, expectedNames);			
			if (!result) {
				Assert.fail("Test result is not as expected");
			}
			
			return solutions;		
	}
	
	private boolean listDiscrepancies( Collection<Solution> result, String [] expected) {
		boolean retval = true;
		Map<String, String> resultMap = new HashMap<String, String>();
		Map<String, Solution> nameToSolutionMap = new HashMap<String, Solution>();
		Map<Identification, Solution> identificationToSolutionMap = CodingMap.createHashMapBased( new IdentificationWrapperCodec());
		for (Solution solution : result) {			
			resultMap.put( solution.getGroupId() + ":" + solution.getArtifactId(), VersionProcessor.toString(solution.getVersion()));
			nameToSolutionMap.put( solution.getGroupId() + ":" + solution.getArtifactId() +"#" + VersionProcessor.toString(solution.getVersion()), solution);
			identificationToSolutionMap.put( solution, solution);			
		}
		Map<String, String> expectedMap = new HashMap<String, String>();
		for (String str : expected) {
			if (str.startsWith(";"))
				continue;
			int p = str.indexOf('#');
			expectedMap.put( str.substring(0,p), str.substring(p+1));
		}
		Map<String, Boolean> checkedMap = new HashMap<String, Boolean>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String expectedVersion = expectedMap.get( entry.getKey());
			if (expectedVersion == null) {
				System.out.println("Unexpected artifact : " + entry.getKey() + "#" + entry.getValue());
				retval = false;
				// list the dependency .. 
				Solution solution = nameToSolutionMap.get( entry.getKey() + "#" + entry.getValue());
				Set<Solution> processed = CodingSet.createHashSetBased( new SolutionWrapperCodec());
				printPath( processed, solution, 1);
				

			}
			else {
				if (!expectedVersion.equalsIgnoreCase( entry.getValue())) {
					System.out.println("Unexpected version : expected for [" + entry.getKey() + "] is [" + expectedVersion +"], retrieved is [" + entry.getValue() + "]");
					retval = false;
				}
				checkedMap.put( entry.getKey() + "#" + expectedVersion, true);
			}
		}
		//
		for (String str : expected) {
			if (str.startsWith(";"))
				continue;
			
			if (!Boolean.TRUE.equals(checkedMap.get(str))) {				
				retval = false;
				Identification identification = Identification.T.create();
				int g = str.indexOf(":");
				identification.setGroupId( str.substring(0, g));
				identification.setArtifactId( str.substring( g+1, str.indexOf('#')));
				Solution discrepancy = identificationToSolutionMap.get( identification);
				if (discrepancy != null) {
					System.out.println("Artifact [" + str +"] expected, but [" + NameParser.buildName(discrepancy) + "] retrieved");
				}
				else {
					System.out.println("Artifact [" + str +"] expected, but not retrieved");	
				}
			}
		}					
		return retval;
	}
	
	private void printPath( Set<Solution> processed, Solution solution, int index) {
		if (processed.contains( solution)) {
			System.err.println("loop detected on [" + NameParser.buildName(solution));
			return;
		}
		processed.add( solution);
		for (Dependency dependency : solution.getRequestors()) {
			Set<Artifact> requesters = dependency.getRequestors();
			String offset = "";
			for (int i = 0; i < index; i++) {
				offset+="\t";
			}
			int i = 0;
			for (Artifact artifact : requesters) {
				i++;			
				System.out.println(offset+ i + ": " + NameParser.buildName(artifact));				
				printPath( processed, (Solution) artifact, index+1);
			}			
		}
	}
	
	private String [] loadExpectedNamesFromFile( File file) {
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
			String contents = IOTools.slurp(stream, "UTF-8");
			String [] cnt = contents.split("\r\n");
			return cnt;
		} catch (Exception e) {
			Assert.fail("cannot load file [" + file.getAbsolutePath() + "]");
			return null;
		}
		finally {
			if (stream != null)
				IOTools.closeQuietly(stream);
		}		
	}
	
	/*
	 * 
	 * clash resolving / merging 
	 * 
	 */
	/**
	 * testing clash resolving - non weeded (monitor tests actually)
	 */

	public void clashResolvingTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.clashTest");
			terminal.setArtifactId( "ClashTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));			
			String[] expectedNames = new String [] {
					"com.braintribe.test.dependencies.clashTest:A#1.0",
					"com.braintribe.test.dependencies.clashTest:B#1.0",
					"com.braintribe.test.dependencies.clashTest:C#1.1",					
			};
			
			test( "clashTest", terminal, buildCompileWalkDenotationType(ClashStyle.optimistic, true), expectedNames,1,0);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	
	/**
	 * testing clash resolving - non weeded (monitor tests actually)
	 */
	
	public void clashVerificationTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.model.processing");
			terminal.setArtifactId( "MetaModelExtractor");
			terminal.setVersion( VersionProcessor.createFromString( "2.2"));			
			
			
			test( "clashVerification", terminal, buildCompileWalkDenotationType(ClashStyle.optimistic, true), null,1,0);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	
	
	
	



	public void unresolvedWeedingAndClashTest() {
		try {
			Solution terminal = Solution.T.create();
			String group = "com.braintribe.test.dependencies.unresolvedDependenciesWeedingTest";
			terminal.setGroupId( group);
			terminal.setArtifactId( "UnresolvedDependenciesWeedingTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {
					group + ":A#1.0",			
					group + ":B#1.0",
					group + ":C#1.1"					
			};
			
			test( "unresolvingWeedingAndClash", terminal, buildCompileWalkDenotationType(ClashStyle.optimistic, true), expectedNames,1,0);			
						
		} catch (Exception e) {
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
	}
	


	public void clashWeedingTest() {
		try {
			Solution terminal = Solution.T.create();
			String group = "com.braintribe.test.dependencies.clashWeedingTest";
			terminal.setGroupId( group);
			terminal.setArtifactId( "ClashWeedingTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {
					group + ":A#1.0",			
					group + ":B#1.0",
					group + ":C#1.1",					
					group + ":D#1.1"
			};
			
			test( "clashWeeding", terminal, buildCompileWalkDenotationType(ClashStyle.optimistic, true), expectedNames,1,0);			
						
		} catch (Exception e) {
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
	}
	
	
	
	/*
	 * 
	 *  simple tests 
	 * 
	 */
	
	
	
	
	/*
	 * 
	 * performance tests 
	 * 
	 */
	
	/**
	 * performance test only on Malaclypse itself 
	 */
	//@Test
	public void performanceTest_Compile_Malaclypse_3_0() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.build.artifacts");
			terminal.setArtifactId( "Malaclypse");
			terminal.setVersion( VersionProcessor.createFromString( "3.0"));
			//loadExpectedNamesFromFile( new File("res/walk/results/Malaclypse#2.0.launch.txt"))
			test( "malaclypse", terminal, buildCompileWalkDenotationType(ClashStyle.optimistic, true), null, 1, 0);			
						
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
	}
	/**
	 * performance test only on tribefire services 
	 */
	
	public void performanceTest_Compile_TribefireServicesDeps_2_0() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.product.tribefire");
			terminal.setArtifactId( "TribefireServicesDeps");
			terminal.setVersion( VersionProcessor.createFromString( "2.0"));
			
			test( "tfServicesDeps#2.0", terminal, buildCompileWalkDenotationType( buildMavenClashResolverDenotationType( InitialDependencyClashPrecedence.hierarchyIndex, true),  ScopeKind.launch, WalkDomain.repository, WalkKind.classpath, true), loadExpectedNamesFromFile( new File("res/walk/results/TribefireServicesDeps#2.0.launch.txt")), 1,0);			
						
		} catch (Exception e) {
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
	}
	/**
	 * performance test only on tribefire services depses 
	 */
	
	public void performanceTest_Compile_TribefireServicesDeps_1_1() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.product.tribefire");
			terminal.setArtifactId( "TribefireServicesDeps");
			terminal.setVersion( VersionProcessor.createFromString( "1.1"));
			
			test( "tfServicesDeps#1.1", terminal, buildCompileWalkDenotationType( buildMavenClashResolverDenotationType( InitialDependencyClashPrecedence.hierarchyIndex, true), ScopeKind.launch, WalkDomain.repository, WalkKind.classpath, true), loadExpectedNamesFromFile( new File("res/walk/results/TribefireServicesDeps#1.1.launch.txt")), 1,0);			
						
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
	}
	/*
	 * 
	 *  undetermined (deps without valid version-range), reassign
	 * 
	 */
	
	/**
	 * testing undetermined / reassigned dependencies 
	 */
	
	public void undeterminedReassigningTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.undeterminedDependenciesTest");
			terminal.setArtifactId( "UndeterminedDependenciesTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {					
					"com.braintribe.test.dependencies.undeterminedDependenciesTest:A#1.0",
					"com.braintribe.test.dependencies.undeterminedDependenciesTest:B#1.0",
					"com.braintribe.test.dependencies.undeterminedDependenciesTest:C#1.0",
					"com.braintribe.test.dependencies.undeterminedDependenciesTest:D#1.0",				
			};
			
			test( "undetermined", terminal, buildCompileWalkDenotationType(ClashStyle.optimistic, true), expectedNames,1,0);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
	}
	
	
	
	
	/**
	 * parent test 
	 */
	
	public void parentTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.parentTest");
			terminal.setArtifactId( "ParentTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {					
					"com.braintribe.test.dependencies.parentTest:A#1.0",
					"com.braintribe.test.dependencies.parentTest:B#1.0",			
					"com.braintribe.test.dependencies.parentTest:C#1.0",			
					"com.braintribe.test.dependencies.parentTest:D#1.0",			
					"com.braintribe.test.dependencies.parentTest:T#1.0",			
			};
			
			test( "parent", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), expectedNames,1,0);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
	}
	
	
	public void parentDependencyTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.parentDependencyTest");
			terminal.setArtifactId( "ParentDependencyTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {					
					"com.braintribe.test.dependencies.parentDependencyTest:A#1.1",							
			};
			
			test( "parentDependency", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), expectedNames,1,0);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
	}
	
	
	public void importDependencyTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.importDependencyTest");
			terminal.setArtifactId( "ImportDependencyTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {					
					"com.braintribe.test.dependencies.importDependencyTest:A#1.2",							
			};
			
			test( "parentDependency", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), expectedNames,1,0);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
	}
	
	
	
	//
	
	
	
	public void dominantTerminalTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.dominantTest");
			terminal.setArtifactId( "DominantTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			
			String[] expectedNames = new String [] {					
					"com.braintribe.test.dependencies.dominantTest:A#1.0",
					"com.braintribe.test.dependencies.dominantTest:B#1.0",
					"com.braintribe.test.dependencies.dominantTest:C#1.0",
					"com.braintribe.test.dependencies.dominantTest:D#1.0",
			};		
			
			test( "dominantTerminalTest", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), expectedNames,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}
	
	/**
	 *  parent tests 
	 */
	
	public void grandParentTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.grandParentTest");
			terminal.setArtifactId( "GrandParentTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {					
					"com.braintribe.test.dependencies.grandParentTest:A#1.0",
					"com.braintribe.test.dependencies.grandParentTest:B#1.0",			
					"com.braintribe.test.dependencies.grandParentTest:C#1.0",			
					"com.braintribe.test.dependencies.grandParentTest:D#1.0",			
					"com.braintribe.test.dependencies.grandParentTest:X#1.0",
					"com.braintribe.test.dependencies.grandParentTest:Y#1.0",
			};
			
			test( "parent", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), expectedNames,1,0);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
	}
	
	
	/**
	 * exclusions - class-path, optional skipped
	 */

	public void test_Compile_TikaMimeTools_1_1_exclusion() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.utils");
			terminal.setArtifactId( "TikaMimeTools");
			terminal.setVersion( VersionProcessor.createFromString( "1.1"));			
			
			//test( terminal, buildCompileWalkDenotationType(buildClashResolverDenotationType(InitialDependencyClashPrecedence.hierarchyIndex),  ScopeKind.launch, WalkDomain.repository, WalkKind.classpath, true),  loadExpectedNamesFromFile( new File("res/walk/results/TikaMimeTools#1.1.launch.txt")),1,0);
			test( "tikaMimeTools", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.launch, WalkDomain.repository, WalkKind.classpath, true),  loadExpectedNamesFromFile( new File("res/walk/results/TikaMimeTools#1.1.launch.txt")),1,0);			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() +"] thrown");
		}
	}
	
	/**
	 * exclusions - reacting on types (only null/jar allowed)
	 */
	
	public void test_types() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.typeTest");
			terminal.setArtifactId( "TypeTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			
			String[] expectedNames = new String [] {					
					"com.braintribe.test.dependencies.typeTest:A#1.0",
					"com.braintribe.test.dependencies.typeTest:B#1.0",			
					"com.braintribe.test.dependencies.typeTest:C#1.0",			
			};
			
			test( "typeTest", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.launch, WalkDomain.repository, WalkKind.classpath, true),  expectedNames,1,0);			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() +"] thrown");
		}
	}
	
	

	/*
	 * 
	 * loops
	 * 
	 */
	
	public void loopingParentTest() {
		boolean expectedExceptionThrown = false;
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.parentLoopTest");
			terminal.setArtifactId( "Terminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
		
			test( "loopingParents", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {
			expectedExceptionThrown = true;
			Assert.assertTrue( "expected WalkException not thrown", e instanceof WalkException);
			Throwable ce = ((WalkException) e).getCause();
			Assert.assertTrue( "expected PomReaderException not cause ", ce instanceof PomReaderException);
		}
		Assert.assertTrue("expected exception wasn't thrown", expectedExceptionThrown);
	}
	
	
	public void loopingDependenciesTest() {
	
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.walkLoopTest");
			terminal.setArtifactId( "Terminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {											
					"com.braintribe.test.dependencies.walkLoopTest:A#1.0",			
					"com.braintribe.test.dependencies.walkLoopTest:B#1.0",
					"com.braintribe.test.dependencies.walkLoopTest:C#1.0",					
			};
		
			test( "loopingDependencies", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, false), expectedNames,1,0);
			
		} catch (Exception e) {		
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}
	// 
	// corrupt tests 
	//
	
	public void corruptDependenciesTest() {
		boolean expectedExceptionThrown = false;
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.corrupt.corruptDependenciesTest");
			terminal.setArtifactId( "CorruptDependenciesTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));			
		
			test( "corruptDependencies", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.workingCopy, WalkKind.classpath, false), null, 1,0);
			
		} catch (Exception e) {		
			expectedExceptionThrown = true;
			Assert.assertTrue( "expected WalkException not thrown", e instanceof WalkException);
			Throwable ce = ((WalkException) e).getCause();
			Assert.assertTrue( "expected ResolvingException not cause ", ce instanceof ResolvingException);
		}		
		Assert.assertTrue("expected exception wasn't thrown", expectedExceptionThrown);
	}
	
	
	public void corruptParentTest() {
		boolean expectedExceptionThrown = false;
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.corrupt.corruptParentTest");
			terminal.setArtifactId( "CorruptParentTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));			
		
			test( "corruptParents", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.workingCopy, WalkKind.classpath, false), null, 1,0);
			
		} catch (Exception e) {		
			expectedExceptionThrown = true;
			Assert.assertTrue( "expected WalkException not thrown", e instanceof WalkException);
			Throwable ce = ((WalkException) e).getCause();
			Assert.assertTrue( "expected PomReaderException not cause ", ce instanceof PomReaderException);
		}		
		Assert.assertTrue("expected exception wasn't thrown", expectedExceptionThrown);
	}
	
	
	public void corruptTerminalTest() {
		boolean expectedExceptionThrown = false;
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.corrupt.corruptTerminalTest");
			terminal.setArtifactId( "Terminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));			
		
			test( "corruptTerminals", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.workingCopy, WalkKind.classpath, false), null, 1,0);
			
		} catch (Exception e) {		
			expectedExceptionThrown = true;
			Assert.assertTrue( "expected WalkException not thrown", e instanceof WalkException);
			Throwable ce = ((WalkException) e).getCause();
			Assert.assertTrue( "expected PomReaderException not cause ", ce instanceof PomReaderException);
		}		
		Assert.assertTrue("expected exception wasn't thrown", expectedExceptionThrown);
	}
	
	
	public void corruptResolvingTest_Strict() {
		boolean expectedExceptionThrown = false;
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.corrupt.corruptResolvingTest");
			terminal.setArtifactId( "Terminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));				
			test( "corruptResolving", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.workingCopy, WalkKind.classpath, false), null,1,0);
			
		} catch (Exception e) {	
			expectedExceptionThrown = true;
			Assert.assertTrue( "expected WalkException not thrown", e instanceof WalkException);
			Throwable ce = ((WalkException) e).getCause();
			Assert.assertTrue( "expected PomReaderException not cause ", ce instanceof PomReaderException);			
		}	
		Assert.assertTrue("expected exception wasn't thrown", expectedExceptionThrown);
	}
	
	
	public void resolvingPerPropertyTest() {
	
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.resolvingPerPropertyTest");
			terminal.setArtifactId( "Terminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {											
					"com.braintribe.test.dependencies.resolvingPerPropertyTest:A#1.0",			
			};		
			test( "resolvingPerProperty", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, false), expectedNames,1,0);
			
		} catch (Exception e) {		
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}
	
	
	public void resolvingPerParentTest() {
	
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.resolvingPerParentTest");
			terminal.setArtifactId( "Terminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {											
					"com.braintribe.test.dependencies.resolvingPerParentTest:A#1.0",			
			};		
			test( "resolvingPerParent", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, false), expectedNames,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}
	
	
	public void importPropertyParentTest() {
	
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.parentLookupTest");
			terminal.setArtifactId( "ParentLookupTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			String[] expectedNames = new String [] {											
					"com.braintribe.test.dependencies.parentLookupTest:A#1.0",
					"com.braintribe.test.dependencies.parentLookupTest:B#1.0",
					"com.braintribe.test.dependencies.parentLookupTest:C#1.0",
			};		
			test( "importPropertyParentTest", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, false), expectedNames,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}
	
	
	public void kieTest() {
	
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "org.kie");
			terminal.setArtifactId( "kie-api");
			terminal.setVersion( VersionProcessor.createFromString( "6.4.0.Final"));
			
			test( "kieTest", terminal, buildCompileWalkDenotationType(buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}
	
	
	public void mavenAntTasksTest() {
	
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "org.apache.maven");
			terminal.setArtifactId( "maven-ant-tasks");
			terminal.setVersion( VersionProcessor.createFromString( "2.1.1"));
			
			test( "mavenAntTasks", terminal, buildCompileWalkDenotationType(buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}
	
	
	public void conversionTest() {
	
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.model.processing");
			terminal.setArtifactId( "Conversion");
			terminal.setVersion( VersionProcessor.createFromString( "1.3"));
			
			test( "conversion", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}
	/**
	 *  test for issue with repository role detection, i.e. loading it from maven-metadata on artifact level. 
	 */
	//@Test
	public void metadataAccessTerminal() {
		/*
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.metadataAccessTest");
			terminal.setArtifactId( "MetadataAccessTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			
			test( "MetadataAccessTest-1", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
		
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.metadataAccessTest");
			terminal.setArtifactId( "MetadataAccessTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "2.0"));
			
			test( "MetadataAccessTest-1", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
		*/
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.metadataAccessTest");
			terminal.setArtifactId( "MetadataAccessTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "2.1"));
			
			test( "MetadataAccessTest-1", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}
		
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.metadataAccessTest");
			terminal.setArtifactId( "MetadataAccessTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "2.2"));
			
			test( "MetadataAccessTest-1", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}

	}
	
	
	public void buildOrderTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.buildOrderTest");
			terminal.setArtifactId( "BuildOrderTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));			
			String[] expectedNames = new String [] {											
					"com.braintribe.test.dependencies.buildOrderTest:D#1.0",
					"com.braintribe.test.dependencies.buildOrderTest:C#1.0",
					"com.braintribe.test.dependencies.buildOrderTest:B#1.0",
					"com.braintribe.test.dependencies.buildOrderTest:A#1.0",
			};		
			
			Collection<Solution> result = test( "subtreeMergingBuildOrder", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.workingCopy, WalkKind.buildOrder, true), null,1,0);
			
			int i = 0;
			boolean failed=false;
			for (Solution solution : result) {
				String testName = NameParser.buildName( solution);
				String expectedName = expectedNames[i++];
				if (!testName.equalsIgnoreCase(expectedName)) {
					System.out.println("expected [" + expectedName + "], encountered [" + testName + "]");
					failed= true;
				}
			}
			Assert.assertFalse( "expectations not met", failed);
			
		} catch (Exception e) {
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	
	
	public void rangedBuildOrderTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.buildOrderTest");
			terminal.setArtifactId( "BuildOrderTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.1"));			
			String[] expectedNames = new String [] {											
					"com.braintribe.test.dependencies.buildOrderTest:D#1.1",
					"com.braintribe.test.dependencies.buildOrderTest:C#1.1",
					"com.braintribe.test.dependencies.buildOrderTest:B#1.1",
					"com.braintribe.test.dependencies.buildOrderTest:A#1.1",
			};		
			
			Collection<Solution> result = test( "subtreeMergingBuildOrder", terminal, buildCompileWalkDenotationType( buildOptimisticClashResolverDenotationType( true), ScopeKind.compile, WalkDomain.workingCopy, WalkKind.buildOrder, true), null,1,0);
			
			int i = 0;
			boolean failed=false;
			for (Solution solution : result) {
				String testName = NameParser.buildName( solution);
				String expectedName = expectedNames[i++];
				if (!testName.equalsIgnoreCase(expectedName)) {
					System.out.println("expected [" + expectedName + "], encountered [" + testName + "]");
					failed= true;
				}
			}
			Assert.assertFalse( "expectations not met", failed);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail( "exception [" + e + "] thrown");
		}
	}
	
	public void localOnlyTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "com.braintribe.test.dependencies.localOnlyTest");
			terminal.setArtifactId( "LocalOnlyTestTerminal");
			terminal.setVersion( VersionProcessor.createFromString( "1.0"));
			
			test( "localOnly", terminal, buildCompileWalkDenotationType(buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}
	

	public void jasperCrapTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "org.mortbay.jasper");
			terminal.setArtifactId( "jasper-jsp");
			terminal.setVersion( VersionProcessor.createFromString( "8.0.23.M1"));
			
			test( "localOnly", terminal, buildCompileWalkDenotationType(buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}

	//@Test
	public void mavenJxrTest() {
		try {
			Solution terminal = Solution.T.create();
			terminal.setGroupId( "org.apache.maven");
			terminal.setArtifactId( "maven-jxr");
			terminal.setVersion( VersionProcessor.createFromString( "2.4"));
			
			test( "mavenJxr", terminal, buildCompileWalkDenotationType(buildOptimisticClashResolverDenotationType( true),  ScopeKind.compile, WalkDomain.repository, WalkKind.classpath, true), null,1,0);
			
		} catch (Exception e) {	
			e.printStackTrace();
			Assert.fail( "exception [" + e.getMessage() + "] thrown");
		}				
	}	
	
}
