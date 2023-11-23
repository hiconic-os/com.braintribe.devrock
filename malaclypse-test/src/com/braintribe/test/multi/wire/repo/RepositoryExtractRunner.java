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
package com.braintribe.test.multi.wire.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.braintribe.build.artifact.api.BuildRange;
import com.braintribe.build.artifact.api.BuildRangeDependencyResolver;
import com.braintribe.build.artifact.api.BuildRangeDependencySolution;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifacts.mc.wire.buildwalk.BuildDependencyResolvers;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.space.FilterConfigurationSpace;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.wire.api.context.WireContext;

/**
 * standalone runner for the repository extract 
 * 
 * @author pit
 *
 */
public class RepositoryExtractRunner {
	private static Logger log = Logger.getLogger(RepositoryExtractRunner.class);
	private List<Pattern> globalExclusions = new ArrayList<>();
	private String walkScopeId = UUID.randomUUID().toString();	
	private WireContext<BuildDependencyResolutionContract> buildDependencyResolutionContext = null; 
	
	public RepositoryExtractRunner() {
		
		WireContext<BuildDependencyResolutionContract> wireContext = null;
		RepositoryExtractFilterConfiguration filterConfiguration = new RepositoryExtractFilterConfiguration();
		
		wireContext = BuildDependencyResolvers.standard(b -> {  
			b.bindContract(FilterConfigurationContract.class, filterConfiguration);
		});
				
		
		//wireContext = Wire.context( new BuildDependencyResolverTerminalWireModule(filterConfiguration));
		
		buildDependencyResolutionContext = wireContext;
	}
	
	/**
	 * @param globalExclusions - list of Pattern to match possible exclusions against
	 */
	public void setGlobalExclusions(List<Pattern> globalExclusions) {
		this.globalExclusions = globalExclusions;
	}

	/**
	 * @param condensedTerminalNames
	 * @return
	 */
	public Map<String,List<String>> extractArtifacts( List<String> condensedTerminalNames) {
		
		Map<String, List<String>> result = new HashMap<>();
				
		for (String condensedTerminalName : condensedTerminalNames) {
			result.put( condensedTerminalName, extractArtifacts( condensedTerminalName));
		}
		return result;
	}
	
	
	
	
	private List<String> extractArtifacts(String condensedTerminalName) {
		Dependency rangedDependency = NameParser.parseCondensedDependencyNameAndAutoRangify(condensedTerminalName);

		Set<Solution> solutions = buildDependencyResolutionContext.contract().plainOptimisticDependencyResolver().resolve(rangedDependency);

		List<String> result = new ArrayList<>();
		if (solutions != null) {

			solutions.stream().forEach(solution -> {
				
				ArtifactPomReader pomReader = buildDependencyResolutionContext.contract().pomReader();
				Solution terminal = pomReader.read(walkScopeId, solution);
				
				BuildRange buildRange = new BuildRange(terminal.getDependencies(), null, null);
				BuildRangeDependencyResolver buildDependencyResolver = buildDependencyResolutionContext.contract().buildDependencyResolver();
				BuildRangeDependencySolution set = buildDependencyResolver.resolve(buildRange);
				
				for (Solution s : set.getSolutions()) {
					String name = NameParser.buildName(s);
					result.add(name);
				}
				
			});
		}


		return result;
	}




	private class RepositoryExtractFilterConfiguration extends FilterConfigurationSpace {
		
		@Override
		public Predicate<? super Solution> solutionFilter() {
			// filter the solutions with the configured global exclusions
			return RepositoryExtractRunner.this::isExcluded;
		}

		@Override
		public Predicate<? super Dependency> dependencyFilter() {
			// filter the dependency with the correct scope and optional flag
			return RepositoryExtractRunner.this::isIncluded;
		}

		@Override
		public Predicate<? super PartTuple> partFilter() {
			// every part should be included as this is a full export of the artifact
			return p -> true;
		}

		@Override
		public boolean filterSolutionBeforeVisit() {		
			return true;
		}
		
	}

	private boolean isExcluded(Solution solution) {
		return isExcluded(NameParser.buildName(solution));
	}
	
	private boolean isExcluded(String artifactId) {
		if (globalExclusions != null && !globalExclusions.isEmpty()) {
			for (Pattern p : globalExclusions) {
				if (p.matcher(artifactId).matches()) {
					log.debug("Excluding artifact "+artifactId+" because it is globally excluded.");
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isIncluded(Dependency dependency) {
		String scope = dependency.getScope();
		
		boolean excluded = dependency.getOptional() || (scope != null && (scope.equalsIgnoreCase("provided") || scope.equalsIgnoreCase("test")));
		return !excluded;
	}
}
