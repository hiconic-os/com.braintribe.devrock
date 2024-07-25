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

import static com.braintribe.model.typescript.ModelEnsuringDTsWriter.jsSignatureWith$T;
import static com.braintribe.model.typescript.TypeScriptWriterHelper.nameBaseOfEnsure;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.braintribe.codec.marshaller.jse.JseMarshaller;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tools.AbstractStringifier;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.shortener.NameShortener.ShortNameEntry;
import com.braintribe.model.shortener.NameShortener.ShortNames;

import jsinterop.context.JsKeywords;

/**
 * @author peter.gazdik
 */
public class ModelEnsuringJsWriter extends AbstractStringifier {

	public static void writeJs(ModelEnsuringContext context, Appendable writer) {
		new ModelEnsuringJsWriter(context, writer).writeJs();
	}

	private final ModelEnsuringContext context;
	private final GmMetaModel model;
	private final String gid;
	private final String aid;
	private final String version;

	public ModelEnsuringJsWriter(ModelEnsuringContext context, Appendable writer) {
		super(writer, "", "\t");

		this.context = context;

		this.model = context.model();
		this.gid = context.gid();
		this.aid = context.aid();
		this.version = context.version();
	}

	public void writeJs() {
		writeDependencyImports();
		writeMeta();
		writeModelAssemblerFunction();
		writeShortAliasesForTypes();
	}

	private void writeDependencyImports() {
		if (context.forNpm()) {
			String ns = context.npmNamespace();
			for (ArtifactIdentification d : context.dependencies())
				println("import \"" + TypeScriptWriterHelper.npmPackageFullName(ns, d) + "\";");

		} else {
			for (VersionedArtifactIdentification d : context.dependencies())
				println("import \"" + TypeScriptWriterHelper.relativePathTo(d) + nameBaseOfEnsure(d.getArtifactId()) + ".js\";");
		}

		if (!context.dependencies().isEmpty())
			println();
	}

	private void writeMeta() {
		println("export const meta = {");
		levelUp();
		println("groupId: \"" + gid + "\",");
		println("artifactId: \"" + aid + "\",");
		println("version: \"" + version + "\",");
		levelDown();
		println("}");
	}

	private void writeShortAliasesForTypes() {
		ShortNames<GmType> shortNames = context.shortNames();

		List<ShortNameEntry<GmType>> defaultNs = shortNames.paths.get("");
		if (defaultNs != null) {
			println();
			printTopLevelNs(defaultNs);
		}

		for (Entry<String, List<ShortNameEntry<GmType>>> e : shortNames.paths.entrySet()) {
			if (e.getKey().isEmpty())
				continue;

			println("");
			println("export const " + JsKeywords.packageToJsNamespace(e.getKey()) + " = {");
			levelUp();
			printInnerNs(e.getValue());
			levelDown();
			println("}");
		}
	}

	private void printTopLevelNs(List<ShortNameEntry<GmType>> list) {
		for (ShortNameEntry<GmType> e : list)
			println("export const " + e.simpleName + " = " + jsSignatureWith$T(e) + ";");
	}

	private void printInnerNs(List<ShortNameEntry<GmType>> list) {
		Iterator<ShortNameEntry<GmType>> it = list.iterator();
		while (it.hasNext()) {
			ShortNameEntry<GmType> e = it.next();
			String ending = it.hasNext() ? "," : "";
			println(e.simpleName + ": " + jsSignatureWith$T(e) + ending);
		}
	}

	private void writeModelAssemblerFunction() {
		println();
		println("function modelAssembler($, P, _) {");

		GmMetaModel shallowModel = shallowModel();
		JseMarshaller jseMarshaller = jseMarshaller();

		String jseCode = jseMarshaller.encode(shallowModel);
		println(jseCode);

		println("}");
		println();
		println(GmCoreApiInteropNamespaces.internal + ".ensureModel(modelAssembler)");
	}

	private GmMetaModel shallowModel() {
		ConfigurableCloningContext cc = ConfigurableCloningContext.build() //
				.withCanTransferPropertyTest(this::canTransferModelProperty) //
				.done();

		return GmMetaModel.T.clone(cc, model, null);
	}

	@SuppressWarnings("unused")
	private boolean canTransferModelProperty(EntityType<?> entityType, Property property, GenericEntity origin, GenericEntity clone,
			AbsenceInformation originAi) {

		if (origin instanceof GmMetaModel)
			return !property.getName().equals(GmMetaModel.dependencies);

		if (!(origin instanceof GmType))
			return true;

		if (origin instanceof GmCollectionType)
			return true;

		if (property.getName().equals("typeSignature"))
			return true;

		GmType gmType = (GmType) origin;
		return model.getTypes().contains(gmType);
	}

	private JseMarshaller jseMarshaller() {
		JseMarshaller result = new JseMarshaller();
		result.setHostedMode(false);

		return result;
	}

}
