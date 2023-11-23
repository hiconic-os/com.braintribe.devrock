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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.lab;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.testing.category.KnownIssue;

/**
 * simple lab, just to create a repolet to be accessed via the associated repository-configuration.
 * Basically used in tests of devrock's Eclipse offering - artifact selector (where an arbitrary repo-cfg can be injected) 
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class AlternativeRepoCfgForEclipseLab implements HasCommonFilesystemNode{	
	
	protected File repo;
	protected File input;
	protected File output;
	
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/update");
		input = pair.first;
		output = pair.second;			
		repo = new File( output, "repo");		
	}
		
	protected TransitiveResolutionContext standardResolutionContext = TransitiveResolutionContext.build().done();
	
	protected File initial = new File( input, "initial");	

	protected File settings = new File( input, "settings.xml");
	protected File repositoryConfiguration = new File( input, "repository-configuration.yaml");
	
	protected Map<String, List<String>> downloads;

	protected Launcher launcher;
	{
		launcher = Launcher.build()
				.repolet()
						.name("archive")
						.descriptiveContent()
							.descriptiveContent(archiveInput())
						.close()						
					.close()											
			.done();
	}
	

	protected void additionalTasks() {}
	
	protected RepoletContent archiveInput() { return archiveInput("archive.definition.yaml");}
	

	@Before
	public void runBefore() {
		
		downloads = new HashMap<>();		
		TestUtils.ensure(repo); 	
		launcher.launch();				
		additionalTasks();
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	

	protected RepoletContent archiveInput(String definition) {
		File file = new File( input, definition);
		try {
			return RepositoryGenerations.unmarshallConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
	

	@Test
	public void test() {
		System.out.println("to add break-point here");
	}
}
