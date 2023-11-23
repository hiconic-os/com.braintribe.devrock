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

		mustContain("declare namespace $T.com.braintribe.model.generic {");
		mustContain("interface GenericEntity extends $tf.reflection.EntityBase {");
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
		mustContain("const TsJoat: $tf.reflection.EntityType<TsJoat>;");
		mustContain(
				"interface TsJoat extends $T.com.braintribe.model.typescript.model.sub.TsSub, $T.com.braintribe.model.generic.pr.AbsenceInformation {");

		mustContain("primitiveBoolean: boolean");
		mustContain("wrapperBoolean: boolean");

		mustContain("primitiveDouble: number");
		mustContain("wrapperDouble: number");

		mustContain("primitiveFloat: number");
		mustContain("wrapperFloat: $tf.Float");

		mustContain("primitiveInteger: number");
		mustContain("wrapperInteger: $tf.Integer");

		mustContain("primitiveLong: $tf.Long");
		mustContain("wrapperLong: $tf.Long");

		mustContain("date: $tf.Date;");
		mustContain("decimal: $tf.BigDecimal;");
		mustContain("object: any;");
		mustContain("string: string;");

		mustContain("entity: TsJoat;");
		mustContain("otherNamespaceEntity: $T.com.braintribe.model.typescript.model.sub.TsSub;");
		mustContain("tsEnum: TsEnum;");

		mustContain("listOfStrings: $tf.List<string>;");
		mustContain("mapOfStrings: $tf.Map<string, string>;");
		mustContain("setOfStrings: $tf.Set<string>;");

		mustContain("interface TsEnum extends $tf.reflection.EnumBase {}");
		mustContain("namespace TsEnum {");
		mustContain("const ModelS: TsEnum;");
		mustContain("const Model3: TsEnum;");
		mustContain("const ModelX: TsEnum;");

		mustContain("// Mandatory");
		mustContain("// Unique");
		mustContain("// Color(value=\"#ff0000\")");

		mustContain(
				"Eval(evaluator: $tf.eval.Evaluator<$T.com.braintribe.model.service.api.ServiceRequest>): $tf.eval.JsEvalContext<$tf.List<string>>;");
		mustContain("EvalAndGet(evaluator: $tf.eval.Evaluator<$T.com.braintribe.model.service.api.ServiceRequest>): Promise<$tf.List<string>>;");
		mustContain(
				"EvalAndGetReasoned(evaluator: $tf.eval.Evaluator<$T.com.braintribe.model.service.api.ServiceRequest>): Promise<$tf.reason.Maybe<$tf.List<string>>>;");
	}

	@Test
	public void tsWriter_EvalMultiInheritance() throws Exception {
		write(buildTsMultiInheritanceModel());

		cutTypeFromOutput(TsEvalABB.T);

		mustContain("Eval(evaluator: $tf.eval.Evaluator<$T.com.braintribe.model.service.api.ServiceRequest>): $tf.eval.JsEvalContext<TsEvalB>;");
		mustContain("EvalAndGet(evaluator: $tf.eval.Evaluator<$T.com.braintribe.model.service.api.ServiceRequest>): Promise<TsEvalB>;");
		mustContain(
				"EvalAndGetReasoned(evaluator: $tf.eval.Evaluator<$T.com.braintribe.model.service.api.ServiceRequest>): Promise<$tf.reason.Maybe<TsEvalB>>;");
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

		for (String jsKeyword : jsKeywords)
			mustContain(jsKeyword + "_: string");

		mustContain("yield__: string");
		mustContain("keywordPackage: $T.com.braintribe.model.typescript.model.keyword.with_.TsKeywordPackageEntity;");
		mustContain("declare namespace $T.com.braintribe.model.typescript.model.keyword.with_");
	}

	@Test
	public void tsWriter_JsKeywords_Enum() throws Exception {
		write(TsKeywordEnumOwner.T);

		mustContain("interface TsKeywordEnum extends $tf.reflection.EnumBase {}");
		mustContain("namespace TsKeywordEnum {");
		for (TsKeywordEnum e : TsKeywordEnum.class.getEnumConstants())
			mustContain("const " + e.name() + "_: TsKeywordEnum;");
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
