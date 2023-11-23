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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationListener;
import com.braintribe.build.artifact.walk.multi.clash.listener.ClashResolverNotificationListener;
import com.braintribe.build.artifact.walk.multi.clash.merger.listener.DependencyMergerNotificationListener;
import com.braintribe.build.artifact.walk.multi.listener.WalkNotificationListener;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;

public class Monitor implements WalkNotificationListener, PomReaderNotificationListener, ClashResolverNotificationListener, DependencyMergerNotificationListener, DependencyResolverNotificationListener, SolutionEnricherNotificationListener {
	
	private Set<Set<Dependency>> merges = new HashSet<Set<Dependency>>();
	private Map<Dependency, List<Dependency>> dependencyClashMap = new HashMap<Dependency, List<Dependency>>();
	private Set<List<Solution>> solutionClashes = new HashSet<List<Solution>>();
	private Set<Dependency> unresolvedDependencies = new HashSet<Dependency>();
	private Set<Dependency> unassignedDependencies = new HashSet<Dependency>();
	private Map<Dependency, Dependency> reassignedDependencyMap = new HashMap<Dependency, Dependency>();
	private Solution startedSolution;
	private Solution endedSolution;
	private Set<PomReaderNotificationTuple> pomReaderNotifications = new HashSet<PomReaderNotificationTuple>();
	private Map<Artifact, Artifact> redirectionMap = new HashMap<Artifact, Artifact>();
	
	private boolean verbose;
	
	public void setVerbosity(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public void acknowledgeMerges(String walkScopeId, Set<Dependency> mergedDependencies) {
		merges.add(mergedDependencies);
		if (verbose) {
			System.out.println("merged:");
			for (Dependency dependency : mergedDependencies) {
				System.out.println("\t" + NameParser.buildName(dependency));
			}
		}
	}
	
	

	@Override
	public void acknowledgeCollectedDependencies(String walkScopeId, List<Dependency> collectedDependencies) {	
	}

	@Override
	public void acknowledgeDependencyClashes(String walkScopeId, Dependency dependency, List<Dependency> clashes) {
		dependencyClashMap.put(dependency, clashes);
		if (verbose) {
			System.out.println("dependency clashes on " + NameParser.buildName(dependency)); 
			for (Dependency suspect : clashes) {
				System.out.println("\t" + NameParser.buildName( suspect));
			}
		}
	}

	@Override
	public void acknowledgeSolutionClashes(String walkScopeId, Solution solution, List<Solution> clashes) {
		solutionClashes.add(clashes);
		if (verbose) {
			System.out.println("solution clashes on :" + NameParser.buildName(solution));
			for (Solution suspect : clashes) {
				System.out.println( "\t" + NameParser.buildName(suspect));
			}
		}
	}

	@Override
	public void acknowledgeReadErrorOnFile(String walkScopeId, String location, String reason) {
		PomReaderNotificationTuple tuple = new PomReaderNotificationTuple();
		tuple.location = location;
		tuple.reason = reason;
		pomReaderNotifications.add(tuple);
		if (verbose) {
			System.out.println( "read error:" + reason + " in " + location);
		}
	}
	

	@Override
	public void acknowledgeVariableResolvingError(String walkScopeId, Artifact artifact, String expression) {
		PomReaderNotificationTuple tuple = new PomReaderNotificationTuple();
		tuple.location = NameParser.buildName(artifact, artifact.getVersion());
		tuple.reason = expression;
		pomReaderNotifications.add(tuple);
		if (verbose) {
			System.out.println( "variable resolving error:" + expression + " in " + tuple.location);
		}	
	}

	@Override
	public void acknowledgeSolutionAssociation(String walkScopeId, String location, Artifact solution) {
		if (verbose) {
			System.out.println( "associating [" + location + "] with [" + NameParser.buildName(solution, solution.getVersion()) + "]");			
		}		
	}
	
	
	
	@Override
	public void acknowledgeReadErrorOnArtifact(String walkScopeId, Artifact artifact, String reason) {	
		PomReaderNotificationTuple tuple = new PomReaderNotificationTuple();
		tuple.location = NameParser.buildName(artifact, artifact.getVersion());
		tuple.reason = reason;
		pomReaderNotifications.add(tuple);
		if (verbose) {
			System.out.println( "read error:" + reason + " in " + tuple.location);
		}
	}

	@Override
	public void acknowledgeReadErrorOnString(String walkScopeId, String contents, String reason) {
		PomReaderNotificationTuple tuple = new PomReaderNotificationTuple();
		tuple.location = contents;
		tuple.reason = reason;
		pomReaderNotifications.add(tuple);
		if (verbose) {
			System.out.println( "read error:" + reason + " in " + tuple.location);
		}
		
	}

	@Override
	public void acknowledgeStartOn(String walkScopeId, Solution solution, WalkDenotationType denotationType) {
		startedSolution = solution;
		if (verbose) {
			System.out.println("started on :" + NameParser.buildName(solution));
		}
	}

	@Override
	public void acknowledgeEndOn(String walkScopeId, Solution solution) {
		endedSolution = solution;
		if (verbose) {
			System.out.println("ended on :" + NameParser.buildName(solution));
		}
	}
	

	@Override
	public void acknowledgeWalkResult(String walkScopeId, List<Solution> solutions) {
		
	}

	

	@Override
	public void acknowledgeTraversing(String walkScopeId, Solution solution, Dependency parent, int level, boolean isvalid) {	
		if (verbose) {
			System.out.println( "traversing solution : " + NameParser.buildName(solution) + "@" + level + "-> valid:" + isvalid);			
		}
	}
	

	@Override
	public void acknowledgeTraversing(String walkScopeId, Dependency dependency, Solution parent, int level) {
		if (verbose) {
			System.out.println( "traversing dependency : " + NameParser.buildName( dependency) + "@" + level);			
		}		
	}

	@Override
	public void acknowledgeTraversingEndpoint(String walkScopeId, Dependency dependency, Solution parent, int level) {
		System.out.println( "traversion endpoint reached - cached : " + NameParser.buildName( dependency) + "@" + level);
	}
	

	@Override
	public void acknowledgeTraversingEndpoint(String walkScopeId, Solution solution, Dependency parent, int level) {
		System.out.println( "traversion endpoint reached - cached : " + NameParser.buildName( solution) + "@" + level);
		
	}

	@Override
	public void acknowledgeUnresolvedDependency(String walkScopeId, Dependency dependency) {		
		unresolvedDependencies.add(dependency);
		if (verbose) {
			System.out.println("Unresolved : " + NameParser.buildName(dependency));
		}
	}

	@Override
	public void acknowledgeReassignedDependency(String walkScopeId, Dependency undetermined, Dependency replacement) {
		reassignedDependencyMap.put(undetermined, replacement);
		if (verbose) {
			System.out.println( "reassigned " + NameParser.buildName(undetermined) + " to " + NameParser.buildName(replacement));
		}
		
	}

	@Override
	public void acknowledgeUndeterminedDependency(String walkScopeId, Dependency undetermined) {
		unassignedDependencies.add(undetermined);
		if (verbose) {
			System.out.println("Unassigned : " + NameParser.buildName( undetermined));
		}
	}
	
	@Override
	public void acknowledgeRedirection(String walkScopeId, Part source, Solution target) {		
		redirectionMap.put(source, target);
		if (verbose) {		
			System.out.println("redirection :" + NameParser.buildName( source) + "->" + NameParser.buildName( target));
		}
	}
	
	

	// access 
	@Override
	public void acknowledgeImportAssociation(String walkScopeId, Artifact requestingSolution, Solution requestedSolution) {
		if (verbose) {			
			System.out.println("import relation :" + NameParser.buildName( requestingSolution) + " -> " + NameParser.buildName( requestedSolution));			
		}		
		
	}
	
	@Override
	public void acknowledgeImportAssociationError(String walkScopeId, Artifact requestingSolution, String groupId, String artifactId, String version) {
		System.err.println( "failed import relation : " + NameParser.buildName(requestingSolution) + " -> " + groupId + ":" + artifactId + "#" + version);
		
	}
	
	@Override
	public void acknowledgeParentAssociation(String walkScopeId, Artifact child, Solution parent) {
		if (verbose) {			
			System.out.println("parent relation :" + NameParser.buildName( child) + "->" + NameParser.buildName( parent));			
		}		
	}

	
	@Override
	public void acknowledgeParentAssociationError(String walkScopeId, Artifact child, String groupId, String artifactId, String version) {	
		System.err.println( "failed parent relation : " + NameParser.buildName(child) + " -> " + groupId + ":" + artifactId + "#" + version);
	}

	@Override
	public void acknowledgeFileEnrichmentSuccess(String walkScopeId, String file) {
		if (verbose) {
			System.out.println("successfully enriched : " + file);
		}		
	}

	@Override
	public void acknowledgeFileEnrichmentFailure(String walkScopeId, Solution solution, PartTuple tuple) {
		if (verbose) {
			System.out.println("failed to enrich part " + PartTupleProcessor.toString( tuple) + " for " + NameParser.buildName(solution) + "");
		}		
	}

	public Set<Set<Dependency>> getMerges() {
		return merges;
	}

	public Map<Dependency, List<Dependency>> getDependencyClashes() {
		return dependencyClashMap;
	}

	public Set<List<Solution>> getSolutionClashes() {
		return solutionClashes;
	}

	public Set<Dependency> getUnresolvedDependencies() {
		return unresolvedDependencies;
	}

	public Set<Dependency> getUnassignedDependencies() {
		return unassignedDependencies;
	}

	public Map<Dependency, Dependency> getReassignedDependencies() {
		return reassignedDependencyMap;
	}

	public Solution getStartedSolution() {
		return startedSolution;
	}

	public Solution getEndedSolution() {
		return endedSolution;
	}

	public Set<PomReaderNotificationTuple> getPomReaderNotifications() {
		return pomReaderNotifications;
	}

	@Override
	public void acknowledgeSolutionEnriching(String walkScopeId, Solution solution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acknowledgeDependencyClashResolving(String walkScopeId, Dependency dependency) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acknowledgeSolutionClashResolving(String walkScopeId, Solution solution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acknowledgeTraversingPhase(String walkScopeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acknowledgeDeterminationPhase(String walkScopeId, int numUndetermined) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acknowledgeDependencyClashResolvingPhase(String walkScopeId, int nunDependencies) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acknowledgeSolutionClashResolvingPhase(String walkScopeId, int numSolutions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acknowledgeEnrichingPhase(String walkScopeId, int numSolutions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acknowledgeClashOnDependencyClassifier(String walkScopeId, Dependency dependency, String current,
			String requested) {
		// TODO Auto-generated method stub
		
	}


	

	
	
}
