// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.model.artifact.Dependency;

public interface BuildRanges {
	static BuildRange parseFromConcatString(String concatString) {
		return parseFromConcatString(concatString, null, null);
	}
	
	static BuildRange parseFromConcatString(String concatString, String defaultGroup, Function<String, String> groupDefaultVersionLookup) {
		DisjunctionBoundaryComparator lowerBound = new DisjunctionBoundaryComparator();
		DisjunctionBoundaryComparator upperBound = new DisjunctionBoundaryComparator();
		List<Dependency> entryPoints = new ArrayList<>();
		
		String boundsAsString[] = concatString.split("\\+");
		
		
		for (String boundaryAsStr: boundsAsString) {
			boundaryAsStr = boundaryAsStr.trim();
			if (boundaryAsStr.isEmpty())
				throw new IllegalArgumentException("illegal build range boundary [" + boundaryAsStr + "]");
			
			char firstChar = boundaryAsStr.charAt(0);
			char lastChar = boundaryAsStr.charAt(boundaryAsStr.length() - 1);
			int s = 0, e = boundaryAsStr.length();
			
			List<Consumer<Dependency>> consumers = new ArrayList<>(2);

			boolean noLowerBracket = false;
			 
			switch (firstChar) {
			case '(':
			case ']':
				consumers.add(d -> lowerBound.addOperand(new BoundaryComparator(d, true)));
				s++;
				break;
			case '[':
				consumers.add(d -> lowerBound.addOperand(new BoundaryComparator(d, false)));
				s++;
				break;
			default:
				noLowerBracket = true;
				break;
			}
			
			switch (lastChar) {
				case ')':
				case '[':
					consumers.add(d -> upperBound.addOperand(new BoundaryComparator(d, true)));
					e--;
					break;
				case ']':
					consumers.add(d -> upperBound.addOperand(new BoundaryComparator(d, false)));
					e--;
					break;
				default:
					if (noLowerBracket) {
						consumers.add(d -> upperBound.addOperand(new BoundaryComparator(d, false)));
					}
					break;
			}
			
			String dependencyAsStr = boundaryAsStr.substring(s, e);
			
			int versionDelimiter = dependencyAsStr.lastIndexOf('#');
			int groupDelimiter = dependencyAsStr.indexOf(':');
			
			String group = null;
			
			if (groupDelimiter == -1) {
				if (defaultGroup == null)
					throw new IllegalArgumentException("default group is missing but required for dependency descriptor: " + dependencyAsStr);
				
				group = defaultGroup; 
				dependencyAsStr = defaultGroup + ":" + dependencyAsStr;
			}
			else {
				group = dependencyAsStr.substring(0, groupDelimiter);
			}
			
			if (versionDelimiter == -1) {
				String defaultVersion = groupDefaultVersionLookup != null? groupDefaultVersionLookup.apply(group): null;
				
				if (defaultVersion == null)
					throw new IllegalArgumentException("default version could not be determined but required for dependency descriptor: " + dependencyAsStr);
				
				dependencyAsStr +=  "#" + defaultVersion;
			}
			
			Dependency dependency = NameParser.parseDependencyFromHotfixShorthand(dependencyAsStr);
			
			consumers.forEach(c -> c.accept(dependency));
			
			entryPoints.add(dependency);
		}
		
		if (lowerBound.isEmpty()) {
			lowerBound.addOperand(BoundaryFloorComparator.INSTANCE);
		}
		
		
		BuildRange range = new BuildRange(entryPoints, lowerBound, upperBound);
		
		return range;
	}
}
