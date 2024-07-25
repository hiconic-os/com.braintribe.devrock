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

import static com.braintribe.model.typescript.KnownJsType.JS_INTEROP_AUTO;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static jsinterop.context.JsKeywords.packageToJsNamespace;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.GmSystemInterface;
import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.utils.ReflectionTools;
import com.braintribe.utils.lcd.StringTools;

import jsinterop.annotations.JsType;
import jsinterop.context.JsKeywords;

/**
 * @author peter.gazdik
 */
public class TypeScriptWriterHelper {

	/** Actually JS reserved words, but keywords is shorter and cooler */
	public static final Set<String> jsKeywords = newSet(JsKeywords.jsKeywords);

	public static Function<Class<?>, String> jsNameResolver(ClassLoader classLoader) {
		return clazz -> TypeScriptWriterHelper.resolveJsName(clazz, classLoader);
	}

	// Just to be safe, but we expect this to be called with EntityBase, EntityType and EnumType and those should be
	// annotated with JsType.
	private static String resolveJsName(Class<?> type, ClassLoader classLoader) {
		Class<?> clazz = ReflectionTools.getClassOrNull(type.getName(), classLoader);
		if (clazz == null)
			return "Object";

		JsType jsType = clazz.getDeclaredAnnotation(JsType.class);
		if (jsType == null)
			return "Object";

		String name = jsNameOrDefault(jsType.name(), () -> extractSimpleName(type));
		String namespace = jsNameOrDefault(jsType.namespace(), () -> extractPackageName(type));

		return namespace + "." + name;
	}

	/**
	 * <tt>jsName</tt> is a value read from a js-interop annotation,
	 * <p>
	 * This method returns given jsName as long as it is not {@code <auto>}, in which case it returns the value from
	 * <tt>defaultValueSupplier</tt>
	 */
	public static String jsNameOrDefault(String jsName, Supplier<String> defaultValueSupplier) {
		return jsName.equals(JS_INTEROP_AUTO) ? defaultValueSupplier.get() : jsName;
	}

	public static String extractNamespace(Class<?> clazz) {
		return packageToJsNamespace(extractPackageName(clazz));
	}

	private static String extractPackageName(Class<?> clazz) {
		Package p = clazz.getPackage();
		return p == null ? "" : p.getName();
	}

	public static String extractSimpleName(Class<?> clazz) {
		return StringTools.findSuffix(clazz.getName(), ".");
	}

	public static Predicate<Class<?>> createCustomGmTypeFilter(ClassLoader classLoader) {
		Class<?> ge = findBaseClass(GenericEntity.class.getName(), classLoader);
		Class<?> enm = findBaseClass(EnumBase.class.getName(), classLoader);
		Class<? extends Annotation> gsi = findBaseClass(GmSystemInterface.class.getName(), classLoader);

		/* We actually expect either none of them to be null or all of them, as the findBaseClass method returns null iff it
		 * cannot find the class with given class-loader. As all these classes come from "gm-core-api", they will either all be
		 * there or none. But just to be sure, we check if either of them was not found. */
		if (ge == null || enm == null || gsi == null)
			return c -> Boolean.FALSE;
		else
			return c -> (c.isEnum() && enm.isAssignableFrom(c)) || //
					(c.isInterface() && ge.isAssignableFrom(c) && c.getAnnotation(gsi) == null);
	}

	public static <T> Class<T> findBaseClass(String baseClassName, ClassLoader classLoader) {
		try {
			Class<?> result = Class.forName(baseClassName, false, classLoader);
			if (result.getClassLoader() != classLoader)
				return null;

			return (Class<T>) result;

		} catch (ClassNotFoundException e) {
			throw Exceptions.unchecked(e);
		}
	}

	public static String toShortNotationVersion(String versionAsString) {
		return VersionExpression.parse(versionAsString).asShortNotation();
	}

	public static void writeTripleSlashReferences(GmMetaModel model, Function<String, String> versionRangifier, Appendable writer)
			throws IOException {
		writeTripleSlashReferences(getDependencyIdentifications(model, versionRangifier), writer);
	}

	public static void writeTripleSlashReferences(List<VersionedArtifactIdentification> dependencies, Appendable writer) throws IOException {
		for (VersionedArtifactIdentification d : dependencies)
			writeTripleSlashReference(relativePathTo(d) + dtsFileName(d.getArtifactId()), writer);

		if (!dependencies.isEmpty())
			writer.append("\n");
	}

	public static String dtsFileName(String artifactId) {
		return artifactId + ".d.ts";
	}

	public static void writeTripleSlashReferenceToMain(String aid, Appendable writer) throws IOException {
		writeTripleSlashReference(mainDtsFileName(aid), writer);
		writer.append("\n");
	}
		
	public static void writeTripleSlashReference(String path, Appendable writer) throws IOException {
		writer.append("/// <reference path=\"" + path + "\" />\n");
	}

	public static String mainDtsFileName(String artifactId) {
		return artifactId + ".d.ts";		
	}

	public static String staticDtsFileName(String artifactId) {
		return artifactId + ".static.d.ts";
	}

	public static String typesDtsFileName(String artifactId) {
		return artifactId + ".types.d.ts";
	}
	
	public static String jsinteropDtsFileName(String artifactId) {
		return artifactId + ".jsinterop.d.ts";
	}

	/* package */ static String nameBaseOfEnsure(String artifactId) {
		return "ensure-" + artifactId;
	}

	public static String npmPackageFullName(String namespace, ArtifactIdentification aa) {
		return "@" + namespace + "/" + npmPackageName(aa);
	}

	/* package */  static String npmPackageName(ArtifactIdentification aa) {
		return npmPackageName(aa.getArtifactId(), aa.getGroupId());
	}

	/* package */  static String npmPackageName(String artifactId, String groupId) {
		return groupId + "--" + artifactId;
	}

	public static String relativePathTo(VersionedArtifactIdentification depInfo) {
		String gid = depInfo.getGroupId();
		String aid = depInfo.getArtifactId();
		String snv = toShortNotationVersion(depInfo.getVersion());
		return "../" + gid + "." + aid + "-" + snv + "/";
	}

	private static List<VersionedArtifactIdentification> getDependencyIdentifications(GmMetaModel model, Function<String, String> versionRangifier) {
		return model.getDependencies().stream() //
				.map(m -> modelToArtifactInfo(m, versionRangifier)) //
				.collect(Collectors.toList());
	}

	public static VersionedArtifactIdentification modelToArtifactInfo(GmMetaModel model) {
		return modelToArtifactInfo(model, version -> version);
	}

	public static VersionedArtifactIdentification modelToArtifactInfo(GmMetaModel model, Function<String, String> versionRangifier) {
		String[] parts = model.getName().split(":");
		if (parts.length != 2)
			throw new IllegalArgumentException(
					"Unexpected model name format. Expected: ${groupId}:${artifactId}, but the name was: " + model.getName());

		return VersionedArtifactIdentification.create(parts[0], parts[1], versionRangifier.apply(model.getVersion()));
	}

}
