// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.artifacts.codebase.build;

import static com.braintribe.build.artifact.name.NameParser.parseDependencyFromHotfixShorthand;
import static com.braintribe.wire.api.util.Lists.list;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.api.BuildRange;
import com.braintribe.build.artifact.api.BuildRangeDependencyResolver;
import com.braintribe.build.artifact.api.BuildRanges;
import com.braintribe.build.artifact.api.DependencyResolver;
import com.braintribe.build.artifact.api.RangedArtifact;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.build.artifacts.mc.wire.buildwalk.BuildDependencyResolvers;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.CodebaseAwareBuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.CodebaseConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.paths.PathCollectors;
import com.braintribe.wire.api.context.WireContext;

@Category(KnownIssue.class)
public class BuildResolverTest implements CodebaseConfigurationContract, FilterConfigurationContract {
	
	private static File codebaseRoot = new File(PathCollectors.filePath.join("res", "grouping", "grouping.flattened"));
	private String codebasePattern = "${groupId}/${version}/${artifactId}";
	private Predicate<RangedArtifact> artifactFilter = buildFilter();

	private static Predicate<RangedArtifact> buildFilter() {
		Set<String> groupNames = new HashSet<>();
		for (File file: codebaseRoot.listFiles()) {
			if (file.isDirectory()) {
				groupNames.add(file.getName());
			}
		}
		
		return d -> groupNames.contains(d.getGroupId());
	}
	
	private Dependency dependency(String dependency) {
		try {
			return parseDependencyFromHotfixShorthand(dependency);
		} catch (NameParserException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Iterable<Dependency> dependencies() {
		return list(
			dependency("com.braintribe.terminal:Terminal#1.0")
		);
	}
	
	@Test
	public void test() {
		
		WireContext<CodebaseAwareBuildDependencyResolutionContract> context = BuildDependencyResolvers.codebaseAware(contextBuilder -> {
			contextBuilder.bindContract(CodebaseConfigurationContract.class, this);
		});
		
		try {
			DependencyResolver buildDependencyResolver = context.contract().buildDependencyResolver();
			
			Set<Solution> solutions = buildDependencyResolver.resolve(dependencies());
			
			List<String> expectedSolutionNames = list(
					"com.braintribe.terminal:TerminalParent#1.0.1",
					"com.braintribe.terminal:A#1.0.1",
					"com.braintribe.grpBase:GrpBaseParent#1.0.1",
					"com.braintribe.grpBase:BaseDependency#1.0.1",
					"com.braintribe.grpOne:GrpOneParent#1.0.1",
					"com.braintribe.grpOne:C#1.0.1",
					"com.braintribe.grpOne:A#1.0.1",
					"com.braintribe.grpOne.subOne:GrpOneSubOneParent#1.0.1",
					"com.braintribe.grpOne.subOne:B#1.0.1",
					"com.braintribe.grpOne.subOne:A#1.0.1",
					"com.braintribe.terminal:Terminal#1.0.1"
				);
			
			List<String> actualSolutionNames = solutions.stream().map(NameParser::buildName).collect(Collectors.toList());
			
			Assert.assertTrue("build dependency resolution did not yield the expected results", actualSolutionNames.equals(expectedSolutionNames));
		}
		finally {
			context.shutdown();
		}
		
	}
	
	@Test
	public void rangedTest1() {
		rangedTest(
				"[com.braintribe.grpOne:C#1.0+com.braintribe.terminal:Terminal#1.0]", 
				
				list(
						"com.braintribe.grpOne:C#1.0.1",
						"com.braintribe.grpOne:A#1.0.1",
						"com.braintribe.grpOne.subOne:A#1.0.1",
						"com.braintribe.terminal:Terminal#1.0.1"
						)
				);
	}
	
	@Test
	public void rangedTest2() {
		rangedTest(
				"[com.braintribe.grpOne.subOne:B#1.0+[com.braintribe.grpOne:C#1.0+com.braintribe.terminal:Terminal#1.0]", 
				
				list(
				"com.braintribe.grpOne.subOne:B#1.0.1",
				"com.braintribe.grpOne:C#1.0.1",
				"com.braintribe.grpOne:A#1.0.1",
				"com.braintribe.grpOne.subOne:A#1.0.1",
				"com.braintribe.terminal:Terminal#1.0.1"
				)
		);
	}
	
	@Test
	public void rangedTest3() {
		rangedTest(
				"[com.braintribe.grpOne:C#1.0]+[com.braintribe.terminal:Terminal#1.0]", 
				
				list(
						"com.braintribe.grpOne:C#1.0.1",
						"com.braintribe.terminal:Terminal#1.0.1"
						)
				);
	}
	
	@Test
	public void rangedTest4() {
		rangedTest(
				"(com.braintribe.grpOne:C#1.0+com.braintribe.terminal:Terminal#1.0)", 
				
				list(
						"com.braintribe.grpOne:A#1.0.1",
						"com.braintribe.grpOne.subOne:A#1.0.1"
						)
				);
	}
	
	@Test
	public void rangedTest5() {
		rangedTest(
				"com.braintribe.terminal:Terminal#1.0", 
				
				list(
						"com.braintribe.terminal:TerminalParent#1.0.1",
						"com.braintribe.terminal:A#1.0.1",
						"com.braintribe.grpBase:GrpBaseParent#1.0.1",
						"com.braintribe.grpBase:BaseDependency#1.0.1",
						"com.braintribe.grpOne:GrpOneParent#1.0.1",
						"com.braintribe.grpOne:C#1.0.1",
						"com.braintribe.grpOne:A#1.0.1",
						"com.braintribe.grpOne.subOne:GrpOneSubOneParent#1.0.1",
						"com.braintribe.grpOne.subOne:B#1.0.1",
						"com.braintribe.grpOne.subOne:A#1.0.1",
						"com.braintribe.terminal:Terminal#1.0.1"
					)
				);
	}
	
	private void rangedTest(String buildRangeAsStr, List<String> expectedSolutionNames) {
		
		BuildRange buildRange = BuildRanges.parseFromConcatString(buildRangeAsStr);
		
		WireContext<CodebaseAwareBuildDependencyResolutionContract> context = BuildDependencyResolvers.codebaseAware(contextBuilder -> {
			contextBuilder.bindContract(CodebaseConfigurationContract.class, this);
		});
		
		try {
			BuildRangeDependencyResolver buildDependencyResolver = context.contract().buildDependencyResolver();
			
			Set<Solution> solutions = buildDependencyResolver.resolve(buildRange.getEntryPoints(), buildRange.getLowerBound(), buildRange.getUpperBound());
			
			List<String> actualSolutionNames = solutions.stream().map(NameParser::buildName).collect(Collectors.toList());
			
			//actualSolutionNames.stream().forEach(System.out::println);
			
			boolean ok = actualSolutionNames.equals(expectedSolutionNames);
			
			if (!ok) {
				actualSolutionNames.stream().forEach(System.out::println);
				Assert.fail("build dependency resolution did not yield the expected results");
			}
		}
		finally {
			context.shutdown();
		}
		
	}

	@Override
	public File codebaseRoot() {
		return codebaseRoot;
	}
	

	@Override
	public String defaultVersion() {	
		return null;
	}

	@Override
	public String codebasePattern() {
		return codebasePattern;
	}

	@Override
	public Predicate<RangedArtifact> artifactFilter() {
		return artifactFilter;
	}

	@Override
	public Predicate<? super Solution> solutionFilter() {
		return s -> true;
	}

	@Override
	public Predicate<? super Dependency> dependencyFilter() {
		return s -> true;
	}

	@Override
	public Predicate<? super PartTuple> partFilter() {
		return null;
	}

	@Override
	public Collection<PartTuple> partExpectation() {
		return null;
	}
}
