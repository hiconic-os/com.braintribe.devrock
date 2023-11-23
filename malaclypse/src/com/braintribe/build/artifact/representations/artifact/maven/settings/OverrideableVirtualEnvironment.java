// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * 
 * @author pit
 *
 */
public class OverrideableVirtualEnvironment extends StandardEnvironment {
	
	private Map<String, String> environmentOverrides = new HashMap<>();
	private Map<String, String> propertyOverrides = new HashMap<>();
	
	@Configurable
	public void setEnvironmentOverrides(Map<String, String> environmentOverrides) {
		this.environmentOverrides = environmentOverrides;
	}
	
	public void addEnvironmentOverride( String name, String value) {
		environmentOverrides.put(name, value);
	}
	
	@Configurable
	public void setPropertyOverrides(Map<String, String> propertyOverrides) {
		this.propertyOverrides = propertyOverrides;
	}
	
	public void addPropertyOverride( String key, String value) {
		propertyOverrides.put(key, value);
	}

	@Override
	public String getEnv(String name) {
		String value = environmentOverrides.get(name);
		if (value != null) {
			return value;
		}
		return super.getEnv(name);
	}

	@Override
	public String getProperty(String name) {
		String value = propertyOverrides.get(name);
		if (value != null) {
			return value;
		}
		return super.getProperty(name);
	}

	
}
