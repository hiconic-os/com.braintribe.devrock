package com.braintribe.build.artifacts.mc.wire.repositoryExtract.expert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.braintribe.build.artifact.api.BuildRange;
import com.braintribe.build.artifact.api.BuildRangeDependencyResolver;
import com.braintribe.build.artifact.api.BuildRangeDependencySolution;
import com.braintribe.build.artifact.api.DependencyResolver;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

/**
 * a runner for the repository extract 
 * 
 * @author pit
 *
 */
public class RepositoryExtractRunner {
	private String walkScopeId = UUID.randomUUID().toString();	
	private BuildRangeDependencyResolver buildDependencyResolver;
	private DependencyResolver plainDependencyResolver;
	private ArtifactPomReader pomReader;
	private List<String> condensedTerminalNames;

	/**
	 * @param buildDependencyResolver - the build dependency resolver (-thingi)  as configured
	 */
	@Configurable @Required
	public void setBuildDependencyResolver(BuildRangeDependencyResolver buildDependencyResolver) {
		this.buildDependencyResolver = buildDependencyResolver;
	}

	/**
	 * @param plainDependencyResolver - a simple dependency resolver as configured
	 */
	@Configurable @Required
	public void setPlainDependencyResolver(DependencyResolver plainDependencyResolver) {
		this.plainDependencyResolver = plainDependencyResolver;
	}

	/**
	 * @param pomReader - a fully configured {@link ArtifactPomReader}
	 */
	@Configurable @Required
	public void setPomReader(ArtifactPomReader pomReader) {
		this.pomReader = pomReader;
	}
	/**
	 * @param condensedTerminalNames - a list with the names of the terminals to process 
	 */
	@Configurable @Required
	public void setCondensedTerminalNames(List<String> condensedTerminalNames) {
		this.condensedTerminalNames = condensedTerminalNames;
	}
		
	
	/**
	 * runs the extraction as configured
	 * @return - a Map of Name of terminal to list of CondensedName of dependencies 
	 */
	public Map<String,List<String>> runExtraction() {
		
		Map<String, List<String>> result = new HashMap<>();
				
		for (String condensedTerminalName : condensedTerminalNames) {
			result.put( condensedTerminalName, extractArtifacts( condensedTerminalName));
		}
		return result;
	}
		
	/**
	 * run an extract for a single terminal 
	 * @param condensedTerminalName - the qualified name of the terminal 
	 * @return - a list of dependencies as condensed names
	 */
	private List<String> extractArtifacts(String condensedTerminalName) {
		Dependency rangedDependency = NameParser.parseCondensedDependencyNameAndAutoRangify(condensedTerminalName);

		Set<Solution> solutions = plainDependencyResolver.resolve(rangedDependency);

		List<String> result = new ArrayList<>();
		if (solutions != null) {

			solutions.stream().forEach(solution -> {
								
				Solution terminal = pomReader.read(walkScopeId, solution);
				
				BuildRange buildRange = new BuildRange(terminal.getDependencies(), null, null);			
				BuildRangeDependencySolution set = buildDependencyResolver.resolve(buildRange);
				
				for (Solution s : set.getSolutions()) {
					String name = NameParser.buildName(s);
					result.add(name);
				}
				
			});
		}


		return result;
	}
	
}
