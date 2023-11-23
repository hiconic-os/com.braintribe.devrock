// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings.properties;

import java.util.Collection;
import java.util.StringTokenizer;

import com.braintribe.build.artifact.representations.artifact.maven.properties.CommonPropertyResolver;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;

/**
 * @author pit
 *
 */
public abstract class AbstractSettingsPropertyResolver extends CommonPropertyResolver{

	private static Logger log = Logger.getLogger(AbstractSettingsPropertyResolver.class);
	

	/**
	 * uses TypeReflection to look up property values 
	 * @param initial - the GenericEntity which is the starting point 
	 * @param expression - the expression to parse (can have multiple points, i.e a path to a property
	 * @return - the string representation of the last element in the expression 
	 */
	@SuppressWarnings("rawtypes")
	protected String resolveValue(GenericEntity initial, String expression) {		
		GenericEntity entity = initial;
		StringBuilder processed = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(expression, ".");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (processed.length() > 0) {
				processed.append( ".");
			}
			processed.append( token);
			
			Property property;
			try {
				property = GMF.getTypeReflection().getEntityType(entity.type().getTypeSignature()).getProperty( token);
			} catch (GenericModelException e) {
				if (log.isDebugEnabled()) { 
					log.warn("cannot find a value for [" + expression + "] as it's not a property of the type [" + initial.getClass().getName());
				}
				return null;
			}
			Object obj = property.get( entity);
			if (obj instanceof String)
				return (String) obj;
			if (obj == null)
				return null;		
			if (obj instanceof GenericEntity == false) {
				if (obj instanceof Collection) {
					String remainingTokens = expression.substring( processed.length()+1);
					for (Object suspect : (Collection) obj) {
						if (suspect instanceof GenericEntity) {
							if (suspect instanceof com.braintribe.model.maven.settings.Property) {
								com.braintribe.model.maven.settings.Property prop = (com.braintribe.model.maven.settings.Property) suspect;
								if (prop.getName().equalsIgnoreCase( remainingTokens)) {
									return prop.getRawValue();
								}
							}
							else {
								String value = resolveValue( (GenericEntity) suspect, remainingTokens);
								if (value != null) {							
									return value;
								}
							}
						}	
					}
					return null;
				}								
			}
			if (tokenizer.hasMoreTokens() == false)
				return null;
			entity = (GenericEntity) obj;			
		}		
		return null;
	}

}
