// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
