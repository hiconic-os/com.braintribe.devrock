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
package com.braintribe.devrock.mc.core.resolver.transitive;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.braintribe.devrock.mc.api.commons.Functions;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;



/**
 * smart exclusion handler : interfaces between exclusion declarations and JAVA predicates 
 *  
 * @author pit / dirk
 *
 */
public interface Exclusions {
	static String normalizeExclusionValue(String s) {
		if (s == null || s.isEmpty())
			return "*";
		else
			return s;
	}
	
	/**
	 * @param exclusion - the {@link ArtifactIdentification} to exclude ('*' allowed as wildcard) 
	 * @return - a respective {@link Predicate}
	 */
	static Predicate<ArtifactIdentification> predicate(ArtifactIdentification exclusion) {
		String groupId = normalizeExclusionValue(exclusion.getGroupId());
		String artifactId = normalizeExclusionValue(exclusion.getArtifactId());
		
		final Predicate<ArtifactIdentification> groupPredicate = groupId.equals("*")? //
			Functions.invariantTrue(): //
			i -> i.getGroupId().equals(groupId); //
		
		Predicate<ArtifactIdentification> identificationPredicate = artifactId.equals("*")? //
			groupPredicate: //
			groupPredicate.and(i -> i.getArtifactId().equals(artifactId)); //
					
		return identificationPredicate;
	}
	
	/**
	 * @param exclusions - some {@link ArtifactIdentification}s to exclude ('*' allowed as wildcard)
	 * @return - a {@link Predicate} that combines all into one 
	 */
	static Predicate<ArtifactIdentification> predicate(Iterable<? extends ArtifactIdentification> exclusions) {
		Predicate<ArtifactIdentification> predicate = Functions.invariantFalse();
		
		for (ArtifactIdentification exclusion: exclusions) {
			predicate = predicate.or(predicate(exclusion));
		}
		
		return predicate;
	}
	/**
	 * @param exclusions - a stream  {@link ArtifactIdentification}s to exclude ('*' allowed as wildcard)
	 * @return - a {@link Predicate} that combines all into one 
	 */
	static Predicate<ArtifactIdentification> predicate(Stream<? extends ArtifactIdentification> exclusions) {
		Iterable<? extends ArtifactIdentification> asIterable = asIterable(exclusions);
		return predicate(asIterable);
	}
	
	static <T> Iterable<T> asIterable(Stream<T> stream) {
		return stream::iterator;
	}
	
	
	/**
	 * @param dependency - a {@link AnalysisDependency} to exclude 
	 * @return - a respective {@link Predicate} 
	 */
	static Predicate<ArtifactIdentification> predicate(AnalysisDependency dependency) {
		return predicate(dependency.getOrigin());
	}
	
	/**
	 * @param dependency - a {@link CompiledDependency} to exclude 
	 * @return - a respective {@link Predicate}
	 */
	static Predicate<ArtifactIdentification> predicate(CompiledDependency dependency) {
		return predicate(dependency.getExclusions());
	}
	
	/**
	 * @param artifact - a {@link CompiledArtifact} to exclude
	 * @return - a respective {@link Predicate}
	 */
	static Predicate<ArtifactIdentification> predicate(CompiledArtifact artifact) {
		return predicate(artifact.getExclusions());
	}
}