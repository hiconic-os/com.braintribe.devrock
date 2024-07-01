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
package com.braintribe.devrock.artifact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.braintribe.asm.ClassWriter;
import com.braintribe.asm.MethodVisitor;
import com.braintribe.asm.Opcodes;
import com.braintribe.common.artifact.ArtifactReflection;
import com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.FilesystemError;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.utils.paths.UniversalPath;

/**
 * The ArtifactReflectionGenerator can generate artifact reflection based on different input data. Either just from the <b>projectDir</b>, in which a "pom.xml" must be located, 
 * or from a {@link CompiledArtifact} object. The output is stored in the build-output <b>classesFolder</b>, which can be specified. <br>
 * 
 * The output consists of two files per artifact:  
 * <ul>
 *   <li>build-dir/META-INF/artifact-descriptor.properties</li>
 *   <li>build-dir/group_dir/artifact_name.class</li>
 * </ul> 
 *  where the former is an ASCII/properties version of the latter. Note, that <b>group_dir</b> is the default way to 
 *  build group directories from the namespace by replacing the "." with "/". The <b>artifact_name</b> is determined from the artifactId by 
 *  Camel-casing the artifactId: "this-is-my-artifact" will become "ThisIsMyArtifact". The class file contains the corresponding {@link ArtifactReflection}.
 *  
 *  <p>The {@link ArtifactReflectionGenerator} is fully {@link Reason}ed. </p>
 * 
 * @author Dirk Scheffler
 *
 */
public class ArtifactReflectionGenerator implements Opcodes {
	
	private DeclaredArtifactCompiler artifactCompiler;
	
	/**
	 * To set the {@link DeclaredArtifactCompiler}, if only the project directory is available to read the pom.xml file.
	 * 
	 * @param artifactCompiler {@link DeclaredArtifactCompiler}
	 */
	public void setArtifactCompiler(DeclaredArtifactCompiler artifactCompiler) {
		this.artifactCompiler = artifactCompiler;
	}

	/**
	 * Generate artifact reflection based on the project directory. The {@link ArtifactReflectionGenerator} will read the "pom.xml" file
	 * from the <b>projectDir</b>. 
	 * 
	 * @param projectDir Project directory containing "pom.xml"
	 */
	public Maybe<Void> generate(File projectDir) {
		return generate(projectDir, new File(projectDir, "build"));
	}

	/**
	 * Same as {@link #generate(File)} but with the additional option to specify the output directory, where 
	 * the artifact reflection files will be stored.  
	 * 
	 * @param projectDir Project directory containing "pom.xml"
	 * @param classesFolder Output directory
	 */
	public Maybe<Void> generate(File projectDir, File classesFolder) {
		return  getPomFile(projectDir) //
				.flatMap(this::compileArtifact) //
				.flatMap(new StatefulGenerator(classesFolder)::generate);
	}
	
	/**
	 * Generate artifact reflection based on {@link CompiledArtifact}. The output folder can be specified. 
	 * 
	 * @param artifact {@link CompiledArtifact}
	 * @param classesFolder Output directory
	 */
	public Maybe<Void> generate(CompiledArtifact artifact, File classesFolder) {
		return new StatefulGenerator(classesFolder).generate(artifact);
	}
	
	private Maybe<CompiledArtifact> compileArtifact(File file) {
		return (artifactCompiler != null? //
				artifactCompiler.compileReasoned(file): //
				DeclaredArtifactIdentificationExtractor.extractMinimalArtifact(file));
	}

	private Maybe<File> getPomFile(File projectDir) {
		if (projectDir == null)
			return Reasons.build(InvalidArgument.T).text("Argument projectDir must not be null").toMaybe();

		File pomFile = new File(projectDir, "pom.xml");

		if (!pomFile.exists()) {
			return Reasons.build(NotFound.T)
					.text("Project descriptor not found in directory: " + pomFile.getAbsolutePath()).toMaybe();
		}

		return Maybe.complete(pomFile);
	}

	
	/*
	 * Internal helper class for properly {@link Reason}ed artifact reflection generation.
	 */	
	private class StatefulGenerator {
		
		CompiledArtifact artifact; // input
		File classesFolder; // output
		
		private String canonizedGroupdId;
		private String canonizedArtifactId;
		private String className;

		public StatefulGenerator(File classesFolder) {
			this.classesFolder = classesFolder;
		}

		public Maybe<Void> generate(CompiledArtifact artifact) {
			this.artifact = artifact;
			return checkArtifact() //
					.flatMap(this::generateClassWithAsm) //
					.flatMap(this::writeArtifactReflection) //
					.flatMap(v -> writeMetaInf());
		}
		
		private Maybe<CompiledArtifact> checkArtifact() {
			if (this.artifact.getInvalid()) {
				return this.artifact.getWhyInvalid().asMaybe();
			}

			return Maybe.complete(this.artifact);
		}

		private Maybe<byte[]> generateClassWithAsm(CompiledArtifact artifact) {
			String groupId = artifact.getGroupId();
			String artifactId = artifact.getArtifactId();
			String version = artifact.getVersion().asString();
			String archetype = artifact.getProperties().get("archetype");

			return generateClassWithAsm(groupId, artifactId, version, archetype);
		}

		private Maybe<byte[]> generateClassWithAsm(String groupId, String artifactId, String version,
				String archetype) {
			try {
				ClassWriter classWriter = new ClassWriter(0);

				String className = buildCanonizedClassName(groupId, artifactId);

				String internalName = className.replace('.', '/');
				String superName = "java/lang/Object";
				String artifactReflectionDesc = "Lcom/braintribe/common/artifact/ArtifactReflection;";
				String reflectionFieldName = "reflection";
//				String groupId();
//
//				String artifactId();
//
//				String version();
//
//				Set<String> archetypes();
//
//				/**
//				 * @return "groupId:artifactId"
//				 */
//				String name();
//
//				/**
//				 * @return "groupId:artifactId#version"
//				 */
//				String versionedName();


				// create class with a name based on artifact identification deduction
				classWriter.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, internalName, null, superName, null);

				// add public static final field for the reflection instance
				classWriter.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, reflectionFieldName, artifactReflectionDesc,
						null, null);
				
				// build class initializer
				MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "<clinit>", "()V", null, null);
				
				String name = groupId + ":" + artifactId;
				String versionedName = name + "#" + version;
				
				fillStaticField(classWriter, internalName, "groupId", groupId);
				fillStaticField(classWriter, internalName, "artifactId", artifactId);
				fillStaticField(classWriter, internalName, "version", version);
				fillStaticField(classWriter, internalName, "name", name);
				fillStaticField(classWriter, internalName, "versionedName", versionedName);

				// instantiate plain StandardArtifactReflection instance which places it on
				// stack
				String implementationName = "com/braintribe/common/artifact/StandardArtifactReflection";
				mv.visitTypeInsn(NEW, implementationName);

				// duplicate instance on stack for field assignment after constructor
				mv.visitInsn(DUP);

				// push constructor arguments on stack: groupId, artifactId, version, archetype
				mv.visitLdcInsn(groupId);
				mv.visitLdcInsn(artifactId);
				mv.visitLdcInsn(version);
				if (archetype != null)
					mv.visitLdcInsn(archetype);
				else
					mv.visitInsn(ACONST_NULL);

				// invoke constructor of StandardArtifactReflection
				mv.visitMethodInsn(INVOKESPECIAL, implementationName, "<init>",
						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);

				// assign new StandardArtifactReflection instance of the static reflection field
				mv.visitFieldInsn(PUTSTATIC, internalName, reflectionFieldName, artifactReflectionDesc);

				// return from class initializer
				mv.visitInsn(RETURN);
				mv.visitMaxs(6, 0);

				return Maybe.complete(classWriter.toByteArray());
			} catch (Exception e) {
				return Reasons.build(InternalError.T).text("Error while compiling artifact reflection information to bytecode").cause(InternalError.from(e)).toMaybe();
			}
		}

		private void fillStaticField(ClassWriter classWriter, String internalName, String name, String value) {
			// add public static final field for the reflection instance
			String stringDesc = "Ljava/lang/String;";
			classWriter.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL, name, stringDesc, null, value);
		}

		private String buildCanonizedClassName(String groupId, String artifactId) {

			canonizedGroupdId = canonizedGroupdId(groupId);
			canonizedArtifactId = canonizedArtifactId(artifactId);
			className = canonizedGroupdId + "." + canonizedArtifactId;
			return className;
		}

		private Maybe<Void> writeArtifactReflection(byte[] classBytes) {
			
			File targetFile = UniversalPath.from(classesFolder) //
					.pushDottedPath(canonizedGroupdId) //
					.push(canonizedArtifactId + ".class") //
					.toFile();

			try {
				targetFile.getParentFile().mkdirs();
				try (OutputStream out = new FileOutputStream(targetFile)) {
					out.write(classBytes);
				}
			} catch (IOException e) {
				return Reasons.build(FilesystemError.T) //
						.text("Failed write class file:" + targetFile.getAbsolutePath()) //
						.cause(InternalError.from(e)) //
						.toMaybe();
			}

			return Maybe.complete(null);
		}

		private Maybe<Void> writeMetaInf() {
			
			File targetFile = UniversalPath.from(classesFolder) //
					.push("META-INF") //
					.push("artifact-descriptor.properties").toFile();

			try {
				targetFile.getParentFile().mkdirs();

				HashMap<String, String> properties = new LinkedHashMap<>();
				properties.put("groupId", artifact.getGroupId());
				properties.put("artifactId", artifact.getArtifactId());
				properties.put("version", artifact.getVersion().asString());
				
				String archetype = artifact.getProperties().get("archetype");
				
				if (archetype != null)
					properties.put("archetypes", archetype);
				
				properties.put("reflection-class", className);
				
				try (PrintStream ps = new PrintStream(new FileOutputStream(targetFile), false, "UTF-8")) {
					for (Map.Entry<String, String> entry: properties.entrySet()) {
						ps.print(entry.getKey());
						ps.print('=');
						ps.println(entry.getValue());
					}
				}
			} catch (IOException e) {
				return Reasons.build(FilesystemError.T) //
						.text("Failed write artifact-reflection file:" + targetFile.getAbsolutePath()) //
						.cause(InternalError.from(e)) //
						.toMaybe();
			}

			return Maybe.complete(null);
		}

		// Camel-casing of artifactId: this-artifact will be ThisArtifact
		// Furthermore, this will be "underscore-cased": _ThisArtifact_
		private String canonizedArtifactId(String name) {
			
			StringTokenizer tokenizer = new StringTokenizer(name, "-");

			StringBuilder builder = new StringBuilder();
			builder.append("_");

			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();

				if (token.isEmpty())
					continue;

				builder.append(Character.toUpperCase(token.charAt(0)));
				builder.append(token, 1, token.length());
			}
			builder.append("_");

			return builder.toString();
		}

		// path-version of groupId: "this.group-v2" will become "this.group_v2". 
		// Later, for the file-system also with pushDottedPath will produce "this/group_v2".
		private String canonizedGroupdId(String name) {
			
			return name.replace('-', '_');
		}
	}	

}