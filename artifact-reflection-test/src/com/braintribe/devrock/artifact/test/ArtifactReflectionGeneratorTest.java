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
package com.braintribe.devrock.artifact.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Properties;

import org.junit.Test;

import com.braintribe.common.artifact.ArtifactReflection;
import com.braintribe.devrock.artifact.ArtifactReflectionGenerator;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class ArtifactReflectionGeneratorTest {
	
	@Test
	public void testArtifactReflectionGeneratorWired() throws Exception {
		try (WireContext<ArtifactDataResolverContract> wireContext = Wire.context(ArtifactReflectionGeneratorTestWireModule.INSTANCE)) {
			
			ArtifactReflectionGenerator generator = new ArtifactReflectionGenerator();
			generator.setArtifactCompiler(wireContext.contract().declaredArtifactCompiler());
			
			File classesFolder = new File("test-output/build");
			
			
			Maybe<Void> maybe = generator.generate(new File("res"), classesFolder);
			
			if (maybe.isUnsatisfied()) {
				Assertions.fail(maybe.whyUnsatisfied().stringify());
			}
			
			testGeneratorOutput(classesFolder);
		}
		
	}
	
	@Test
	public void testArtifactReflectionGeneratorPlain() throws Exception {
		ArtifactReflectionGenerator generator = new ArtifactReflectionGenerator();
		File classesFolder = new File("test-output/build");
		Maybe<Void> maybe = generator.generate(new File("res"), classesFolder);
			
		if (maybe.isUnsatisfied()) {
			Assertions.fail(maybe.whyUnsatisfied().stringify());
		}

		testGeneratorOutput(classesFolder);
	}


	private void testGeneratorOutput(File classesFolder) throws Exception {
		
		URL[] classpath = new URL[] {classesFolder.getAbsoluteFile().toURI().toURL()};
		
		ClassLoader cl = new URLClassLoader(classpath, ArtifactReflectionGeneratorTest.class.getClassLoader());
		
		final Properties p;
		try (Reader reader = new InputStreamReader(new FileInputStream(new File(classesFolder, "META-INF/artifact-descriptor.properties")), "UTF-8")) {
			p = new Properties();
			p.load(reader);
		}
		
		String expectedReflectionClass = "foo.bar.sensation_core._FixFox_";
		
		String expectedGroupId = "foo.bar.sensation-core";
		String expectedArtifactId = "fix-fox";
		String expectedVersion = "1.0";
		String expectedName = expectedGroupId + ":" + expectedArtifactId;
		String expectedVersionedName = expectedName + "#" + expectedVersion;
		String expectedArchetype = "library";
		
		Assertions.assertThat(p.getProperty("groupId")).isEqualTo(expectedGroupId);
		Assertions.assertThat(p.getProperty("artifactId")).isEqualTo(expectedArtifactId);
		Assertions.assertThat(p.getProperty("version")).isEqualTo(expectedVersion);
		Assertions.assertThat(p.getProperty("archetypes")).isEqualTo(expectedArchetype);
		Assertions.assertThat(p.getProperty("reflection-class")).isEqualTo(expectedReflectionClass);

		Class<?> namespaceClass = Class.forName(expectedReflectionClass, true, cl);
		ArtifactReflection artifactReflection = (ArtifactReflection) namespaceClass.getField("reflection").get(null);
		
		Assertions.assertThat(artifactReflection.groupId()).isEqualTo(expectedGroupId);
		Assertions.assertThat(artifactReflection.artifactId()).isEqualTo(expectedArtifactId);
		Assertions.assertThat(artifactReflection.version()).isEqualTo(expectedVersion);
		Assertions.assertThat(artifactReflection.name()).isEqualTo(expectedName);
		Assertions.assertThat(artifactReflection.versionedName()).isEqualTo(expectedVersionedName);
		Assertions.assertThat(artifactReflection.archetypes()).isEqualTo(Collections.singleton(expectedArchetype));
		
		assertStaticField(namespaceClass, "groupId", expectedGroupId);
		assertStaticField(namespaceClass, "artifactId", expectedArtifactId);
		assertStaticField(namespaceClass, "version", expectedVersion);
		assertStaticField(namespaceClass, "name", expectedName);
		assertStaticField(namespaceClass, "versionedName", expectedVersionedName);
	}
	
	private void assertStaticField(Class<?> namespaceClass, String fieldName, String expectedValue) throws Exception {
		String value = (String)namespaceClass.getDeclaredField(fieldName).get(null);
		Assertions.assertThat(value).isEqualTo(expectedValue);
	}
	
}
