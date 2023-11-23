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
package com.braintribe.devrock.mc.core.compiled;

import java.util.function.Function;

import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

/**
 * the mapping for expressive properties, such as 'artifact redirections', 'global exclusions', 'global dominants'
 * @author pit / dirk
 *
 * @param <D> - the declared type 
 * @param <T> - the final type 
 */
public class ExpressivePropertyMapping<D, T> {
	private GenericModelType declaredType;
	private Property property;
	Function<D, T> transformerFunction;
	
	/**
	 * @return - the {@link GenericModelType} as declared in the YAML snippet
	 */
	public GenericModelType getDeclaredType() {
		return declaredType;
	}
	public void setDeclaredType(GenericModelType declaredType) {
		this.declaredType = declaredType;
	}
	/**
	 * @return - the property of the {@link CompiledArtifact} to set
	 */
	public Property getProperty() {
		return property;
	}
	public void setProperty(Property property) {
		this.property = property;
	}
	/**
	 * @return - a function that can turn the declared type into the final type 
	 */
	public Function<D, T> getTransformerFunction() {
		return transformerFunction;
	}
	public void setTransformerFunction(Function<D, T> transformerFunction) {
		this.transformerFunction = transformerFunction;
	}

}
