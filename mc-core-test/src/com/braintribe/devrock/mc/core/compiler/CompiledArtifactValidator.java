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
package com.braintribe.devrock.mc.core.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.version.Version;

/**
 * helper to validate {@link CompiledArtifact}s
 * @author pit
 *
 */
public interface CompiledArtifactValidator {

	/**
	 * @param ca - {@link CompiledArtifact}
	 */
	public void validate( CompiledArtifact ca);
	
	/**
	 * @param tag - a string to tag the output 
	 * @param expected - the expected value 
	 * @param found - the found value
	 */
	default void validate(String tag, String expected, String found) {
		if (expected != null) {
			Assert.assertTrue( tag + ": expected [" + expected + "], found [" + found + "]", expected.equals( found));
		}
		else {
			Assert.assertTrue( tag + ": expected [null], found [" + found + "]", found != null);
		}	
	}
	
	/**
	 * @param tag - a sting to tag the output
	 * @param expected - the expected {@link Version}
	 * @param found - the found {@link Version}
	 */
	default void validate (String tag, Version expected, Version found) {
		if (expected != null) {
			Assert.assertTrue( tag + ": expected [" + expected.asString() + "], found [" + found.asString() + "]", expected.compareTo( found) == 0);
		}
		else {
			Assert.assertTrue( tag + ": expected [null], found [" + found + "]", found != null);
		}
	}
	
	/**
	 * validate artifact identification properties
	 * @param ca - the {@link CompiledArtifact}
	 * @param cai - the expected {@link CompiledArtifactIdentification}
	 */
	default void validate(CompiledArtifact ca, CompiledArtifactIdentification cai) {
		validate( "groupId", cai.getGroupId(), ca.getGroupId());
		validate( "artifactId", cai.getArtifactId(), ca.getArtifactId());
		validate( "version", cai.getVersion(), ca.getVersion());
	}
	
	/**
	 * collate property test results to string 
	 * @param expected - the expected {@link Map}
	 * @param found - the found {@link Map}
	 * @param keys - a {@link List} of non-matching keys
	 * @return - a {@link String} representation 
	 */
	default String collate(Map<String,String> expected, Map<String,String> found, List<String> keys) {
		return keys.stream().map( k -> {
			String eV = expected.get(k);
			String fV = found.get( k);
			return k + ":" + eV + " vs " + fV; 
		}).collect( Collectors.joining(","));
	}
	
	
	/**
	 * validates properties 
	 * @param ca - the {@link CompiledArtifact} to test 
	 * @param expected - a {@link Map} of expected properties
	 */
	default void validateProperties( CompiledArtifact ca, Map<String, String> expected) {
		Map<String, String> found = ca.getProperties();
		List<String> matchingKeys = new ArrayList<>();
		List<String> nonMatchingKeys = new ArrayList<>();
		List<String> missingKeys = new ArrayList<>();
		for (Map.Entry<String, String> entry : expected.entrySet()) {
			String key = entry.getKey();
			String foundValue = found.get( key);
			if (foundValue == null) {
				missingKeys.add( key);
			}
			else if (!foundValue.equalsIgnoreCase( entry.getValue())) {
				nonMatchingKeys.add( key);				
			}
			else {
				matchingKeys.add( key);
			}
		}
		Assert.assertTrue("mismatches: [" + collate( expected, found, nonMatchingKeys) + "]", nonMatchingKeys.size() == 0);
		Assert.assertTrue("missing: [" + missingKeys.stream().collect(Collectors.joining(",")) + "]", missingKeys.size() == 0);
		
		Collection<String> foundKeys = found.keySet();	
		List<String> excessKeys = foundKeys.stream().filter( k -> !expected.containsKey(k)).collect(Collectors.toList());
		excessKeys.removeAll( Arrays.asList( "global-dominants","global-exclusions", "artifact-redirects"));
		Assert.assertTrue("excess found: [" + excessKeys.stream().collect(Collectors.joining(",")) + "]", excessKeys.size() == 0);
	}
	
	/**
	 * @param expected
	 * @param found
	 * @return
	 */
	default boolean validateDependency( CompiledDependency expected, CompiledDependency found) {
		// already know that groupId, artifactId and version match (otherwise it would be declared missing by caller)
		// so, only need to test classifier & type
		if (expected.getClassifier() != null) {
			if (!expected.getClassifier().equals( found.getClassifier())) {
				return false;
			}
		}
		else if (found.getClassifier() != null) {
				return false;
		}
		
		if (expected.getType() != null) {
			if (!expected.getType().equals( found.getType())) {
				return false;
			}
		}
		else if (found.getType() != null) {
			return false;
		}
		return true;
	}
	
	default CompiledDependency findMatch( List<CompiledDependency> suspects, CompiledDependency key) {
		for (CompiledDependency suspect : suspects) {
			if (!key.getGroupId().equals(suspect.getGroupId()))
					continue;
			if (!key.getArtifactId().equals(suspect.getArtifactId()))
				continue;
			if (!key.getVersion().asString().equals( suspect.getVersion().asString())) 
				continue;
			return suspect;			
		}
		return null;
	}
	
	default void validateDependencies( CompiledArtifact ca, List<CompiledDependency> expected) {
		List<CompiledDependency> found = ca.getDependencies();
		List<CompiledDependencyIdentification> missing = new ArrayList<>();
		List<CompiledDependencyIdentification> nonMatching = new ArrayList<>();
		List<CompiledDependencyIdentification> matching = new ArrayList<>();
		for (CompiledDependency eD : expected) {
			CompiledDependency fD = findMatch(found, eD);
			if (fD == null) {
				missing.add( eD);
			}
			else if (!validateDependency(eD, fD)) {
				nonMatching.add(eD);
			}
			else {
				matching.add(eD);
			}			
		}
		Assert.assertTrue("mismatches: [" +  nonMatching.stream().map( d -> d.asString()).collect(Collectors.joining(",")) + "]", nonMatching.size() == 0);
		Assert.assertTrue("missing: [" + missing.stream().map( d -> d.asString()).collect(Collectors.joining(",")) + "]", missing.size() == 0);
		
		List<CompiledDependency> excess = new ArrayList<>();
		for (CompiledDependency fD : found) {
			if (findMatch( expected, fD) == null) {
				excess.add( fD);
			}
		}
		Assert.assertTrue("excess: [" + excess.stream().map( d -> d.asString()).collect(Collectors.joining(",")) + "]", excess.size() == 0);
		
	}
		
	
	static Map<String,String> transposeRedirects( Map<CompiledDependencyIdentification, CompiledDependencyIdentification> redirects) {
		Map<String,String> result = new HashMap<>( redirects.size());
		redirects.entrySet().stream().forEach( e -> result.put( e.getKey().asString(), e.getValue().asString()));
		return result;
	}
	
	
	
	default void validateArtifactRedirects( CompiledArtifact ca, Map<String,String> expected) {
		Map<CompiledDependencyIdentification, CompiledDependencyIdentification> found = ca.getArtifactRedirects();
		
		if (expected == null) {
			Assert.assertTrue("no redirects expected, but found [" + found.size() +"]", found == null);
		}
		Assert.assertTrue("expected [" + expected.size() + "] redirects, but found [" + found.size() +"]", expected.size() == found.size());
		
		Map<String,String> foundTransposed = transposeRedirects(found);
		
		for (Map.Entry<String, String> expectedEntry : expected.entrySet()) {
			String key = expectedEntry.getKey();
			String foundValue = foundTransposed.get(key);
			Assert.assertTrue("expected key [" + key + "] not found", foundValue != null);
			
			String value = expectedEntry.getValue();
			Assert.assertTrue("expected value [" + value +"] for key [" + key + "], found [" + foundValue +"]", value.equals(foundValue));			
		}					
	}
	
	
	default void validate( String tag, List<String> expected, List<String> found) {
		if (expected == null) {
			Assert.assertTrue("no " + tag + " expected, but found [" + found.size() +"]", found == null);
		}
		Assert.assertTrue("expected [" + expected.size() + "] " + tag + ", but found [" + found.size() +"]", expected.size() == found.size());
		
		List<String> matching = new ArrayList<>();
		List<String> missing = new ArrayList<>();
		for (String e : expected) {
			if (found.contains(e)) {
				matching.add(e);
			}
			else {
				missing.add( e);
			}
		}
			
		List<String> excessKeys = found.stream().filter( k -> !expected.contains(k)).collect(Collectors.toList());		
		Assert.assertTrue("excess found: [" + excessKeys.stream().collect(Collectors.joining(",")) + "]", excessKeys.size() == 0);		
	}
	default void validateDominants( CompiledArtifact ca, List<String> expected) {
		List<String> found = ca.getDominants().stream().map( cdi -> cdi.asString()).collect( Collectors.toList());
		validate("dominants", expected, found);
		
	}
	
	default void validateExclusions( CompiledArtifact ca, List<String> expected) {
		List<String> found = ca.getExclusions().stream().map( ai -> ai.asString()).collect( Collectors.toList());
		validate("exclusions", expected, found);
	}
}
 