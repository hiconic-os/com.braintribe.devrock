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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.mc.reason.UndefinedProperty;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.value.UnsatisfiedValue;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.processing.clone.AbstractDirectCloning;
import com.braintribe.model.processing.clone.BasicCloneTarget;
import com.braintribe.model.processing.clone.CloneTarget;
import com.braintribe.model.processing.clone.CloningVisitor;

public class StatefulPropertyResolution extends AbstractDirectCloning implements CloningVisitor {

	private ArtifactPropertyResolution propertyResolution;
	private Deque<Pair<GenericEntity, Property>> propertyStack = new ArrayDeque<>();

	private Map<GenericEntity, CloneTarget> entities = new ConcurrentHashMap<>();
	private Object currentMapKey;
	private Property propertiesProperty = DeclaredArtifact.T.getProperty(DeclaredArtifact.properties);
	private Map<String, Reason> propertyProblems = new HashMap<>();

	public StatefulPropertyResolution(DeclaredArtifact artifact) {
		propertyResolution = new ArtifactPropertyResolution(artifact);
		setCloningVisitor(this);
	}
	
	public StatefulPropertyResolution(CompiledArtifact compiledArtifact) {
		propertyResolution = new ArtifactPropertyResolution(compiledArtifact);
		setCloningVisitor(this);
	}

	public Map<String, Reason> getPropertyProblems() {
		return propertyProblems;
	}

	@Override
	protected CloneTarget acquireCloneTarget(GenericEntity entity) {
		return entities.computeIfAbsent(entity, this::createCloneTarget);
	}

	private CloneTarget createCloneTarget(GenericEntity e) {
		GenericEntity entity = e.entityType().createRaw(StatefulPropertyResolutionPai.INSTANCE);
		return new BasicCloneTarget(entity, true);
	}

	@Override
	public void enterPropertyValue(GenericEntity entity, GenericEntity clonedEntity, Property property,
			GenericModelType type, Object value) {
		propertyStack.push(Pair.of(clonedEntity, property));
	}

	@Override
	public void leavePropertyValue(GenericEntity entity, GenericEntity clonedEntity, Property property,
			GenericModelType type, Object value) {
		propertyStack.pop();
	}

	protected Maybe<String> resolvePropertyPlaceholders(String expression) {
		return propertyResolution.resolvePropertyPlaceholders(expression);
	}

	@Override
	public void enterMapKey(MapType mapType, Map<?, ?> map, GenericModelType type, Object value) {
		currentMapKey = value;
	}

	@Override
	protected Object doCloneScalar(GenericModelType type, Object value) {
		if (value == null)
			return null;
		
		if (type == EssentialTypes.TYPE_STRING) {
			String expressionCandidate = (String) value;
			Maybe<String> maybe = propertyResolution.resolvePropertyPlaceholders(expressionCandidate);

			if (maybe.isUnsatisfied()) {
				Reason whyUnsatisfied = maybe.whyUnsatisfied();

				invalidatePropertyPath(whyUnsatisfied);

				if (getCurrentProperty() == propertiesProperty) {
					String properyName = (String) currentMapKey;
					propertyProblems.put(properyName, whyUnsatisfied);
				}

				return null;
			} else {
				String resolvedValue = maybe.get();
				
				if (resolvedValue.isEmpty())
					return null;
				
				return resolvedValue;
			}
		}

		return value;
	}

	private Property getCurrentProperty() {
		if (propertyStack.isEmpty())
			return null;

		return propertyStack.peek().second();
	}

	private void invalidatePropertyPath(Reason whyUnsatisfied) {

		int i = 0;
		for (Pair<GenericEntity, Property> propertyLevel : propertyStack) {
			GenericEntity entity = propertyLevel.first();
			Property property = propertyLevel.second();

			if (property.getType().getTypeCode() == TypeCode.mapType)
				break;

			if (i == 0) {
				setUnsatisfied(entity, property, whyUnsatisfied);
			} else {
				Reason reason = getCollectorReason(entity, property);
				reason.getReasons().add(whyUnsatisfied);
				whyUnsatisfied = reason;
			}

			if (entity instanceof DeclaredDependency)
				break;
			i++;
		}

	}

	private Reason getCollectorReason(GenericEntity entity, Property property) {
		ValueDescriptor valueDescriptor = property.getVd(entity);

		if (valueDescriptor instanceof UnsatisfiedValue) {
			return ((UnsatisfiedValue) valueDescriptor).getWhy();
		} else {
			UndefinedProperty reason = Reasons.build(UndefinedProperty.T)
					.text("Undefined property " + entity.entityType().getShortName() + "." + property.getName())
					.toReason();
			setUnsatisfied(entity, property, reason);

			return reason;
		}
	}

	private void setUnsatisfied(GenericEntity entity, Property property, Reason reason) {
		UnsatisfiedValue unsatisfiedValue = UnsatisfiedValue.create(reason);
		property.setVd(entity, unsatisfiedValue);
	}

	@Override
	protected void transferProperty(GenericEntity entity, Property property, Object value) {
		Object vdCandidate = property.getDirectUnsafe(entity);
		if (VdHolder.isVdHolder(vdCandidate))
			return;

		super.transferProperty(entity, property, value);
	}
}
