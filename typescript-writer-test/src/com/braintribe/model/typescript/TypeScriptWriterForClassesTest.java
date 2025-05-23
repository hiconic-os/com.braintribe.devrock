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
import com.braintribe.ts.sample.nointerop.JsTypeWithTsIgnore;
import com.braintribe.ts.sample.nointerop.TsTypeWithNoInteropRefs;
import com.braintribe.ts.sample.nointerop.TsTypeWithUnignores;
import com.braintribe.ts.sample.nointerop.TsWithJavaScriptObject;
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
		mustContain("_long(): hc.Long");
		mustContain("_short(): number");
		mustContain("_void(): void");
	}

	@Test
	public void wrappers() throws Exception {
		write(TsWrappers.class);

		mustContain("_Boolean(): boolean");
		mustContain("_Byte(): hc.Byte");
		mustContain("_Char(): hc.Character");
		mustContain("_Double(): number");
		mustContain("_Float(): hc.Float");
		mustContain("_Integer(): hc.Integer");
		mustContain("_Long(): hc.Long");
		mustContain("_Short(): any");
	}

	@Test
	public void simpleTypes() throws Exception {
		write(TsSimpleTypes.class);

		mustContain("bigDecimal(): T.Decimal");
		mustContain("date(): hc.Date");
		mustContain("string(): string");
	}

	@Test
	public void arrays() throws Exception {
		write(TsArrays.class);

		mustContain("arrayWithGenerifiedComponent(): hc.Enum<hc.Enum<any>>[];");
		mustContain("dates(): hc.Date[];");
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
		mustContain("otherNs(): hc.test.other.TsOtherNamespaceInterface");
		mustContain("sameNs(): TsCustomInterface");
		mustContain("nativeGlobalNamespaceCustomName(): globalThis.nativeType;");
		mustContain("nativeGlobalNamespace(): globalThis.TsNativeGlobalNamespace;");
		mustContain("nativeCustomNamespace(): ns.TsNativeCustomNamespace;");
		mustContain("nativeWithGenerics(): globalThis.NativeWithGenerics<string>;");
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
		mustContain("interface TsEnum extends hc.Comparable<TsEnum>{}");
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
		mustContain("interface TsEnumWithIface extends TsEnumInterface, hc.Comparable<TsEnumWithIface>{}");
		mustContain("class TsEnumWithIface {");
		mustContain("static low: TsEnumWithIface;");
		mustContain("static middle: TsEnumWithIface;");
		mustContain("static high: TsEnumWithIface;");
	}

	@Test
	public void entityType() throws Exception {
		write(TsCustomGmTypes.class);

		mustContain("interface TsCustomGmTypes {");
		mustContain("gmEntity(): T.com.braintribe.model.resource.Resource;");
		mustContain("gmEnum(): T.com.braintribe.model.time.TimeUnit;");
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
		mustContainOnce("function asList<T extends hc.Collection<any>>(): hc.List<T>;");
		mustContainOnce("function getStaticAutoCast<T extends hc.Collection<any>>(): T;");
		mustContainOnce("function getStaticListString(): hc.List<string>;");
		mustContainOnce("function getStaticString(): string;");

		mustContainOnce("// com.braintribe.ts.sample.statics.HasStaticMembers#hasParameters(Integer, int)");
		mustContainOnce("function hasParams(i: hc.Integer, ii: number): string;");
	}

	@Test
	public void genericsInFileds() throws Exception {
		write(TsGenericsInFields.class);

		mustContainOnce("static listString: hc.List<string>;");
	}

	@Test
	public void genericsInMethods() throws Exception {
		write(TsGenericsInMethods.class);

		mustContain("genericMethod<K extends TsGenericsInMethods, V extends hc.List<string>>(k: K, v: V): hc.Map<K, V>");
		mustContain("genericMethod_MultiExtends<R extends hc.Iterable<any> & TsGenericsInMethods>(): R");
		mustContain("genericMethod_MultiExtends2<R extends hc.Map<any, any>>(): R");
		mustContain("genericMethod_MultiExtends3<R extends hc.Map<TsGenericsInMethods, any>>(): hc.List<R>");
		mustContain("genericMethod_NonJsParam<E>(e1: E, e2: E): hc.List<E>");
		mustContain("genericMethod_Simple<R extends TsGenericsInMethods>(): R");

		mustContain("listConsumer(list: hc.List<TsGenericsInMethods>): void;");
		mustContain("listOfListsProducer(): hc.List<hc.List<TsGenericsInMethods>>;");
		mustContain("listProducer(): hc.List<TsGenericsInMethods>;");
		mustContain("listString(): hc.List<string>;");
		mustContain("mapStringInteger(): hc.Map<string, hc.Integer>;");
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

		mustContain("interface TsGenericInterfaceWithBounds<T extends TsPrimitives & hc.Map<string, hc.Integer>> {");
		mustContain("getValue(): T;");
		mustContain("setValue(value: T): void;");
	}

	// Seeing that we only write declared methods, this test doesn't really have anything to test.
	@Test
	public void genericsTypes_Inheritance() throws Exception {
		write(TsGenericInterfaceExtending.class);

		mustContain("interface TsGenericInterfaceExtending<W>");
		// mustContain("nonTsFirstElement(): W;");
		// mustContain("nonTsGet(): hc.List<W>;");
		// mustContain("nonTsMapKeys(): hc.Set<string>;");
		// mustContain("nonTsMapValues(): hc.Collection<W>;");
		mustContain("value(): W;");
		mustContain("value(value: W): void;");
	}

	@Test
	public void classLiterals() throws Exception {
		write(TsClassLiterals.class);

		mustContain("static absenceInformationClass: hc.Class<T.com.braintribe.model.generic.pr.AbsenceInformation>;");
		mustContain("static myClass: hc.Class<TsClassLiterals>;");
		mustContain("get(clazz: hc.Class<T.com.braintribe.model.generic.GenericEntity>): string;");
	}

	@Test
	public void jsFunctions() throws Exception {
		write(TsJsFunctionUser.class);

		mustContain("static staticWithGenericsOfMethod<A, B>(fun: (t: A) => B): hc.Map<A, B>;");

		mustContain("interface TsJsFunctionUser<X> {");
		mustContain("apply(function_: (s: string) => number): void;");
		mustContain("applyWithGenerics(fun: (t: string) => hc.Integer): void;");
		mustContain("applyWithGenericsOfClass(fun: (t: X) => string): void;");
		mustContain("applyWithGenericsOfMethod<A, B>(fun: (t: A) => B): hc.Map<A, B>;");
	}

	@Test
	public void knownTypeExtensions() throws Exception {
		write(TsKnownTypeExtension.class);

		mustContain("class TsKnownTypeExtension extends hc.RuntimeException {");
	}

	@Test
	public void gwtClasses() throws Exception {
		write(TsGwtClasses.class);

		mustContain("asyncCallback(): hc.session.AsyncCallback<string>;");
	}

	@Test
	public void noInteropTypes() throws Exception {
		write(TsTypeWithNoInteropRefs.class);

		mustContain("nullCheck(o: any): void;");
		notContains("nullCheck(o: any): void; //");

		mustContain("nonJsGenericParam<T>(): T; // JS-WARN: com.braintribe.ts.sample.nointerop.NoInterop");
		mustContain("nonJsGenericParam2<T>(t: T): void; // JS-WARN: com.braintribe.ts.sample.nointerop.NoInterop");
		mustContain("nonJsGenericParam3(t: hc.List<any>): void; // JS-WARN: com.braintribe.ts.sample.nointerop.NoInterop");
		mustContain("nonJsParam(param: any): void; // JS-WARN: com.braintribe.ts.sample.nointerop.NoInterop");
		mustContain("nonJsReturnType(): any; // JS-WARN: com.braintribe.ts.sample.nointerop.NoInterop");
	}

	@Test
	public void noJsWarnForJavaScriptObject() throws Exception {
		write(TsWithJavaScriptObject.class);

		mustContain("newJsObject(): any;");
		notContains("JS-WARN");
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

		mustContain("namespace com.braintribe.ts.sample.keyword.with_");
		mustContain("packageKeyword(): com.braintribe.ts.sample.keyword.with_.TsKeywordPackageInterface;");
		mustContain("packageKeywordEntity(): T.com.braintribe.ts.sample.keyword.with_.TsKeywordPackageEntity_NotInModel;");
		mustContain("packageKeywordEnum(): com.braintribe.ts.sample.keyword.with_.TsKeywordPackageEnum;");
		mustContain("static packageKeywordStatic(): com.braintribe.ts.sample.keyword.with_.TsKeywordPackageInterface;");
	}

	@Test
	public void keywordsStatic() throws Exception {
		write(TsKeywordsStatic.class);

		mustContain("declare let STATIC_KEYWORD_PACKAGE: com.braintribe.ts.sample.keyword.with_.TsKeywordPackageInterface;");
		mustContain("declare let STATIC_KEYWORD_PACKAGE_ENTITY: T.com.braintribe.ts.sample.keyword.with_.TsKeywordPackageEntity_NotInModel");

		mustContain("declare function pckgKeywordFun(arg: com.braintribe.ts.sample.keyword.with_.TsKeywordPackageInterface): void;");
		mustContain("declare function pckgKeywordEntityFun(arg: T.com.braintribe.ts.sample.keyword.with_.TsKeywordPackageEntity_NotInModel): void;");

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

		notContains("hc.Integer");
	}

	@Test
	public void tsIgnore() throws Exception {
		write(JsTypeWithTsIgnore.class);

		notContains("JsTypeWithTsIgnore");
		notContains("foobar");
		notContains("namespace");
	}

	@Test
	public void tsUnignore() throws Exception {
		write(TsTypeWithUnignores.class);

		mustContain("TsTypeWithUnignores");
		mustContain("unignored(): string;");
		mustContain("unignoredAndMapped(): string;");
		mustContain("create(): string;");

		ensureBridgeCreateMethodNotPresent();
	}

	/**
	 * This could fail when run from command line, but not from Eclips.
	 *
	 * @see TsTypeWithUnignores#create()
	 */
	private void ensureBridgeCreateMethodNotPresent() {
		notContains("create(): any");
	}

	private void write(Class<?>... classes) {
		StringBuilder sb = new StringBuilder();

		TypeScriptWriterForClasses.write(Arrays.asList(classes), customGmTypeFilter, sb);

		output = sb.toString();

		spOut(1, "File content:\n" + output);

		mustContain("declare module '@dev.hiconic/hc-js-base' {");
		notContains("declare namespace");
	}

}
