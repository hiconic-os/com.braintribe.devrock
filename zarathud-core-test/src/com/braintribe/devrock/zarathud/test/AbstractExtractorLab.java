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
package com.braintribe.devrock.zarathud.test;

import java.io.File;

import com.braintribe.build.artifacts.mc.wire.classwalk.ClasspathResolvers;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ConfigurableClasspathResolverExternalSpace;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.Scopes;
import com.braintribe.devrock.zarathud.test.utils.FakeLocalRepositoryProvider;
import com.braintribe.devrock.zarathud.test.utils.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.zarathud.data.Artifact;
import com.braintribe.wire.api.context.WireContext;

public class AbstractExtractorLab {
	/**
	 * generate a {@link WireContext} to allow retrieving walkers 	
	 * @param settings - the settings file to use 
	 * @param localRepo - the local repository to use 
	 * @param resolvingInstant - when to resolve clashes 
	 * @return - a {@link WireContext} of the {@link ClasspathResolverContract}
	 */
	protected WireContext<ClasspathResolverContract> getClasspathWalkContext(File settings, File localRepo, ResolvingInstant resolvingInstant) {
		ConfigurableClasspathResolverExternalSpace cfg = new ConfigurableClasspathResolverExternalSpace();
		
		cfg.setScopes( Scopes.compileScopes());
		cfg.setSkipOptional(false);
		
		cfg.setResolvingInstant( resolvingInstant);
		
		if (settings != null) {		
			FakeMavenSettingsPersistenceExpertImpl persistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
			cfg.setOverrideSettingsPersistenceExpert(persistenceExpert);
		}
		
		if (localRepo != null) {
			FakeLocalRepositoryProvider localRepositoryProvider = new FakeLocalRepositoryProvider(localRepo);
			cfg.setOverrideLocalRepositoryExpert(localRepositoryProvider);
		}
		
		
		WireContext<ClasspathResolverContract> context = ClasspathResolvers.classpathResolverContext( b -> {  
			b.bindContract(ClasspathResolverExternalContract.class, cfg);	
		});
		
		return context;		
	}
	
	
	protected Artifact toArtifact( String condensedName) {
		String [] values = condensedName.split( ":#");
		if (values.length < 3) {
			throw new IllegalArgumentException("passed value [" + condensedName + "] is not a valid solution name");
		}		
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( values[0]);
		artifact.setArtifactId( values[1]);
		artifact.setVersion( values[2]);
		
		return artifact;
	}
	
	protected Artifact toArtifact( Solution solution) {
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( solution.getGroupId());
		artifact.setArtifactId( solution.getArtifactId());
		artifact.setVersion( VersionProcessor.toString( solution.getVersion()));		
		return artifact;
	}
}
