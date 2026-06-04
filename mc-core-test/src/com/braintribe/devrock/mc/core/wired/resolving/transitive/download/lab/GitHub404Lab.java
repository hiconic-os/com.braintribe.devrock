package com.braintribe.devrock.mc.core.wired.resolving.transitive.download.lab;

import java.io.File;

import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocator;
import com.braintribe.devrock.mc.core.commons.ArtifactResolutionUtil;
import com.braintribe.devrock.mc.core.configuration.ConfigurableRepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLocators;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.utils.FileTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class GitHub404Lab {
	

	public static void main(String[] args) {
		FileTools.deleteDirectoryRecursivelyUnchecked(new File("res/output/lab-cache"));
		RepositoryConfigurationLocator locater = RepositoryConfigurationLocators.build().addLocation(new File("res/repository-configuration.yaml")).done();
		
		ConfigurableRepositoryConfigurationLoader configLoader = new ConfigurableRepositoryConfigurationLoader();
		configLoader.setLocator(locater);
		
		try (WireContext<ClasspathResolverContract> wireContext = Wire.contextBuilder(ClasspathResolverWireModule.INSTANCE).bindContract(RepositoryConfigurationContract.class, () -> configLoader.get()).build()) {
			ClasspathResolverContract contract = wireContext.contract();
			
			ClasspathDependencyResolver resolver = contract.classpathResolver();
			ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build().enrichJar(true).done();
			
			CompiledTerminal terminal = CompiledTerminal.parse("com.braintribe.gm:root-model#[2,3)");
			
			AnalysisArtifactResolution resolution = resolver.resolve(resolutionContext, terminal);
			
			ArtifactResolutionUtil.printDependencyTree(resolution);
		}
	}
}
