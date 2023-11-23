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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.mc.reason.UnresolvedProperty;
import com.braintribe.devrock.model.mc.reason.UnresolvedPropertyExpression;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledSolution;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.core.expert.api.MutableDenotationMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.model.MergeContext;

public class ArtifactPropertyResolution {
	private static final String UNDEFINED = new String("<undefined>");
	
	private DeclaredArtifact artifact;
	private Map<String, Maybe<String>> effectiveProperties = new ConcurrentHashMap<>();
	private CompiledArtifact compiledArtifact;
	private boolean consumeEscapes = true;
	
	private static MutableDenotationMap<GenericEntity, BiFunction<? extends GenericEntity, String, Maybe<Pair<GenericModelType, ?>>>> specialPropertyResolvers = new PolymorphicDenotationMap<>();
	
	private static <T extends GenericEntity> void registerSpecialPropertyResolver(EntityType<T> type, BiFunction<T, String, Maybe<Pair<GenericModelType, ?>>> resolver) {
		specialPropertyResolvers.put(type, resolver);
	}
	
	static {
		registerSpecialPropertyResolver(GenericEntity.T, ArtifactPropertyResolution::resolveSpecialPropertyGenerically);
		registerSpecialPropertyResolver(CompiledArtifact.T, ArtifactPropertyResolution::resolveSpecialPropertyFromCompiledArtifact);
	}
	
	private static Maybe<Pair<GenericModelType, ?>> resolveSpecialPropertyFromCompiledArtifact(CompiledArtifact entity, String property) {
		switch (property) {
		case "parent":
			CompiledSolution parentSolution = entity.getParentSolution();
			
			if (parentSolution == null)
				return Reasons.build(NotFound.T).text("Property parent not found in artifact " + entity).toMaybe();
			
			if (parentSolution.hasFailed())
				return parentSolution.getFailure().asMaybe();
			
			return Maybe.complete(Pair.of(CompiledArtifact.T, parentSolution.getSolution()));
			
		default:
			return resolveSpecialPropertyGenerically(entity, property);
		}
	}
	
	private static Maybe<Pair<GenericModelType, ?>> resolveSpecialPropertyGenerically(GenericEntity entity, String propertyName) {
		Property property = entity.entityType().findProperty(propertyName);
		
		if (property == null) {
			return Reasons.build(NotFound.T).text("Property " + propertyName + " not found in entity " + entity).toMaybe();
		}

		return Maybe.complete(Pair.of(property.getType(), property.get(entity)));
	}
	
	private static Maybe<Pair<GenericModelType, ?>> resolveSpecialProperty(GenericEntity entity, String propertyName) {
		BiFunction<GenericEntity, String, Maybe<Pair<GenericModelType, ?>>> biFunction = specialPropertyResolvers.get(entity);
		return biFunction.apply(entity, propertyName);
	}

	
	public ArtifactPropertyResolution(DeclaredArtifact artifact) {
		this.artifact = artifact;
	}
	
	public ArtifactPropertyResolution(DeclaredArtifact artifact, CompiledArtifact compiledArtifact) {
		this.artifact = artifact;
		this.compiledArtifact = compiledArtifact;
	}
	
	public ArtifactPropertyResolution(CompiledArtifact compiledArtifact) {
		this.compiledArtifact = compiledArtifact;
		this.consumeEscapes = false;
	}
	
	/**
	 * @param expression - the expression to resolved all variables in
	 * @return - the resolved expression
	 */
	public Maybe<String> resolvePropertyPlaceholders( String expression) {
		if (expression == null)
			return Maybe.complete(null);
		
		Template template = Template.parse(expression, consumeEscapes);
		
		if (!template.containsVariables())
			return Maybe.complete(expression);
		
		LazyInitialized<UnresolvedPropertyExpression> lazyReason = new LazyInitialized<>(() -> 
			Reasons.build(UnresolvedPropertyExpression.T).text("Could not evaluate property expression: " + expression).toReason());
		
		MergeContext mergeContext = new MergeContext();
		mergeContext.setVariableProvider(n -> {
			Maybe<String> result = resolveProperty(n);
			
			if (result.isUnsatisfied()) {
				lazyReason.get().getReasons().add(result.whyUnsatisfied());
				return "";
			}
			
			return result.get();
		});
		
		String result = template.merge(mergeContext);
		
		if (lazyReason.isInitialized()) {
			return lazyReason.get().asMaybe();
		}
			
		return Maybe.complete(result);
	}
	
	public Maybe<String> resolveProperty(String name) {
		Maybe<String> value = effectiveProperties.get(name);
		
		if (value == null) {
			value = _resolveProperty(name);
			effectiveProperties.put(name, value);
		}
		
		return value;
	}
	
	private Iterator<String> iteratePropertyPath(String path) {
		StringTokenizer tokenizer = new StringTokenizer(path, ".");
		// TODO : once group has been officially allowed to use higher java 
		// version, remove this cludge... 
		List<String> result = new ArrayList<>();
		while (tokenizer.hasMoreElements()) {
			result.add( tokenizer.nextToken());
		}
		return result.iterator();
		//return (Iterator<String>)(Object)tokenizer.asIterator();
	}
	
	private Maybe<String> resolveProjectPropertyPath(Iterator<String> path) {
		GenericEntity currentEntity = compiledArtifact;
		
		Object currentValue = currentEntity;
		
		while (path.hasNext()) {
			if (currentEntity == null)
				return Reasons.build(NotFound.T).text("Invalid property path").toMaybe();
			
			String property = path.next();
			
			Maybe<Pair<GenericModelType, ?>> valueMaybe = resolveSpecialProperty(currentEntity, property);

			if (valueMaybe.isUnsatisfied()) {
				return valueMaybe.whyUnsatisfied().asMaybe();
			}
			
			Pair<GenericModelType, ?> typeAndValue = valueMaybe.get();
			
			GenericModelType type = typeAndValue.first();
			currentValue = typeAndValue.second();
			
			if (type.isEntity()) {
				currentEntity = (GenericEntity)currentValue;
			} else
				currentEntity = null;
		}
		
		if (currentValue == null)
			return Maybe.complete("");
		
		return Maybe.complete(currentValue.toString());
	}
	
	
	private Maybe<String> resolveSpecialProperty(String propertyPath) {
		Iterator<String> it = iteratePropertyPath(propertyPath);
		
		if (it.hasNext()) {
			String firstElement = it.next();
			
			switch (firstElement) {
			case "pom":
			case "project":
				return resolveProjectPropertyPath(it);
			
			// legacy property paths
			case "groupId":
			case "version":
			case "artifactId":
			case "parent":
				return resolveProjectPropertyPath(iteratePropertyPath(propertyPath));
				
			default:
				break;
			}
		}
		
		// TODO: optimize by not always constructing a reason here
		return Reasons.build(NotFound.T).text("property path not found").toMaybe();
	}
	
	private Maybe<String> _resolveProperty(String name) {
		if (compiledArtifact != null) {
			Maybe<String> valueMaybe = resolveSpecialProperty(name);
			
			if (valueMaybe.isSatisfied())
				return valueMaybe;
			
			if (artifact == null)
				return Maybe.complete("${" + name + "}");
		}
		
		// environment variable reference
		if (name.startsWith( "env.")) {
			String key = name.substring( 4);
			String value = System.getenv(key);
			
			if (value == null)
				return Reasons.build(UnresolvedProperty.T).text("Undefined environment property " + name).toMaybe();

			return Maybe.complete(value);
		}
		
		// settings variable reference
		if (name.startsWith( "settings.")) {
			// currently not supported
			return Reasons.build(UnresolvedProperty.T).text("Settings variables are not supported: " + name).toMaybe();
		}
		
		// property of owning artifact
		Map<String, String> properties = artifact.getProperties();
		String value = properties.getOrDefault(name, UNDEFINED);
		
		if (value == null)
			return Maybe.complete("");
			
		if (value == UNDEFINED)
			value = null;
					
		if (value != null) {
			Maybe<String> resolvedValueMaybe = resolvePropertyPlaceholders(value);
			if (resolvedValueMaybe.isUnsatisfied()) {
				return Reasons.build(UnresolvedProperty.T) //
						.text("Unresolved artifact declaration property: " + name) //
						.cause(resolvedValueMaybe.whyUnsatisfied()) //
						.toMaybe();
			}
			return resolvedValueMaybe;
		}

		// last fallback are system properties
		value = System.getProperty(name);
		if (value == null)
			return Reasons.build(UnresolvedProperty.T).text("Unresolved artifact declaration property: " + name).toMaybe();
		
		return Maybe.complete(value);
	}
}