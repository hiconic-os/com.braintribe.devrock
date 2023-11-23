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
package com.braintribe.model.typescript.gwt_user_emul;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.braintribe.common.attribute.TypeSafeAttributes;
import com.braintribe.exception.Exceptions;
import com.braintribe.utils.FileTools;

/**
 * @author peter.gazdik
 */
public class GwtUserEmul_CodeMover {

	/**
	 * First copy the classes from the emulation to the gwt-user-emul folder. Emulation: com.google.gwt:gwt-user/com.google.gwt.emul...
	 * 
	 * The folder will look something like this:
	 * 
	 * <pre>
	 * 	gwt-user-emul
	 *     java
	 *       lang
	 *         Byte.java
	 *         Character.java
	 *         Class.java
	 *         ...
	 *       util
	 *         Collection.java
	 *         ...
	 *       JsAnnotationsPackageNames.java
	 * </pre>
	 * 
	 * When it comes to java.lang.Class, remove the entire body, add JsInterop annotation and delete all imports except Type, JsType and
	 * JsAnnotationsPackageNames (see below).
	 * 
	 * Then, run this thingie, which hopefully creates compilable sources out of it in the src/ folder, with packages being moved to fake.java....
	 * 
	 * Then, generate the JsInterop TypeScript files with {@link GwtUserEmul_TypeScriptGenerator}.
	 * 
	 * Note regarding java.lang.Class.<br>
	 * It is not JsInteropped originally, so we have prepare the class as stated above. We won't be able to call any methods anyway. The reason for
	 * Class being included is due to methods where a Class object is expected, like {@link TypeSafeAttributes#getAttribute(Class)}. There is no way
	 * in GWT to get a class literal (that does not involve extensive hacking) so we also have to prepare some fields of type Class to store the
	 * literals and JsInterop them. The fact that we artificially jsinterop the Class, without GWT-compiler knowing about it, simply means our
	 * typescript will make such methods type-compatible with only our static class fields, rather than say the method accepts "any".
	 */
	public static void main(String[] args) {
		new GwtUserEmul_CodeMover().run();
	}

	private static final String JAVA_FILE_SUFFIX = ".java";

	private final File sourceFolder = new File("gwt-user-emul");
	private final Path sourcePath = sourceFolder.toPath();

	private final File targetFolder = new File("src/fake");
	private final Path targetPath = targetFolder.toPath();

	private void run() {
		try {
			Files.walk(sourcePath) //
					.filter(this::isJavaSourceFile) //
					.forEach(this::moveFile);

			System.out.println("Code was successfully moved to: " + targetFolder.getPath());

		} catch (IOException e) {
			throw Exceptions.unchecked(e);
		}
	}

	private boolean isJavaSourceFile(Path path) {
		return Files.isRegularFile(path) && path.toString().endsWith(JAVA_FILE_SUFFIX);
	}

	private void moveFile(Path path) {
		Path relativePath = sourcePath.relativize(path);
		Path outputPath = targetPath.resolve(relativePath);

		String s = FileTools.read(path.toFile()).asString();

		s = ensureCompilableWhileMaintainingMethodSignatures(s);

		FileTools.write(outputPath.toFile()).string(s);
	}

	private String ensureCompilableWhileMaintainingMethodSignatures(String s) {
		// change package to fake.java....
		s = s.replaceFirst("package\\s([^;]+);", "package fake.$1;");

		// update import of JsAnnotationsPackageNames to fake.java package
		s = s.replaceFirst("import\\sjava\\.JsAnnotationsPackageNames;", "import fake.java.JsAnnotationsPackageNames;");

		// update import of com.goole to fake.com.google
		s = s.replaceAll("import\\s(static\\s)?(com\\.[^;]+);", "import $1 fake.$2;");

		// remove javaemul imports
		s = s.replaceAll("import\\s(static\\s)?javaemul.+;", "");

		s = s.replaceAll("import\\s.+\\.JavaScriptObject;", "");
		s = s.replace("JavaScriptObject", "Object");
		
		// Simple types have a weird field, e.g.: public static final Class<Float> TYPE = float.class;
		s = s.replaceAll("public\\sstatic\\sfinal\\sClass<[\\w]+>\\sTYPE.+;", "");

		// In Character source there are usages of this method statically imported from javaemul
		s = s.replaceAll("checkCriticalArgument\\(.+;", "");
		
		// In java.util.function classes this is used
		s = s.replaceAll("checkCriticalNotNull\\(.+;", "");

		// There are methods returning this type - not sure what that is, just to so we can compile I replace it with Object
		s = s.replace("NativeRegExp", "Object");

		// In BigDecimal there are two fields FIVE_POW[] and TEN_POW - initialized in static initializer that needs to be deleted 
		s = s.replaceAll("private static final.*_POW\\[\\];", "");
		// In BigDecimal there is setUnscaledValue(
		s = s.replaceAll("setUnscaledValue\\(checkNotNull.*", "");
		
		// In collections source there are usages of this method statically imported from javaemul
		s = s.replaceAll("checkNotNull.*", "");
		
		s = removeStaticInitializer(s);

		// remove body of every single void method
		s = replaceVoidBody(s);
				
		// see replaceBody
		s = replaceBody(s, "boolean", "false");
		s = replaceBody(s, "int", "0");
		s = replaceBody(s, "long", "0");
		s = replaceBody(s, "double", "0");
		s = replaceBody(s, "String", "null");
		s = replaceBody(s, "Spliterator<E>", "null");
		s = replaceBody(s, "BigDecimal", "null");
		s = replaceBody(s, "BigInteger", "null");

		// Since our target types will be in package fake.java.util, we have to make sure classes from java.util are reference properly. We could may
		// add import to java.util.*?
		s = addJavaUtil(s, "Spliterator");
		s = addJavaUtil(s, "Arrays");
		s = addJavaUtil(s, "Objects");

		s = addJavaMath(s, "BigInteger");
		s = addJavaMath(s, "MathContext");
		s = addJavaMath(s, "RoundingMode");
		
		return s;
	}

	private String addJavaUtil(String s, String type) {
		return addPackage(s, type, "java.util");
	}

	private String addJavaMath(String s, String type) {
		return addPackage(s, type, "java.math");
	}
	
	private String addPackage(String s, String type, String _package) {
		return s.replaceAll("(\\s|\\(|\\!)" + type, "$1" + _package + "." + type);
	}


	/**
	 * Replace a method like:
	 * 
	 * <pre>
	 * public static boolean isSomething(args) throws SomethingException { 
	 *   //mega complicated body with many emulated classes we don't have
	 * }
	 * </pre>
	 * 
	 * With something like:
	 * 
	 * <pre>
	 * public static boolean isSomething(args) throws SomethingException { return false }
	 * </pre>
	 */
	private String replaceBody(String s, String type, String result) {
		String replace =  "$1 {$2  return " + result + ";$2}";

		return s.replaceAll(methodRegex(type), replace);
	}

	static final String nonFiledNamAndSemicolon = "(?![\\w]+;)";
	static final String methodTillEnd = "(?s:(.(?!\\n\\s\\s\\}))+)\r?\n\\s\\s\\}";

	private static final String topLevelOffeset = "\n\\s\\s";

	private String replaceVoidBody(String s) {
		return s.replaceAll(methodRegex("void"), "$1 {$2  /* NOOP */$2}");
	}
	
	private String removeStaticInitializer(String s) {
		return s.replaceAll(topLevelOffeset + "static\\s\\{" + methodTillEnd, "");
	}

	private String methodRegex(String type) {
		return "(("+topLevelOffeset+")(public|private|protected|default)\\s(static\\s)?" + type + "\\s"+nonFiledNamAndSemicolon+"[^\\{]+)"+methodTillEnd;
	}


}
