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

import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;

/**
 * @author peter.gazdik
 */
public class ModelEnsuringWritersTest extends AbstractWriterTest {

	@Test
	public void tsWriterWorks() throws Exception {
		writeDTs(TypeScriptWriterForModelsTest.buildTsModel());

		mustContain("/// <reference path=\"./ts-test-model.d.ts\" />");
		mustContain("export declare namespace meta {");
		mustContain("const groupId: string;");
		mustContain("const artifactId: string;");
		mustContain("const version: string;");
		mustContain("}");
		mustContain("export import TsJoat = $T.com.braintribe.model.typescript.model.TsJoat;");
		mustContain("export import TsSub = $T.com.braintribe.model.typescript.model.sub.TsSub;");
		mustContain("export import TsEnum = $T.com.braintribe.model.typescript.model.TsEnum;");
		mustContain("export import TsStringEval = $T.com.braintribe.model.typescript.model.TsStringEval;");

		mustContain("export declare namespace with_ {");
		mustContain("TsKeywordPackageEntity = $T.com.braintribe.model.typescript.model.keyword.with_.TsKeywordPackageEntity;");
		mustContain("export declare namespace await_ {");
		mustContain("TsKeywordPackageEntity = $T.com.braintribe.model.typescript.model.keyword.await_.TsKeywordPackageEntity;");
	}

	private void writeDTs(GmMetaModel model) {
		writeAndPrintForTwoFramesLower(model, true);
	}

	@Test
	public void jsWriterWorks() throws Exception {
		writeJs(TypeScriptWriterForModelsTest.buildTsModel());

		mustContain("import \"../com.braintribe.gm.absence-information-model-1.420~/ensure-absence-information-model.js\";");

		mustContain("export const meta = {");
		mustContain("groupId: \"test\",");
		mustContain("artifactId: \"ts-test-model\",");
		mustContain("version: \"1.42.1\",");
		mustContain("function modelAssembler($, P, _) {");
		mustContain("$tf.reflection.internal.ensureModel(modelAssembler)");

		mustContain("export const TsStringEval = $T.com.braintribe.model.typescript.model.TsStringEval;");
		mustContain("export const TsJoat = $T.com.braintribe.model.typescript.model.TsJoat;");
		mustContain("export const TsSub = $T.com.braintribe.model.typescript.model.sub.TsSub;");
		mustContain("export const TsEnum = $T.com.braintribe.model.typescript.model.TsEnum;");

		mustContain("export const duplicate_name = {");
		mustContain("TsJoat: $T.com.braintribe.model.typescript.model.duplicate_name.TsJoat");

		mustContain("export const await_ = {");
		mustContain("TsKeywordPackageEntity: $T.com.braintribe.model.typescript.model.keyword.await_.TsKeywordPackageEntity");

		mustContain("export const with_ = {");
		mustContain("TsKeywordPackageEntity: $T.com.braintribe.model.typescript.model.keyword.with_.TsKeywordPackageEntity");

		// We had a bug that these properties were not cloned
		mustContain("com.braintribe.model.meta.GmListType");
		mustContain("com.braintribe.model.meta.GmSetType");
		mustContain("'elementType'");
		mustContain("com.braintribe.model.meta.GmMapType");
		mustContain("'keyType'");
		mustContain("'valueType'");
	}

	private void writeJs(GmMetaModel model) {
		writeAndPrintForTwoFramesLower(model, false);
	}

	private void writeAndPrintForTwoFramesLower(GmMetaModel model, boolean dts) {
		StringBuilder sb = new StringBuilder();

		if (dts)
			ModelEnsuringDTsWriter.writeDts(meContext(model), sb);
		else
			ModelEnsuringJsWriter.writeJs(meContext(model), sb);

		output = sb.toString();

		spOut(2, "Output:\n" + output);
	}

	private ModelEnsuringContext meContext(GmMetaModel model) {
		return ModelEnsuringContext.create(model, this::rangifyModelVersion);
	}

}
