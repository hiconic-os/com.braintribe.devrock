package com.braintribe.build.artifact.walk.build;

import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Exclusion;
import com.braintribe.model.artifact.Identification;

public interface Exclusions {
	static String normalizeExclusionValue(String s) {
		if (s == null || s.isEmpty())
			return "*";
		else
			return s;
	}
	
	static Predicate<Identification> predicate(Exclusion exclusion) {
		String groupId = normalizeExclusionValue(exclusion.getGroupId());
		String artifactId = normalizeExclusionValue(exclusion.getArtifactId());
		
		final Predicate<Identification> groupPredicate = groupId.equals("*")? //
			i -> true: //
			i -> i.getGroupId().equals(groupId); //
		
		Predicate<Identification> identificationPredicate = artifactId.equals("*")? //
			groupPredicate: //
			groupPredicate.and(i -> i.getArtifactId().equals(artifactId)); //
					
		return identificationPredicate;
	}
	
	static Predicate<Identification> predicate(Set<Exclusion> exclusions) {
		Predicate<Identification> predicate = e -> false;
		
		for (Exclusion exclusion: exclusions) {
			predicate = predicate.or(predicate(exclusion));
		}
		
		return predicate;
	}
	
	static Predicate<Identification> predicate(Dependency dependency) {
		return predicate(dependency.getExclusions());
	}
}
