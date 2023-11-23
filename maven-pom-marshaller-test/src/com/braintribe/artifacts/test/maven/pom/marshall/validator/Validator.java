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
package com.braintribe.artifacts.test.maven.pom.marshall.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.braintribe.artifacts.test.maven.pom.marshall.AbstractPomMarshallerTest;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.common.lcd.Pair;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Exclusion;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Property;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.VirtualPart;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.meta.data.MetaData;



/**
 * validator interface (to be used by abstract {@link AbstractPomMarshallerTest}, with some default implementations 
 * @author pit
 *
 */
public interface Validator {

	default boolean validateHeader(Solution solution, ValidatorContext context) {
		boolean retval =  (
						context.groupId().equalsIgnoreCase( solution.getGroupId()) &&
						context.artifactId().equalsIgnoreCase( solution.getArtifactId()) &&
						context.version().equalsIgnoreCase( VersionProcessor.toString( solution.getVersion()))
					);
		
		if (!retval) {
			Assert.fail( "expected [" + context.groupId() + ":" + context.artifactId() + "#" + context.version() + "], found [" + NameParser.buildName(solution) + "]");
		}
		return retval;
	}
	
	/**
	 * @param dependency
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param type
	 * @param scope
	 * @param optional
	 * @param expectedTags
	 * @return
	 */
	default boolean validateDependency(ValidatorContext context) {
		Dependency dependency = context.dependency();
		String dependencyName = NameParser.buildName(dependency);
		boolean retval =  (
						context.groupId().equalsIgnoreCase( dependency.getGroupId()) &&
						context.artifactId().equalsIgnoreCase( dependency.getArtifactId()) &&
						context.version().equalsIgnoreCase( VersionRangeProcessor.toString( dependency.getVersionRange()))
					);
		
		if (!retval) {
			Assert.fail( "expected [" + context.groupId() + ":" + context.artifactId() + "#" + context.version() + "], found [" + dependencyName + "]");
			return false;
		}
		else {
			if (context.type() != null) {
				if (!context.type().equalsIgnoreCase( dependency.getType())) {
					Assert.fail("expected type is [" + context.type() + "] yet found ["+ dependency.getType() + "] for [" + dependencyName + "]") ;
					return false;
				}
			}
			if (context.scope() != null) {
				if (!context.scope().equalsIgnoreCase( dependency.getScope())) {
					Assert.fail("expected scope is [" + context.scope() + "] yet found ["+ dependency.getScope() + "] for [" + dependencyName + "]") ;
					return false;
				}
			}
			if (context.optional() != null) {
				if (!context.optional().equals( dependency.getOptional())) {
					Assert.fail("expected optional flag is [" + context.optional() + "] yet found ["+ dependency.getOptional() + "] for [" + dependencyName + "]") ;
					return false;
				}
			}
			
			if (context.group() != null) {
				if (!context.group().equalsIgnoreCase( dependency.getGroup())) {
					Assert.fail("expected group is [" + context.group() + "] yet found ["+ dependency.getGroup() + "] for [" + dependencyName + "]") ;
					return false;
				}
			}
			
			// validate tags
			if (context.tags() != null) {
				validateTags( dependency.getTags(), context.tags());									
			}
			
			// validate redirects 
			if (context.redirects() != null) {
				validateRedirects(dependency.getRedirectionMap(), context.redirects());
			}
			
			if (context.virtualParts() != null) {
				validateVirtualParts( dependency, context.virtualParts());
			}
			
		}
		return true;
	}
	
	default void validateVirtualParts(Dependency dependency, Map<String, String> virtualParts) {
		Set<MetaData> metaData = dependency.getMetaData();
		Assert.assertTrue("no metadata found", metaData != null && metaData.size() != 0);
		List<String> mks = new ArrayList<>();
		List<String> fks = new ArrayList<>();
		
		for (MetaData md : metaData) {
			if (md instanceof VirtualPart == false) {
				Assert.fail("expected a [" + VirtualPart.T.getTypeSignature() + "], but found [" + md.getClass().getName() + "]");				
			}
			VirtualPart vp = (VirtualPart) md;
			String key = PartTupleProcessor.toString(vp.getType());
			String value = virtualParts.get(key);
			if (value == null) {
				mks.add( key);
			}
			else {
				fks.add( key);
				Assert.assertTrue("expected [" + key + "=" + value + "], but found [" + key  + "=" + vp.getPayload() + "]", value.equalsIgnoreCase( vp.getPayload()));
			}
		}
		List<String> eks = new ArrayList<>(virtualParts.keySet());
		eks.removeAll( fks);
		Assert.assertTrue("not all keys were found, missing [" + toString( mks) + "]", mks.size() == 0);
		Assert.assertTrue("more keys were found, excess [" + toString( eks) + "]", eks.size() == 0);
	}

	default void validateTags(List<String> found, List<String> expected) {
		List<String> missing = new ArrayList<>();
		List<String> existing = new ArrayList<>( found);
									
		for (String tag : expected) {
			if (existing.contains( tag)) {
				existing.remove( tag);						
			}
			else {
				missing.add(tag);
			}
		}
		
		String ms="", ex="";
		if (missing.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			for (String t : missing) {
				if (buffer.length() > 0) 
					buffer.append(",");
				buffer.append( t);
			}
			ms = buffer.toString();
		}
		if (existing.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			for (String t : existing) {
				if (buffer.length() > 0) 
					buffer.append(",");
				buffer.append( t);
			}
			ex = buffer.toString();
		}			
		if (missing.size() > 0 || existing.size() > 0) {					
			Assert.fail( "tags are not as expected : missing [" + ms + "], existing [" + ex + "]");
		}		
	}
	
	/**
	 * compares to list of string, returns a Pair of missing,excess
	 * @param found - the found strings
	 * @param expected - the expected strings 
	 * @return - a {@link Pair} of missing string (expected, yet not found) and excess string (not expected, yet found)
	 */
	default Pair<List<String>, List<String>> compareList( Collection<String> found, Collection<String> expected) {
		List<String> fs = new ArrayList<>( found);
		List<String> es = new ArrayList<>( expected);
		List<String> ms = new ArrayList<>();
		List<String> os = new ArrayList<>();
		for (String f : fs) {
			if (es.contains( f)) {
				os.add(f);
			}
			else {
				ms.add( f);
			}
		}
		List<String> xs = new ArrayList<>( expected);
		xs.removeAll( os);
		return Pair.of(ms, xs);
	}

	/**
	 * @param found
	 * @param expected
	 */
	default void validateRedirects(Map<String, String> found, Map<String, String> expected) {
		Pair<List<String>,List<String>> missingAndExcess = compareList( found.keySet(), expected.keySet());
		String msg = "expected [" + toString(expected.keySet()) + "], but found [" + toString(found.keySet()) + "]";
				
		Assert.assertTrue(msg + " missing [" + toString( missingAndExcess.first()) + "]", missingAndExcess.first().size() == 0);		
		Assert.assertTrue(msg + " excess [" + toString( missingAndExcess.second()) + "]", missingAndExcess.second().size() == 0);
		
		for (Entry<String,String> entry : expected.entrySet()) {
			String val = found.get( entry.getKey());
			Assert.assertTrue("expected value of [" + entry.getKey() + "] is [" + entry.getValue() + "], found [" + val + "]", entry.getValue().equalsIgnoreCase(val));
		}
		
	}

	default String toString(Collection<String> expected) {
		return expected.stream().collect(Collectors.joining(","));
	}

	default Dependency retrieveDependency( Collection<Dependency> dependencies, String groupId, String artifactId, String range) {
		for (Dependency dependency : dependencies) {
			if (!groupId.equalsIgnoreCase(dependency.getGroupId()))
				continue;
			if (!artifactId.equalsIgnoreCase( dependency.getArtifactId())) 
				continue;
			if (!range.equalsIgnoreCase( VersionRangeProcessor.toString( dependency.getVersionRange()))) 
				continue;
			return dependency;
		}
		return null;
	}
	
	default boolean validateProperties( Solution solution, Map<String, String> expectedProperties) {
		Set<Property> foundProperties = solution.getProperties();
		if (foundProperties == null || foundProperties.size() != expectedProperties.size()) {
			Assert.fail("expected [" + expectedProperties.size() + "], but found [" + foundProperties == null ? "0" : foundProperties.size() + "]");
			return false;
		}
		List<String> foundValues = new ArrayList<>();
		for (Property property : foundProperties) {
			String name = property.getName();
			String value = property.getRawValue();
			
			if (!expectedProperties.keySet().contains(name)) {
				Assert.fail("property name [" + name + "] is unexpected");
				return false;
			}
			String expectedValue =  expectedProperties.get(name);
			if (expectedValue == null && value != null) {
				Assert.fail("expected value for key [" + name + "] is [" + expectedValue + "] but found [" + value +"]");
				return false;
			}
			if (!expectedValue.equalsIgnoreCase( value)) {
				Assert.fail("expected value for key [" + name + "] is [" + expectedValue + "] but found [" + value +"]");
				return false;
			}
			foundValues.add( name);
		}
		// check if all were found
		for (String key : expectedProperties.keySet()) {
			if (!foundValues.contains(key)) {
				Assert.fail( "no property with key [" + key + "] was found");
				return false;
			}
		}
		
		return true;
	}
	default boolean validateExclusions( Dependency dependency, Set<Exclusion> expectedExclusions) {
		
		Set<Exclusion> foundExclusions = dependency.getExclusions();
		if (foundExclusions == null || foundExclusions.size() != expectedExclusions.size()) {
			Assert.fail("expected [" + expectedExclusions.size() + " exclusions, but found [" + foundExclusions == null ? "0" : foundExclusions.size() + "]");
		}
		Set<Exclusion> processedExclusions = new HashSet<>(expectedExclusions);
		for (Exclusion exclusion : foundExclusions) {
			String expectedGroupId = exclusion.getGroupId();
			String expectedArtifactId = exclusion.getArtifactId();
			
			Iterator<Exclusion> iterator = processedExclusions.iterator();
			while (iterator.hasNext()) {
				Exclusion suspect = iterator.next();
				String suspectGroupId = suspect.getGroupId();
				String suspectArtifactId = suspect.getArtifactId();
				
				if (
						(expectedGroupId == null && suspectGroupId != null) ||
						(expectedGroupId != null && suspectGroupId == null)
					) {
					continue;
				}
				else if (expectedGroupId != null && suspectGroupId != null) {
					if (!expectedGroupId.equalsIgnoreCase( suspectGroupId))
						continue;
				}
				if (
						(expectedArtifactId == null && suspectArtifactId != null) ||
						(expectedArtifactId != null && suspectArtifactId == null)
					) {
					continue;
				}
				else if (expectedArtifactId != null && suspectArtifactId != null) {
					if (!expectedArtifactId.equalsIgnoreCase( suspectArtifactId))
						continue;
				}
				
				iterator.remove();	
				
			}
		}
		if (processedExclusions.size() != 0) {
			Assert.fail("no all expected exclusions were found of dependency [" + NameParser.buildName(dependency) +"]");
			return false;
		}
		return true;
	}
	
	default boolean validateVirtualPart( Dependency dep2, PartTuple checkTuple, String expectedPayload) {
		Set<MetaData> metaData = dep2.getMetaData();
		if (metaData == null || metaData.size() != 1) {
			Assert.fail("expected [1] metadata, but found [" + metaData == null ? "0" : metaData.size() + "]");
			return false;
		}
		MetaData suspect = metaData.toArray( new MetaData[0])[0];
		if (suspect instanceof VirtualPart == false) {
			Assert.fail("expected a [" + VirtualPart.T.getTypeSignature() + "], but found [" + suspect.getClass().getName() + "]");
			return false;
		}
		VirtualPart virtualPart = (VirtualPart) suspect;
		PartTuple tuple = virtualPart.getType();
		if (!PartTupleProcessor.equals(checkTuple, tuple)) {
			Assert.fail("expected a type of [" + PartTupleProcessor.toString(checkTuple) + "] but found [" + PartTupleProcessor.toString(tuple) + "]");
			return false;
		}
		String payload = virtualPart.getPayload();
		if (payload == null || payload.length() == 0) {
			Assert.fail("expected a non-empty payload but found [" + payload + "]");
			return false;
		}
		
		if (!expectedPayload.equalsIgnoreCase(payload)) {
			Assert.fail("expected payload [" + expectedPayload + "] but found [" + payload + "]");
			return false;
		}
		return true;
	}
	boolean validate( Solution solution);
	
	
}
