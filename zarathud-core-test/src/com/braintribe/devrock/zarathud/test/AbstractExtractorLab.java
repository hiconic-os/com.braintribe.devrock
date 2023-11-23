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
