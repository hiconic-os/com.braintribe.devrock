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
