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

	public static String JS_INTEROP_AUTO = "<auto>";
	public static String JS_INTEROP_GLOBAL = "<global>";

	public static final Map<Class<?>, KnownJsType> java2Ts = newIdentityMap();
	public static final Map<String, String> java2NsWeak = newMap();

	public static final KnownJsType TS_NUMBER = new KnownJsType("number");
	public static final KnownJsType TS_BOOLEAN = new KnownJsType("boolean");
	public static final KnownJsType TS_ANY = new KnownJsType("any");
	public static final KnownJsType TS_STRING = new KnownJsType("string");

	public static final KnownJsType TS_LIST;
	public static final KnownJsType TS_SET;
	public static final KnownJsType TS_MAP;

	static {
		final String NS_JAVA = "$tf";
		final String NS_VIEW = "$tf.view";
		final String NS_ASYNC = "$tf.session";

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

		registerKnownJsType(Byte.class, NS_JAVA);
		registerKnownJsType(Character.class, NS_JAVA);
		registerKnownJsType(CharSequence.class, NS_JAVA);
		registerKnownJsType(Date.class, NS_JAVA);
		registerKnownJsType(BigDecimal.class, NS_JAVA);
		registerKnownJsType(Float.class, NS_JAVA);
		registerKnownJsType(Integer.class, NS_JAVA);
		registerKnownJsType(Long.class, NS_JAVA);

		registerKnownJsType(Class.class, NS_JAVA);
		registerKnownJsType(Enum.class, NS_JAVA);

		registerKnownJsType(Comparable.class, NS_JAVA);
		registerKnownJsType(Exception.class, NS_JAVA);
		registerKnownJsType(Iterable.class, NS_JAVA);
		registerKnownJsType(RuntimeException.class, NS_JAVA);
		registerKnownJsType(StackTraceElement.class, NS_JAVA);
		registerKnownJsType(Throwable.class, NS_JAVA);
		registerKnownJsType(Void.class, NS_JAVA);

		registerKnownJsType(InputStream.class, NS_JAVA);
		registerKnownJsType(OutputStream.class, NS_JAVA);

		registerKnownJsType(BiConsumer.class, NS_JAVA);
		registerKnownJsType(BiFunction.class, NS_JAVA);
		registerKnownJsType(BinaryOperator.class, NS_JAVA);
		registerKnownJsType(Consumer.class, NS_JAVA);
		registerKnownJsType(Function.class, NS_JAVA);
		registerKnownJsType(Predicate.class, NS_JAVA);
		registerKnownJsType(Supplier.class, NS_JAVA);
		registerKnownJsType(UnaryOperator.class, NS_JAVA);

		registerKnownJsType(Stream.class, NS_JAVA);
		registerKnownJsType(Collector.class, NS_JAVA);
		registerKnownJsType(Collector.Characteristics.class, NS_JAVA);

		registerKnownJsType(Collection.class, NS_JAVA);
		registerKnownJsType(Comparator.class, NS_JAVA);
		registerKnownJsType(Iterator.class, NS_JAVA);
		registerKnownJsType(List.class, NS_JAVA);
		registerKnownJsType(ListIterator.class, NS_JAVA);
		registerKnownJsType(Map.class, NS_JAVA);
		registerKnownJsType(Map.Entry.class, NS_JAVA);
		registerKnownJsType(Optional.class, NS_JAVA);
		registerKnownJsType(Set.class, NS_JAVA);
		registerKnownJsType(Stack.class, NS_JAVA);

		registerWeakJsType("com.google.gwt.core.client.JsDate", NS_VIEW);
		registerWeakJsType("com.google.gwt.user.client.rpc.AsyncCallback", NS_ASYNC);

		// This can obviously be only set after we register known JsType for Long.class
		java2Ts.put(long.class, java2Ts.get(Long.class));

		TS_LIST = java2Ts.get(List.class);
		TS_SET = java2Ts.get(Set.class);
		TS_MAP = java2Ts.get(Map.class);
	}

	public static KnownJsType resolveIfTypeKnownByName(Class<?> clazz) {
		String namespace = KnownJsType.java2NsWeak.get(clazz.getName());
		if (namespace == null)
			return null;

		registerKnownJsType(clazz, namespace);

		return java2Ts.get(clazz);
	}

	private static void registerKnownJsType(Class<?> clazz, String namespace) {
		java2Ts.put(clazz, new KnownJsType(extractSimpleName(clazz), namespace));
	}

	private static void registerWeakJsType(String className, String tsNamespace) {
		java2NsWeak.put(className, tsNamespace);
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
