// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.virtualenvironment;

public interface VirtualPropertyResolver {
	String getSystemProperty( String key);
	String getEnvironmentProperty( String key);
	String resolve(String expression);	
	boolean isActive();
}
