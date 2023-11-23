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
package com.braintribe.devrock.mc.core.wired.repository.simple;




import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactPartResolver;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;


@Category(KnownIssue.class)
public abstract class OfflineCompoundResolvingTest implements LauncherTrait, HasCommonFilesystemNode {
	private static Logger log = Logger.getLogger(OfflineCompoundResolvingTest.class);
	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots( getRoot());
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	
	private File preparedInitialRepository = new File( input, "initial");	
	private File settings = new File( input, "settings/basic-settings.xml");

	private static String grp = "com.braintribe.devrock.test";
	private static String art = "artifact";
		
	
	private List<CompiledArtifactIdentification> cais;
	{
		cais = new ArrayList<>();
		cais.add( CompiledArtifactIdentification.parse(grp + ":" + art + "#1.0"));
		cais.add( CompiledArtifactIdentification.parse(grp + ":" + art + "#2.0"));
		cais.add( CompiledArtifactIdentification.parse(grp + ":" + art + "#3.0"));
	}
	
	protected String getRoot() {
		return "wired/offline";
	}
	
	 
	public void before() {		
		TestUtils.ensure(output);
		TestUtils.copy(preparedInitialRepository, repo);			
	}
	
	@After
	public void after() {		
	}
	
	@Override
	public void log(String message) {	
		log.debug(message);
	}
	
		
	protected void resolvingTest(boolean withPartavailablitity) throws Exception {
		
		OverridingEnvironment ves = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ves.setEnv("repo", repo.getAbsolutePath());
		ves.setEnv( "port", "8080");
		ves.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());

		try (
				
				WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> ves)				
					.build();
		) {
			
			ArtifactDataResolverContract artifactDataResolverContract = resolverContext.contract();
			
			runDependencyResolverTest( artifactDataResolverContract);
			
			runArtifactResolverTest( artifactDataResolverContract, withPartavailablitity);
			
			runPartResolvingTest( artifactDataResolverContract, withPartavailablitity);
						
		}
		
		
	}

	private void runPartResolvingTest(ArtifactDataResolverContract artifactDataResolverContract, boolean partAvailablitity) {
		ArtifactPartResolver artifactPartResolver = artifactDataResolverContract.artifactResolver();
		
		// pom on 1.0
		PartIdentification pomPi = PartIdentification.of("pom");
		String pomPath = ArtifactAddressBuilder.build().root(repo.getAbsolutePath()).compiledArtifact(cais.get(0)).part(pomPi).toPath().toFilePath();		
		validateResolving(artifactPartResolver, cais.get(0), pomPi, partAvailablitity ? pomPath : null);
		
		// jar on 2.0
		PartIdentification jarPi = PartIdentification.of("jar");
		String jarPath = ArtifactAddressBuilder.build().root(repo.getAbsolutePath()).compiledArtifact(cais.get(1)).part( jarPi).toPath().toFilePath();		
		validateResolving(artifactPartResolver, cais.get(1), jarPi, partAvailablitity ? jarPath : null);
		

		// no javadoc-jar on 2.0		
		validateResolving(artifactPartResolver, cais.get(1), PartIdentification.create("javadoc", "jar"), null);
				
		
		// sources-jar on 3.0
		PartIdentification sourcesJarPi = PartIdentification.create("sources", "jar");
		String sourcesJarPath = ArtifactAddressBuilder.build().root(repo.getAbsolutePath()).compiledArtifact(cais.get(2)).part(sourcesJarPi).toPath().toFilePath();		
		validateResolving(artifactPartResolver, cais.get(2), sourcesJarPi, sourcesJarPath);
				
	}
	
	private void validateResolving(ArtifactPartResolver resolver, CompiledArtifactIdentification cai, PartIdentification pi, String path) {
		Maybe<ArtifactDataResolution> pomOptional = resolver.resolvePart(cai, pi);
		CompiledPartIdentification cpi = CompiledPartIdentification.from(cai, pi);
		if (path != null) {		
			Assert.assertTrue("expected to find [" + cpi.asString() + "], yet found nothing", pomOptional.isSatisfied());								
			
			Resource resource = pomOptional.get().getResource();
			if (resource instanceof FileResource) {
				FileResource fresource = (FileResource) resource;
				Assert.assertTrue("expected path is [" + path + "], yet found [" + fresource.getPath() + "]" , path.equalsIgnoreCase( fresource.getPath()));
			}
		}
		else {
			Assert.assertTrue("expected not to find [" + cpi.asString() + "], yet found it", !pomOptional.isSatisfied());
		}
		
	}

	private void runArtifactResolverTest(ArtifactDataResolverContract artifactDataResolverContract, boolean withPartavailablitity) {
		CompiledArtifactResolver compiledArtifactResolver = artifactDataResolverContract.redirectAwareCompiledArtifactResolver();
		
		Maybe<CompiledArtifact> resolvedCai1 = compiledArtifactResolver.resolve(cais.get(0));
		if (withPartavailablitity) {
			Assert.assertTrue("expected [" + cais.get(0).asString() + "], yet found nothing", resolvedCai1.isSatisfied());
			Assert.assertTrue("expected [" + cais.get(0).asString() + "], found [" + resolvedCai1.get().asString() +"]", resolvedCai1.get().compareTo( cais.get(0)) == 0);
		}
		else {
			Assert.assertTrue("should not find [" + cais.get(0).asString() + "], yet found something", !resolvedCai1.isSatisfied());			
		}
		
		Maybe<CompiledArtifact> resolvedCai2 = compiledArtifactResolver.resolve(cais.get(1));
		if (withPartavailablitity) {
			Assert.assertTrue("expected [" + cais.get(1).asString() + "], yet found nothing", resolvedCai2.isSatisfied());
			Assert.assertTrue("expected [" + cais.get(1).asString() + "], found [" + resolvedCai2.get().asString() +"]", resolvedCai2.get().compareTo( cais.get(1)) == 0);
		}
		else {
			Assert.assertTrue("should not find [" + cais.get(1).asString() + "], yet found something ", !resolvedCai2.isSatisfied());			
		}
		
		Maybe<CompiledArtifact> resolvedCai3 = compiledArtifactResolver.resolve(cais.get(2));
		Assert.assertTrue("expected [" + cais.get(2).asString() + "], yet found nothing", resolvedCai3.isSatisfied());
		Assert.assertTrue("expected [" + cais.get(2).asString() + "], found [" + resolvedCai3.get().asString() +"]", resolvedCai3.get().compareTo( cais.get(2)) == 0);
		
	}

	private void runDependencyResolverTest(ArtifactDataResolverContract artifactDataResolverContract) {
		DependencyResolver dependencyResolver = artifactDataResolverContract.dependencyResolver();
		
		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse( grp + ":" + art + "#[1.0,3.0]");		
		Maybe<CompiledArtifactIdentification> resolvedDependency = dependencyResolver.resolveDependency( cdi);
		Assert.assertTrue("expected [" + grp + ":" + art + "#3.0], found nothing", resolvedDependency.isSatisfied());
		
		CompiledArtifactIdentification cai = resolvedDependency.get();
		Assert.assertTrue("expected [" + grp + ":" + art + "#3.0], found [" + cai.asString() + "]", cai.asString().equals( cais.get(2).asString()));		
	}
	
}
