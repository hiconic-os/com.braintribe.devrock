// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;

import java.io.File;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

/**
 * implementation of the {@link DependencyResolver} for local repository (filesystem, yet names like in the remote)
 * @author pit
 *
 */
@Deprecated
public class LocalRepositoryDependencyResolverImpl extends AbstractDependencyResolverImpl {

	@Override
	protected String getPomName(Part pomPart) throws ResolvingException {			
		return pomPart.getArtifactId() + "-" + VersionProcessor.toString( pomPart.getVersion()) + ".pom";	
	}

	@Override
	protected String getPartLocation(Part pomPart) throws ResolvingException {
		try {
			String partLocation = RepositoryReflectionHelper.getSolutionFilesystemLocation( locationExpert.getLocalRepository(null), pomPart) + File.separator + getPomName( pomPart);
			return partLocation;				
		} catch (RepresentationException e) {
			throw new ResolvingException(e);
		}
	}

	
}
