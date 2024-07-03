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
