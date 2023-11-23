// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.cache;

import java.util.Collection;

import com.braintribe.model.artifact.Part;

public interface PartCache {
	public void addToPartCache( String name, Part part);
	public void addToCache( Part part);
	public Part getPartFromCache( String name);	
	public Collection<Part> getPartsFromCache();
	public Part clone(Part part);
	public void clearCache();
}
