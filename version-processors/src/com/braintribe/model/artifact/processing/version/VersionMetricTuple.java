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
package com.braintribe.model.artifact.processing.version;

/**
 * a simple tuple to extract kind of a metric from a version.<br/>
 * 
 * major : the value before the first delimiter, or the full version as a string if no other delimiter's found <br/>
 * minor : the value after the first delimiter, but before the second delimiter or NULL or the rest if not other delimiter's found <br/>
 * revision : the value after the second delimiter,  but before the third delimiter or NULL or the rest if no other delimiter's found <br/>
 * @author pit
 *
 */
public class VersionMetricTuple {
	public Integer major;
	public Integer minor;
	public Integer revision;
	
	public VersionMetricTuple() {
		major = 0;
		minor = 0;
		revision = 0;
	}
	
	public VersionMetricTuple(Integer major, Integer minor, Integer revision) {
		super();
		this.major = major;
		this.minor = minor;
		this.revision = revision;
	}
}
