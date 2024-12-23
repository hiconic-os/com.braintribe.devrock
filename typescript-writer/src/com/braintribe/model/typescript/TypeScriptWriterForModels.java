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

import static com.braintribe.utils.lcd.CollectionTools2.acquireTreeSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newTreeMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.common.lcd.UnsupportedEnumException;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.annotation.meta.api.synthesis.AnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.synthesis.MdaSynthesis;
import com.braintribe.model.generic.base.EntityBase;
import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.tools.AbstractStringifier;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.utils.lcd.StringTools;

import jsinterop.context.JsKeywords;

/**
 * Properties with names that are reserved words in JS (one of: {@value JsKeywords#jsKeywords}) are escaped (here and in tf.js).
 * 
 * @author peter.gazdik
 */
public class TypeScriptWriterForModels extends AbstractStringifier {

	/** Writes .d.ts information for given GM types, including triple-slash references. */
	public static void write(GmMetaModel model, Function<String, String> versionRangifier, //
			Function<Class<?>, String> jsNameResolver, Appendable writer) {

		try {
			TypeScriptWriterHelper.writeTripleSlashReferences(model, versionRangifier, writer);
			write(model.getTypes(), jsNameResolver, writer);

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while generating typescript declarations for model: " + model.getName() + "#" + model.getVersion());
		}
	}

	/** Writes .d.ts information for given GM types. Assumes the triple-slash references were already written */
	public static void write(Collection<GmType> gmTypes, Function<Class<?>, String> jsNameResolver, Appendable writer) {
		if (!gmTypes.isEmpty()) {
			new TypeScriptWriterForModels(gmTypes, jsNameResolver, writer).writeNamespaceDeclarationForTheModel();
		}
	}

	// ####################################################
	// ## . . . . . . . . Implementation . . . . . . . . ##
	// ####################################################

	private final Collection<GmType> gmTypes;
	private final Function<Class<?>, String> jsNameResolver;

	private final NavigableMap<String, Set<CustomTypeDescriptor>> typesByNamespace = newTreeMap();
	private final Map<GmCustomType, CustomTypeDescriptor> ctdByGmType = newMap();

	private CustomTypeDescriptor currentCtd;

	private final String entityTypeJsName;
	private final String enumTypeJsName;
	private final String enumBaseJsName;
	private final String enumName;

	private TypeScriptWriterForModels(Collection<GmType> gmTypes, Function<Class<?>, String> jsNameResolver, Appendable writer) {
		super(writer, "", "\t");
		this.gmTypes = gmTypes;
		this.jsNameResolver = jsNameResolver;

		this.entityTypeJsName = jsNameResolver.apply(EntityType.class);
		this.enumTypeJsName = jsNameResolver.apply(EnumType.class);
		this.enumBaseJsName = jsNameResolver.apply(EnumBase.class);
		this.enumName = KnownJsType.java2Ts.get(Enum.class).fullName;

		indexTypes();
	}

	private void indexTypes() {
		for (GmType gmType : gmTypes) {
			if (gmType.isGmCustom()) {
				indexType((GmCustomType) gmType);
			}
		}
	}

	private void indexType(GmCustomType gmType) {
		CustomTypeDescriptor ctd = acquireCustomTypeDescriptor(gmType);

		acquireTreeSet(typesByNamespace, ctd.namespace).add(ctd);
	}

	private static class CustomTypeDescriptor implements Comparable<CustomTypeDescriptor> {

		private final GmCustomType gmType;
		private final String typeSignature;
		private final String namespace;
		private final String simpleName;

		public CustomTypeDescriptor(GmCustomType gmType) {
			this.gmType = gmType;

			this.typeSignature = gmType.getTypeSignature();
			this.namespace = resolveNamespace(typeSignature);
			this.simpleName = extractSimpleName(typeSignature);
		}

		private static String resolveNamespace(String typeSignature) {
			return GmCoreApiInteropNamespaces.type + "." + extractPackage(typeSignature);
		}

		private static String extractPackage(String typeSignature) {
			String packageName = StringTools.getFirstNCharacters(typeSignature, typeSignature.lastIndexOf("."));
			return JsKeywords.packageToJsNamespace(packageName);
		}

		private static String extractSimpleName(String typeSignature) {
			return StringTools.findSuffix(typeSignature, ".");
		}

		@Override
		public int compareTo(CustomTypeDescriptor o) {
			return simpleName.compareTo(o.simpleName);
		}
	}

	private void writeNamespaceDeclarationForTheModel() {
		println();
		println(TypeScriptWriterHelper.HC_JS_MODULE_AUGMENTATION_OPENING);
		println();
		levelUp();
		{
			typesByNamespace.forEach(this::writeNamespace);
		}
		levelDown();
		println("}");
	}

	private void writeNamespace(String namespace, Set<CustomTypeDescriptor> types) {
		print("namespace ");
		print(namespace);
		println(" {\n");

		levelUp();

		types.forEach(this::writeTypeDeclarationForCustomType);

		levelDown();

		println("}\n");
	}

	private void writeTypeDeclarationForCustomType(CustomTypeDescriptor ctd) {
		printEssentialMdAnnotations(ctd.gmType);
		if (ctd.gmType.isGmEnum()) {
			writeEnumTypeDeclaration(ctd);
		} else {
			writeEntityTypeDeclaration(ctd);
		}
	}

	private void writeEnumTypeDeclaration(CustomTypeDescriptor ctd) {
		print("interface ");
		print(ctd.simpleName);
		print(" extends ");
		print(enumBaseJsName);
		print("<");
		print(ctd.simpleName);
		print(">, ");
		print(enumName);
		print("<");
		print(ctd.simpleName);
		println("> {}");

		print("const ");
		print(ctd.simpleName);
		println(": {");

		levelUp();
		{
			writeEnumTypeSymbol(ctd);
			((GmEnumType) ctd.gmType).getConstants().forEach(c -> writeConstant(c, ctd));
		}
		levelDown();

		println("}\n");
	}

	private void writeEnumTypeSymbol(CustomTypeDescriptor ctd) {
		print("readonly [");
		print(KnownJsType.SYMBOL_ENUM_TYPE);
		print("]: ");
		print(enumTypeJsName);
		print("<");
		print(ctd.simpleName);
		println(">,");
	}

	private void writeConstant(GmEnumConstant c, CustomTypeDescriptor cType) {
		print("readonly ");
		print(JsKeywords.javaIdentifierToJs(c.getName()));
		print(": ");
		print(cType.simpleName);
		println(",");
	}

	private void writeEntityTypeDeclaration(CustomTypeDescriptor ctd) {
		currentCtd = ctd;
		GmEntityType gmEntityType = (GmEntityType) ctd.gmType;

		print("const ");
		print(ctd.simpleName);
		print(": ");
		print(entityTypeJsName);
		print("<");
		print(ctd.simpleName);
		println(">;");

		print("type ");
		print(ctd.simpleName);
		print(" = ");

		EvalInfo evalInfo = resolveEvaluatesTo(gmEntityType);
		boolean shouldWriteEval = evalInfo != null && evalInfo.needsDeclarationInTs;
		if (shouldWriteEval) {
			print("Evaluable<");
			printType(evalInfo.gmType, true);
			println("> &");
		}

		List<GmEntityType> superTypes = gmEntityType.getSuperTypes();
		if (!superTypes.isEmpty()) {
			if (shouldWriteEval)
				print("  ");
			int i = 0;
			for (GmEntityType superType : superTypes) {
				if (i++ > 0) {
					print(" & ");
				}
				printNullableType(superType);
			}

		} else if (gmEntityType.getTypeSignature().equals(GenericEntity.class.getName())) {
			print(jsNameResolver.apply(EntityBase.class));
		}

		List<GmProperty> properties = gmEntityType.getProperties();

		println(" &");
		print("  ");

		print("Entity<\"");
		print(ctd.typeSignature);
		print("\"");

		if (!properties.isEmpty()) {
			println(", {");

			levelUp();
			properties.forEach(this::writeProperty);

			levelDown();
			print("}");
		}

		println(">;");
		println();
	}

	private void writeProperty(GmProperty p) {
		printEssentialMdAnnotations(p);
		print(JsKeywords.javaIdentifierToJs(p.getName()));
		print(": ");
		printType(p.getType(), p.getNullable());
		println(";");
	}

	private final Map<GmEntityType, EvalInfo> evaluatesTo = newMap();

	private static class EvalInfo {
		GmType gmType;
		boolean needsDeclarationInTs;

		public EvalInfo(GmType gmType, boolean needsDeclarationInTs) {
			this.gmType = gmType;
			this.needsDeclarationInTs = needsDeclarationInTs;
		}
	}

	private EvalInfo resolveEvaluatesTo(GmEntityType gmEntityType) {
		EvalInfo info = evaluatesTo.get(gmEntityType);
		if (info == null) {
			evaluatesTo.put(gmEntityType, info = newEvalInfoFor(gmEntityType));
		}

		return info;
	}

	private EvalInfo newEvalInfoFor(GmEntityType gmEntityType) {
		GmType evaluatesTo = gmEntityType.getEvaluatesTo();
		if (evaluatesTo != null) {
			return new EvalInfo(evaluatesTo, true);
		}

		List<GmEntityType> superTypes = gmEntityType.getSuperTypes();
		EvalInfo result = null;
		for (GmEntityType superType : superTypes) {
			EvalInfo superInfo = resolveEvaluatesTo(superType);
			if (superInfo == null) {
				continue;
			}

			if (result == null) {
				result = new EvalInfo(superInfo.gmType, false);
			}

			if (result.gmType != superInfo.gmType) {
				result.gmType = pickMoreSpecificType(result.gmType, superInfo.gmType);
				result.needsDeclarationInTs = true;
			}
		}

		return result;
	}

	private GmType pickMoreSpecificType(GmType t1, GmType t2) {
		if (t1.isGmCollection()) {
			if (t2.isGmBase()) {
				return t1;
			}

			if (t1.typeKind() == GmTypeKind.MAP) {
				return pickMoreSpecificMapType((GmMapType) t1, (GmMapType) t2);
			}

			t1 = ((GmLinearCollectionType) t1).getElementType();
			t2 = ((GmLinearCollectionType) t2).getElementType();
		}

		if (t1.isGmBase()) {
			return t2;
		}

		if (t2.isGmBase()) {
			return t1;
		}

		if (t1.isGmEntity() && t2.isGmEntity()) {
			return isFirstSuperOfSecond((GmEntityType) t1, (GmEntityType) t2) ? t2 : t1;
		}

		// This is not expected, but I do not want to throw an exception here. Rather fix a broken typescript file.
		return t1;
	}

	private GmType pickMoreSpecificMapType(GmMapType mt1, GmMapType mt2) {
		// kv stands for keyOrValue
		GmType kv1 = mt1.getKeyType();
		GmType kv2 = mt2.getKeyType();

		if (kv1 == kv2) {
			kv1 = mt1.getValueType();
			kv2 = mt2.getValueType();
		}

		GmType mskv = pickMoreSpecificType(kv1, kv2);
		return mskv == kv1 ? mt1 : mt2;
	}

	private boolean isFirstSuperOfSecond(GmEntityType t1, GmEntityType t2) {
		List<GmEntityType> superTypes = t2.getSuperTypes();
		if (superTypes.contains(t1)) {
			return true;
		}

		for (GmEntityType superType : superTypes) {
			if (isFirstSuperOfSecond(t1, superType)) {
				return true;
			}
		}

		return false;
	}

	private void printNullableType(GmType gmType) {
		printType(gmType, true);
	}

	private void printType(GmType gmType, boolean nullable) {
		if (gmType.isGmSimple()) {
			printSimpleType((GmSimpleType) gmType, nullable);
			return;
		}

		switch (gmType.typeKind()) {
			case BASE:
				printBase();
				return;

			case ENUM:
			case ENTITY:
				printCustomType((GmCustomType) gmType);
				return;

			case MAP:
				printMapType((GmMapType) gmType);
				return;

			case LIST:
				printListType((GmListType) gmType);
				return;
			case SET:
				printSetType((GmSetType) gmType);
				return;

			default:
				throw new UnsupportedEnumException(gmType.typeKind());
		}
	}

	private void printSimpleType(GmSimpleType gmType, boolean nullable) {
		SimpleType st = gmType.<SimpleType> reflectionType();

		if (!nullable)
			print("P<");

		switch (st.getTypeCode()) {
			case dateType:
				print("date");
				break;
			case decimalType:
				print("decimal");
				break;
			case doubleType:
				print("double");
				break;
			case floatType:
				print("float");
				break;
			case integerType:
				print("integer");
				break;
			case longType:
				print("long");
				break;
			case booleanType:
				print("boolean");
				break;
			case stringType:
				print("string");
				break;
			default:
				throw new UnknownEnumException(st.getTypeCode());
		}

		if (!nullable)
			print(", { nullable: false }>");
	}

	private void printCustomType(GmCustomType gmType) {
		CustomTypeDescriptor ctd = acquireCustomTypeDescriptor(gmType);

		if (!currentCtd.namespace.equals(ctd.namespace)) {
			print(ctd.namespace);
			print(".");
		}

		print(ctd.simpleName);
	}

	private boolean insideCollection;

	private void printMapType(GmMapType gmType) {
		insideCollection = true;
		print("map<");
		printNullableType(gmType.getKeyType());
		print(", ");
		printNullableType(gmType.getValueType());
		print(">");
		insideCollection = false;
	}

	private void printListType(GmListType gmType) {
		print("list");
		printCollectionTypeParameters(gmType);
	}

	private void printSetType(GmSetType gmType) {
		print("set");
		printCollectionTypeParameters(gmType);
	}

	private void printCollectionTypeParameters(GmLinearCollectionType gmType) {
		insideCollection = true;
		print("<");
		printType(gmType.getElementType(), true);
		print(">");
		insideCollection = false;
	}

	private void printBase() {
		print(insideCollection ? "CollectionElement" : "Base");
	}

	private CustomTypeDescriptor acquireCustomTypeDescriptor(GmCustomType gmType) {
		return ctdByGmType.computeIfAbsent(gmType, CustomTypeDescriptor::new);
	}

	private void printEssentialMdAnnotations(HasMetaData modelElement) {
		for (AnnotationDescriptor ad : MdaSynthesis.synthesizeMetaDataAnnotations(modelElement)) {
			ad.withSourceCode(s -> println("// " + s));
		}
	}

}
