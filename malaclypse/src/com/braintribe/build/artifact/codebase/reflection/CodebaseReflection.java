// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.codebase.reflection;

import java.io.File;
import java.util.List;

public interface CodebaseReflection {
	
	/**
	 * Lists all artifact folders for a given groupId and version if this codebase reflection supports {@link #reflectsGroupArtifacts()}
	 */
	List<File> findGroupArtifacts(String groupId, String version);
	
	/**
	 * Lists all version folders for a given groupId and artifactId if this codebase reflection supports {@link #reflectsVersions()}
	 */
	List<String> findVersions(String groupId, String artifactId);
	
	/**
	 * Finds an artifact by its codebase-precise full qualification. Codebase-precise means that the version used here comes from a version enumeration of this
	 * codebase reflection or from a direct dependency. This method can only be called if this codebase supports {@link #reflectsVersions()}. 
	 */
	File findArtifact(String groupId, String artifactId, String version);

	/**
	 * Finds an artifact by its groupdId and artifactId. This method can only be called if this codebase does not support {@link #reflectsVersions()}.
	 */
	File findArtifact(String groupId, String artifactId);
	
	/**
	 * Returns if the reflected codebase is organized in a way that it supports enumeration of artifact versions  
	 */
	boolean reflectsVersions();

	/**
	 * Returns if the reflected codebase is organized in a way that it supports enumeration of group artifacts  
	 */
	boolean reflectsGroupArtifacts();
}
