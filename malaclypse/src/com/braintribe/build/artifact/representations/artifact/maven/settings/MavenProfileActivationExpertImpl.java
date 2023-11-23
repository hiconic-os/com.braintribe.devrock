// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings;

import java.io.File;

import com.braintribe.build.artifact.representations.artifact.maven.settings.properties.SettingsPropertyResolver;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.maven.settings.Activation;
import com.braintribe.model.maven.settings.ActivationFile;
import com.braintribe.model.maven.settings.ActivationOS;
import com.braintribe.model.maven.settings.ActivationProperty;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * maven like implementation of the activation logic 
 * 
 * @author pit
 *
 */
public class MavenProfileActivationExpertImpl implements MavenProfileActivationExpert {
	private SettingsPropertyResolver propertyResolver;
	protected VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;

	public MavenProfileActivationExpertImpl() {
	}
	
	@Configurable 
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.representations.artifact.maven.settings.MavenProfileActivationExpert#setPropertyResolver(com.braintribe.build.artifact.representations.artifact.maven.settings.SettingsPropertyResolver)
	 */
	@Override
	@Configurable @Required
	public void setPropertyResolver(SettingsPropertyResolver propertyResolver) {
		this.propertyResolver = propertyResolver;
	}

	protected String resolveProperty( String key) {
		String variable = key;
		if (!variable.startsWith( "${")) {
			variable = "${" + variable + "}";
		}
				
		String value = propertyResolver.expandValue(variable);
		/*
		if (value == null) {		
			value = propertyResolver.getSystemProperty(key);			
		}
		if (value == null) {
			value = propertyResolver.getEnvironmentProperty(key);
		}
		*/
		return value;
	}
	
	private String lookupProperty( String key) {		
		return resolveProperty(key);
	}

	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.representations.artifact.maven.settings.MavenProfileActivationExpert#isActive(com.braintribe.model.maven.settings.Profile, com.braintribe.model.maven.settings.Activation)
	 */
	@Override
	public Boolean isActive( Profile profile, Activation activation){
		if (activation == null)
			return false;
		
		boolean result = true;
		// active by default
		if (Boolean.TRUE.equals(activation.getActiveByDefault()))
			return true;
		
		// jdk test 
		String jdk = activation.getJdk();
		if (jdk != null && jdk.length() > 0) {
			String suspect = lookupProperty("java.specification.version");			
			if (suspect == null || !jdk.startsWith( suspect))
				return false;
		}
		
		// os 
		ActivationOS os = activation.getOs();
		if (os != null) {
			if (os.getFamily() !=  null && os.getFamily().length() > 0) {
				String osFamily = lookupProperty( "os.name");			
				if (osFamily == null || !os.getName().startsWith(osFamily))
					return false;
			}		
			if (os.getName() !=  null && os.getName().length() > 0) {
				String osName = lookupProperty( "os.name");
				
				if (osName == null || !os.getName().equalsIgnoreCase(osName))
					return false;
			}
			if (os.getArch() !=  null && os.getArch().length() > 0) {
				String osArch = lookupProperty( "os.arch");				
				if (osArch == null || !os.getArch().equalsIgnoreCase(osArch))
					return false;
			}
			if (os.getVersion() !=  null && os.getVersion().length() > 0) {
				String osVersion = lookupProperty( "os.version");				
				if (osVersion == null || !os.getVersion().equalsIgnoreCase(osVersion))
					return false;
			}
			
			// family?
			
		}
		// property 
		ActivationProperty activationProperty = activation.getProperty();
		if (activationProperty != null) {
			String key = activationProperty.getName();
			if (key.startsWith( "!")) {
				if (lookupProperty( key.substring(1)) != null)
					return false;
				else
					return true;
			}
		
			String testValue = activationProperty.getValue();

			String value = lookupProperty( key);
			
			if (value == null) {
				if (testValue != null) {
					// if a test value is specified, the variable has to exist
					return false;
				}
				else {
					// if no test value is specified, the existence of the variable is enough
					return true;
				}
			}
			else {
				// ! as the first character means negation.. 
				if (testValue.startsWith("!")) {
					return !testValue.substring(1).equalsIgnoreCase(value);
				}
				else {
					return testValue.equalsIgnoreCase(value);
				}
			}
		}
		
		// files
		ActivationFile activationFile = activation.getFile();
		if (activationFile != null) {
			// existing
			String existing = activationFile.getExists();
			if (existing != null) {
				// check if overridden (might have been a symbolic value)
				String value = resolveProperty(existing);				
				if (value != null) {
					existing = value;
				}
				File file = new File(existing);
				if (file.exists() == false)
					return false;
			}
			// missing
			String missing = activationFile.getMissing();
			if (missing != null) {
				// check if overridden (might have been a symbolic value)
				String value = resolveProperty(missing);
				if (value != null) {
					missing = value;
				}
				File file = new File( missing);
				if (file.exists())
					return false;
			}
		}
		return result;
	}
}
