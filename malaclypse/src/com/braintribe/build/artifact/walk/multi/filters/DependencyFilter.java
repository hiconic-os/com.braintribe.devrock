package com.braintribe.build.artifact.walk.multi.filters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.braintribe.model.artifact.Dependency;

public interface DependencyFilter {
	boolean filterDependency(Dependency dependency);
	
	/**
	 * filter a {@link List} of {@link Dependency} according their tag values
	 * @param dependencies - a {@link List} of {@link Dependency}
	 * @return - a new {@link ArrayList} with the remaining {@link Dependency}
	 */
	default List<Dependency> filterDependencies( List<Dependency> dependencies) {	
		List<Dependency> filteredList = new ArrayList<>( dependencies); 
		Iterator<Dependency> iterator = filteredList.iterator();
		while (iterator.hasNext()) {
			Dependency dependency = iterator.next();
			if (!filterDependency( dependency)) {
				iterator.remove();
			}
		}
		return filteredList;
	}
	
}
