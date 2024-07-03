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
package com.braintribe.artifacts.quickimport;

import java.io.File;
import java.util.List;

import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import com.braintribe.build.quickscan.standard.StandardQuickImportScanner;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.dom.genericmodel.GenericModelRootDomCodec;
import com.braintribe.logging.Logger;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;
@Category(KnownIssue.class)
public class QuickImporterLab {
	private static Logger log = Logger.getLogger(QuickImporterLab.class);
	 	
	private void quickImportLab(SourceRepository sourceRepository, String localSvn) {
		
		StandardQuickImportScanner scanner = new StandardQuickImportScanner();
		scanner.setSourceRepository(sourceRepository);
		long before = System.nanoTime();			
		List<SourceArtifact> sourceArtifacts = scanner.scanLocalWorkingCopy(localSvn);
		long after = System.nanoTime();
		System.out.println("Scanning took [" + ((after - before)/1E6) + "] ms for [" + sourceArtifacts.size() + "] projects");
		
		try {
			Codec<List<SourceArtifact>, Document> codec = new GenericModelRootDomCodec<List<SourceArtifact>>();
			Document document = codec.encode(sourceArtifacts);
			DomParser.write().from( document).to(new File("sourceartifacts.xml"));
		} catch (CodecException e) {
			String msg = "cannot convert source artifacts to document";
			log.error( msg, e);
		} catch (DomParserException e) {
			String msg="cannot save source artifact xml file";
			log.error( msg, e);
		}
		
	}
	
	private SourceRepository generateDefaultSourceRepository() {
		SourceRepository sourceRepository = SourceRepository.T.create();
		sourceRepository.setName( "Local SVN working copy");
		sourceRepository.setRepoUrl("file:/" + System.getenv( "BT__ARTIFACTS_HOME"));	
		return sourceRepository;
	}
	
	//@Test
	public void mainLab() {
		String localSvn = System.getenv( "BT__ARTIFACTS_HOME");
		quickImportLab( generateDefaultSourceRepository(), localSvn);
	}
	
	//@Test
	public void shortLab() {
		String localSvn = System.getenv( "BT__ARTIFACTS_HOME") + "/com/braintribe/csp/client/custom/WebArchiveWorkflowClient";
		quickImportLab(generateDefaultSourceRepository(), localSvn);
	}
	
	//@Test
	public void pmpLab() {
		String localSvn = System.getenv( "BT__ARTIFACTS_HOME") + "/com/braintribe/model/ProcessDefinitionModel";
		quickImportLab( generateDefaultSourceRepository(), localSvn);
	}
	
	
}
