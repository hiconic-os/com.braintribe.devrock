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
package com.braintribe.test.multi.wire;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifacts.mc.wire.classwalk.ClasspathResolvers;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ConfigurableClasspathResolverExternalSpace;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.Scopes;
import com.braintribe.logging.Logger;
import com.braintribe.logging.LoggerInitializer;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract test class for the wired CP walker.. 
 *  
 * @author pit
 *
 */
public abstract class AbstractWalkerWireTest {
	private static Logger log = Logger.getLogger(AbstractWalkerWireTest.class);

	
	protected WireContext<ClasspathResolverContract> getClasspathWalkContext(File settings, File localRepo, Map<String,String> overrides) {
		return getClasspathWalkContext(settings, localRepo, ResolvingInstant.adhoc, overrides);
		
	}
	
	protected WireContext<ClasspathResolverContract> getClasspathWalkContext(File settings, File localRepo, ResolvingInstant resolvingInstant) {
		return getClasspathWalkContext(settings, localRepo, resolvingInstant, new HashMap<>());
	}
	/**
	 * generate a {@link WireContext} to allow retrieving walkers 	
	 * @param settings - the settings file to use 
	 * @param localRepo - the local repository to use 
	 * @param resolvingInstant - when to resolve clashes 
	 * @return - a {@link WireContext} of the {@link ClasspathResolverContract}
	 */
	protected WireContext<ClasspathResolverContract> getClasspathWalkContext(File settings, File localRepo, ResolvingInstant resolvingInstant, Map<String,String> overrides) {
		ConfigurableClasspathResolverExternalSpace cfg = new ConfigurableClasspathResolverExternalSpace();
		
		cfg.setScopes( Scopes.compileScopes());
		cfg.setSkipOptional(false);
		
		cfg.setResolvingInstant( ResolvingInstant.adhoc);
		
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		ove.setEnvironmentOverrides(overrides);
		
		cfg.setVirtualEnvironment(ove);
		
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
	
	protected void loggingInitialize(File file) {
		LoggerInitializer loggerInitializer = new LoggerInitializer();
		try {								
			if (file.exists()) {
				loggerInitializer.setLoggerConfigUrl( file.toURI().toURL());		
				loggerInitializer.afterPropertiesSet();
			}
		} catch (Exception e) {		
			String msg = "cannot initialize logging";
			log.error(msg, e);
		}
	
	}
	
	
}
