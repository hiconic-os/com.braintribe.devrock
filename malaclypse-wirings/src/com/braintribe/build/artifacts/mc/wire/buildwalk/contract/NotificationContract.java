package com.braintribe.build.artifacts.mc.wire.buildwalk.contract;

import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.wire.api.space.WireSpace;

/**
 * a contract to inject listeners - for now, only a pom reader notification listener
 * @author pit
 *
 */
public interface NotificationContract extends WireSpace{

	PomReaderNotificationListener pomReaderNotificationListener();
}
