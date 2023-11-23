// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash;

import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.build.artifact.walk.multi.clash.listener.ClashResolverNotificationBroadcaster;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverDenotationType;

public interface ClashResolverFactory extends Function<ClashResolverDenotationType, ClashResolver>, ClashResolverNotificationBroadcaster {
	@Deprecated
	void setTerminalProvider( Supplier<Artifact> terminalProvider);
}
