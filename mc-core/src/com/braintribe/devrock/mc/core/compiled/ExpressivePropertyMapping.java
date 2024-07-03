// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
