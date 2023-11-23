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
