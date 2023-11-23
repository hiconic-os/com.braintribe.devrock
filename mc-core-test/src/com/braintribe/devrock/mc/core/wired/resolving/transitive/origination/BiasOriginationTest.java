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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.origination;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.compiler.configuration.origination.ReasoningHelper;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryBiasAdded;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryBiasLoaded;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Reason;

/**
 * tests how the originations work if a bias file is active. 
 * Expected is to have one reason (a {@link RepositoryBiasLoaded} instance pointing to the file) and one reason (a {@link RepositoryBiasAdded}
 * per repository affected. 
 * 
 * @author pit
 *
 */
public class BiasOriginationTest extends AbstractTransitiveRepositoryConfigurationTest  {

	@Override
	protected Map<String, RepoletContent> archives() {
		Map<String, RepoletContent> map = new HashMap<>();
		map.put( "archiveA", archiveInput("archiveA.definition.yaml"));
		map.put( "archiveB", archiveInput("archiveB.definition.yaml"));
		return map;
	}

	@Override
	protected File biasFileInput() {	
		return new File( input, "standard.bias.txt");
	}

	@Override
	protected File settings() {	
		return new File( input, "settings.xml");
	}
	
	@Test
	public void testBiasOrigination() {
		RepositoryConfiguration rcfg = compileConfiguration();		

		// validate origination 
		Reason reason = rcfg.getOrigination();
		
		// find bias origination
		Predicate<Reason> filter = new Predicate<Reason>() {
			@Override
			public boolean test(Reason r) {
				if (r instanceof RepositoryBiasLoaded || r instanceof RepositoryBiasAdded)
					return true;
				return false;
			}
			
		};
		List<Reason> biasReasons = ReasoningHelper.extractAllReasons(reason, filter);
		
		boolean foundLoaded=false, foundA=false, foundB=false;
		
		for (Reason biasReason : biasReasons) {
			if (biasReason instanceof RepositoryBiasLoaded) {
				foundLoaded = true;
				// validate 
				RepositoryBiasLoaded r = (RepositoryBiasLoaded) biasReason;
				String fileName = r.getBiasFilename().replace('\\', '/');
				String testFile = new File( repo, ".pc_bias").getAbsolutePath().replace('\\', '/');
				Assert.assertTrue("expected bias file to be [" + testFile + "], yet found [" + fileName + "]" , testFile.equals(fileName));				
			}
			else if (biasReason instanceof RepositoryBiasAdded) {
				RepositoryBiasAdded r = (RepositoryBiasAdded) biasReason;
				String id = r.getRepositoryId();
				if (id.equals( "archiveA")) { 
					foundA = true;
				}
				
				if (id.equals( "archiveB")) {
					foundB = true;
				}
			}
		}
		
		Assert.assertTrue("expected a RepositoryBiasLoaded reason, yet found none", foundLoaded);
		Assert.assertTrue("expected a RepositoryBiasAdded reason for 'archiveA', yet found none", foundA);
		Assert.assertTrue("expected a RepositoryBiasAdded reason for 'archiveB', yet found none", foundB);
								
	}
	

}
