// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.devrock.mc.core.configuration.maven;

import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.model.MergeContext;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * a cloner that has a list of properties to work with 
 * 
 * deviates from a standard cloner as it also replaces variables in GE properties that aren't of 
 * base-type string.. hack, yes, but it should be fine (for now).. settings.xml will be deprecated anyhow
 * see  {@link #clonedValuePostProcessor(GenericModelType, Object)}
 * 
 * @author pit
 *
 */
public class ConceptualizedCloner {
	
	private Map<String, String> effectiveProperties;
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	
	@Configurable @Required
	public void setEffectiveProperties(Map<String, String> effectiveProperties) {
		this.effectiveProperties = effectiveProperties;
	}

	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	/**
	 * @param variable - the variable name to resolve
	 * @return - the value of the variable or null if not found 
	 */
	private String lookupProperty(String variable) {	
		// environment variable reference
		if (variable.startsWith( "env.")) {
			String key = variable.substring( 4);
			return virtualEnvironment.getEnv(key);
		}		
		return virtualEnvironment.getProperty(variable);		
	}
	
	/**
	 * @param type - the {@link GenericModelType} as passed by cloning
	 * @param obj - the {@link Object} to look at
	 * @return - either the object as entered, or if string, a resolved property 
	 */
	public Object clonedValuePostProcessor( GenericModelType type, Object obj) {
		if (obj instanceof String == false)
			return obj;
		String value = (String) obj;
		if (type == EssentialTypes.TYPE_STRING) {
			return resolvePropertyPlaceholders(value);
		} 
		else if (type == EssentialTypes.TYPE_OBJECT) {
			// hack for the ID property used for Repository, Profile, Server
			if (value.startsWith( "${")) {
				return resolvePropertyPlaceholders(value);
			}
		}
		return obj;
	}
	
	/**
	 * @param expression - the expression to resolved all variables in
	 * @return - the resolved expression
	 */
	private String resolvePropertyPlaceholders( String expression) {
		if (expression == null)
			return null;
		Template template = Template.parse(expression);
		MergeContext mergeContext = new MergeContext();
		mergeContext.setVariableProvider( n -> {			
			String property = effectiveProperties.get(n);
			if (property == null) {
				property = lookupProperty(n);
			}
			if (property != null) {				
				return resolvePropertyPlaceholders( property);
			}
			else {						
				return "${" + n + "}";
			}
		});
		
		return template.merge(mergeContext);
	}
		
}
