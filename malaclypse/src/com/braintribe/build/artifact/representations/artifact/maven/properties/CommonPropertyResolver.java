// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.properties;

import java.util.Properties;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.properties.PropertyResolvingException;
import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * common base for property resolving in {@link MavenSettingsReader} and {@link ArtifactPomReader},
 * allows for overloading system and environment property resolving, see {@link #setExternalPropertyResolverOverride(VirtualPropertyResolver)}
 * @author pit
 *
 */
public class CommonPropertyResolver implements VirtualPropertyResolver {
	private static Logger log = Logger.getLogger(CommonPropertyResolver.class);
	private Properties properties = System.getProperties();
	private VirtualPropertyResolver externalPropertyResolverOverride;
	protected VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	
	public CommonPropertyResolver() {
		super();
	}

	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	@Configurable
	public void setExternalPropertyResolverOverride(VirtualPropertyResolver externalPropertyResolverOverride) {
		this.externalPropertyResolverOverride = externalPropertyResolverOverride;
	}
	/**
	 * just checks if the expression contains ${..} somehow
	 * @param expression - the string to check 
	 * @return - true if a variable reference is in the string and false otherwise 
	 */
	protected boolean requiresEvaluation(String expression) {
		String extract = extract(expression);
		return !extract.equalsIgnoreCase(expression);
	}

	/**
	 * extracts the first variable in the expression 
	 * @param expression - the {@link String} to extract the variable from 
	 * @return - the first variable (minus the ${..} stuff)
	 */
	protected String extract(String expression) {
		int p = expression.indexOf( "${");
		if (p < 0)
			return expression;
		int q = expression.indexOf( "}", p+1);
		return expression.substring(p+2, q);	
	}

	/**
	 * replaces any occurrence of the variable by its value 
	 * @param variable - without ${..}, it will be added 
	 * @param value - the value of the variable
	 * @param expression - the expression to replace in 
	 * @return - the expression after the replace 
	 */
	protected String replace(String variable, String value, String expression) {
		return expression.replace( "${" + variable + "}", value);
	}

	/**
	 * splits an expression - typically a variable into two parts<br/>
	 * - the part before the first point<br/>
	 * - the remainder. 
	 * @param expression - such as env.* or settings.* or project.*
	 * @return - a tuple as an array 
	 */
	protected String [] split(String expression) {
		String [] tokens = new String[2];
		int p = expression.indexOf(".");
		if (p == 0) {
			tokens[1] = expression.substring(1);
		}
		else if (p > 0) {
			tokens[0] = expression.substring(0, p);
			tokens[1] = expression.substring( p+1);
		}
		else {
			tokens[0] = expression;
		}			
		return tokens;
	}

	/**
	 * looks up a system property, if there isn't any named like this, it throws an {@link PropertyResolvingException}
	 * @param key - the name of the property 
	 * @return - the value of the property  
	 */
	@Override
	public String getSystemProperty(String key) {
		String value = null;
		if (externalPropertyResolverOverride != null) {
			try {
				value = externalPropertyResolverOverride.getSystemProperty(key);
				if (value != null)
					return value;
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("external property resolver has thrown exception, using default system property resolver", e);
				}				
			}
		}
		value = virtualEnvironment.getProperty(key);
		if (value == null)
			value = properties.getProperty(key);
		return value;
	}
	@Override
	public String getEnvironmentProperty( String key) {
		String keyToUse = key;
		if (keyToUse.startsWith( "env.")) {
			keyToUse = keyToUse.substring( "env.".length());
		}
		String value = null;
		if (externalPropertyResolverOverride != null) {
			value = externalPropertyResolverOverride.getEnvironmentProperty(keyToUse);
			if (value != null)
				return value;
		}
		return virtualEnvironment.getEnv(keyToUse);
	}

	@Override
	public boolean isActive() {
		if (externalPropertyResolverOverride != null)
			return externalPropertyResolverOverride.isActive();
		return false;
	}

	@Override
	public String resolve(String expression) {
		if (!requiresEvaluation(expression))
			return expression;
		String value = expression;
		do {
			String key = extract(value);
			String v = getEnvironmentProperty(key);
			if (v == null) {
				v = getSystemProperty(key);
			}
			value = replace( key, v, value);			
		} while ( requiresEvaluation( value));
		return value;
	}
	
	

}
