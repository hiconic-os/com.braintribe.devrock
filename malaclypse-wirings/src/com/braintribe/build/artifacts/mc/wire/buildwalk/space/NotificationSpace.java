package com.braintribe.build.artifacts.mc.wire.buildwalk.space;

import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.NotificationContract;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class NotificationSpace implements NotificationContract {

	@Override	
	public PomReaderNotificationListener pomReaderNotificationListener() {	
		return null;
	}
		
}
