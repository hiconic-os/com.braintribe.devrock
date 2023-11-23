// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.mc.wire.buildwalk.contract;

import java.io.File;

import com.braintribe.wire.api.space.WireSpace;

public interface CodebaseConfigurationContract extends WireSpace {
	File codebaseRoot();
	String defaultVersion();
	String codebasePattern();
}
