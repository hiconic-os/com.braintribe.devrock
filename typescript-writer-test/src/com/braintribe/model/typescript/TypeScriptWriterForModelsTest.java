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
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.meta.data.display.Color;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.typescript.model.TsJoat;
import com.braintribe.model.typescript.model.TsStringEval;
import com.braintribe.model.typescript.model.eval.TsEvalA;
import com.braintribe.model.typescript.model.eval.TsEvalABB;
import com.braintribe.model.typescript.model.eval.TsEvalB;
import com.braintribe.model.typescript.model.eval.TsEvalBB;
import com.braintribe.model.typescript.model.keyword.TsKeywordEntity;
import com.braintribe.model.typescript.model.keyword.TsKeywordEnum;
import com.braintribe.model.typescript.model.keyword.TsKeywordEnumOwner;
import com.braintribe.model.typescript.model.keyword.with.TsKeywordPackageEntity;
import com.braintribe.model.typescript.model.sub.TsSub;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.lcd.StringTools;

import jsinterop.annotations.JsType;
import jsinterop.context.JsKeywords;

/**
 * Tests for {@link TypeScriptWriterForModels}
 *
 * @author peter.gazdik
 */
public class TypeScriptWriterForModelsTest extends AbstractWriterTest {

	private static final List<EntityType<?>> tsModelTypes = asList( //
			TsSub.T, //
			TsStringEval.T, //
			TsJoat.T, //
			com.braintribe.model.typescript.model.duplicate_name.TsJoat.T, //
			com.braintribe.model.typescript.model.keyword.with.TsKeywordPackageEntity.T, //
			com.braintribe.model.typescript.model.keyword.await.TsKeywordPackageEntity.T //
	);

	@Test
	public void rootMdel() throws Exception {
		write(GenericEntity.T.getModel().getMetaModel());

		mustContain("namespace T.com.braintribe.model.generic {");
		mustContain("interface GenericEntity extends hc.reflection.EntityBase {");
		mustContain("globalId: string;");
		mustContain("id: any;");
		mustContain("partition: string;");
		mustContain("interface StandardIdentifiable extends GenericEntity {");
		mustContain("interface StandardIntegerIdentifiable extends GenericEntity {");
		mustContain("interface StandardStringIdentifiable extends GenericEntity {");
	}

	@Test
	public void tsWriterWritesReasonableOutput() throws Exception {
		write(buildTsModel());

		mustContain("/// <reference path=\"../com.braintribe.gm.absence-information-model-1.420~/absence-information-model.d.ts\" />");
		mustContain("const TsJoat: hc.reflection.EntityType<TsJoat>;");
		mustContain(
				"interface TsJoat extends T.com.braintribe.model.typescript.model.sub.TsSub, T.com.braintribe.model.generic.pr.AbsenceInformation {");

		mustContain("primitiveBoolean: boolean");
		mustContain("wrapperBoolean: boolean");

		mustContain("primitiveDouble: double");
		mustContain("wrapperDouble: double");

		mustContain("primitiveFloat: float");
		mustContain("wrapperFloat: float");

		mustContain("primitiveInteger: integer");
		mustContain("wrapperInteger: integer");

		mustContain("primitiveLong: long");
		mustContain("wrapperLong: long");

		mustContain("date: date;");
		mustContain("decimal: decimal;");
		mustContain("object: any;");
		mustContain("string: string;");

		mustContain("entity: TsJoat;");
		mustContain("otherNamespaceEntity: T.com.braintribe.model.typescript.model.sub.TsSub;");
		mustContain("tsEnum: TsEnum;");

		mustContain("listOfStrings: list<string>;");
		mustContain("mapOfStrings: map<string, string>;");
		mustContain("setOfStrings: set<string>;");

		mustContain("interface TsEnum extends hc.reflection.EnumBase<TsEnum>, hc.Enum<TsEnum> {}");
		mustContain("const TsEnum: {");
		mustContain("readonly [hc.Symbol.enumType]: hc.reflection.EnumType<TsEnum>,");
		mustContain("readonly ModelS: TsEnum,");
		mustContain("readonly ModelX: TsEnum,");

		mustContain("// Mandatory");
		mustContain("// Unique");
		mustContain("// Color(value=\"#ff0000\")");

		mustContain(
				"Eval(evaluator: hc.eval.Evaluator<T.com.braintribe.model.service.api.ServiceRequest>): hc.eval.JsEvalContext<list<string>>;");
		mustContain("EvalAndGet(evaluator: hc.eval.Evaluator<T.com.braintribe.model.service.api.ServiceRequest>): globalThis.Promise<list<string>>;");
		mustContain(
				"EvalAndGetReasoned(evaluator: hc.eval.Evaluator<T.com.braintribe.model.service.api.ServiceRequest>): globalThis.Promise<hc.reason.Maybe<list<string>>>;");
	}

	@Test
	public void tsWriter_EvalMultiInheritance() throws Exception {
		write(buildTsMultiInheritanceModel());

		cutTypeFromOutput(TsEvalABB.T);

		mustContain("Eval(evaluator: hc.eval.Evaluator<T.com.braintribe.model.service.api.ServiceRequest>): hc.eval.JsEvalContext<TsEvalB>;");
		mustContain("EvalAndGet(evaluator: hc.eval.Evaluator<T.com.braintribe.model.service.api.ServiceRequest>): globalThis.Promise<TsEvalB>;");
		mustContain(
				"EvalAndGetReasoned(evaluator: hc.eval.Evaluator<T.com.braintribe.model.service.api.ServiceRequest>): globalThis.Promise<hc.reason.Maybe<TsEvalB>>;");
	}

	private static final Set<String> jsKeywords = jsKeywordsWithout_Class();

	// class is omitted as that cannot be used as a property - getClass wouldn't work
	private static Set<String> jsKeywordsWithout_Class() {
		Set<String> result = newSet(JsKeywords.jsKeywords);
		result.remove("class");

		return result;
	}

	@Test
	public void tsWriter_JsKeywords() throws Exception {
		write(TsKeywordEntity.T, TsKeywordPackageEntity.T);

		for (String jsKeyword : jsKeywords) {
			mustContain(jsKeyword + "_: string");
		}

		mustContain("yield__: string");
		mustContain("keywordPackage: T.com.braintribe.model.typescript.model.keyword.with_.TsKeywordPackageEntity;");
		mustContain("namespace T.com.braintribe.model.typescript.model.keyword.with_");
	}

	@Test
	public void tsWriter_JsKeywords_Enum() throws Exception {
		write(TsKeywordEnumOwner.T);

		mustContain("interface TsKeywordEnum extends hc.reflection.EnumBase<TsKeywordEnum>, hc.Enum<TsKeywordEnum> {}");
		mustContain("const TsKeywordEnum: {");
		mustContain("readonly [hc.Symbol.enumType]: hc.reflection.EnumType<TsKeywordEnum>,");
		for (TsKeywordEnum e : TsKeywordEnum.class.getEnumConstants()) {
			mustContain("readonly " + e.name() + "_: TsKeywordEnum,");
		}
	}

	private void cutTypeFromOutput(EntityType<?> et) {
		output = StringTools.findSuffixWithBoundary(output, "const " + et.getShortName());
		output = StringTools.findPrefixWithBoundary(output, "}");
	}

	private void write(EntityType<?>... types) {
		writeAndPrintForTwoFramesLower(buildModel(types));
	}

	private void write(GmMetaModel model) {
		writeAndPrintForTwoFramesLower(model);
	}

	private void writeAndPrintForTwoFramesLower(GmMetaModel model) {
		StringBuilder sb = new StringBuilder();

		TypeScriptWriterForModels.write(model, this::rangifyModelVersion, this::resolveJsName, sb);

		output = sb.toString();

		spOut(2, "File content:\n" + output);

		mustContain("declare module '@dev.hiconic/hc-js-base' {");
		notContains("declare namespace");
	}

	private String resolveJsName(Class<?> clazz) {
		JsType jsType = clazz.getAnnotation(JsType.class);

		String namespace = jsType.namespace();
		String name = clazz.getSimpleName();

		return namespace + "." + name;
	}

	public static GmMetaModel buildTsModel() {
		ArrayList<Model> knownModels = asList( //
				AbsenceInformation.T.getModel(), //
				GenericEntity.T.getModel() //
		);

		GmMetaModel absenceInfoModel = AbsenceInformation.T.getModel().getMetaModel();
		absenceInfoModel.setVersion("1.420.1");

		NewMetaModelGeneration mmg = new NewMetaModelGeneration(knownModels);
		GmMetaModel result = mmg.buildMetaModel("test:ts-test-model", tsModelTypes, asList(absenceInfoModel));
		result.setVersion("1.42.1");

		enrich(result);

		return result;
	}

	private static void enrich(GmMetaModel tsTestModel) {
		BasicModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(tsTestModel);
		mdEditor.onEntityType(TsJoat.T) //
				.addMetaData(redColor()) //
				.addPropertyMetaData("enriched", Unique.T.create(), Mandatory.T.create());
	}

	private static Color redColor() {
		Color result = Color.T.create();
		result.setCode("#ff0000");

		return result;
	}

	private GmMetaModel buildTsMultiInheritanceModel() {
		return buildModel(TsEvalA.T, TsEvalB.T, TsEvalBB.T, TsEvalABB.T);
	}

}
