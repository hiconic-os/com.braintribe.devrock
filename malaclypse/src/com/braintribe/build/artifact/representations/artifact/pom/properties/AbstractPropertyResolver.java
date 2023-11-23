// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.braintribe.build.artifact.representations.artifact.maven.properties.CommonPropertyResolver;
import com.braintribe.build.artifact.representations.artifact.pom.VersionCodec;
import com.braintribe.build.artifact.representations.artifact.pom.VersionRangeCodec;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;

/**
 * resolver functions for environments and java system properties<br/> 
 * <ul>
 * 	<li>env : denotes a enviroment variable's value</li>
 *  <li>anything else: this deemed to be a java system property</li>
 * </ul>
 * if neither ways lead to a value, null is returned
 * @author pit
 *
 */
public class AbstractPropertyResolver extends CommonPropertyResolver {
	private static Logger log = Logger.getLogger(AbstractPropertyResolver.class);	
	
	private Map<String, Codec<GenericEntity, String>> codecs = new HashMap<String, Codec<GenericEntity,String>>();
	
	protected AbstractPropertyResolver() {
		codecs.put("com.braintribe.model.artifact.version.Version", new VersionCodec());
		codecs.put("com.braintribe.model.artifact.version.VersionRange", new VersionRangeCodec());
	}
	
	/**
	 * uses TypeReflection to look up property values 
	 * @param initial - the GenericEntity which is the starting point 
	 * @param expression - the expression to parse (can have multiple points, i.e a path to a property
	 * @return - the string representation of the last element in the expression 
	 */
	protected String resolve(GenericEntity initial, String expression) {
		GenericEntity entity = initial;
		StringTokenizer tokenizer = new StringTokenizer(stripExpression(expression), ".");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			// if initial entity is a solution, then "parent" is the original dependency and not the 
			// actual solution which is resolved and attached, this one is  "resolvedParent". For lookups, the resolved
			// parent solution is of interest and not what the codec read from the pom.
			if (entity instanceof Solution && token.equals( "parent")) {
				token = "resolvedParent";				
			}
			Property property;
			try {								
				property = GMF.getTypeReflection().getEntityType( entity.type().getTypeSignature()).getProperty( token);
			} catch (GenericModelException e) {
				if (log.isDebugEnabled()) {
					log.debug("cannot find a value for [" + expression + "] as it's not a property of the type [" + initial.getClass().getName());
				}
				return null;
			}
			Object obj = property.get( entity);
		
			if (tokenizer.hasMoreTokens() == false) { 
				if (obj instanceof String)
					return (String) obj;
				if (obj == null)
					return null;
				if (obj instanceof GenericEntity == false) {
					return obj.toString();
				}
				String signature = ((GenericEntity) obj).entityType().getTypeSignature();
				Codec<GenericEntity, String> codec = codecs.get( signature);
				if (codec != null) {
					try {
						return codec.encode( (GenericEntity) obj);
					} catch (CodecException e) {
						String msg="cannot convert property value to string by codec";
						log.warn( msg, e);
						return null;
					}
				}
			}
			
			if (obj instanceof GenericEntity) {
				entity = (GenericEntity) obj;		
			}
		}		
		return null;
	}
	
	private String stripExpression(String expression){
		int i = expression.indexOf("${");
		if (i < 0)
			return expression;
		return expression.substring( i+2, expression.length()-1);
	}
	

}
