// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations;

public class DebugHelper {
	private static final String ON = "ON";
	private static final String MC_DEBUG = "MC_DEBUG";
	private static Boolean debugging;

	public static boolean isDebugging() {
		if (debugging == null) {
			String mcDebugValue = System.getenv( MC_DEBUG);
			if (mcDebugValue == null) {
				mcDebugValue = System.getProperty(MC_DEBUG);
			}
			if (mcDebugValue == null || !mcDebugValue.equalsIgnoreCase(ON)) {
				debugging = false;
			}
			else {
				debugging = true;
			}
		}
		
		return debugging;
		
	}
}
