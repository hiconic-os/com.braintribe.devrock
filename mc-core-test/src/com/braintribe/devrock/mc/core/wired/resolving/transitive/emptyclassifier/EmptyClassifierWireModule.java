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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.emptyclassifier;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

public enum EmptyClassifierWireModule implements WireTerminalModule<ClasspathResolverContract> {
	
	INSTANCE;
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		RepositoryConfiguration config = RepositoryConfiguration.T.create();
		
		File localRepo = new File("res/output/wired/transitive/emptyclassifier/repo");
		
		TestUtils.ensure(localRepo);
		
		File rootPath = new File("res/input/wired/transitive/emptyclassifier/repo");
		
		MavenFileSystemRepository repo = MavenFileSystemRepository.T.create();
		repo.setRootPath(rootPath.getAbsolutePath());
		repo.setName("test");
		
		config.setLocalRepositoryPath(localRepo.getAbsolutePath());
		config.getRepositories().add(repo);
		
		contextBuilder.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(config));
	}
	
	@Override
	public List<WireModule> dependencies() {
		return Collections.singletonList(ClasspathResolverWireModule.INSTANCE);
	}

}
