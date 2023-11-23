// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.commons.launcher;

import java.util.Collection;
import java.util.Collections;

import com.braintribe.build.artifacts.mc.wire.buildwalk.space.FilterConfigurationSpace;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;

public class PlainOptimisticResolverConfiguration extends FilterConfigurationSpace {
	
	private PartTuple commandTuple = PartTupleProcessor.fromString("cmd", "zip"); 
	@Override
	public Collection<PartTuple> partExpectation() {
		return Collections.singletonList( commandTuple);
	}
}
