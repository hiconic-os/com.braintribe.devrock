// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.artifacts.codebase.build;

import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;

public class VersionRangeLab {
	public static void main(String[] args) {
		VersionRange range = VersionRangeProcessor.createFromString("[0,2000000]");
		Version version = VersionProcessor.createFromString("1.0");
		VersionRange r2 = VersionRangeProcessor.createFromString("(1.0,2.0)");
		
		System.out.println(VersionRangeProcessor.matches(range, version));
		System.out.println(VersionRangeProcessor.contains(range, r2));
	}
}
