// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;

import java.io.File;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.model.artifact.Part;

/**
 * implementation of the {@link DependencyResolver} for working copy (file system and pom's are named pom.xml)
 * @author pit
 *
 */
@Deprecated
public class WorkingCopyDependencyResolverImpl extends AbstractDependencyResolverImpl {

	@Override
	protected String getPomName(Part pomPart) throws ResolvingException{
		return "pom.xml";
	}

	@Override
	protected String getPartLocation(Part pomPart) throws ResolvingException{
		try {
			String partLocation = RepositoryReflectionHelper.getHotfixSavySolutionFilesystemLocation( locationExpert.getLocalRepository(null), pomPart) + File.separator + getPomName( pomPart);
			return partLocation;
		} catch (RavenhurstException e) {
			throw new ResolvingException(e);			
		} catch (RepresentationException e) {
			throw new ResolvingException(e);
		}
	}
	
	
}
