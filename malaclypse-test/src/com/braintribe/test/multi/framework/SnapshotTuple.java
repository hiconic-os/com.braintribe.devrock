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
