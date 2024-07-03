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
package com.braintribe.devrock.mc.core.commons;

import java.util.List;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionInterval;

/**
 * a class to support equality functions (as in {@link HashingComparator}) on the common denomintators of {@link VersionExpression}.
 * @author pit/dirk
 *
 */
public class VersionIntervalsEqualitySupport {
	private List<VersionInterval> intervals;
	
	
	public VersionIntervalsEqualitySupport(List<VersionInterval> intervals) {
		super();
		this.intervals = intervals;
	}


	@Override
	public boolean equals(Object obj) {
		VersionIntervalsEqualitySupport other = (VersionIntervalsEqualitySupport) obj;
		
		List<VersionInterval> l1 = intervals;
		List<VersionInterval> l2 = other.intervals;
		if (l1.size() != l2.size())
			return false;
		for (int i = 0; i < l1.size(); i++) {
			if (!matches( l1.get(i), l2.get(i))) {
				return false;
			}
		}		
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		for (VersionInterval interval: intervals) {
			int hc = interval.asString().hashCode();
			result = prime * result + hc;
		}

		return result;
	}

	/**
	 * @param i1 - the first {@link VersionInterval}
	 * @param i2 - the second {@link VersionInterval}
	 * @return - true if the two intervals match 
	 */
	private boolean matches(VersionInterval i1, VersionInterval i2) {
		if (
				i1.lowerBoundExclusive() != i2.lowerBoundExclusive() ||  				
				i1.upperBoundExclusive() != i2.upperBoundExclusive()
		   )
		return false;
		
		Version l1 = i1.lowerBound();
		Version u1 = i1.upperBound();
		
		Version l2 = i2.lowerBound();
		Version u2 = i2.upperBound();
		
		if (l1.compareTo(l2) != 0)
			return false;
		
		if (l1 == u1 && l2 == u2) {
			return true;
		}
		return l2.compareTo( u2) == 0;		
	}
	
	/**
	 * @param ve - the {@link VersionExpression}
	 */
	public VersionIntervalsEqualitySupport(VersionExpression ve) {
		this.intervals = ve.asVersionIntervalList();
	} 
	
	/**
	 * @param cdi - the {@link CompiledDependencyIdentification}
	 */
	public VersionIntervalsEqualitySupport(CompiledDependencyIdentification cdi) {
		this( cdi.getVersion());
	} 
	

}
