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
package com.braintribe.test.multi.framework;

import java.util.Date;

public class SnapshotTuple {

	private int [] builds;
	private Date [] timestamps;
	private String artifact;
	private String pomMatch;
	private String pomFile;
	
	public SnapshotTuple( int [] num, Date [] timestamp, String artifact, String pomMatch, String pomFile) {
		this.builds = num;
		this.timestamps = timestamp;
		this.artifact = artifact;
		this.pomMatch = pomMatch;
		this.pomFile = pomFile;
	}

	public int[] getBuilds() {
		return builds;
	}
	public void setBuilds(int[] builds) {
		this.builds = builds;
	}

	public Date[] getTimestamps() {
		return timestamps;
	}
	public void setTimestamps(Date[] timestamps) {
		this.timestamps = timestamps;
	}

	public String getArtifact() {
		return artifact;
	}
	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}
	
	public String getPomMatch() {
		return pomMatch;
	}
	public void setPomMatch(String pom) {
		this.pomMatch = pom;
	}

	public String getPomFile() {
		return pomFile;
	}

	public void setPomFile(String pomFile) {
		this.pomFile = pomFile;
	}	
}
