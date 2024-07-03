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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.discovery;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.model.artifactory.FileItem;
import com.braintribe.devrock.model.artifactory.FolderInfo;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;

/**
 * test for 'rest-based part availability access' - see {@link AbstractDiscoveryTest} for details 
 * 
 * @author pit
 *
 */
public class RestAccessPartavailabilityDiscoveryTest extends AbstractDiscoveryTest {
	
	private static JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
	private static GmDeserializationOptions options = GmDeserializationOptions.defaultOptions.derive().setInferredRootType( FolderInfo.T).build();

	{
		launcher = Launcher.build()
				.repolet()
					.name("archive")
					.descriptiveContent()						
						.descriptiveContent(content)
					.close()		
					.restApiUrl("http://localhost:${port}/api/storage/archive")
					.serverIdentification("Artifactory/faked.by.repolet")
				.close()
			.done();				
	}

	
	
	

	/**
	 * load the 'standard', i.e. fully reflecting part availability data, REST style.. key is "" (no UUID)
	 * @param file
	 * @return
	 */
	@Override
	protected Pair<String, Map<EqProxy<PartIdentification>, PartAvailability>> loadPartAvailabilityFile(CompiledArtifact compiledArtifact) {		
		
		File file = ArtifactAddressBuilder.build().root( repository.getAbsolutePath()).compiledArtifact(compiledArtifact).partAvailability("archive", "artifactory.json").toPath().toFile();
		
		Map<EqProxy<PartIdentification>, PartAvailability> partMap = new HashMap<>();
		// use compiled artifact to get an idea of the file names (remainder is classifier:type)
		String prefix = compiledArtifact.getArtifactId() + "-" + compiledArtifact.getVersion().asString();
		
		try (InputStream in = new FileInputStream( file)) {
			FolderInfo fi = (FolderInfo) marshaller.unmarshall(in, options);
			for (FileItem item : fi.getChildren()) {
				String suffix = item.getUri().substring( prefix.length()+1);
				int pt = suffix.indexOf( '.');
				PartIdentification pi = null;
				if (pt >= 0) {
					String classifier = suffix.substring(0, pt);
					String type = suffix.substring( pt+1);				
					pi = PartIdentification.create(classifier, type);
				}
				else {
					String type = suffix.substring( pt+1);				
					pi = PartIdentification.create(type);
				}
				
				partMap.put(HashComparators.partIdentification.eqProxy(pi), PartAvailability.available);
			}
		}
		catch (Exception e) {
			throw Exceptions.unchecked( e, "error while unmarshalling [" + file.getAbsolutePath() + "]");
		}		
		
		return Pair.of( "", partMap);		
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
		File offlineContentsForRest = new File( offlineInitial, "rest");
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
