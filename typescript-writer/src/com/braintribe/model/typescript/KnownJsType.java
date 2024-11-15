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
package com.braintribe.model.typescript;

import static com.braintribe.model.typescript.TypeScriptWriterHelper.extractSimpleName;
import static com.braintribe.utils.lcd.CollectionTools2.newIdentityMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Describes either a native JS type, or a JsType type from gwt-user emulation, i.e. a type which in GWT world has a JsType annotation, but in our
 * world the annotation is not visible and thus we have to hard-code it.
 * 
 * @author peter.gazdik
 */
/* package */class KnownJsType {

	public static final String JS_INTEROP_AUTO = "<auto>";
	public static final String JS_INTEROP_GLOBAL = "<global>";
	public static final String JS_INTEROP_GLOBAL_THIS = "globalThis";

	public static final String NS_TYPE = "T";
	public static final String NS_HC = "hc";
	public static final String NS_ASYNC = NS_HC + ".session";

	public static final String SYMBOL_ENUM_TYPE = NS_HC + ".Symbol.enumType";

	public static final Map<Class<?>, KnownJsType> java2Ts = newIdentityMap();
	public static final Map<String, QualifiedName> java2NsWeak = newMap();

	public static final KnownJsType TS_NUMBER = new KnownJsType("number");
	public static final KnownJsType TS_BOOLEAN = new KnownJsType("boolean");
	public static final KnownJsType TS_ANY = new KnownJsType("any");
	public static final KnownJsType TS_STRING = new KnownJsType("string");

	public static final KnownJsType TS_LIST;
	public static final KnownJsType TS_SET;
	public static final KnownJsType TS_MAP;

	private static record QualifiedName(String name, String namespace) {

	}

	static {
		java2Ts.put(String.class, TS_STRING);

		java2Ts.put(int.class, TS_NUMBER);
		java2Ts.put(short.class, TS_NUMBER);
		java2Ts.put(byte.class, TS_NUMBER);
		java2Ts.put(float.class, TS_NUMBER);
		java2Ts.put(double.class, TS_NUMBER);
		java2Ts.put(Double.class, TS_NUMBER);
		java2Ts.put(char.class, TS_NUMBER);

		java2Ts.put(boolean.class, TS_BOOLEAN);
		java2Ts.put(Boolean.class, TS_BOOLEAN);

		registerKnownJsType(Byte.class, NS_HC);
		registerKnownJsType(Character.class, NS_HC);
		registerKnownJsType(CharSequence.class, NS_HC);
		registerKnownJsType(Date.class, NS_HC);
		registerKnownJsType(BigDecimal.class, NS_TYPE, "Decimal");
		registerKnownJsType(Float.class, NS_HC);
		registerKnownJsType(Integer.class, NS_HC);
		registerKnownJsType(Long.class, NS_HC);

		registerKnownJsType(Class.class, NS_HC);
		registerKnownJsType(Enum.class, NS_HC);

		registerKnownJsType(Comparable.class, NS_HC);
		registerKnownJsType(Exception.class, NS_HC);
		registerKnownJsType(Iterable.class, NS_HC);
		registerKnownJsType(RuntimeException.class, NS_HC);
		registerKnownJsType(StackTraceElement.class, NS_HC);
		registerKnownJsType(Throwable.class, NS_HC);
		registerKnownJsType(Void.class, NS_HC);

		registerKnownJsType(InputStream.class, NS_HC);
		registerKnownJsType(OutputStream.class, NS_HC);

		registerKnownJsType(BiConsumer.class, NS_HC);
		registerKnownJsType(BiFunction.class, NS_HC);
		registerKnownJsType(BinaryOperator.class, NS_HC);
		registerKnownJsType(Consumer.class, NS_HC);
		registerKnownJsType(Function.class, NS_HC);
		registerKnownJsType(Predicate.class, NS_HC);
		registerKnownJsType(Supplier.class, NS_HC);
		registerKnownJsType(UnaryOperator.class, NS_HC);

		registerKnownJsType(Stream.class, NS_HC);
		registerKnownJsType(Collector.class, NS_HC);
		registerKnownJsType(Collector.Characteristics.class, NS_HC);

		registerKnownJsType(Collection.class, NS_HC);
		registerKnownJsType(Comparator.class, NS_HC);
		registerKnownJsType(Iterator.class, NS_HC);
		registerKnownJsType(List.class, NS_HC);
		registerKnownJsType(ListIterator.class, NS_HC);
		registerKnownJsType(Map.class, NS_HC);
		registerKnownJsType(Map.Entry.class, NS_HC);
		registerKnownJsType(Optional.class, NS_HC);
		registerKnownJsType(Set.class, NS_HC);
		registerKnownJsType(Stack.class, NS_HC);

		registerWeakJsType("com.google.gwt.core.client.JsDate", "Date", JS_INTEROP_GLOBAL_THIS);
		registerWeakJsType("com.google.gwt.user.client.rpc.AsyncCallback", "AsyncCallback", NS_ASYNC);

		// This can obviously be only set after we register known JsType for Long.class
		java2Ts.put(long.class, java2Ts.get(Long.class));

		TS_LIST = java2Ts.get(List.class);
		TS_SET = java2Ts.get(Set.class);
		TS_MAP = java2Ts.get(Map.class);
	}

	public static KnownJsType resolveIfTypeKnownByName(Class<?> clazz) {
		QualifiedName qn = KnownJsType.java2NsWeak.get(clazz.getName());
		if (qn == null) {
			return null;
		}

		KnownJsType result = new KnownJsType(qn.name, qn.namespace);
		java2Ts.put(clazz, result);

		return result;
	}

	private static void registerKnownJsType(Class<?> clazz, String namespace) {
		registerKnownJsType(clazz, namespace, extractSimpleName(clazz));
	}

	private static void registerKnownJsType(Class<?> clazz, String namespace, String name) {
		java2Ts.put(clazz, new KnownJsType(name, namespace));
	}

	private static void registerWeakJsType(String className, String tsName, String tsNamespace) {
		java2NsWeak.put(className, new QualifiedName(tsName, tsNamespace));
	}

	public final String name;
	public final String namespace;
	public final String fullName;

	private KnownJsType(String name) {
		this(name, JS_INTEROP_AUTO, name);
	}

	private KnownJsType(String name, String namespace) {
		this(name, namespace, namespace + "." + name);
	}

	private KnownJsType(String name, String namespace, String fullName) {
		this.name = name;
		this.namespace = namespace;
		this.fullName = fullName;
	}

}
