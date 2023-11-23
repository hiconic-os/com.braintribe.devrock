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
package com.braintribe.ts.sample.keyword;

import com.braintribe.ts.sample.keyword.with.TsKeywordPackageEntity_NotInModel;
import com.braintribe.ts.sample.keyword.with.TsKeywordPackageInterface;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unused")
public interface TsKeywordsStatic {

	// @formatter:off
	// Integers method are omitted
	@JsMethod static Integer arguments() {return null;}
	@JsMethod static Integer await() {return null;}
	@JsMethod static Integer debugger() {return null;}
	@JsMethod static Integer delete() {return null;}
	@JsMethod static Integer eval() {return null;}
	@JsMethod static Integer export() {return null;}
	@JsMethod static Integer function() {return null;}
	@JsMethod static Integer in() {return null;}
	@JsMethod static Integer let() {return null;}
	@JsMethod static Integer prototype() {return null;}
	@JsMethod static Integer typeof() {return null;}
	@JsMethod static Integer var() {return null;}
	@JsMethod static Integer with() {return null;}
	@JsMethod static Integer yield() {return null;}

	
	@JsMethod static void argumentsMethod(String arguments) {/*NOOP*/}
	@JsMethod static void awaitMethod(String await) {/*NOOP*/}
	@JsMethod static void debuggerMethod(String debugger) {/*NOOP*/}
	@JsMethod static void deleteMethod(String delete) {/*NOOP*/}
	@JsMethod static void evalMethod(String eval) {/*NOOP*/}
	@JsMethod static void exportMethod(String export) {/*NOOP*/}
	@JsMethod static void functionMethod(String function) {/*NOOP*/}
	@JsMethod static void inMethod(String in) {/*NOOP*/}
	@JsMethod static void letMethod(String let) {/*NOOP*/}
	@JsMethod static void prototypeMethod(String prototype) {/*NOOP*/}
	@JsMethod static void typeofMethod(String typeof) {/*NOOP*/}
	@JsMethod static void varMethod(String var) {/*NOOP*/}
	@JsMethod static void withMethod(String with) {/*NOOP*/}
	@JsMethod static void yieldMethod(String yield) {/*NOOP*/}
	
	@JsMethod static void arguments_Method(String arguments_) {/*NOOP*/}

	@JsProperty static TsKeywordPackageInterface STATIC_KEYWORD_PACKAGE = null;
	@JsProperty static TsKeywordPackageEntity_NotInModel STATIC_KEYWORD_PACKAGE_ENTITY = null;

	@JsMethod static void pckgKeywordFun(TsKeywordPackageInterface arg) {/*NOOP*/}
	@JsMethod static void pckgKeywordEntityFun(TsKeywordPackageEntity_NotInModel arg) {/*NOOP*/}
	// @formatter:on

}
