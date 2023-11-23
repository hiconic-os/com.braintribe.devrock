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
package com.braintribe.model.processing.panther;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class ArtifactAddress {
	private static final String POM_XML = "/pom.xml";

	private List<String> path;
	private String pathAsString;
	private OrganizationKind organizationKind;
	private String groupId;
	private String artifactId;
	private String version;
	private String qualifiedName;
	
	/**
	 * @return an ArtifactAddress if the file path leads ends with "/pom.xml" and the path transports the groupId, artifactId and version either in grouped (some/group/x.y/artifact) or in individual (some/group/artifact/x.y) form    
	 */
	public static ArtifactAddress parseFromPomFilePath(String pathAsString) {
		
		if (!pathAsString.endsWith(POM_XML))
			return null;

		pathAsString = pathAsString.substring(0, pathAsString.length() - POM_XML.length());
		
		List<String> path = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(pathAsString, "/");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			path.add(token);
		}
		
		int size = path.size();
		if (size < 3)
			return null;
		
		String lastFolder = path.get(size - 1);
		String secondLastFolder = path.get(size - 2);
		
		boolean lastFolderIsVersion = isVersion(lastFolder);
		boolean secondLastFolderIsVersion = isVersion(secondLastFolder);
		
		if (!(lastFolderIsVersion ^ secondLastFolderIsVersion))
			return null;
		
		ArtifactAddress address = new ArtifactAddress();

		if (lastFolderIsVersion) {
			address.version = lastFolder;
			address.artifactId = secondLastFolder;
			address.organizationKind = OrganizationKind.individual;
		}
		else {
			address.version = secondLastFolder;
			address.artifactId = lastFolder;
			address.organizationKind = OrganizationKind.grouped;
		}
		
		address.groupId = path.subList(0, size - 2).stream().collect(Collectors.joining("."));
		address.path = path;
		address.pathAsString = pathAsString;
		return address;
	}
	
	private static boolean isVersion(String s) {
		StringTokenizer tokenizer = new StringTokenizer(s, ".");
		
		int tokenCount = 0;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (!isNumber(token))
				return false;
			tokenCount++;
		}
		
		return tokenCount == 2;
	}
	
	private static boolean isNumber(String s) {
		int len = s.length();
		
		if (len == 0)
			return false;
		
		for (int i = 0; i < len; i++) {
			if (!Character.isDigit(s.charAt(i)))
				return false;
		}
		
		return true;
	}
	
	private ArtifactAddress() {
		
	}
	
	public String getGroupId() {
		return groupId;
	}
	
	public String getArtifactId() {
		return artifactId;
	}
	
	public String getVersion() {
		return version;
	}
	
	public List<String> getPath() {
		return path;
	}
	
	public String getPathAsString() {
		return pathAsString;
	}
	
	public OrganizationKind getOrganizationKind() {
		return organizationKind;
	}
	
	public String getQualifiedName() {
		if (qualifiedName == null) {
			qualifiedName = groupId + ':' + artifactId + '#' + version;
		}

		return qualifiedName;
	}
}
