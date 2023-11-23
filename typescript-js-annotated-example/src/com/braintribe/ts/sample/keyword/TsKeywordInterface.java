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
import com.braintribe.ts.sample.keyword.with.TsKeywordPackageEnum;
import com.braintribe.ts.sample.keyword.with.TsKeywordPackageInterface;

import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@JsType
public interface TsKeywordInterface {

	// @formatter:off
	// int methods are omitted
	int arguments();
	int await();
	int debugger();
	int delete();
	int eval();
	int export();
	int function();
	int in();
	int let();
	int prototype();
	int typeof();
	int var();
	int with();
	static int yield() {return 1;}

	void argumentsMethod(String arguments);
	void awaitMethod(String await);
	void debuggerMethod(String debugger);
	void deleteMethod(String delete);
	void evalMethod(String eval);
	void exportMethod(String export);
	void functionMethod(String function);
	void inMethod(String in);
	void letMethod(String let);
	void prototypeMethod(String prototype);
	void typeofMethod(String typeof);
	void varMethod(String var);
	void withMethod(String with);
	static void yieldMethod(String yield) {yield.toString();}

	void arguments_Method(String arguments_);

	TsKeywordPackageInterface packageKeyword();
	TsKeywordPackageEntity_NotInModel packageKeywordEntity();
	TsKeywordPackageEnum packageKeywordEnum();
	static TsKeywordPackageInterface packageKeywordStatic() {return null;}
	// @formatter:on

}
