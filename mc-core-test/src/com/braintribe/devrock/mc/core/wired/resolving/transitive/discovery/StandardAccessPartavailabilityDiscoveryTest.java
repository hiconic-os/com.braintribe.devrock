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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;

/**
 * test for 'standard part availability access' - see {@link AbstractDiscoveryTest} for details 
 * 
 * @author pit
 *
 */
public class StandardAccessPartavailabilityDiscoveryTest extends AbstractDiscoveryTest {

	{
		launcher = Launcher.build()
				.repolet()
					.name("archive")
					.descriptiveContent()						
						.descriptiveContent(content)
					.close()					
				.close()
			.done();				
	}
	
	/**
	 * load the 'standard', i.e. interactive part availability data
	 * @param file
	 * @return
	 */
	@Override
	protected Pair<String, Map<EqProxy<PartIdentification>, PartAvailability>> loadPartAvailabilityFile(CompiledArtifact compiledArtifact) {		
		File file = ArtifactAddressBuilder.build().root( repository.getAbsolutePath()).compiledArtifact(compiledArtifact).partAvailability("archive").toPath().toFile();
		Map<EqProxy<PartIdentification>, PartAvailability> partMap = new HashMap<>();
		String releaseKey = null;
		
		try (BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream(file), "UTF-8"))) {			
			releaseKey = reader.readLine();
			
			reader.lines().forEach( l -> {		
				Pair<PartIdentification, PartAvailability> pair = digest( l);			
				if (pair != null) {
					partMap.put( HashComparators.partIdentification.eqProxy(pair.first), pair.second); 					
				}
			});		
		}
		catch (IOException e) {
			throw new UncheckedIOException( e);
		}
		return Pair.of( releaseKey, partMap);		
	}
	
	/**
	 * parse a line in the file 
	 * @param l
	 * @return
	 */
	private Pair<PartIdentification, PartAvailability> digest(String l) {
		l = l.trim();
		if (l.length() == 0)
			return null;
		char c = l.charAt(0);
		PartAvailability pa = PartAvailability.unknown;
		switch (c) {
			case '+' :
				pa = PartAvailability.available;
				break;
			case '-' :
				pa = PartAvailability.unavailable;
				break;
			default:
				throw new IllegalStateException("a term [" + l + "] is not a valid expression");				
		}		
		String piAsString = l.substring(1).trim();
		PartIdentification pi = PartIdentification.parse(piAsString);
		return Pair.of( pi, pa);
	}
	

	@Override
	protected void validateAdditionalOnlineAspects() {
		PartIdentification pi = PartIdentification.create("asset", "man");
		
		CompiledPartIdentification [] cpis = new CompiledPartIdentification [] {
				CompiledPartIdentification.from( CompiledArtifactIdentification.parse( COM_BRAINTRIBE_DEVROCK_TEST + ":a#1.0.1"), pi),
				CompiledPartIdentification.from( CompiledArtifactIdentification.parse( COM_BRAINTRIBE_DEVROCK_TEST + ":b#1.0.1"), pi),
				CompiledPartIdentification.from( CompiledArtifactIdentification.parse( COM_BRAINTRIBE_DEVROCK_TEST + ":c#1.0.1"), pi),
		};
		validateDownloads("archive", cpis);
	}
	
	@Override
	protected void prepareOfflineTest() {			
		File offlineContentsForRest = new File( offlineInitial, "standard");
		TestUtils.copy( offlineContentsForRest, repository);
	}
	@Override
	protected void validateAdditionalOfflineAspects() {			
	}
	
	@Test 
	public void runOnline() {
		super.runOnline();
	}

	@Test 
	public void runOffline() {
		super.runOffline();
	}
	
}
