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

import static com.braintribe.model.typescript.ModelEnsuringDTsWriter.jsSignatureWith$T;
import static com.braintribe.model.typescript.TypeScriptWriterHelper.nameBaseOfEnsure;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.braintribe.codec.marshaller.jse.JseMarshaller;
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
		for (VersionedArtifactIdentification d : context.dependencies())
			println("import \"" + TypeScriptWriterHelper.relativePathTo(d) + nameBaseOfEnsure(d.getArtifactId()) + ".js\";");

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
