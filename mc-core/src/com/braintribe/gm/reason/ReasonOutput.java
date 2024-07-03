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
package com.braintribe.gm.reason;

import static com.braintribe.console.ConsoleOutputs.brightRed;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;

public class ReasonOutput {
	
	private PolymorphicDenotationMap<GenericEntity, BiConsumer<ConfigurableConsoleOutputContainer, ? extends GenericEntity>> formatters = new PolymorphicDenotationMap<>();
	private boolean withType = false;
	
	public ReasonOutput() {
		registerFormatter(GenericEntity.T, this::formatEntity);
	}
	
	private void formatEntity(ConfigurableConsoleOutputContainer out, GenericEntity e) {
		out.append(e.toString());
	}
	
	public <E extends GenericEntity> void registerFormatter(EntityType<E> type,BiConsumer<ConfigurableConsoleOutputContainer, E> formatter) {
		formatters.put(type, formatter);
	}
	
	public <E extends GenericEntity> void registerFormatter(EntityType<E> type,Function<E, ConsoleOutput> formatter) {
		registerFormatter(type, (o, e) -> o.append(formatter.apply(e)));
	}
	
	public void print(Reason reason) {
		ConsoleOutputs.print(output(reason));
	}

	public void println(Reason reason) {
		ConsoleOutputs.println(output(reason));
	}
	
	public ConsoleOutput output(Reason reason) {
		ConfigurableConsoleOutputContainer out = ConsoleOutputs.configurableSequence();
		output(out, reason);
		return out;
	}
	
	public void output(ConfigurableConsoleOutputContainer out, Reason reason) {
		new ConsoleOutputReasonMessageCollector(out).format(reason, 0);
	}
	
	private class ConsoleOutputReasonMessageCollector implements ReasonMessageCollector {

		private ConfigurableConsoleOutputContainer out;
		
		public ConsoleOutputReasonMessageCollector(ConfigurableConsoleOutputContainer out) {
			super();
			this.out = out;
		}

		public void format(Reason reason, int depth) {
			try {
				writeIndentBulleted(depth);
				
				if (withType) {
					out.append(
						brightRed( //
							sequence( //
								text(reason.entityType().getShortName()), //
								text(": ") //
							) //
						)
					);
				}
				
				if (!TemplateReasons.format(reason, this)) {
					String text = reason.getText();
					if (text != null) {
						writeTextIndented(text, depth + 1);
					}
				}

				List<Reason> attachedReasons = reason.getReasons();
				
				for (Reason cause: attachedReasons) {
					out.append("\n");
					format(cause, depth + 1);
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		
		private void writeTextIndented(String text, int depth) throws IOException {
			int index = 0;
			
			do {
				int found = text.indexOf("\n", index);
				
				int end = found != -1? found + 1: text.length();

				if (index > 0) {
					writeIndent(depth);
				}
				
				out.append(brightRed(text.substring(index, end)));
				
				index = end;
			}
			while (index < text.length());
		}

		private void writeIndent(int depth) throws IOException {
			int i = 0;
					
			for (; i < depth; i++) {
				out.append("  ");
			}
		}
		
		private void writeIndentBulleted(int depth) throws IOException {
			int i = 0;
					
			for (; i < depth - 1; i++) {
				out.append("  ");
			}
			
			if (i < depth)
				out.append("- ");
		}
		
		@Override
		public void append(String text) {
			out.append(brightRed(text));
		}

		@Override
		public void appendProperty(GenericEntity entity, Property property, GenericModelType type, Object value) {
			outputValue(value, type);
		}
		
		private void outputValue(Object value, GenericModelType type) {
			if (value == null) {
				out.append("<n/a>");
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
				out.append(value.toString());
				break;

			case listType:
			case setType: {
				int i = 0;
				GenericModelType eT = ((LinearCollectionType)type).getCollectionElementType(); 
				for (Object e : (Collection<?>)value) {
					if (i > 0)
						out.append(", ");
					outputValue(e, eT.getActualType(e));
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
						out.append(", ");
				
					Object k = e.getKey();
					Object v = e.getValue();
					outputValue(k, kT.getActualType(k));
					out.append(" = ");
					outputValue(v, vT.getActualType(v));
					i++;
				}
				break;
			}
			case entityType:
				BiConsumer<ConfigurableConsoleOutputContainer, GenericEntity> formatter = formatters.get((EntityType<?>)type);
				formatter.accept(out, (GenericEntity)value);
				break;
			default:
				out.append("<n/a>");
				break;
			}
		}
	}
}
