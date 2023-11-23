package com.braintribe.build.artifacts.mc.wire.repositoryExtract.space;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.braintribe.build.artifact.api.RangedArtifact;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.contract.ExternalConfigurationContract;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class FilterConfigurationSpace implements FilterConfigurationContract {
	private static Logger log = Logger.getLogger(FilterConfigurationSpace.class);
	
	@Import
	ExternalConfigurationContract externalConfiguration;
	
	@Override
	public Predicate<? super Solution> solutionFilter() {
		// filter the solutions with the configured global exclusions
		return this::isExcluded;
	}

	@Override
	public Predicate<? super Dependency> dependencyFilter() {
		// filter the dependency with the correct scope and optional flag
		return this::isIncluded;
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
	
	
	private boolean isExcluded(Solution solution) {
		return isExcluded(NameParser.buildName(solution));
	}
	
	
	
	private boolean isExcluded(String qualifiedName) {
		
		if (externalConfiguration.globalExclusions() != null && !externalConfiguration.globalExclusions().isEmpty()) {
			for (Pattern p : externalConfiguration.globalExclusions()) {
				if (p.matcher(qualifiedName).matches()) {
					log.debug("Excluding artifact "+qualifiedName+" because it is globally excluded.");
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

	@Override
	public Predicate<? super RangedArtifact> artifactFilter() {	
		return s -> true;
	}

	@Override
	public Collection<PartTuple> partExpectation() {
		return null;
	}

	
}
