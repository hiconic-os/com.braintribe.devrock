// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.listener;

import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Solution;

/**
 * notifications from the POM reader subsystem 
 * @author pit
 *
 */
/**
 * @author Pit
 *
 */
public interface PomReaderNotificationListener {
	/**
	 * signals a read error while reading a pom
	 * @param location - the file name 
	 * @param reason - the actual error message
	 */
	void acknowledgeReadErrorOnFile( String walkScopeId, String location, String reason);
	/**
	 * signals a read error while reading a pom 
	 * @param artifact - the {@link Artifact} 
	 * @param reason  - the actual reason 
	 */
	void acknowledgeReadErrorOnArtifact( String walkScopeId, Artifact artifact, String reason);
	
	/**
	 * signals a read error while reading a pom's content 
	 * @param contents - the actual XML formatted {@link String}
	 * @param reason - the error message
	 */
	void acknowledgeReadErrorOnString( String walkScopeId, String contents, String reason);
	
	/**
	 * signal that a variable could not be resolved 
	 * @param artifact - the {@link Artifact} in which pom it happened 
	 * @param expression - that contained the variable 
	 */
	void acknowledgeVariableResolvingError( String walkScopeId, Artifact artifact, String expression);
	
	/**
	 * signal an association between pom file and artifact 
	 * @param location - the pom file's location 
	 * @param artifact - the {@link Artifact} it represents 
	 */
	void acknowledgeSolutionAssociation( String walkScopeId, String location, Artifact artifact);
	
	/**
	 * signal an association between an artifact and a parent
	 * @param child - the child {@link Artifact}
	 * @param parent - the parent {@link Solution}
	 */
	void acknowledgeParentAssociation( String walkScopeId, Artifact child, Solution parent);
	
	
	/**
	 * signal an error while associating a pom with a parent
	 * @param walkScopeId - the scope id
	 * @param child - the {@link Artifact} representing the child
	 * @param groupId - the groupid id of the parent
	 * @param artifactId - the artifact id of the parent
	 * @param version - the version of the parent.
	 */
	void acknowledgeParentAssociationError( String walkScopeId, Artifact child, String groupId, String artifactId, String version);
	
	/**
	 * signal an association between a parent and an import
	 * @param requestingSolution - the {@link Artifact} requesting the import
	 * @param requestedSolution - the {@link Solution} which was requested
	 */
	void acknowledgeImportAssociation( String walkScopeId, Artifact requestingSolution, Solution requestedSolution);
	
	/**
	 * signal an error while associating a parent and an import
	 * @param requestingSolution - the {@link Artifact} that is requesting the import 
	 * @param groupId - the groupId of the import
	 * @param artifactId - the artifact of the import
	 * @param version - the version of the import
	 */
	void acknowledgeImportAssociationError( String walkScopeId, Artifact requestingSolution, String groupId, String artifactId, String version);

}
