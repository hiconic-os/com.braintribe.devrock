// ============================================================================
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

import java.util.List;
import java.util.Map.Entry;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.tools.AbstractStringifier;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.shortener.NameShortener.ShortNameEntry;
import com.braintribe.model.shortener.NameShortener.ShortNames;

import jsinterop.context.JsKeywords;

/**
 * @author peter.gazdik
 */
public class ModelEnsuringDTsWriter extends AbstractStringifier {

	public static void writeDts(ModelEnsuringContext context, Appendable writer) {
		new ModelEnsuringDTsWriter(context, writer).writeDTs();
	}

	private final ModelEnsuringContext context;

	public ModelEnsuringDTsWriter(ModelEnsuringContext context, Appendable writer) {
		super(writer, "", "\t");
		this.context = context;
	}

	public void writeDTs() {
		writeTripleSlashReferenceToModelDTs();
		writeMeta();
		writeShortAliasesForTypes();
	}

	private void writeTripleSlashReferenceToModelDTs() {
		println("/// <reference path=\"./" + context.aid() + ".d.ts\" />\n");
	}

	private void writeMeta() {
		println("export declare namespace meta {");
		levelUp();
		println("const groupId: string;");
		println("const artifactId: string;");
		println("const version: string;");
		levelDown();
		println("}");
	}

	private void writeShortAliasesForTypes() {
		ShortNames<GmType> shortNames = context.shortNames();

		List<ShortNameEntry<GmType>> defaultNs = shortNames.paths.get("");
		if (defaultNs != null) {
			println();
			printNs(defaultNs, true);
		}

		for (Entry<String, List<ShortNameEntry<GmType>>> e : shortNames.paths.entrySet()) {
			if (e.getKey().isEmpty())
				continue;

			println("");
			println("export declare namespace " + JsKeywords.packageToJsNamespace(e.getKey()) + " {");
			levelUp();
			printNs(e.getValue(), false);
			levelDown();
			println("}");
		}
	}

	private void printNs(List<ShortNameEntry<GmType>> list, boolean topLevel) {
		for (ShortNameEntry<GmType> e : list) {
			if (topLevel)
				print("export import ");
			println(e.simpleName + " = " + jsSignatureWith$T(e) + ";");
		}
	}

	/* package */ static String jsSignatureWith$T(ShortNameEntry<GmType> e) {
		return GmCoreApiInteropNamespaces.type + "." + JsKeywords.classNameToJs(e.value.getTypeSignature());
	}
}
