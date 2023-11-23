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
package com.braintribe.devrock.mc.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.PrintStreamConsole;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.core.commons.McReasonOutput;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.DevelopmentEnvironmentContract;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.model.mc.reason.IncompleteArtifactResolution;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependencyVersion;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.version.Version;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class ClassifierLab2 {
	public static void main(String[] args) {
		ConsoleConfiguration.install(new PrintStreamConsole(System.out));
		//testCpr();
		
		IncompleteArtifactResolution reason = Reasons.build(IncompleteArtifactResolution.T).text("foobar").toReason();
		
		Version v = Version.create(1, 0);
		UnresolvedDependencyVersion uV = TemplateReasons.build(UnresolvedDependencyVersion.T).enrich(r -> r.setVersion(v)).toReason();
		
		new McReasonOutput().println(uV);
		System.out.println(uV.getText());
		
	}

	private static void testCpr() {
		try (WireContext<ClasspathResolverContract> wireContext = cprWireContext()) {
			CompiledArtifact terminal = wireContext.contract().transitiveResolverContract().dataResolverContract().declaredArtifactCompiler().compileReasoned(new File("pom.xml")).get();
			CompiledDependencyIdentification terminal2 = CompiledDependencyIdentification.create("com.braintribe.common", "common-api", "[2.0,2.1)");
			List<CompiledTerminal> terminals = new ArrayList<>(Arrays.asList(terminal, terminal2));
			
			ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build().scope(ClasspathResolutionScope.compile).lenient(true).done();
			AnalysisArtifactResolution resolution = wireContext.contract().classpathResolver().resolve(resolutionContext, terminals);
			
			if (resolution.hasFailed()) {
				new McReasonOutput().println(resolution.getFailure());
				return;
			}
		}
	}

	private static WireContext<ClasspathResolverContract> cprWireContext() {
		File devRoot = detectDevEnvFolder(new File(".").getAbsoluteFile());
		return Wire.contextBuilder(ClasspathResolverWireModule.INSTANCE, EnvironmentSensitiveConfigurationWireModule.INSTANCE)
				.bindContract(DevelopmentEnvironmentContract.class, () -> devRoot)
				.build();
	}
	
	private static WireContext<ArtifactDataResolverContract> wireContext() {
		File devRoot = detectDevEnvFolder(new File(".").getAbsoluteFile());
		return Wire.contextBuilder(ArtifactDataResolverModule.INSTANCE, EnvironmentSensitiveConfigurationWireModule.INSTANCE)
				.bindContract(DevelopmentEnvironmentContract.class, () -> devRoot)
				.build();
	}
	
	private static File detectDevEnvFolder(File dir) {
		boolean isDevEnvRoot = isDevEnvRootFolder(dir);
		
		if (isDevEnvRoot)
			return dir;
		
		File parent = dir.getParentFile();
		
		if (parent == null)
			return null;
		
		return detectDevEnvFolder(parent);
	}
	
	private static boolean isDevEnvRootFolder(File dir) {
		File markerFile = new File(dir, "dev-environment.yaml");
		return markerFile.exists();
	}



}
