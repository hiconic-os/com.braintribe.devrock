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
