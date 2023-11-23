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
package com.braintribe.devrock.mc.core.compiler.pom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.core.commons.ManagedFilesystemLockSupplier;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.compiled.ArtifactCompiler;
import com.braintribe.devrock.mc.core.compiler.AbstractCompilerTest;
import com.braintribe.devrock.mc.core.compiler.CompiledArtifactValidator;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactResolver;
import com.braintribe.devrock.mc.core.filters.AllMatchingArtifactFilterExpert;
import com.braintribe.devrock.mc.core.repository.local.BasicArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.core.resolver.BasicDependencyResolver;
import com.braintribe.devrock.mc.core.resolver.LocalRepositoryCachingArtifactResolver;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

@Category(KnownIssue.class)
public class PomCompilerTest extends AbstractCompilerTest {
	
	@Override
	protected String getRoot() {		
		return "compiler/maven.pom";
	}
	
	@Before
	public void before() {
		runBefore();
		TestUtils.copy( preparedInitialRepository, repo); 
	}
	
	private CompiledArtifactResolver setup() {
		LocalRepositoryCachingArtifactResolver artifactResolver = new LocalRepositoryCachingArtifactResolver();		
		artifactResolver.setLocalRepository( repo);
		artifactResolver.setLockProvider( new ManagedFilesystemLockSupplier());
		BasicArtifactPartResolverPersistenceDelegate localDelegate = BasicArtifactPartResolverPersistenceDelegate.createLocal();
		localDelegate.setArtifactFilter( AllMatchingArtifactFilterExpert.instance);
		artifactResolver.setDelegates(Collections.singletonList( localDelegate));
		artifactResolver.postConstruct();
		
		DeclaredArtifactMarshaller marshaller = new DeclaredArtifactMarshaller();
		
		DeclaredArtifactResolver declaredArtifactResolver = new DeclaredArtifactResolver();
		declaredArtifactResolver.setMarshaller(marshaller);
		declaredArtifactResolver.setPartResolver(artifactResolver);
		
		BasicDependencyResolver dependencyResolver = new BasicDependencyResolver(artifactResolver);
		
		ArtifactCompiler artifactCompiler = new ArtifactCompiler();
		artifactCompiler.setCompiledArtifactResolver(artifactCompiler);
		artifactCompiler.setDeclaredArtifactResolver(declaredArtifactResolver);
		artifactCompiler.setDependencyResolver(dependencyResolver);
		
		return artifactCompiler;
	}
	
	private CompiledDependency createCompiledDependency( String grp, String art, String vrs) {
		CompiledDependency cd = CompiledDependency.T.create();
		cd.setGroupId(grp);
		cd.setArtifactId(art);
		cd.setVersion( VersionExpression.parse( vrs));
		return cd;		
	}
	
	private CompiledDependency createCompiledDependency( String grp, String art, String vrs, String cls, String type) {
		CompiledDependency cd = createCompiledDependency(grp, art, vrs);
		cd.setClassifier(cls);
		cd.setType(type);
		return cd;
	}
	
	private Maybe<CompiledArtifact> testReasoned( CompiledArtifactIdentification cai, CompiledArtifactValidator validator) {
		CompiledArtifactResolver compiledArtifactResolver = setup();
		Maybe<CompiledArtifact> compiledArtifact = compiledArtifactResolver.resolve(cai);
		return compiledArtifact;
	}
	
	private CompiledArtifact test( CompiledArtifactIdentification cai, CompiledArtifactValidator validator) {
		Maybe<CompiledArtifact> compiledArtifact = testReasoned(cai, validator);
		if (compiledArtifact.isSatisfied()) {
			if (validator != null) {
				validator.validate(compiledArtifact.get());
			}
			return compiledArtifact.get();
		}
		else {
			return null;
		}
		
	}

	@Test
	public void testImportLevel() {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse( "com.braintribe.devrock.test:import#1.0");
		test(cai, new CompiledArtifactValidator() {
			
			@Override
			public void validate(CompiledArtifact ca) {
				
				validate( ca, cai);
				
				
				Map<String,String> expectedProperties = new HashMap<>();				
				expectedProperties.put("major", "1");
				expectedProperties.put("minor", "0");
				expectedProperties.put("nextMinor", "1");				
				expectedProperties.put("V.com.braintribe.devrock.test.c", "1.0");
				validateProperties(ca, expectedProperties);
			}
		});	
		
	}
	@Test
	public void testParentLevel() {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse( "com.braintribe.devrock.test:parent#1.0");
		test(cai, new CompiledArtifactValidator() {
			
			@Override
			public void validate(CompiledArtifact ca) {
				validate( ca, cai);
				
				Map<String,String> expectedProperties = new HashMap<>();				
				expectedProperties.put("major", "1");
				expectedProperties.put("minor", "0");
				expectedProperties.put("nextMinor", "1");  
				expectedProperties.put("V.default.range", "[1.0,1.1)");
				expectedProperties.put("V.com.braintribe.devrock.test", "[1.0,1.1)");
				expectedProperties.put("V.com.braintribe.devrock.test.b", "[1.0,1.1)");				
				expectedProperties.put("overwrite", "false");
				
				validateProperties( ca, expectedProperties);
				
				List<CompiledDependency> expectedDependencies = new ArrayList<>();				
				expectedDependencies.add( createCompiledDependency("com.braintribe.devrock.test", "x", "[1.0,1.1)", null, "jar"));
				
				validateDependencies(ca, expectedDependencies);
			}
		});	
		
	}
	
	@Test
	public void testTopLevel() {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse( "com.braintribe.devrock.test:artifact#1.0");
		test(cai, new CompiledArtifactValidator() {
			
			@Override
			public void validate(CompiledArtifact ca) {
				validate( ca, cai);
				
				Map<String,String> expectedProperties = new HashMap<>();				
				expectedProperties.put("major", "1");
				expectedProperties.put("minor", "0");
				expectedProperties.put("nextMinor", "1");  
				expectedProperties.put("V.default.range", "[1.0,1.1)");
				expectedProperties.put("V.com.braintribe.devrock.test", "[1.0,1.1)");
				expectedProperties.put("V.com.braintribe.devrock.test.b", "[1.0,1.1)");				
				expectedProperties.put("overwrite", "true");
				validateProperties( ca, expectedProperties);
				
				List<CompiledDependency> expectedDependencies = new ArrayList<>();				
				expectedDependencies.add( createCompiledDependency("com.braintribe.devrock.test", "a", "[1.0,1.1)", null, "jar"));
				expectedDependencies.add( createCompiledDependency("com.braintribe.devrock.test", "b", "[1.0,1.1)", null, "jar"));
				expectedDependencies.add( createCompiledDependency("com.braintribe.devrock.test", "c", "1.0", "classifier", "jar"));
				expectedDependencies.add( createCompiledDependency("com.braintribe.devrock.test", "x", "[1.0,1.1)", null, "jar"));
				
				validateDependencies(ca, expectedDependencies);
			}
		});	
		
	}
	@Test
	public void testBrokenProperties() {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse( "com.braintribe.devrock.test:broken-properties-artifact#1.0");
		test(cai, artifact -> {
			if (artifact.findProperty("foo").isSatisfied()) {
				Assertions.fail("property foo should be unavailable");
			}
			
			if (artifact.findProperty("fix").isUnsatisfied()) {
				Assertions.fail("property fix should be available");
			}
			
			if (!artifact.getDependencies().get(0).getInvalid()) {
				Assertions.fail("dependency should be invalid because of unresolved property");
			}
		});
	}
	
	@Test
	public void testYamlExpressiveProperties() {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse( "com.braintribe.devrock.test:artifact#1.1");
		test(cai, new CompiledArtifactValidator() {
			

			@Override
			public void validate(CompiledArtifact ca) {
				validate( ca, cai);
				
				Map<String,String> expectedProperties = new HashMap<>();				
				expectedProperties.put("major", "1");
				expectedProperties.put("minor", "1");
				expectedProperties.put("nextMinor", "2");  
				expectedProperties.put("V.default.range", "[1.0,1.1)");
				expectedProperties.put("V.com.braintribe.devrock.test", "[1.0,1.1)");
				expectedProperties.put("V.com.braintribe.devrock.test.b", "[1.0,1.1)");				
				expectedProperties.put("overwrite", "true");
				validateProperties( ca, expectedProperties);
				
				
				Map<String, String> expectedRedirects = new HashMap<>();
				expectedRedirects.put( "com.braintribe.model:gm-core-api#[1.0,1.1)", "tribefire.cortex.gwt:gm-core-api#[1.0,1.1)");
				expectedRedirects.put( "com.braintribe.model:gm-core-api#[1.0,1.1)", "tribefire.cortex.gwt:gm-core-api#[1.0,1.1)");
				validateArtifactRedirects(ca, expectedRedirects);
												
				List<String> expectedDominants = Arrays.asList( "com.braintribe.model:platform-api#2.0", "com.braintribe.model:common-api#2.0");
				validateDominants( ca, expectedDominants);
				
				List<String> expectedExclusions = Arrays.asList( "com.braintribe.model:platform-api","com.braintribe.model:*");
				validateExclusions( ca, expectedExclusions);
				
				
				List<CompiledDependency> expectedDependencies = new ArrayList<>();				
				expectedDependencies.add( createCompiledDependency("com.braintribe.devrock.test", "a", "[1.0,1.1)", null, "jar"));
				expectedDependencies.add( createCompiledDependency("com.braintribe.devrock.test", "b", "[1.0,1.1)", null, "jar"));
				expectedDependencies.add( createCompiledDependency("com.braintribe.devrock.test", "c", "1.0", "classifier", "jar"));
				expectedDependencies.add( createCompiledDependency("com.braintribe.devrock.test", "x", "[1.0,1.1)", null, "jar"));
				
				validateDependencies(ca, expectedDependencies);
			}
		});	
		
	}
}
