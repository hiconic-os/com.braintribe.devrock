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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.cache;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.utils.paths.UniversalPath;

/**
 * t has a dependency a
 * a exists in 3 forms
 * 		remote - highest version 
 * 		local repo / cache - medium version
 * 		install - lowest version
 * 
 * @author pit
 *
 */
public class RepositoryInstallLogicTest extends AbstractRepositoryCacheTest  {	
	
	@Override
	protected File config() {	
		return new File( input, "repository-configuration.yaml");
	}
	
	YamlMarshaller marshaller = new YamlMarshaller();
	
	@Test 
	public void runUpdateableTest() {
	
		
		RepositoryConfiguration repositoryConfiguration = null;
		try {
			RepositoryReflection reflection = getReflection();
			repositoryConfiguration = reflection.getRepositoryConfiguration();
			
			/*
			try (FileOutputStream out = new FileOutputStream( new File( output, "repo-dump.yaml"))) {
				marshaller.marshall(out, repositoryConfiguration);
			}
			*/
			System.out.println();
			 
			
		} catch (Exception e1) {
			e1.printStackTrace();
			Assert.fail("Resolution failed with an exception");
		}
		
		
		// run cp resolution on an artifact t
		AnalysisArtifactResolution resolution = null;
		try {
			resolution = run("com.braintribe.devrock.test:t#1.0.1", standardClasspathResolutionContext);			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Resolution failed with an exception");
		}
		System.out.println(resolution);
		
		// check result 
		Validator validator = new Validator();
		File validationFile = new File( input, "archive.validation.yaml");
		validator.validate( validationFile, resolution);
		
		// check footprint
		//	- local repo : no caching
		// check if there's something of a#1.0.1 
		String cachePath = repositoryConfiguration.getCachePath();
		validator.assertTrue("no cache path defined", cachePath != null);
		
		if (cachePath != null) {
			File cacheDir = new File( cachePath);
			File artifactDir = UniversalPath.from(cacheDir).pushDottedPath( "com.braintribe.devrock.test").push("a").push( "1.0.1").toFile();
			validator.assertTrue("Unexpectedly, test artifact does exist in local repo", !artifactDir.exists());
		}
		
	
		
		validator.assertResults();
		
		
	}
	

}
