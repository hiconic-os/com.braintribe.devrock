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
package com.braintribe.devrock.test.analytics.commons.validator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.analytics.dependers.DependerAnalysisNode;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.devrock.model.repolet.content.Dependency;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.analysis.DependencyClash;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.resource.Resource;

/**
 * a validator for the most common cases - uses a {@link RepoletContent} as 'expectation description'.
 * Does all validations (and collects error messages) and ONLY asserts that at the last moment. 
 * Can be run in a deferred mode where you need to call {@link #assertResults()} to get the assertion. In standard mode, the 
 * assertion happens within {@link #validate(File, AnalysisArtifactResolution)}, {@link #validateExpressive(File, AnalysisArtifactResolution)} and
 * {@link #validateYaml(File, AnalysisArtifactResolution)}
 * @author pit
 *
 */
public class Validator {
	private List<String> assertionMessages = new ArrayList<>();
	private static Validator instance;
	private boolean defer = false;
	
	public static Validator instance() {
		if (instance == null) {
			instance = new Validator();
		}
		return instance;
	}
	
	/**
	 * standard constructor to get a Validator that asserts the findings directly after one of the main functions is called
	 */
	public Validator() {	
	}
	
	/**
	 * constructor to get a {@link Validator} that only asserts the results if called via {@link #assertResults()}, depending on the boolean
	 * @param defer  - true if no assertion should happen after the standard {@link AnalysisArtifactResolution} validation, false if so
	 */
	public Validator(boolean defer) {
		this.defer = defer;
	}
		
	/**
	 * simply add the fail message to the list of fail messages if !success
	 * @param failMessage - the message to start 
	 * @param success - if true, nothing happens otherwise the message is stored 
	 */
	public boolean assertTrue( String failMessage, boolean success) {
		if (!success) {
			assertionMessages.add(failMessage);
		}
		return success;
	}
		
	/**
	 * @param message - add the message (just as calling {@link #assertTrue(String, false)})
	 */
	public void insertAssertionMessage(String message) {
		assertionMessages.add(message);
	}
	
	/**
	 * asserts the result, i.e. if any assertion messages were accrued, the assertion fails. Clears the messages after call.
	 */
	public boolean assertResults() {
		if (!assertionMessages.isEmpty()) {
			Assert.fail("assertion failed : \n\t" + assertionMessages.stream().collect(Collectors.joining("\n\t")));
			assertionMessages.clear();
			return false;
		}		
		return true;
	}
	
	public static String collate(Collection<String> data) {
		return data.stream().collect( Collectors.joining(","));
	}
	
	public void validate( File file, AnalysisArtifactResolution resolution) {
		validate( file, resolution, false, false);
	}
	
	public void validateTerminal( File file, AnalysisArtifactResolution resolution) {
		validate( file, resolution, false, true);
	}
		
	/**
	 * standard {@link AnalysisArtifactResolution} validation. Automatically uses appropriate marshalling. If not deferred, it will assert the findings before returning
	 * @param file - a {@link RepoletContent} either as an expressive file (.txt) or as a YAML file (.yaml), 
	 * @param resolution - the {@link AnalysisArtifactResolution} as resulting from the resolver
	 * @param validateParts - true if the parts of the contained artifacts should be validated
	 * @param terminal - true if it's the terminal to be validated
	 */
	public void validate( File file, AnalysisArtifactResolution resolution, boolean validateParts, boolean terminal) {
		String name = file.getName().toLowerCase();
		if (name.endsWith( ".yaml")) {
			validateYaml(file, resolution, validateParts, terminal);
		}
		else if (name.endsWith( ".txt")){
			validateExpressive(file, resolution, validateParts, terminal);
		}
		else {
			throw new IllegalStateException("the file [" + file.getAbsolutePath() + "[ doesn't seem to be a know file format. Only .txt and .yaml are supported");
		}
		if (!defer) {
			assertResults();
		}
	}
	
	public void validateYaml( File file, AnalysisArtifactResolution resolution) {
		validateYaml(file, resolution, false, false);
	}
	
	/**
	 * standard {@link AnalysisArtifactResolution} validation. Uses YAML marshalling. If not deferred, it will assert the findings before returning 
	 * @param file - the {@link File} that contains the YAML formatted expectation {@link RepoletContent}
	 * @param resolution - the {@link AnalysisArtifactResolution} 
	 * @param terminal - true if the terminal is to be validated
	 */
	public void validateYaml( File file, AnalysisArtifactResolution resolution, boolean validateParts, boolean terminal) {
		try  {
			RepoletContent content = RepositoryGenerations.unmarshallConfigurationFile(file);
			validate( content, resolution, validateParts, terminal);			
		}
		catch (Exception e) {
			//Assert.fail("cannot parse validation YAML file [" + file.getAbsolutePath() + "] as " + e.getMessage());
			throw Exceptions.unchecked(e, "cannot parse validation file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		}
	}
	
	public void validateExpressive( File file, AnalysisArtifactResolution resolution) {
		validateExpressive(file, resolution,false, false);
	}
	
	/**
	 * standard {@link AnalysisArtifactResolution} validation. Automatically uses EXPRESSIVE marshalling. If not deferred, it will assert the findings before returning 
	 * @param file - the {@link File} that contains the EXPRESSIVE formatted expectation {@link RepoletContent}
	 * @param resolution - the {@link AnalysisArtifactResolution} 
	 * @param terminal 
	 */
	public void validateExpressive( File file, AnalysisArtifactResolution resolution, boolean validateParts, boolean terminal) {
		try {
			RepoletContent content = RepositoryGenerations.parseConfigurationFile(file);
			validate( content, resolution, validateParts, terminal);
		} catch (Exception e) {		
			throw Exceptions.unchecked(e, "cannot parse validation file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 
	}
	
	
	
	
		
	/**
	 * @param expectedArtifact - expected {@link Artifact} from the {@link RepoletContent}
	 * @param foundArtifact - the {@link AnalysisArtifact} from the resolution
	 */
	private void validate( Artifact expectedArtifact, AnalysisArtifact foundArtifact, boolean validateParts) {

		// ordering
		if (expectedArtifact.getOrder() != null) {
			assertTrue("expected order [" + expectedArtifact.getOrder() + "], but found [" + foundArtifact.getDependencyOrder() + "] for [" + expectedArtifact.asString() + "]", expectedArtifact.getOrder() == foundArtifact.getDependencyOrder());
		}
			
		// dependencies 
		validateDependencies(expectedArtifact, foundArtifact);				
		
		// parts
		if (validateParts) {
			validateParts(expectedArtifact, foundArtifact);
		}
	}

	/**
	 * validates the dependencies of an artifact 
	 * @param expectedArtifact - the expected {@link Artifact}
	 * @param foundArtifact - the found {@link AnalysisArtifact}
	 */
	private void validateDependencies(Artifact expectedArtifact, AnalysisArtifact foundArtifact) {
		List<AnalysisDependency> dependencies = foundArtifact.getDependencies();				
		
		List<com.braintribe.devrock.model.repolet.content.Dependency> missing = new ArrayList<>();
		List<AnalysisDependency> matching = new ArrayList<>();
		
		for (com.braintribe.devrock.model.repolet.content.Dependency expected : expectedArtifact.getDependencies()) {
			AnalysisDependency found = null;
			for (AnalysisDependency suspect : dependencies) {
				if (
						suspect.getGroupId().equals( expected.getGroupId()) &&
						suspect.getArtifactId().equals( expected.getArtifactId())
						
					) {
					AnalysisArtifact aa = suspect.getSolution();
					if (aa != null) {
						if (suspect.getSolution().getVersion().equals( expected.getVersion())) {
							found = suspect;
							break;
						}
					} else {
						System.out.println("unresolved dependency [" + suspect.asString() + "] ignoring it here");					
						if (suspect.getVersion().equals( expected.getVersion())) {
							found = suspect;
							break;
						}
					}
				}
			}
			if (found == null) {
				missing.add( expected);
			}
			else {				
				// post process found  
				if (validateDependency( foundArtifact, expected, found)) {
					matching.add( found);				
				}
				else {					
					missing.add(expected);
				}
			}			
		}
		String name = expectedArtifact.asString();
		assertTrue( "artifact : " + name + " -> missing dependencies [" + Collator.collateMissingDependencies( missing) + "]", missing.size() == 0);
		
		List<AnalysisDependency> excess = new ArrayList<>( dependencies);
		excess.removeAll( matching);		
		assertTrue( "artifact : " + name + " -> excess dependencies [" + Collator.collateDependencies( excess) + "]", excess.size() == 0);
	}
	
	/**
	 * @param foundArtifact 
	 * @param expected - the {@link Dependency} from the expectations 
	 * @param found - the {@link Dependency} as found
	 * @return - true if it matches, false otherwise
	 */
	private boolean validateDependency(AnalysisArtifact foundArtifact, Dependency expected, AnalysisDependency found) {
		// groupId, artifactId, version is already processed at this point
		String foundScope = found.getScope();
		String expectedScope = expected.getScope();
		if (expectedScope == null) {
			expectedScope = "compile";
		}
		
		
		boolean matchScope = assertTrue( "in [" + foundArtifact.asString() + "] dependencies don't match, expected scope for [" + expected.asString() + "] to be [" + expectedScope + "], found [" + found.asString() + "] with scope [" + foundScope + "]", expectedScope.equals(foundScope));		

		String foundClassifier = found.getClassifier();
		if (foundClassifier == null)
			foundClassifier = "";
		
		String expectedClassifier = expected.getClassifier();
		if (expectedClassifier == null) {
			expectedClassifier = "";
		}
		
		boolean matchClassifier = assertTrue( "in [" + foundArtifact.asString() + "] dependencies don't match, expected classifier for [" + expected.asString() + "] to be [" + expectedClassifier + "], found [" + found.asString() + "] with classifier [" + foundClassifier + "]", expectedClassifier.equals(foundClassifier));

		return matchScope && matchClassifier;
	}

	/**
	 * validates the parts of an artifact
	 * @param expectedArtifact - the expected {@link Artifact}
	 * @param foundArtifact - the found {@link AnalysisArtifact
	 */
	private void validateParts( Artifact expectedArtifact, AnalysisArtifact foundArtifact) {
		Map<String, Resource> expectedParts = expectedArtifact.getParts();
		
		Set<EqProxy<PartIdentification>> expected = new HashSet<>();
		expectedParts.keySet().stream().forEach( p -> {
			expected.add( HashComparators.partIdentification.eqProxy( PartIdentification.parse(p)));
		});
		
		// add the pom here.. 
		expected.add( HashComparators.partIdentification.eqProxy( PartIdentifications.pom));
		
		List<EqProxy<PartIdentification>> matching = new ArrayList<>();
		List<PartIdentification> excess = new ArrayList<>();
		
		Map<String, Part> foundParts = foundArtifact.getParts();
		for (Map.Entry<String, Part> entry : foundParts.entrySet()) {
			
			Part part = entry.getValue();
			EqProxy<PartIdentification> eqProxy = HashComparators.partIdentification.eqProxy( part);
			if (expected.contains( eqProxy)) {
				matching.add( eqProxy);
			}
			else {
				excess.add( part);
			}
		}
		List<EqProxy<PartIdentification>> missing = new ArrayList<>( expected);
		missing.removeAll( matching);
		
		assertTrue("artifact : " + foundArtifact.asString() + " -> missing parts [" + Collator.collatePartProxies(missing) + "]", missing.size() == 0); 
		
		assertTrue("artifact : " + foundArtifact.asString() + " -> excess parts [" + Collator.collateParts( excess) + "]", excess.size() == 0);
	}
	
	/**
	 * @param repoletContent - the expectation as {@link RepoletContent}
	 * @param resolution - the {@link AnalysisArtifactResolution} to check
	 * @param validateParts - true if parts need to be validated
	 * @param terminal - true if the it's the terminal to be validated.
	 */
	private  void validate( RepoletContent repoletContent, AnalysisArtifactResolution resolution, boolean validateParts, boolean terminal) {
		if (!terminal) {		
			validateSolutions(repoletContent, resolution, validateParts);
		}
		else {
			validateTerminal(repoletContent, resolution, validateParts);	
		}
	}
	/**
	 * validate a terminal
	 * @param repoletContent
	 * @param resolution
	 * @param validateParts
	 */
	private void validateTerminal(RepoletContent repoletContent, AnalysisArtifactResolution resolution, boolean validateParts) {
		List<Artifact> expectedArtifacts = repoletContent.getArtifacts();

		List<AnalysisTerminal> terminals = resolution.getTerminals();
		List<AnalysisArtifact> excess = new ArrayList<>();
		List<Artifact> matching = new ArrayList<>();

		for (AnalysisTerminal terminal : terminals) {
			if (terminal instanceof AnalysisArtifact) {
				AnalysisArtifact suspect = (AnalysisArtifact) terminal;		
				Artifact match = null;
				for (Artifact expected : expectedArtifacts) {
					if (
							suspect.getGroupId().equals( expected.getGroupId()) && 
							suspect.getArtifactId().equals( expected.getArtifactId()) &&
							suspect.getVersion().equals( expected.getVersion())
						) {
						match = expected;
						break;
					}				
				}
				if (match == null) {
					excess.add(suspect);
				}
				else {
					matching.add(match);
					validate( match, suspect, validateParts);	
				}											
			}
		}
		assertTrue( "unexpected excess found [" + Collator.collateAnalysisArtifacts(excess) + "]", excess.size() == 0);
		
		List<Artifact> missing = new ArrayList<>( expectedArtifacts);
		missing.removeAll( matching);
		
		assertTrue( "unexpected missing terminals found [" + Collator.collateArtifacts(missing) + "]", missing.size() == 0);
		
	}
	/**
	 * validate the solutions
	 * @param repoletContent
	 * @param resolution
	 * @param validateParts
	 */
	private void validateSolutions(RepoletContent repoletContent, AnalysisArtifactResolution resolution, boolean validateParts) {
		List<Artifact> expectedArtifacts = repoletContent.getArtifacts();
		List<AnalysisArtifact> solutions = resolution.getSolutions();
		
		// compare any artifacts..
		List<Artifact> missing = new ArrayList<>();
		List<AnalysisArtifact> matching = new ArrayList<>();
		for (Artifact expected : expectedArtifacts) {			
			AnalysisArtifact found = null;
			for (AnalysisArtifact suspect : solutions) {
				if (
						suspect.getGroupId().equals( expected.getGroupId()) && 
						suspect.getArtifactId().equals( expected.getArtifactId()) &&
						suspect.getVersion().equals( expected.getVersion())
					) {
					found = suspect;
					break;
				}
			}
			if (found == null) {
				missing.add(expected);
			}
			else {				
				matching.add(found);
				validate( expected, found, validateParts);							
			}			
		}
		
		assertTrue( "missing artifacts in solution set [" + Collator.collateArtifacts( missing) + "]", missing.size() == 0);		
		
		List<AnalysisArtifact> excess = new ArrayList<>( resolution.getSolutions());
		excess.removeAll( matching);
		assertTrue( "excess artifacts in solution set [" + Collator.collateAnalysisArtifacts( excess) + "]", excess.size() == 0);
	}
	
	/**
	 * validates the clash data as packaged in the resolution 
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @param replacementMap - a simple {@link Map} of the winning artifact's full name vs the loosing artifact's names
	 */
	public  void validateClashes(AnalysisArtifactResolution resolution, Map<String,List<String>> replacementMap) {
		List<DependencyClash> clashes = resolution.getClashes();
		
		// if replacement map isn't empty, we expect clashes
		if (replacementMap != null && replacementMap.size() > 0) {
			assertTrue("expected clashes, found none", clashes != null && !clashes.isEmpty());
		}
		else {
			assertTrue("expected no clashes, yet found some", clashes == null || clashes.isEmpty());
			return;
		}
		assertTrue("expected [" + replacementMap.size() + "] clashes, found [" + clashes.size() + "]", replacementMap.size() == clashes.size());
		
		Map<AnalysisArtifact, List<AnalysisArtifact>> winnerToLoserMap = new HashMap<>(); 
		
		for (DependencyClash clash : clashes) {
			AnalysisArtifact winnerArtifact = clash.getSolution();
			AnalysisDependency winingDependency = clash.getSelectedDependency();
			
			assertTrue( "winning dependency is not among the dependers of the winning solution", winnerArtifact.getDependers().contains(winingDependency));
			assertTrue( "winning dependency is not among the involved dependencies", clash.getInvolvedDependencies().contains(winingDependency));
					
			for (AnalysisDependency dependency : clash.getInvolvedDependencies()) {
				if (dependency == winingDependency)
					continue;
				
				// 
				AnalysisArtifact replacedArtifact = clash.getReplacedSolutions().get(dependency);
				assertTrue("no replacement artifact for losing dependency [" + dependency.asString() + "]", replacedArtifact != null);
				List<AnalysisArtifact> losingArtifacts = winnerToLoserMap.computeIfAbsent( winnerArtifact, w -> new ArrayList<>());
				losingArtifacts.add(replacedArtifact);			
			}						
		}
		
		for (Map.Entry<AnalysisArtifact, List<AnalysisArtifact>> entry : winnerToLoserMap.entrySet()) {
			AnalysisArtifact winner = entry.getKey();
			
			List<AnalysisArtifact> losers = entry.getValue();
			List<String> foundLoserValues = losers.stream().map( a -> a.asString()).collect(Collectors.toList());
			List<String> expectedLoserValues = replacementMap.get( winner.asString());
			
			assertTrue("expected [" + expectedLoserValues.size() + "]losers for [" + winner.asString() + "], found [" + foundLoserValues.size() + "]", foundLoserValues.size() == expectedLoserValues.size());
			
			List<String> matching = new ArrayList<>();
			List<String> excess = new ArrayList<>();
			for (String found : foundLoserValues) {
				if (expectedLoserValues.contains( found)) {
					matching.add(  found);
				}
				else {
					excess.add( found);
				}
			}
			List<String> missing = new ArrayList<>( expectedLoserValues);
			missing.removeAll( matching);
			
		
			assertTrue( "missing [" + Collator.collateNames( missing) + "] in [" + Collator.collateNames( foundLoserValues) + "]", missing.size() == 0);
			assertTrue( "unexpected [" + Collator.collateNames( excess) + "] in [" + Collator.collateNames( foundLoserValues) + "]", excess.size() == 0);
		}
	}
	
	/**
	 * validates a failed resolution (only if flagged, and checks incomplete artifacts and unresolved dependencies, no check on reasons yet
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @param expectedIncompleteArtifacts - a {@link List} of qualified artifact names that should've been returned as incomplete
	 * @param expectedUnresolvedDependencies - a {@link List} of qualified dependency names that should've been returned as unresolved
	 */
	public void validateFailedResolution( AnalysisArtifactResolution resolution, List<String> expectedIncompleteArtifacts, List<String> expectedUnresolvedDependencies) {
	
		boolean foundFailure = resolution.getFailure() != null;
		boolean expectedFailure = (expectedIncompleteArtifacts != null && !expectedIncompleteArtifacts.isEmpty()) || 
								  (expectedUnresolvedDependencies != null && !expectedUnresolvedDependencies.isEmpty());
		
		assertTrue( "expected " + (expectedFailure ? "a " : "no ") + "failure, yet found " + (foundFailure ? "a " : "no ") + "failure", foundFailure == expectedFailure);
		
		// incomplete artifacts
		if (expectedIncompleteArtifacts != null && !expectedIncompleteArtifacts.isEmpty()) {
			assertTrue( "expected [" + expectedIncompleteArtifacts.size() + "] incomplete artifacts, yet found [" + resolution.getIncompleteArtifacts().size()+ "] incomplete artifacts", resolution.getIncompleteArtifacts().size() == expectedIncompleteArtifacts.size());
			List<String> foundIncompleteArtifacts = resolution.getIncompleteArtifacts().stream().map( a -> a.asString()).collect( Collectors.toList());
			
			List<String> matching = new ArrayList<>();
			List<String> excess = new ArrayList<>();
			
			for (String found : foundIncompleteArtifacts) {
				if (expectedIncompleteArtifacts.contains( found)) {
					matching.add(found);
				}
				else {
					excess.add(found);
				}
			}
			List<String> missing = new ArrayList<>( expectedIncompleteArtifacts);
			missing.removeAll( matching);
			
		
			assertTrue( "missing incomplete artifact [" + Collator.collateNames( missing) + "] in [" + Collator.collateNames( foundIncompleteArtifacts) + "]", missing.size() == 0);
			assertTrue( "unexpected incomplete artifact [" + Collator.collateNames( excess) + "] in [" + Collator.collateNames( foundIncompleteArtifacts) + "]", excess.size() == 0);									
		}
		else {
			assertTrue( "found [" + resolution.getIncompleteArtifacts().size() + "] unexpected incomplete artifacts : [" + Collator.collateUnexpectedAnalysisArtifacts( resolution.getIncompleteArtifacts()) + "]", resolution.getIncompleteArtifacts().size() == 0);
		}
		
		// unresolved dependencies
		if (expectedUnresolvedDependencies != null && !expectedIncompleteArtifacts.isEmpty()) {
			assertTrue( "expected [" + expectedUnresolvedDependencies.size() + "] unresolved dependencies, yet found [" + resolution.getUnresolvedDependencies().size()+ "] unresolved dependencies", resolution.getUnresolvedDependencies().size() == expectedUnresolvedDependencies.size());
			List<String> foundUnresolvedDependencies = resolution.getUnresolvedDependencies().stream().map( a -> a.asString()).collect( Collectors.toList());
			
			List<String> matching = new ArrayList<>();
			List<String> excess = new ArrayList<>();
			
			for (String found : foundUnresolvedDependencies) {
				if (expectedUnresolvedDependencies.contains( found)) {
					matching.add(found);
				}
				else {
					excess.add(found);
				}
			}
			List<String> missing = new ArrayList<>( expectedUnresolvedDependencies);
			missing.removeAll( matching);			
		
			assertTrue( "missing unresolved dependency [" + Collator.collateNames( missing) + "] in [" + Collator.collateNames( foundUnresolvedDependencies) + "]", missing.size() == 0);
			assertTrue( "unexpected unresolved dependency [" + Collator.collateNames( excess) + "] in [" + Collator.collateNames( foundUnresolvedDependencies) + "]", excess.size() == 0);												
		}
		else {
			assertTrue( "found [" + resolution.getUnresolvedDependencies().size() + "] unexpected unresolved dependencies", resolution.getUnresolvedDependencies().size() == 0);
		}		
	}
	
	/**
	 * asserts whether the passed files do exist or not - depending on the second value of the {@link Pair}
	 * @param filePairings - an array of {@link Pair} of the {@link File} and a {@link Boolean} whether it's expected to exist or not
	 */
	public void validateFileExistance( List<Pair<File,Boolean>> filePairings) {
		if (filePairings == null)
			return;
		for (Pair<File,Boolean> pair : filePairings) {
			validateFileExistance(pair);
		}
	}
	
	/**
	 * asserts whether the passed file does exist or not - depending on the second value of the {@link Pair}
	 * @param filePairings - a{@link Pair} of the {@link File} and a {@link Boolean} whether it's expected to exist or not
	 */
	public void validateFileExistance( Pair<File, Boolean> pair) {
		File file = pair.first;
		if (pair.second) {
			assertTrue( "File [" + file.getAbsolutePath() + "] doesn't exist", file.exists());				
		}
		else {
			assertTrue( "File [" + file.getAbsolutePath() + "] exist", !file.exists());
		}		
	}
	
	/**
	 * asserts the presence and ordering of solutions
	 * @param found - the {@link AnalysisArtifact} found (by the resolution)
	 * @param expected - the {@link AnalysisArtifact} expected (by the test)
	 */
	public void validateSolutionOrdering( List<AnalysisArtifact> found, List<AnalysisArtifact> expected) {
		Map<EqProxy<VersionedArtifactIdentification>, AnalysisArtifact> expectations = new HashMap<>();
		expected.stream().forEach( aa -> {
			EqProxy<VersionedArtifactIdentification> eqProxy = HashComparators.versionedArtifactIdentification.eqProxy(aa);			
			expectations.put(eqProxy, aa);
		});
		
		List<AnalysisArtifact> excess = new ArrayList<>();
		List<AnalysisArtifact> matching = new ArrayList<>();
		for (AnalysisArtifact foundArtifact : found) {
			AnalysisArtifact expectedArtifact = expectations.get(HashComparators.versionedArtifactIdentification.eqProxy(foundArtifact));
			if (expectedArtifact != null) {

				matching.add(expectedArtifact);
				
				int expectedVisitOrder = expectedArtifact.getVisitOrder();
				int foundVisitOrder = foundArtifact.getVisitOrder();
				assertTrue( "expected visit order for [" + expectedArtifact.asString() + "] is [" + expectedVisitOrder + "], but found [" + foundVisitOrder + "]", expectedVisitOrder == foundVisitOrder);
				
				int expectedDependencyOrder = expectedArtifact.getDependencyOrder();
				int foundDependencyOrder = foundArtifact.getDependencyOrder();
				assertTrue( "expected dependency order for [" + expectedArtifact.asString() + "] is [" + expectedDependencyOrder + "], but found [" + foundDependencyOrder + "]", expectedDependencyOrder == foundDependencyOrder);			
			}
			else {
				excess.add(foundArtifact);
			}
		}
		
		// missing
		List<AnalysisArtifact> missing = new ArrayList<>( expected);
		missing.removeAll(matching);
		
		assertTrue( "missing solutions [" + Collator.collateAnalysisArtifacts( missing) + "] in [" + Collator.collateAnalysisArtifacts(found) + "]", missing.size() == 0);
		assertTrue( "excess solutions [" + Collator.collateAnalysisArtifacts( excess) + "] in [" + Collator.collateAnalysisArtifacts(found) + "]", excess.size() == 0);
		
	}
		
	
	public void validate( com.braintribe.model.artifact.consumable.Artifact source, com.braintribe.model.artifact.consumable.Artifact target, boolean expectHashes) {
		assertTrue("expected target to be [" + source.asString() + "] yet found [" + target.asString() + "]", source.asString().equals( target.asString()));
		
		List<String> sourceKeys = source.getParts().keySet().stream().collect(Collectors.toList());
		
		List<String> targetKeys = target.getParts().keySet().stream().collect(Collectors.toList());
		
		List<String> missingKeys = new ArrayList<>();
		List<String> matchingKeys = new ArrayList<>();
		
		for (String sourceKey : sourceKeys) {
			if (!targetKeys.contains( sourceKey)) {
				missingKeys.add( sourceKey);
			} 
			matchingKeys.add( sourceKey);

			if (expectHashes) {
				// md5			
				String md5Key = sourceKey + ".md5";
				if (!targetKeys.contains( md5Key)) {
					missingKeys.add( md5Key);				
				}
				else {
					matchingKeys.add( md5Key);
				}
				// sha1
				String sha1Key = sourceKey + ".sha1";
				if (!targetKeys.contains( sha1Key)) {
					missingKeys.add( sha1Key);				
				}
				else {
					matchingKeys.add( sha1Key);
				}
				// sha256
				String sha256Key = sourceKey + ".sha256";
				if (!targetKeys.contains( sha256Key)) {
					missingKeys.add( sha256Key);				
				}
				else {
					matchingKeys.add( sha256Key);
				}				
			}
		}
		
		assertTrue( "missing : " + missingKeys.stream().collect( Collectors.joining(",")), missingKeys.size() == 0);
		
		List<String> excessKeys = new ArrayList<>( targetKeys);
		excessKeys.removeAll( matchingKeys);
		
		assertTrue( "excess : " + excessKeys.stream().collect( Collectors.joining(",")), excessKeys.size() == 0);
	}

	
	/**
	 * validate two lists of reasons: both must have the same length to be valid
	 * @param foundReasons
	 * @param expectedReasons
	 * @return
	 */
	public boolean validateReasons( List<Reason> foundReasons, List<Reason> expectedReasons) {
		boolean mainResult = true;
		int foundSize = foundReasons.size();
		int expectedSize = expectedReasons.size();
		
		mainResult = assertTrue( "expected same number of attached reasons, yet [found : " + foundSize + ", expected : " + expectedSize + "]", foundSize == expectedSize);
		if (mainResult == false) {
			return false;
		}
		
		for (int i = 0; i < foundSize; i++) {
			Reason foundReason = foundReasons.get(i);
			Reason expectedReason = expectedReasons.get(i);
			boolean result = validateReason( foundReason, expectedReason);
			if (result == false) {
				mainResult = false;
			}
		}
		return mainResult;
	}
	
	/**
	 * validate two reasons: themselves and their children.. 
	 * @param foundReason - the {@link Reason} to check, 
	 * @param expectedReason -  the {@link Reason} to act as comparison
	 * @return - true if both match 
	 */
	public boolean validateReason( Reason foundReason, Reason expectedReason) {
		boolean mainResult = true;
		if (foundReason != expectedReason) {
			EntityType<GenericEntity> foundType = foundReason.entityType();
			EntityType<GenericEntity> expectedType = expectedReason.entityType();			
			boolean result = assertTrue( "expected to find type [" + expectedType.getShortName() + "], yet found [" + foundType.getShortName() + "]", foundType == expectedType);
			if (result == false) {
				mainResult = false;
			}
		}		
		boolean result = validateReasons( foundReason.getReasons(), expectedReason.getReasons());
		if (result == false) {
			mainResult = false;
		}
		
		return mainResult;
		
	}

	public void validate(List<AnalysisArtifact> solutions, List<String> expectedNames) {
		List<String> finds = solutions.stream().map( s -> s.asString()).collect(Collectors.toList());
		List<String> excess = new ArrayList<>( finds.size());
		List<String> matches = new ArrayList<>( expectedNames.size());
		for (String found : finds) {
			if (expectedNames.contains(found)) {
				matches.add( found);
			}
			else {
				excess.add( found);
			}
		}
		
		List<String> missing = new ArrayList<>(expectedNames);
		missing.removeAll(matches);
		
		assertTrue( "missing solutions [" + collate(missing) + "]", missing.size() == 0);
		assertTrue( "excess solutions [" + collate(excess) + "]", excess.size() == 0);
		
	}
	
	/**
	 * simply turns the {@link List} of {@link String} into a {@link String} concatenated by a comma
	 * @param strs
	 * @return
	 */
	public static String toString( List<String> strs) {
		return strs.stream().collect(Collectors.joining(","));
	}

	
	/**
	 * validates output of the ReverseDependencyAnalyzer.. expects the nodes to be in correct order 
	 * @param found - the {@link List} of {@link DependerAnalysisNode}s as returned by the analyzer
	 * @param expected - the {@link List} of {@link DependerAnalysisNode} as expected
	 */
	public void validateReverseDependencies(List<DependerAnalysisNode> found, List<DependerAnalysisNode> expected) {
		assertTrue( "expected [" + expected.size() + "] nodes, but found [" + found.size() + "]", expected.size() == found.size());
		
		List<Pair<DependerAnalysisNode, DependerAnalysisNode>> mismatches = new ArrayList<>();
		for (int i = 0; i < expected.size(); i++) {
			DependerAnalysisNode foundNode = found.get(i);
			DependerAnalysisNode expectedNode = expected.get(i);
			
			if (!validate( foundNode, expectedNode)) {
				mismatches.add( Pair.of( foundNode, expectedNode));
			}
		}
		
	}

	/**
	 * compares to {@link DependerAnalysisNode}s
	 * @param foundNode - the {@link DependerAnalysisNode} as returned by the analyzer
	 * @param expectedNode - the {@link DependerAnalysisNode} as expected 
	 * @return - true if they match
	 */
	private boolean validate(DependerAnalysisNode foundNode, DependerAnalysisNode expectedNode) {
		
		boolean mismatch = false;
		ArtifactIdentification fi = foundNode.getInitialArtifactIdentification();
		ArtifactIdentification ei = expectedNode.getInitialArtifactIdentification();
		if (fi != null && ei != null) {
		
			boolean matches = ei.compareTo( fi) == 0;
			assertTrue("initial AI : expected [" + ei + "], but found [" + fi + "]", matches);
			if (!matches) {
				mismatch = true;
			}
		}
		else if (ei != null && fi == null) {
				assertTrue("expected [" + ei + "], but found [null]", false);
				mismatch = true;
		}
		else if (ei == null && fi != null) {
			assertTrue("expected [null], but found [" + fi + "]", false);
			mismatch = true;
		}
		
		
		
		CompiledDependencyIdentification fcd = foundNode.getReferencingDependency();
		CompiledDependencyIdentification ecd = expectedNode.getReferencingDependency();
		
		if (fcd != null && ecd != null) {
			String fcds = fcd.asString();
			String ecds = ecd.asString();
			
			boolean matches = ecds.equals(fcds);
			assertTrue("referencing dependency: expected [" + ecds + "], but found [" + fcds + "]", matches);
			if (!matches) {
				mismatch = true;
			}						
		} 
		else if (ecd != null && fcd == null) {
			assertTrue("referencing dependency: expected [" + ecd.asString() + "], but found [null]", false);
			mismatch = true;
		}
		else if (ecd == null && fcd != null) {
			assertTrue("referencing dependency: expected [null], but found [" + fcd.asString() + "]", false);
			mismatch = true;
		}
		
		VersionedArtifactIdentification fvai = foundNode.getVersionedArtifactIdentification();
		VersionedArtifactIdentification evai = expectedNode.getVersionedArtifactIdentification();
		
		if (fvai != null && evai != null) {
			String fvais = fvai.asString();
			String evais = evai.asString();			
			boolean matches = evais.equals(fvais);
			assertTrue("referencing artifact: expected [" + evais + "], but found [" + fvais + "]", matches);
			if (!matches) {
				mismatch = true;
			}						
		} 
		else if (evai != null && fvai == null) {
			assertTrue("referencing artifact: expected [" + evai.asString() + "], but found [null]", false);
			mismatch = true;
		}
		else if (evai == null && fvai != null) {
			assertTrue("referencing artifact: expected [null], but found [" + fvai.asString() + "]", false);
			mismatch = true;
		}
	
		return mismatch;
	}
	
}
