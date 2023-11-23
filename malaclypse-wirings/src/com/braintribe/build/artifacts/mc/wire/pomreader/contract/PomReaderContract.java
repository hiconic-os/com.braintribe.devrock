// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.pomreader.contract;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.wire.api.space.WireSpace;

public interface PomReaderContract extends WireSpace {
	ArtifactPomReader pomReader();
	ArtifactPomReader leanPomReader();
	DependencyResolver standardDependencyResolver();
	MavenSettingsReader settingsReader(); 
	RepositoryReflection repositoryReflection();
}
