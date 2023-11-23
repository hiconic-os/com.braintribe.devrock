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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.devrock.mc.api.transitive.BuildRange;
import com.braintribe.devrock.mc.api.transitive.RangedTerminals;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;

public interface RangedTerminalParser {
	static RangedTerminals parseFromConcatString(String concatString) {
		return parseFromConcatString(concatString, null, null);
	}
	
	static RangedTerminals parseFromConcatString(String concatString, String defaultGroup, Function<String, String> groupDefaultVersionLookup) {
		DisjunctionBoundaryComparator lowerBound = new DisjunctionBoundaryComparator();
		DisjunctionBoundaryComparator upperBound = new DisjunctionBoundaryComparator();
		List<CompiledDependencyIdentification> terminals = new ArrayList<>();
		
		String boundsAsString[] = concatString.split("\\+");
		
		
		for (String boundaryAsStr: boundsAsString) {
			boundaryAsStr = boundaryAsStr.trim();
			if (boundaryAsStr.isEmpty())
				throw new IllegalArgumentException("illegal build range boundary [" + boundaryAsStr + "]");
			
			char firstChar = boundaryAsStr.charAt(0);
			char lastChar = boundaryAsStr.charAt(boundaryAsStr.length() - 1);
			int s = 0, e = boundaryAsStr.length();
			
			List<Consumer<CompiledDependencyIdentification>> consumers = new ArrayList<>(2);

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
			
			CompiledDependencyIdentification dependency = CompiledDependencyIdentification.parseAndRangify(dependencyAsStr, true);
			
			consumers.forEach(c -> c.accept(dependency));
			
			terminals.add(dependency);
		}
		
		if (lowerBound.isEmpty()) {
			lowerBound.addOperand(BoundaryFloorComparator.INSTANCE);
		}
		
		BuildRange range = BuildRange.of(lowerBound, upperBound);

		return new RangedTerminals() {
			
			@Override
			public Iterable<? extends CompiledTerminal> terminals() {
				return terminals;
			}
			
			@Override
			public BuildRange range() {
				return range;
			}
		};
	}
}
