// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;

public class PartCacheInstance implements PartCache {
	private Map<String, Part> partByName = new ConcurrentHashMap<String, Part>();
	
	@Override
	public void addToPartCache(String name, Part part) {
		partByName.put( name, part);
	}

	@Override
	public void addToCache(Part part) {		
		partByName.put( NameParser.buildName(part), part);				
	}

	@Override
	public Part getPartFromCache(String name) {		
		return partByName.get(name);
	}

	@Override
	public Collection<Part> getPartsFromCache() {
		return partByName.values();
	}
	

	@Override
	public void clearCache() {
		partByName.clear();
		
	}

	@Override
	public Part clone(Part part) {
		Part result = Part.T.create();
		ArtifactProcessor.transferIdentification(result, part);
		result.setLocation( part.getLocation());
		result.setMd5Hash( part.getMd5Hash());
		result.setSha1Hash( part.getSha1Hash());
		return result;
	}
	

}
