// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.coding;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;

/**
 * wrapper codec for {@link CodingSet} or {@link CodingMap} based structures of {@link Dependency} 
 * @author pit
 *
 */
public class DependencyWrapperCodec extends HashSupportWrapperCodec<Dependency> {
	
	public DependencyWrapperCodec() {
		super( true);
	}

	@Override
	protected int entityHashCode(Dependency e) {		
		return (NameParser.buildName(e)).hashCode();
	}

	@Override
	protected boolean entityEquals(Dependency e1, Dependency e2) {
		if (!e1.getGroupId().equalsIgnoreCase(e2.getGroupId()))
			return false;
		if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
			return false;
		if (!VersionRangeProcessor.equals(e1.getVersionRange(), e2.getVersionRange()))
			return false;
		return true;
	}

}
