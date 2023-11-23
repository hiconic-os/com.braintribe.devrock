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

import static com.braintribe.model.typescript.KnownJsType.JS_INTEROP_AUTO;
import static com.braintribe.model.typescript.KnownJsType.JS_INTEROP_GLOBAL;
import static com.braintribe.model.typescript.TypeScriptWriterHelper.extractNamespace;
import static com.braintribe.model.typescript.TypeScriptWriterHelper.extractSimpleName;
import static com.braintribe.model.typescript.TypeScriptWriterHelper.jsNameOrDefault;
import static com.braintribe.utils.ReflectionTools.isProtected;
import static com.braintribe.utils.ReflectionTools.isPublic;
import static com.braintribe.utils.ReflectionTools.isStatic;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.newTreeMap;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.partitioningBy;
import static jsinterop.context.JsKeywords.javaIdentifierToJs;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.tools.AbstractStringifier;
import com.braintribe.utils.ReflectionTools;
import com.braintribe.utils.collection.api.IStack;
import com.braintribe.utils.collection.impl.ArrayStack;
import com.braintribe.utils.lcd.CollectionTools2;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOptional;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
public class TypeScriptWriterForClasses extends AbstractStringifier {

	public static void write(List<Class<?>> classes, Predicate<Class<?>> customGmTypeFilter, Appendable writer) {
		if (!classes.isEmpty())
			new TypeScriptWriterForClasses(classes, customGmTypeFilter, writer).writeTypeScript();
	}

	// ####################################################
	// ## . . . . . . . . Implementation . . . . . . . . ##
	// ####################################################

	private static final Logger log = Logger.getLogger(TypeScriptWriterForClasses.class);

	private static final String indent = "\t";

	private final List<Class<?>> classes;
	private final Predicate<Class<?>> customGmTypeFilter;

	/* This is used to resolve generic parameter for inherited methods, where the generics used might be something else than on a lower level.
	 * 
	 * class MyMap<KK, VV> extends HashMap<K, V>
	 * 
	 * In this case, any method inherited from superType that returns V must return VV on sub-type. */
	private Class<?> contextClass;
	/* This is used when resolving JsFunction single-abstract methods (SAM) with generics.
	 * 
	 * Imagine we have JsUnaryFunction<A, B> with method B apply(A a) // apply is the SAM
	 * 
	 * We also have a method <X, Y> Function<X, Y> toJavaFunction(JsUnaryFunction<X, Y> jsFunction);
	 * 
	 * We want the result to be toJavaFunction(jsFunction: (X)=>Y): $tf.util.Function<X, Y>
	 * 
	 * But When writing the apply method, we see parameters A and B, so this context type helps us figure out that A is here X and B is Y. */

	/** @see #writeTypeVariableBasedOnContextMethodArgument */
	private final IStack<ParameterizedType> contextMethodArgumentParameterizedType = new ArrayStack<>();
	private Namespace contextNamespace;
	private final Set<Class<?>> nonJsTypes = newSet();

	private TypeScriptWriterForClasses(List<Class<?>> classes, Predicate<Class<?>> customGmTypeFilter, Appendable writer) {
		super(writer, "", indent);

		this.classes = classes;
		this.customGmTypeFilter = customGmTypeFilter;
		this.contextMethodArgumentParameterizedType.push(null); // so we can only check peek() == null, not worry about empty stack
	}

	private final Map<Type, Declaration> typeDeclarations = newMap();
	private final Map<String, Namespace> namespaces = newTreeMap();

	public NativeTypeDeclaration STRING = new NativeTypeDeclaration(KnownJsType.TS_STRING);
	public NativeTypeDeclaration NUMBER = new NativeTypeDeclaration(KnownJsType.TS_NUMBER);
	public NativeTypeDeclaration BOOLEAN = new NativeTypeDeclaration(KnownJsType.TS_BOOLEAN);
	public NativeTypeDeclaration ANY = new NativeTypeDeclaration(KnownJsType.TS_ANY);

	public void writeTypeScript() {

		for (Class<?> type : classes) {
			JsType jsType = type.getAnnotation(JsType.class);

			if (jsType != null)
				handleJsType(type, jsType);
			else
				handleJsMembersOfNonJsType(type);
		}

		namespaces.values().forEach(Namespace::generate);
	}

	/** <tt>type</tt> was given to the top level method; is therefore class, interface or enum. */
	private void handleJsType(Class<?> type, JsType jsType) {
		if (jsType.isNative() && globalOrAuto(jsType.namespace()))
			return;

		Declaration declaration = resolveTypeDeclaration(type);
		String nsName = declaration.getNamespaceName();
		Namespace namespace = acquireNamespace(nsName);
		namespace.add(declaration);
	}

	private boolean globalOrAuto(String s) {
		return JS_INTEROP_AUTO.equals(s) || JS_INTEROP_GLOBAL.equals(s);
	}

	/** <tt>type</tt> was given to the top level method; is therefore class, interface or enum. */
	private void handleJsMembersOfNonJsType(Class<?> type) {
		if (type.isEnum())
			return;

		handleJsFieldsOf(type);
		handleJsMethodsOf(type);
	}

	private void handleJsFieldsOf(Class<?> type) {
		for (Field field : type.getDeclaredFields()) {
			if (!isStatic(field) || !isPublic(field))
				continue;

			JsProperty jsProperty = field.getAnnotation(JsProperty.class);
			if (jsProperty == null)
				continue;

			VariableDeclaration variableDeclaration = new VariableDeclaration(field, jsProperty);
			acquireNamespace(variableDeclaration.getNamespaceName()).add(variableDeclaration);
		}
	}

	private void handleJsMethodsOf(Class<?> type) {
		for (Method method : type.getMethods()) {
			if (!isStatic(method))
				continue;

			JsMethod jsMethod = method.getAnnotation(JsMethod.class);
			if (jsMethod == null)
				continue;

			FunctionDeclaration functionDeclaration = new FunctionDeclaration(method, jsMethod);
			acquireNamespace(functionDeclaration.getNamespaceName()).add(functionDeclaration);
		}
	}

	private Declaration resolveTypeDeclaration(Type type) {
		initTypeDeclarations();

		Declaration declaration = CollectionTools2.computeIfAbsent(typeDeclarations, type, this::typeDeclarationFor);

		markIfNonJsType(type, declaration);

		return declaration;
	}

	/**
	 * {@link WildcardType} type is not possible here - wildcard is never top-level type, just a parameter of something else.
	 * <p>
	 * {@link TypeVariable} is also not relevant here, because type parameter bounds are handled separately in
	 * {@link ClassOrInterfaceDeclaration#writeTypeParameters(TypeVariable[])}.
	 */
	private void markIfNonJsType(Type type, Declaration declaration) {
		if (declaration != ANY)
			return;

		if (type instanceof ParameterizedType)
			type = ((ParameterizedType) type).getRawType();

		if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;
			while (clazz.isArray())
				clazz = clazz.getComponentType();

			if (clazz != Object.class)
				nonJsTypes.add(clazz);
		}
	}

	private void initTypeDeclarations() {
		if (!typeDeclarations.isEmpty())
			return;

		Map<KnownJsType, Declaration> tsType2Dec = newMap();
		tsType2Dec.put(KnownJsType.TS_STRING, STRING);
		tsType2Dec.put(KnownJsType.TS_NUMBER, NUMBER);
		tsType2Dec.put(KnownJsType.TS_BOOLEAN, BOOLEAN);
		tsType2Dec.put(KnownJsType.TS_ANY, ANY);

		for (Entry<Class<?>, KnownJsType> e : KnownJsType.java2Ts.entrySet()) {
			Class<?> clazz = e.getKey();

			Declaration declaration = tsType2Dec.computeIfAbsent(e.getValue(), knownJsType -> new KnownJsTypeDeclaration(knownJsType, clazz));

			typeDeclarations.put(clazz, declaration);
		}
	}

	private Declaration typeDeclarationFor(Type type) {
		if (type instanceof ParameterizedType)
			return resolveParameterizedTypeDeclaration((ParameterizedType) type);

		if (type instanceof WildcardType)
			return resolveWildcardTypeDeclaration((WildcardType) type);

		if (type instanceof TypeVariable<?>)
			return resolveTypeVariableDeclaration((TypeVariable<?>) type);

		if (type instanceof GenericArrayType)
			return resolveGenericArrayTypeDeclarationFor((GenericArrayType) type);

		if (!(type instanceof Class))
			return ANY;

		Class<?> clazz = (Class<?>) type;

		if (clazz.isArray())
			return resolveArrayTypeDeclarationFor(clazz);

		if (clazz.getAnnotation(JsFunction.class) != null)
			return new JsFunctionDeclaration(clazz);

		if (customGmTypeFilter.test(clazz))
			return new GmCustomTypeDeclaration(clazz);

		JsType jsType = clazz.getAnnotation(JsType.class);

		if (jsType == null) {
			KnownJsType knownType = KnownJsType.resolveIfTypeKnownByName(clazz);
			if (knownType != null)
				return new KnownJsTypeDeclaration(knownType, clazz);
			else
				return ANY;
		}

		if (jsType.isNative()) {
			if (globalOrAuto(jsType.namespace()))
				return new NativeTypeDeclaration(jsType);
		}

		if (clazz.isEnum())
			return new EnumTypeDeclaration((Class<? extends Enum<?>>) type, jsType);
		else if (clazz.isInterface())
			return new InterfaceTypeDeclaration(clazz, jsType);
		else
			return new ClassTypeDeclaration(clazz, jsType);
	}

	private Declaration resolveParameterizedTypeDeclaration(ParameterizedType type) {
		Declaration rd = resolveTypeDeclaration(type.getRawType());
		return rd == ANY ? ANY : new ParameterizedTypeDeclaration(type, rd);
	}

	private Declaration resolveWildcardTypeDeclaration(WildcardType wt) {
		Pair<Type, Declaration> td = resolveDeclarationFromBounds(wt.getLowerBounds());

		if (td == null)
			td = resolveDeclarationFromBounds(wt.getUpperBounds());

		return td == null ? ANY : new WildcardTypeDeclaration(wt, td.first, td.second);
	}

	private TypeVaribleDeclaration resolveTypeVariableDeclaration(TypeVariable<?> type) {
		Pair<Type, Declaration> td = resolveDeclarationFromBounds(type.getBounds());

		return td == null ? new TypeVaribleDeclaration(type, Object.class, ANY) : new TypeVaribleDeclaration(type, td.first, td.second);
	}

	private Pair<Type, Declaration> resolveDeclarationFromBounds(Type[] bounds) {
		if (bounds.length == 0)
			return null;

		for (int i = 0; i < bounds.length; i++) {
			Type bound = bounds[i];
			Declaration declaration = resolveTypeDeclaration(bound);
			if (declaration != ANY)
				return new Pair<>(bound, declaration);
		}

		return null;
	}

	private Declaration resolveArrayTypeDeclarationFor(Class<?> clazz) {
		Class<?> componentType = clazz.getComponentType();
		Declaration cd = resolveTypeDeclaration(componentType);
		return new ArrayTypeDeclaration(componentType, cd);
	}

	private Declaration resolveGenericArrayTypeDeclarationFor(GenericArrayType type) {
		Type componentType = type.getGenericComponentType();
		Declaration cd = resolveTypeDeclaration(componentType);
		return new ArrayTypeDeclaration(componentType, cd);
	}

	private Namespace acquireNamespace(String name) {
		return namespaces.computeIfAbsent(name, Namespace::new);
	}

	private static final Comparator<Declaration> declarationComparator = Comparator.//
			comparing(Declaration::declarationType) //
			.thenComparing(Declaration::getName);

	private class Namespace {
		private final String name;
		private final List<Declaration> declarations = newList();
		private final boolean global;

		private Namespace(String name) {
			this.name = name;
			this.global = name.equals("<global>");
		}

		public void add(Declaration declaration) {
			this.declarations.add(declaration);
			declaration.owner = this;
		}

		public boolean isGlobal() {
			return global;
		}

		public void generate() {
			contextNamespace = this;

			if (!global) {
				// write namespace opening
				print("declare namespace ");
				print(name);
				println(" {");
				println("");
				levelUp();
			}

			// write declarations
			declarations.stream() //
					.sorted(declarationComparator) //
					.forEach(Declaration::generate);

			if (!global) {
				// write namespace closing
				levelDown();
				println("}\n");
			}
		}
	}

	private enum DeclarationType {
		VAR,
		FUNCTION,
		FUNCTION_AS_PARAMETER,
		INTERFACE,
		ENUM,
		CLASS,
		NOBODY_CARES,
	}

	interface TopLevelTypeDeclaration {
		Type getType();
		Class<?> getRawType();
	}

	private abstract class Declaration {

		public Namespace owner;

		public void generate() {
			try {
				tryGenerate();
			} catch (RuntimeException e) {
				throw Exceptions.contextualize(e, "Error while generating code for: " + this);
			}
		}

		public abstract void tryGenerate();
		public abstract String getName();
		public abstract String getNamespaceName();

		public boolean isInteroperableClassType() {
			return false;
		}

		public boolean isNative() {
			return false;
		}

		public abstract DeclarationType declarationType();

		public void beginDeclaration() {
			if (owner != null && owner.isGlobal())
				print("declare ");
		}

		protected void writeMemberSourceAsComment(Member member) {
			print("// ");
			print(member.getDeclaringClass().getName());
			print("#");
			print(member.getName());
		}

	}

	/** Declaration based on {@link KnownJsType}s. */
	private class KnownJsTypeDeclaration extends Declaration implements TopLevelTypeDeclaration {
		private final KnownJsType type;
		private final Class<?> javaClass;

		public KnownJsTypeDeclaration(KnownJsType knownType, Class<?> javaClass) {
			this.type = knownType;
			this.javaClass = javaClass;
		}

		// @formatter:off
		@Override public void tryGenerate() { /* noop */ }
		@Override public String getName() { return type.name; }
		@Override public String getNamespaceName() { return type.namespace; }
		@Override public DeclarationType declarationType() { return DeclarationType.CLASS; }
		@Override public boolean isInteroperableClassType() { return true; }
		@Override public String toString() { return getClass().getSimpleName() + "[" + type.name + "]"; }
		// TopLevelTypeDeclaration
		@Override public Type getType() { return javaClass; }
		@Override public Class<?> getRawType() { return javaClass; }
		// @formatter:on

	}

	private class VariableDeclaration extends Declaration {
		private final Field field;
		private final String namespace;
		private final String name;

		public VariableDeclaration(Field field, JsProperty jsProperty) {
			super();
			this.field = field;

			this.name = jsNameOrDefault(jsProperty.name(), field::getName);
			this.namespace = jsNameOrDefault(jsProperty.namespace(), () -> JS_INTEROP_GLOBAL);
		}

		@Override
		public DeclarationType declarationType() {
			return DeclarationType.VAR;
		}

		@Override
		public void tryGenerate() {
			writeMemberSourceAsComment(field);
			println();

			beginDeclaration();
			print("let ");
			print(name);
			print(": ");
			printTypeName(field.getType());
			println(";");
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getNamespaceName() {
			return namespace;
		}

	}

	private class FunctionDeclaration extends Declaration {
		private final Method method;
		private final String name;
		private final String namespace;

		public FunctionDeclaration(Method method, JsMethod jsMethod) {
			this.method = method;

			this.name = jsNameOrDefault(jsMethod.name(), method::getName);
			this.namespace = jsNameOrDefault(jsMethod.namespace(), () -> JS_INTEROP_GLOBAL);
		}

		@Override
		public DeclarationType declarationType() {
			return DeclarationType.FUNCTION;
		}

		@Override
		public void tryGenerate() {
			if (TypeScriptWriterHelper.jsKeywords.contains(name)) {
				log.warn("Will not write function for method '" + method.toString() + "'  because it's name is a reserved word in JavaScript.");
				return;
			}

			writeJavaSourceAsComment();
			beginDeclaration();
			print("function ");
			writeExecutableSignature(name, method);
		}

		private void writeJavaSourceAsComment() {
			writeMemberSourceAsComment(method);
			print("(");
			int i = 0;
			for (Class<?> paramType : method.getParameterTypes()) {
				if (i++ > 0)
					print(", ");
				print(paramType.getSimpleName());
			}
			println(")");
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getNamespaceName() {
			return namespace;
		}
	}

	private class NativeTypeDeclaration extends Declaration {
		private final String name;
		private final String nameSpace;

		// @formatter:off
		public NativeTypeDeclaration(KnownJsType type) { this(type.name, type.namespace); }
		public NativeTypeDeclaration(JsType jsType) { this(jsType.name(), jsType.namespace()); }
		       NativeTypeDeclaration(String name, String nameSpace) { this.name = name; this.nameSpace = toGlobalIfAuto(nameSpace); }
		String toGlobalIfAuto(String s) { return s.equals(JS_INTEROP_AUTO) ? JS_INTEROP_GLOBAL : s; }

		@Override public DeclarationType declarationType() { return DeclarationType.CLASS; }
		@Override public void tryGenerate() { /* noop */ }
		@Override public String getName() { return name; }
		@Override public String getNamespaceName() { return nameSpace; }
		@Override public boolean isNative() { return true; }
		// @formatter:on
	}

	private class ArrayTypeDeclaration extends Declaration {
		private final Type componentType;
		private final Declaration component;

		public ArrayTypeDeclaration(Type componentType, Declaration component) {
			this.componentType = componentType;
			this.component = component;
		}

		// @formatter:off
		@Override public DeclarationType declarationType() { return DeclarationType.CLASS; }
		@Override public void tryGenerate() { /* noop */ }
		@Override public String getName() { return component.getName() + "[]"; }
		@Override public String getNamespaceName() { return component.getNamespaceName(); }
		@Override public boolean isNative() { return component.isNative(); }
		// @formatter:on
	}

	private class JsFunctionDeclaration extends Declaration {

		private final Class<?> functionalIface;
		private Method sam;

		public JsFunctionDeclaration(Class<?> functionalIface) {
			this.functionalIface = functionalIface;
		}

		// @formatter:off
		@Override public void tryGenerate() { /* NOOP */ }
		@Override public String getName() { return functionalIface.getSimpleName(); }
		@Override public String getNamespaceName() { return JS_INTEROP_GLOBAL; }
		@Override public DeclarationType declarationType() { return DeclarationType.FUNCTION_AS_PARAMETER; }
		// @formatter:on

		public void printSignature() {
			if (sam == null)
				sam = findSingleAbstractMethod();

			if (sam == null || sam.getTypeParameters().length > 0) {
				printAny();
				return;
			}

			writeMethodArguments(sam);
			print(" => ");
			printReturnTypeName(sam.getGenericReturnType());
		}

		private void printAny() {
			nonJsTypes.add(functionalIface);
			print(ANY.getName());
		}

		private Method findSingleAbstractMethod() {
			return Stream.of(functionalIface.getMethods()) //
					.filter(ReflectionTools::isAbstract) //
					.findFirst() //
					.orElse(null);
		}
	}

	private class GmCustomTypeDeclaration extends Declaration {
		private final String name;
		private final String namespace;

		public GmCustomTypeDeclaration(Class<?> javaType) {
			this.name = extractSimpleName(javaType);
			this.namespace = GmCoreApiInteropNamespaces.type + "." + extractNamespace(javaType);
		}

		// @formatter:off
		@Override public void tryGenerate() { throw new IllegalStateException("Method 'generate' should not be called for a custom GM type!"); }
		@Override public DeclarationType declarationType() { return DeclarationType.NOBODY_CARES; }
		@Override public boolean isInteroperableClassType() { return true; }
		@Override public String getName() { return name; }
		@Override public String getNamespaceName() { return namespace; }
		// @formatter:on
	}

	private abstract class BoundWrapperTypeDeclaration<T extends Type> extends Declaration {
		public final T type;
		public final Type chosenBoundType;
		public final Declaration chosenBoundDeclaration;

		public BoundWrapperTypeDeclaration(T type, Type chosenBoundType, Declaration chosenBoundDeclaration) {
			this.type = type;
			this.chosenBoundType = chosenBoundType;
			this.chosenBoundDeclaration = chosenBoundDeclaration;
		}

		// @formatter:off
		@Override public boolean isInteroperableClassType() { return chosenBoundDeclaration.isInteroperableClassType(); }
		@Override public void tryGenerate() { /* noop */  }
		@Override public String getName() { return type.getTypeName(); }
		@Override public String getNamespaceName() { return chosenBoundDeclaration.getNamespaceName(); }
		@Override public DeclarationType declarationType() { return chosenBoundDeclaration.declarationType(); }
		// @formatter:on
	}

	private class ParameterizedTypeDeclaration extends BoundWrapperTypeDeclaration<ParameterizedType> implements TopLevelTypeDeclaration {
		public ParameterizedTypeDeclaration(ParameterizedType type, Declaration chosenBoundDeclaration) {
			super(type, type.getRawType(), chosenBoundDeclaration);
		}

		// @formatter:off
		@Override public Type getType() { return type; }
		@Override public Class<?> getRawType() { return (Class<?>) type.getRawType(); }
		// @formatter:on
	}

	private class WildcardTypeDeclaration extends BoundWrapperTypeDeclaration<WildcardType> {
		public WildcardTypeDeclaration(WildcardType type, Type chosenBoundType, Declaration chosenBoundDeclaration) {
			super(type, chosenBoundType, chosenBoundDeclaration);
		}
	}

	private class TypeVaribleDeclaration extends BoundWrapperTypeDeclaration<TypeVariable<?>> {
		public TypeVaribleDeclaration(TypeVariable<?> type, Type chosenBoundType, Declaration chosenBoundDeclaration) {
			super(type, chosenBoundType, chosenBoundDeclaration);
		}
	}

	private abstract class AbstractClassDeclaration extends Declaration {

		protected abstract Class<?> type();

		protected List<TopLevelTypeDeclaration> findTypeDeclarationsForAllInteropableIfaces(Class<?> clazz) {
			List<Class<?>> classAndNonTsSuperClasses = findClassAndNonTsSuperClasses(clazz);

			return classAndNonTsSuperClasses.stream() //
					.flatMap(this::streamTypeDeclarationsForAllInteropableIfaces) //
					.collect(Collectors.toList());
		}

		private List<Class<?>> findClassAndNonTsSuperClasses(Class<?> clazz) {
			List<Class<?>> result = asList(clazz);

			while (true) {
				clazz = clazz.getSuperclass();
				if (clazz != null && clazz.getAnnotation(JsType.class) == null)
					result.add(clazz);
				else
					return result;
			}
		}

		protected Stream<TopLevelTypeDeclaration> streamTypeDeclarationsForAllInteropableIfaces(Type type) {
			Type[] types = getRawClass(type).getGenericInterfaces();

			return Stream.of(types) //
					.flatMap(this::findTypeDeclaration);
		}

		protected Stream<TopLevelTypeDeclaration> findTypeDeclaration(Type type) {
			TopLevelTypeDeclaration declaration = getClassTypeDeclarationIfInteropable(type);
			if (declaration != null)
				return Stream.of(declaration);
			else
				return streamTypeDeclarationsForAllInteropableIfaces(type);

		}

		protected TopLevelTypeDeclaration getClassTypeDeclarationIfInteropable(Type type) {
			if (type == null)
				return null;

			Declaration typeDeclaration = resolveTypeDeclaration(type);

			if (typeDeclaration.isInteroperableClassType())
				return (TopLevelTypeDeclaration) typeDeclaration;
			else
				return null;
		}

		protected void writeJavaSourceAsComment(Class<?> type) {
			print("// ");
			print(declarationType().name().toLowerCase()); // class, interface or enum
			print(" ");
			print(type.getName());
			println("");
		}

		protected void writeExtends(List<TopLevelTypeDeclaration> types) {
			contextClass = type();

			int i = 0;
			for (TopLevelTypeDeclaration declaration : types) {
				if (i++ > 0)
					print(", ");
				else
					print(" extends ");

				printTypeName(declaration.getType());
			}

			contextClass = null;
		}

		@Override
		public String toString() {
			return declarationToString(this, type());
		}

	}

	/**
	 * Outputs:
	 * 
	 * <pre>
	 * 
	 * interface MyEnum extends SomeIface {}; // This is output only if there is at least one interface the enum implements
	 * class MyEnum {
	 *   static A: MyEnum;
	 *   static B: MyEnum;
	 * }
	 * </pre>
	 */
	private class EnumTypeDeclaration extends AbstractClassDeclaration {

		private final Class<? extends Enum<?>> type;
		private final String name;
		private final String namespace;

		public EnumTypeDeclaration(Class<? extends Enum<?>> type, JsType jsType) {
			this.type = type;

			this.name = jsNameOrDefault(jsType.name(), () -> extractSimpleName(type));
			this.namespace = jsNameOrDefault(jsType.namespace(), () -> extractNamespace(type));
		}

		// @formatter:off
		@Override public DeclarationType declarationType() { return DeclarationType.ENUM; }
		@Override public boolean isInteroperableClassType() { return true; }
		@Override public String getName() { return name; }
		@Override public String getNamespaceName() { return namespace; }
		@Override public Class<?> type() { return type; }
		// @formatter:on

		@Override
		public void tryGenerate() {
			writeJavaSourceAsComment(type);
			writeInterfacePart();
			writeClassPart();
		}

		private void writeInterfacePart() {
			List<TopLevelTypeDeclaration> superIfaces = findTypeDeclarationsForAllInteropableIfaces(type);
			if (superIfaces.isEmpty())
				return;

			print("interface ");
			print(name);
			writeExtends(superIfaces);
			println("{}");
		}

		private void writeClassPart() {
			print("class ");
			print(name);
			println(" {");

			levelUp();
			for (Field f : type.getFields())
				if (f.isEnumConstant()) {
					print("static ");
					print(f.getName());
					print(": ");
					print(name);
					println(";");
				}
			levelDown();
			println("}\n");
		}

	}

	private abstract class ClassOrInterfaceDeclaration extends AbstractClassDeclaration implements TopLevelTypeDeclaration {

		protected final Class<?> type;
		protected final String name;
		protected final String namespace;

		protected TopLevelTypeDeclaration superClassDeclaration;
		protected List<TopLevelTypeDeclaration> superIfaceDeclarations = emptyList();

		protected final List<Field> staticFields;
		protected final List<Field> instanceFields;

		protected final List<TsMethod> staticMethods;
		protected final List<TsMethod> instanceMethods;

		public ClassOrInterfaceDeclaration(Class<?> type, JsType jsType) {
			this.type = type;

			this.name = jsNameOrDefault(jsType.name(), () -> extractSimpleName(type));
			this.namespace = jsNameOrDefault(jsType.namespace(), () -> extractNamespace(type));

			Map<Boolean, List<Field>> fields = resolveFields();
			Map<Boolean, List<TsMethod>> methods = resolveMethods();

			staticFields = fields.get(true);
			instanceFields = fields.get(false);

			staticMethods = methods.get(true);
			instanceMethods = methods.get(false);
		}

		// @formatter:off
		@Override public Class<?> type() { return type; }
		// @formatter:on

		private Map<Boolean, List<Field>> resolveFields() {
			return Stream.of(type.getFields()) //
					.filter(this::isFieldInClassScope) //
					.collect( //
							partitioningBy( //
									ReflectionTools::isStatic //
							));
		}

		private boolean isFieldInClassScope(Field field) {
			if (!(isPublic(field) || isProtected(field)))
				return false;

			if (field.isAnnotationPresent(JsIgnore.class))
				return false;

			if (!isStatic(field))
				return true;

			JsProperty jsProperty = field.getAnnotation(JsProperty.class);

			if (jsProperty == null)
				return true;

			if (!jsProperty.namespace().equals(JS_INTEROP_AUTO))
				return false;

			return true;
		}

		private Map<Boolean, List<TsMethod>> resolveMethods() {
			return Stream.of(type.getDeclaredMethods()) //
					.filter(this::isEligibleForTs) //
					.map(this::toTsMethod) //
					.filter(x -> x != null) //
					.sorted() //
					.collect( //
							partitioningBy( //
									tsMethod -> ReflectionTools.isStatic(tsMethod.method) //
							));
		}

		private boolean isEligibleForTs(Method method) {
			if (method.getDeclaringClass() == Object.class)
				return false;

			if (!(isPublic(method) || isProtected(method)))
				return false;

			if (method.isAnnotationPresent(JsIgnore.class))
				return false;

			if (method.isBridge())
				return false;

			return !isTsDeclarationInherited(method);
		}

		/**
		 * Returns <tt>true</tt> iff given method is inherited from a super-type which has a TypeScript declaration. in such case this method doesn't
		 * have to be declared on this level.
		 */
		private boolean isTsDeclarationInherited(Method method) {
			Class<?> dc = method.getDeclaringClass();
			if (type == dc)
				return false;

			if (superClassDeclaration != null && isAssignableFrom(dc, superClassDeclaration))
				return true;

			for (TopLevelTypeDeclaration td : superIfaceDeclarations)
				if (isAssignableFrom(dc, td))
					return true;

			return false;
		}

		private boolean isAssignableFrom(Class<?> dc, TopLevelTypeDeclaration tltd) {
			return dc.isAssignableFrom(tltd.getRawType());
		}

		private TsMethod toTsMethod(Method method) {
			JsMethod jsMethod = method.getAnnotation(JsMethod.class);
			if (jsMethod == null)
				return new TsMethod(method.getName(), method);

			if (isStatic(method) && !jsMethod.namespace().equals(JS_INTEROP_AUTO))
				return null;

			String name = jsNameOrDefault(jsMethod.name(), method::getName);
			return new TsMethod(name, method);
		}

		// @formatter:off
		@Override public boolean isInteroperableClassType() { return true; }
		@Override public String getNamespaceName() { return namespace; }
		@Override public String getName() { return name; }

		@Override public Type getType() { return type; }
		@Override public Class<?> getRawType() { return type; }
		// @formatter:on

		protected void writeInterface(List<TopLevelTypeDeclaration> superTypes, boolean opening) {
			beginDeclaration();
			print("interface ");
			print(name);

			writeTypeParameters(type.getTypeParameters());

			writeExtends(superTypes);

			if (opening)
				println(" {");
			else
				println(" {}");
		}

		protected void writeFields(List<Field> fields) {
			for (Field field : fields)
				writeField(field);
		}

		private void writeField(Field field) {
			JsProperty jsProperty = field.getAnnotation(JsProperty.class);

			String name = field.getName();

			if (jsProperty != null && !jsProperty.name().equals(JS_INTEROP_AUTO))
				name = jsProperty.name();

			if (isProtected(field))
				print("protected ");

			if (isStatic(field))
				print("static ");

			print(name);
			print(": ");
			printTypeName(field.getGenericType());
			println(";");
		}

		protected void writeMethods(List<TsMethod> methods) {
			for (TsMethod method : methods)
				writeMethod(method);
		}

		private void writeMethod(TsMethod tsMethod) {
			Method m = tsMethod.method;
			writeMemberExecutable(tsMethod.jsName, m);
		}

		protected void writeMemberExecutable(String name, Executable method) {
			if (TypeScriptWriterHelper.jsKeywords.contains(name)) {
				log.warn("Will not write method '" + method.toString() + "'  because it's name is a reserved word in JavaScript.");
				return;
			}

			if (isProtected(method))
				print("protected ");

			if (isStatic(method))
				print("static ");

			contextClass = type;

			writeExecutableSignature(name, method);

			contextClass = null;
		}

	}

	private class ClassTypeDeclaration extends ClassOrInterfaceDeclaration {

		// @formatter:off
		public ClassTypeDeclaration(Class<?> type, JsType jsType) { super(type, jsType); }

		@Override public DeclarationType declarationType() { return DeclarationType.CLASS; }
		// @formatter:on

		@Override
		public void tryGenerate() {
			writeJavaSourceAsComment(type);
			writeClassOpening();

			levelUp();

			writeConstructor();
			writeFields(staticFields);
			writeFields(instanceFields);
			writeMethods(staticMethods);
			writeMethods(instanceMethods);

			levelDown();

			println("}\n");
		}

		private void writeClassOpening() {
			superIfaceDeclarations = findTypeDeclarationsForAllInteropableIfaces(type);

			if (!superIfaceDeclarations.isEmpty())
				writeInterface(superIfaceDeclarations, false);

			beginDeclaration();
			print("class ");
			print(name);

			writeTypeParameters(type.getTypeParameters());

			superClassDeclaration = resolveInteropableSuperClass(type);
			if (superClassDeclaration != null)
				writeExtends(asList(superClassDeclaration));

			println(" {");
		}

		private TopLevelTypeDeclaration resolveInteropableSuperClass(Class<?> type) {
			Type superClass = type.getGenericSuperclass();
			while (superClass != null) {
				TopLevelTypeDeclaration result = getClassTypeDeclarationIfInteropable(superClass);
				if (result != null)
					return result;

				superClass = getRawClass(superClass).getGenericSuperclass();
			}

			return null;
		}

		private void writeConstructor() {
			for (Constructor<?> constructor : type.getConstructors())
				if (isPublic(constructor) && !constructor.isAnnotationPresent(JsIgnore.class))
					writeConstructor(constructor);
		}

		private void writeConstructor(Constructor<?> constructor) {
			writeMemberExecutable("constructor", constructor);
		}

	}

	private class InterfaceTypeDeclaration extends ClassOrInterfaceDeclaration {

		// @formatter:off
		public InterfaceTypeDeclaration(Class<?> type, JsType jsType) { super(type, jsType); }

		@Override public DeclarationType declarationType() { return DeclarationType.INTERFACE; }
		// @formatter:on

		@Override
		public void tryGenerate() {
			writeJavaSourceAsComment(type);
			writeAbstractClassForStaticMembersIfneeded();
			writeInterfaceWithInstanceMethods();
		}

		private void writeAbstractClassForStaticMembersIfneeded() {
			if (!hasStaticMembers())
				return;

			writeAbstractClass();
			levelUp();

			writeFields(staticFields);
			writeMethods(staticMethods);

			levelDown();

			println("}");
		}

		private boolean hasStaticMembers() {
			return !staticFields.isEmpty() || !staticMethods.isEmpty();
		}

		private void writeAbstractClass() {
			beginDeclaration();
			print("abstract class ");
			print(name);

			writeTypeParameters(type.getTypeParameters());

			println(" {");
		}

		private void writeInterfaceWithInstanceMethods() {
			writeInterface(superIfaceDeclarations = findTypeDeclarationsForAllInteropableIfaces(type), true);

			levelUp();
			writeMethods(instanceMethods);
			levelDown();

			println("}\n");
		}

	}

	protected void printReturnTypeName(Type type) {
		if (type == void.class)
			print("void");
		else
			printTypeName(type);
	}

	protected void printTypeName(Type type) {
		Declaration declaration = resolveTypeDeclaration(type);

		printTypeName(type, declaration);
	}

	protected void printTypeName(Type type, Declaration declaration) {
		if (declaration.isNative())
			printDeclarationTypeName(declaration);

		else if (declaration instanceof ArrayTypeDeclaration)
			printArrayTypeDeclaration((ArrayTypeDeclaration) declaration);

		else if (declaration.declarationType() == DeclarationType.FUNCTION_AS_PARAMETER)
			printJsFunctionSignature(declaration);

		else if (type instanceof TypeVariable<?>)
			printTypeVariableName((TypeVaribleDeclaration) declaration);

		else if (type instanceof WildcardType)
			printWildcardTypeName((WildcardTypeDeclaration) declaration);

		else if (type instanceof ParameterizedType)
			printParameterizedTypeName((ParameterizedType) type);

		else if (!declaration.isInteroperableClassType())
			print(ANY.getName());

		else
			printDeclarationTypeName(declaration);
	}

	private void printArrayTypeDeclaration(ArrayTypeDeclaration declaration) {
		printTypeName(declaration.componentType, declaration.component);
		print("[]");
	}

	private void printJsFunctionSignature(Declaration declaration) {
		if (declaration instanceof ParameterizedTypeDeclaration) {
			ParameterizedTypeDeclaration ptd = (ParameterizedTypeDeclaration) declaration;
			declaration = ptd.chosenBoundDeclaration;
			// If we have a method say <X> void(JsConsumer<X> jsConsumer), the ptd.type would be JsConsumer<X>
			contextMethodArgumentParameterizedType.push(ptd.type);
		} else {
			contextMethodArgumentParameterizedType.push(null);
		}

		if (!(declaration instanceof JsFunctionDeclaration))
			throw new IllegalStateException("Unexpected declaration type. JsFunctionDeclaration expected, but found: " + declaration);

		((JsFunctionDeclaration) declaration).printSignature();

		contextMethodArgumentParameterizedType.pop();
	}

	private void printTypeVariableName(TypeVaribleDeclaration declaration) {
		GenericDeclaration genericDeclaration = declaration.type.getGenericDeclaration();

		String paramName = declaration.getName();

		// Type Variable Name is declared on a method, we can just print it's name
		if (genericDeclaration instanceof Method) {
			print(paramName);
			return;
		}

		// Type Variable Name is declared on a class, but because of inheritance we might need to resolve the name for a sub-type
		if (genericDeclaration instanceof Class<?>) {
			if (contextMethodArgumentParameterizedType.peek() != null) {
				writeTypeVariableBasedOnContextMethodArgument(declaration, genericDeclaration);
				return;
			}

			if (contextClass != null && contextClass != genericDeclaration) {
				Type localizedGenericType = ReflectionTools.getGenericsParameter(contextClass, (Class<?>) genericDeclaration, paramName);
				printTypeName(localizedGenericType);

			} else {
				print(paramName);
			}
			return;
		}

		throw new IllegalStateException("Unexpected genericDeclaration '" + genericDeclaration + "' of type '"
				+ genericDeclaration.getClass().getName() + "' found for TypeVariable named: " + paramName);
	}

	/**
	 * Imagine we have class{@code JsUnaryFunction<A, B>} with method {@code B apply(A a) // apply is the SAM }
	 * <p>
	 * And we also have a method {@code <X, Y> Function<X, Y> toJavaFunction(JsUnaryFunction<X, Y> jsFunction); }
	 * <p>
	 * <ul>
	 * <li>genericDeclaration is {@code JsUnaryFunction<A, B>}
	 * <li>declaration.type is A or B
	 * <li>index is 0 for A and 1 for B
	 * <li>methodArgumentType is {@code JsUnaryFunction<X, Y>}
	 * <li>typeArgument is X for index 0 and Y for index 1 and that is what we want to write
	 * </ul>
	 */
	private void writeTypeVariableBasedOnContextMethodArgument(TypeVaribleDeclaration declaration, GenericDeclaration genericDeclaration) {
		int index = 0;
		for (TypeVariable<?> typeVariable : genericDeclaration.getTypeParameters()) {
			if (typeVariable == declaration.type) {
				ParameterizedType methodArgumentType = contextMethodArgumentParameterizedType.peek();
				Type typeArgument = methodArgumentType.getActualTypeArguments()[index];

				contextMethodArgumentParameterizedType.push(null);
				printTypeName(typeArgument);
				contextMethodArgumentParameterizedType.pop();

				return;
			}
			index++;
		}

	}

	private void printWildcardTypeName(WildcardTypeDeclaration declaration) {
		printTypeName(declaration.chosenBoundType);
	}

	private void printParameterizedTypeName(ParameterizedType parameterizedType) {
		Type[] typeArgs = parameterizedType.getActualTypeArguments();

		printTypeName(parameterizedType.getRawType());
		print("<");

		int i = 0;
		for (Type typeArg : typeArgs) {
			if (i++ > 0)
				print(", ");
			printTypeName(typeArg);
		}
		print(">");
	}

	private void printDeclarationTypeName(Declaration declaration) {
		String ns = declaration.getNamespaceName();
		if (!ns.equals("<global>") && !ns.equals(contextNamespace.name)) {
			print(ns);
			print(".");
		}

		print(declaration.getName());
	}

	protected void writeExecutableSignature(String name, Executable method) {
		print(name);

		nonJsTypes.clear();

		writeTypeParameters(method.getTypeParameters());

		writeMethodArguments(method);

		if (method instanceof Method) {
			print(": ");
			printReturnTypeName(((Method) method).getGenericReturnType());
		}

		print(";");

		printNonJsTypes();

		println();
	}

	private void printNonJsTypes() {
		if (nonJsTypes.isEmpty())
			return;

		print(" // JS-WARN: ");

		String nonJsTypesComment = nonJsTypes.stream() //
				.map(Class::getName) //
				.collect(Collectors.joining(", "));

		print(nonJsTypesComment);
	}

	protected void writeTypeParameters(TypeVariable<?>[] typeParameters) {
		if (typeParameters.length > 0) {
			print("<");

			int i = 0;
			for (TypeVariable<?> param : typeParameters) {
				if (i++ > 0)
					print(", ");

				print(javaIdentifierToJs(param.getName()));

				int jsBounds = 0;
				for (Type bound : param.getBounds()) {
					Declaration declaration = resolveTypeDeclaration(bound);

					if (declaration.isInteroperableClassType()) {
						if (jsBounds++ == 0)
							print(" extends ");
						else
							print(" & ");
						printTypeName(bound, declaration);
					}
				}
			}

			print(">");
		}
	}

	private void writeMethodArguments(Executable method) {
		print("(");

		int i = 0;
		boolean optional = false;
		Type[] genericParameterTypes = method.getGenericParameterTypes();
		for (Parameter parameter : method.getParameters()) {
			optional |= isOptional(parameter);
			Type genericParameterType = genericParameterTypes[i];

			if (i++ > 0)
				print(", ");

			if (genericParameterTypes.length == i && method.isVarArgs())
				print("...");

			print(javaIdentifierToJs(parameter.getName()));
			if (optional)
				print("?");
			print(": ");
			printTypeName(genericParameterType);
		}

		print(")");
	}

	private boolean isOptional(Parameter parameter) {
		return parameter.getAnnotation(JsOptional.class) != null;
	}

	private static class TsMethod implements Comparable<TsMethod> {
		public String jsName;
		public Method method;

		public TsMethod(String jsName, Method method) {
			this.jsName = jsName;
			this.method = method;
		}

		@Override
		public int compareTo(TsMethod o) {
			return jsName.compareTo(o.jsName);
		}

	}

	private static Class<?> getRawClass(Type type) {
		Type _type = type;

		if (type instanceof ParameterizedType)
			type = ((ParameterizedType) type).getRawType();

		if (type instanceof Class<?>)
			return (Class<?>) type;

		throw new IllegalStateException("Cannot extract java.lang.Class from: " + _type);
	}

	public static String declarationToString(Declaration declaration, Class<?> type) {
		return declaration.getClass().getSimpleName() + "[" + type.getName() + "]";
	}
}
