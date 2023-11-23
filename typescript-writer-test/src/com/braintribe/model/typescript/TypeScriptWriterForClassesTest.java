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

import static com.braintribe.utils.SysPrint.spOut;

import java.util.Arrays;

import org.junit.Test;

import com.braintribe.ts.sample.TsArrays;
import com.braintribe.ts.sample.TsClassLiterals;
import com.braintribe.ts.sample.TsCustomGmTypes;
import com.braintribe.ts.sample.TsCustomInterface;
import com.braintribe.ts.sample.TsEnum;
import com.braintribe.ts.sample.TsEnumWithIface;
import com.braintribe.ts.sample.TsGenericsInFields;
import com.braintribe.ts.sample.TsGenericsInMethods;
import com.braintribe.ts.sample.TsKnownTypeExtension;
import com.braintribe.ts.sample.TsNativeCustomNamespace;
import com.braintribe.ts.sample.TsPrimitives;
import com.braintribe.ts.sample.TsSimpleTypes;
import com.braintribe.ts.sample.TsWrappers;
import com.braintribe.ts.sample.clazz.TsClass;
import com.braintribe.ts.sample.generics.TsGenericInterfaceExtending;
import com.braintribe.ts.sample.generics.TsGenericInterfaceWithBounds;
import com.braintribe.ts.sample.generics.TsSimleGenericInterface;
import com.braintribe.ts.sample.gwt.TsGwtClasses;
import com.braintribe.ts.sample.hierarchy.NonTsSuperType;
import com.braintribe.ts.sample.hierarchy.TsSubType;
import com.braintribe.ts.sample.hierarchy.TsSuperOfNonTsType;
import com.braintribe.ts.sample.hierarchy.TsSuperType;
import com.braintribe.ts.sample.hierarchy.TsType;
import com.braintribe.ts.sample.jsfunction.TsJsFunctionUser;
import com.braintribe.ts.sample.keyword.TsKeywordInterface;
import com.braintribe.ts.sample.keyword.TsKeywordsStatic;
import com.braintribe.ts.sample.keyword.with.TsKeywordPackageInterface;
import com.braintribe.ts.sample.nointerop.TsTypeWithNoInteropRefs;
import com.braintribe.ts.sample.statics.HasStaticMembers;

/**
 * Tests for {@link TypeScriptWriterForClasses}
 * 
 * @author peter.gazdik
 */
public class TypeScriptWriterForClassesTest extends AbstractWriterTest {

	@Test
	public void primitives() throws Exception {
		write(TsPrimitives.class);

		mustContain("_boolean(): boolean");
		mustContain("_byte(): number");
		mustContain("_char(): number");
		mustContain("_double(): number");
		mustContain("_float(): number");
		mustContain("_int(): number");
		mustContain("_long(): $tf.Long");
		mustContain("_short(): number");
		mustContain("_void(): void");
	}

	@Test
	public void wrappers() throws Exception {
		write(TsWrappers.class);

		mustContain("_Boolean(): boolean");
		mustContain("_Byte(): $tf.Byte");
		mustContain("_Char(): $tf.Character");
		mustContain("_Double(): number");
		mustContain("_Float(): $tf.Float");
		mustContain("_Integer(): $tf.Integer");
		mustContain("_Long(): $tf.Long");
		mustContain("_Short(): any");
	}

	@Test
	public void simpleTypes() throws Exception {
		write(TsSimpleTypes.class);

		mustContain("bigDecimal(): $tf.BigDecimal");
		mustContain("date(): $tf.Date");
		mustContain("string(): string");
	}

	@Test
	public void arrays() throws Exception {
		write(TsArrays.class);

		mustContain("arrayWithGenerifiedComponent(): $tf.Enum<$tf.Enum<any>>[];");
		mustContain("dates(): $tf.Date[];");
		mustContain("objects(objects: any[]): any[];");
		mustContain("stringsVarArgs(...strings: string[]): string[];");
		mustContain("es(eArray: E[]): E[];");
		mustContain("esVarArgs(...eVarArray: E[]): E[];");
	}

	@Test
	public void nativeJsTypes() throws Exception {
		write(TsNativeCustomNamespace.class);

		mustContain("// interface com.braintribe.ts.sample.TsNativeCustomNamespace");
		mustContain("interface TsNativeCustomNamespace {");
		mustContain("nativeFoobar(): string;");
	}

	@Test
	public void customInterface() throws Exception {
		write(TsCustomInterface.class);

		String[] tsOutputParts = output.split("\\}");
		output = tsOutputParts[0];
		mustContain("// interface com.braintribe.ts.sample.TsCustomInterface");
		mustContain("abstract class TsCustomInterface {");
		mustContain("static STATIC_FIELD: string;");
		mustContain("static staticMethod(): string;");

		output = tsOutputParts[1];
		mustContain("_enum(): TsEnum");
		mustContain("methodWithOptionalParams(first: number, second: string, thirdOptional?: string, fourthOptional?: string): void;");
		mustContain("otherNs(): $tf.test.other.TsOtherNamespaceInterface");
		mustContain("sameNs(): TsCustomInterface");
		mustContain("nativeGlobalNamespace(): nativeType;");
		mustContain("nativeCustomNamespace(): ns.TsNativeCustomNamespace;");
		mustContain("nativeWithGenerics(): NativeWithGenerics<string>;");
	}

	@Test
	public void customClass() throws Exception {
		write(TsClass.class);

		mustContain("// class com.braintribe.ts.sample.clazz.TsClass");
		mustContain("interface TsClass extends TsInterface1, TsInterface2 {}");
		mustContain("class TsClass {");
		mustContain("constructor(s: string, ts: TsClass);");
	}

	@Test
	public void enumType() throws Exception {
		write(TsEnum.class);

		mustContain("// enum com.braintribe.ts.sample.TsEnum");
		mustContain("interface TsEnum extends $tf.Comparable<TsEnum>{}");
		mustContain("class TsEnum {");
		mustContain("static spade: TsEnum;");
		mustContain("static club: TsEnum;");
		mustContain("static diamond: TsEnum;");
		mustContain("static heart: TsEnum;");

	}

	@Test
	public void enumType_WithInterface() throws Exception {
		write(TsEnumWithIface.class);

		mustContain("// enum com.braintribe.ts.sample.TsEnumWithIface");
		mustContain("interface TsEnumWithIface extends TsEnumInterface, $tf.Comparable<TsEnumWithIface>{}");
		mustContain("class TsEnumWithIface {");
		mustContain("static low: TsEnumWithIface;");
		mustContain("static middle: TsEnumWithIface;");
		mustContain("static high: TsEnumWithIface;");
	}

	@Test
	public void entityType() throws Exception {
		write(TsCustomGmTypes.class);

		mustContain("interface TsCustomGmTypes {");
		mustContain("gmEntity(): $T.com.braintribe.model.resource.Resource;");
		mustContain("gmEnum(): $T.com.braintribe.model.time.TimeUnit;");
	}

	@Test
	public void classHierarchy() throws Exception {
		write(com.braintribe.ts.sample.hierarchy.TsHierarchyClass.class);

		mustContain("// class com.braintribe.ts.sample.hierarchy.TsHierarchyClass");
		mustContain("interface TsHierarchyClass extends TsSuperOfNonTsType<boolean> {}");
		mustContain("class TsHierarchyClass extends TsSuperClassOfNonTsClass<string> {");
		mustContain("constructor();");
	}

	@Test
	public void interfaceHierarchy() throws Exception {
		write(TsType.class, TsSuperType.class, NonTsSuperType.class, TsSuperOfNonTsType.class, TsSubType.class);

		mustContainOnce("subMethod(): string;");
		mustContainOnce("regularMethod(): string;");
		mustContainOnce("tsSuperMethod(): string;");
		notContains("nonTsSuperMethod");

		mustContainOnce("covariant(): TsSubType;");
		mustContainOnce("covariant(): TsType;");
	}

	@Test
	public void statics() throws Exception {
		write(HasStaticMembers.class);

		mustContainOnce("// com.braintribe.ts.sample.statics.HasStaticMembers#STATIC_STRING");
		mustContainOnce("let STATIC_STRING: string;");

		mustContainOnce("function run(): void;");
		mustContainOnce("function jsRun(): void;");
		mustContainOnce("function asList<T extends $tf.Collection<any>>(): $tf.List<T>;");
		mustContainOnce("function getStaticAutoCast<T extends $tf.Collection<any>>(): T;");
		mustContainOnce("function getStaticListString(): $tf.List<string>;");
		mustContainOnce("function getStaticString(): string;");

		mustContainOnce("// com.braintribe.ts.sample.statics.HasStaticMembers#hasParameters(Integer, int)");
		mustContainOnce("function hasParams(i: $tf.Integer, ii: number): string;");
	}

	@Test
	public void genericsInFileds() throws Exception {
		write(TsGenericsInFields.class);

		mustContainOnce("static listString: $tf.List<string>;");
	}

	@Test
	public void genericsInMethods() throws Exception {
		write(TsGenericsInMethods.class);

		mustContain("genericMethod<K extends TsGenericsInMethods, V extends $tf.List<string>>(k: K, v: V): $tf.Map<K, V>");
		mustContain("genericMethod_MultiExtends<R extends $tf.Iterable<any> & TsGenericsInMethods>(): R");
		mustContain("genericMethod_MultiExtends2<R extends $tf.Map<any, any>>(): R");
		mustContain("genericMethod_MultiExtends3<R extends $tf.Map<TsGenericsInMethods, any>>(): $tf.List<R>");
		mustContain("genericMethod_NonJsParam<E>(e1: E, e2: E): $tf.List<E>");
		mustContain("genericMethod_Simple<R extends TsGenericsInMethods>(): R");

		mustContain("listConsumer(list: $tf.List<TsGenericsInMethods>): void;");
		mustContain("listOfListsProducer(): $tf.List<$tf.List<TsGenericsInMethods>>;");
		mustContain("listProducer(): $tf.List<TsGenericsInMethods>;");
		mustContain("listString(): $tf.List<string>;");
		mustContain("mapStringInteger(): $tf.Map<string, $tf.Integer>;");
	}

	@Test
	public void genericTypes_Simple() throws Exception {
		write(TsSimleGenericInterface.class);

		mustContain("interface TsSimleGenericInterface<T> {");
		mustContain("getValue(): T;");
		mustContain("setValue(value: T): void;");
	}

	@Test
	public void genericTypes_WithBounds() throws Exception {
		write(TsGenericInterfaceWithBounds.class);

		mustContain("interface TsGenericInterfaceWithBounds<T extends TsPrimitives & $tf.Map<string, $tf.Integer>> {");
		mustContain("getValue(): T;");
		mustContain("setValue(value: T): void;");
	}

	// Seeing that we only write declared methods, this test doesn't really have anything to test.
	@Test
	public void genericsTypes_Inheritance() throws Exception {
		write(TsGenericInterfaceExtending.class);

		mustContain("interface TsGenericInterfaceExtending<W>");
		// mustContain("nonTsFirstElement(): W;");
		// mustContain("nonTsGet(): $tf.List<W>;");
		// mustContain("nonTsMapKeys(): $tf.Set<string>;");
		// mustContain("nonTsMapValues(): $tf.Collection<W>;");
		mustContain("value(): W;");
		mustContain("value(value: W): void;");
	}

	@Test
	public void classLiterals() throws Exception {
		write(TsClassLiterals.class);

		mustContain("static absenceInformationClass: $tf.Class<$T.com.braintribe.model.generic.pr.AbsenceInformation>;");
		mustContain("static myClass: $tf.Class<TsClassLiterals>;");
		mustContain("get(clazz: $tf.Class<$T.com.braintribe.model.generic.GenericEntity>): string;");
	}

	@Test
	public void jsFunctions() throws Exception {
		write(TsJsFunctionUser.class);

		mustContain("static staticWithGenericsOfMethod<A, B>(fun: (t: A) => B): $tf.Map<A, B>;");

		mustContain("interface TsJsFunctionUser<X> {");
		mustContain("apply(function_: (s: string) => number): void;");
		mustContain("applyWithGenerics(fun: (t: string) => $tf.Integer): void;");
		mustContain("applyWithGenericsOfClass(fun: (t: X) => string): void;");
		mustContain("applyWithGenericsOfMethod<A, B>(fun: (t: A) => B): $tf.Map<A, B>;");
	}

	@Test
	public void knownTypeExtensions() throws Exception {
		write(TsKnownTypeExtension.class);

		mustContain("class TsKnownTypeExtension extends $tf.RuntimeException {");
	}

	@Test
	public void gwtClasses() throws Exception {
		write(TsGwtClasses.class);

		mustContain("asyncCallback(): $tf.session.AsyncCallback<string>;");
		mustContain("jsDate(): $tf.view.JsDate;");
	}

	@Test
	public void noInteropTypes() throws Exception {
		write(TsTypeWithNoInteropRefs.class);

		mustContain("nullCheck(o: any): void;");
		notContains("nullCheck(o: any): void; //");

		mustContain("nonJsGenericParam<T>(): T; // JS-WARN: com.braintribe.ts.sample.nointerop.NoInterop");
		mustContain("nonJsGenericParam2<T>(t: T): void; // JS-WARN: com.braintribe.ts.sample.nointerop.NoInterop");
		mustContain("nonJsGenericParam3(t: $tf.List<any>): void; // JS-WARN: com.braintribe.ts.sample.nointerop.NoInterop");
		mustContain("nonJsParam(param: any): void; // JS-WARN: com.braintribe.ts.sample.nointerop.NoInterop");
		mustContain("nonJsReturnType(): any; // JS-WARN: com.braintribe.ts.sample.nointerop.NoInterop");
	}

	@Test
	public void keywords() throws Exception {
		write(TsKeywordInterface.class, TsKeywordPackageInterface.class);

		mustContain("arguments_Method(arguments__: string): void;");
		mustContain("argumentsMethod(arguments_: string): void;");
		mustContain("awaitMethod(await_: string): void;");
		mustContain("debuggerMethod(debugger_: string): void;");
		mustContain("deleteMethod(delete_: string): void;");
		mustContain("evalMethod(eval_: string): void;");
		mustContain("exportMethod(export_: string): void;");
		mustContain("functionMethod(function_: string): void;");
		mustContain("inMethod(in_: string): void;");
		mustContain("letMethod(let_: string): void;");
		mustContain("prototypeMethod(prototype_: string): void;");
		mustContain("typeofMethod(typeof_: string): void;");
		mustContain("varMethod(var_: string): void;");
		mustContain("withMethod(with_: string): void;");
		mustContain("static yieldMethod(yield_: string): void;");

		notContains("number");

		mustContain("declare namespace com.braintribe.ts.sample.keyword.with_");
		mustContain("packageKeyword(): com.braintribe.ts.sample.keyword.with_.TsKeywordPackageInterface;");
		mustContain("packageKeywordEntity(): $T.com.braintribe.ts.sample.keyword.with_.TsKeywordPackageEntity_NotInModel;");
		mustContain("packageKeywordEnum(): com.braintribe.ts.sample.keyword.with_.TsKeywordPackageEnum;");
		mustContain("static packageKeywordStatic(): com.braintribe.ts.sample.keyword.with_.TsKeywordPackageInterface;");
	}

	@Test
	public void keywordsStatic() throws Exception {
		write(TsKeywordsStatic.class);

		mustContain("declare let STATIC_KEYWORD_PACKAGE: com.braintribe.ts.sample.keyword.with_.TsKeywordPackageInterface;");
		mustContain("declare let STATIC_KEYWORD_PACKAGE_ENTITY: $T.com.braintribe.ts.sample.keyword.with_.TsKeywordPackageEntity_NotInModel");

		mustContain("declare function pckgKeywordFun(arg: com.braintribe.ts.sample.keyword.with_.TsKeywordPackageInterface): void;");
		mustContain("declare function pckgKeywordEntityFun(arg: $T.com.braintribe.ts.sample.keyword.with_.TsKeywordPackageEntity_NotInModel): void;");

		mustContain("declare function arguments_Method(arguments__: string): void;");
		mustContain("declare function argumentsMethod(arguments_: string): void;");
		mustContain("declare function awaitMethod(await_: string): void;");
		mustContain("declare function debuggerMethod(debugger_: string): void;");
		mustContain("declare function deleteMethod(delete_: string): void;");
		mustContain("declare function evalMethod(eval_: string): void;");
		mustContain("declare function exportMethod(export_: string): void;");
		mustContain("declare function functionMethod(function_: string): void;");
		mustContain("declare function inMethod(in_: string): void;");
		mustContain("declare function letMethod(let_: string): void;");
		mustContain("declare function prototypeMethod(prototype_: string): void;");
		mustContain("declare function typeofMethod(typeof_: string): void;");
		mustContain("declare function varMethod(var_: string): void;");
		mustContain("declare function withMethod(with_: string): void;");
		mustContain("declare function yieldMethod(yield_: string): void;");

		notContains("$tf.Integer");
	}

	private void write(Class<?>... classes) {
		StringBuilder sb = new StringBuilder();

		TypeScriptWriterForClasses.write(Arrays.asList(classes), customGmTypeFilter, sb);

		output = sb.toString();

		spOut(1, "File content:\n" + output);
	}

}
