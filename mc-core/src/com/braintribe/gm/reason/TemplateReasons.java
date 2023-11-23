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
package com.braintribe.gm.reason;

import static com.braintribe.utils.lcd.CollectionTools2.newConcurrentMap;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.bvd.string.Concatenation;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.vde.parser.TemplateStringParser;

import jsinterop.annotations.JsMethod;

public class TemplateReasons {
	private static Map<EntityType<? extends Reason>, Object> templates = newConcurrentMap();
	private static final Object missing = new Object();

	/**
	 * Starts a fluent {@link TemplateReasonBuilderImpl} that allows to build a reason.
	 */
	@JsMethod(name = "build", namespace = GmCoreApiInteropNamespaces.reason)
	public static <R extends Reason> TemplateReasonBuilder<R> build(EntityType<R> reasonType) {
		return new TemplateReasonBuilderImpl<>(reasonType);
	}
	
	private static Object resolveTemplate(Reason reason) {
		EntityType<Reason> entityType = reason.entityType();
		
		Object template = templates.get(entityType);
		
		if (template == null) {
			template = _resolveTemplate(reason);
			templates.put(entityType, template);
		}
		
		if (template == missing)
			return null;
		
		return template;
	}

	private static Object _resolveTemplate(Reason reason) {
		SelectiveInformation si = reason.entityType().getJavaType().getAnnotation(SelectiveInformation.class);
		
		if (si == null)
			return missing;
		
		return TemplateStringParser.parse(si.value());
	}
	
	static String buildPlainText(Reason reason) {
		PlainMessageCollector collector = new PlainMessageCollector();
		
		if (!format(reason, collector))
			return null;
		
		return collector.getResult();
	}
	
	private static class PlainMessageCollector implements ReasonMessageCollector {
		private StringBuilder builder = new StringBuilder();
		
		public String getResult() {
			return builder.toString();
		}

		@Override
		public void append(String text) {
			builder.append(text);
		}
		
		@Override
		public void appendProperty(GenericEntity entity, Property property, GenericModelType type, Object value) {
			outputValue(value, property.getType());
		}

		private void outputValue(Object value, GenericModelType type) {
			if (value == null) {
				builder.append("<n/a>");
				return;
			}
			
			switch (type.getTypeCode()) {
			case objectType:
				outputValue(value, type.getActualType(value));
				break;
				
			case booleanType:
			case dateType:
			case decimalType:
			case doubleType:
			case enumType:
			case floatType:
			case integerType:
			case longType:
			case stringType:
			case entityType:
				builder.append(value.toString());
				break;

			case listType:
			case setType: {
				int i = 0;
				GenericModelType eT = ((LinearCollectionType)type).getCollectionElementType(); 
				for (Object e : (Collection<?>)value) {
					if (i > 0)
						builder.append(", ");
					outputValue(e, eT);
					i++;
				}
				break;
			}
			case mapType: {
				int i = 0;
				MapType mapType = (MapType)type;
				
				GenericModelType kT = mapType.getKeyType(); 
				GenericModelType vT = mapType.getValueType(); 
				
				for (Map.Entry<?, ?> e : ((Map<?,?>)value).entrySet()) {
					if (i > 0)
						builder.append(", ");
				
					outputValue(e.getKey(), kT);
					builder.append(" = ");
					outputValue(e.getValue(), vT);
					i++;
				}
				break;
			}
			
			default:
				builder.append("<n/a>");
				break;
			}
		}
	}
	
	
	public static boolean format(Reason reason, ReasonMessageCollector collector) {
		Object object = resolveTemplate(reason);
		
		if (object == null)
			return false;
		
		formatTemplate(reason, object, collector);
		return true;
	}
	
	static void formatTemplateExpression(Reason reason, String templateExpression, ReasonMessageCollector collector) {
		Object value = TemplateStringParser.parse(templateExpression);
		formatTemplate(reason, value, collector);
	}
	
	static void formatTemplate(Reason reason, Object value, ReasonMessageCollector collector) {
		Evaluation evaluation = new Evaluation(reason, collector);
		evaluation.evaluate(value);
	}
	

	private static class Evaluation {
		static PolymorphicDenotationMap<ValueDescriptor, BiConsumer<Evaluation, ? extends ValueDescriptor>> templateExperts = new PolymorphicDenotationMap<>();
		
		private static <V extends ValueDescriptor> void registerExpert(EntityType<V> type, BiConsumer<Evaluation, V> expert) {
			templateExperts.put(type, expert);
		}
		
		static {
			registerExpert(Concatenation.T, Evaluation::evaluateConcatenation);
			registerExpert(Variable.T, Evaluation::evaluateVariable);
		}
		
		private Reason reason;
		ReasonMessageCollector collector;

		public Evaluation(Reason reason, ReasonMessageCollector collector) {
			super();
			this.reason = reason;
			this.collector = collector;
		}

		public void evaluate(Object value) {
			if (value == null) {
				collector.append("<n/a>");
			}
			else if (value instanceof ValueDescriptor) {
				evaluate((ValueDescriptor)value);
			}
			else {
				collector.append(value.toString());
			}
		}
		
		private void evaluate(ValueDescriptor vd) {
			BiConsumer<Evaluation, ValueDescriptor> expert = templateExperts.find(vd);
			
			if (expert == null)
				throw new UnsupportedOperationException("Unsupported ValueDescriptor in template evaluation: " + vd.type().getTypeSignature());

			expert.accept(this, vd);
		}

		private void evaluateVariable(Variable var) {
			String propertyName = var.getName();
			Property property = reason.entityType().getProperty(propertyName);
			Object propertyValue = property.get(reason);
			GenericModelType type = property.getType().getActualType(propertyValue);
			collector.appendProperty(reason, property, type, propertyValue);
		}
		
		private void evaluateConcatenation(Concatenation concatenation) {
			for (Object operand: concatenation.getOperands()) {
				evaluate(operand);
			}
		}
	}
	
}
