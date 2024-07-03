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
