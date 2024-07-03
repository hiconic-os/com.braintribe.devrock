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
package com.braintribe.test.multi.wire.walk.issues;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.model.artifact.Solution;
import com.braintribe.test.multi.wire.AbstractRepoletWalkerWireTest;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;
import com.braintribe.wire.api.context.WireContext;

public class IndexDeclarationLab extends AbstractRepoletWalkerWireTest {
	
private static final String VRS = "1.0";
private static final String ART = "terminal";
private static final String GRP_SUCCESS = "com.braintribe.devrock.test.optionals";
private static final String GRP_FAIL = "tribefire.cortex";
private Map<String, RepoType> launcherMap;
	
	{
		launcherMap = new HashMap<>();		
		launcherMap.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
						
	}
	
	@Override
	protected File getRoot() {	
		return new File("res/wire/issues/index-declarations");
	}
		
	@Before
	public void before() {
		runBefore(launcherMap);
	}
	
	@After
	public void after() {
		runAfter();
	}
	
	

	
	private void test(String condensedTerminalName, boolean found) {
		try {
			WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( new File( testSetup, "settings.xml"), repo, overridesMap);			
			
			WalkerContext walkerContext = new WalkerContext();
			
			
			Walker walker = classpathWalkContext.contract().walker( walkerContext);
			
			String walkScopeId = UUID.randomUUID().toString();
			
			Solution terminal = NameParser.parseCondensedSolutionName(condensedTerminalName);
			
			Collection<Solution> collection = walker.walk( walkScopeId, terminal);
		
			collection.stream().forEach( s -> System.out.println(NameParser.buildName(s)));
			validate( terminal, found);
		} catch (Exception e) {

			e.printStackTrace();
		} 
	}
	
	@Test
	public void successTest() {		
		test( GRP_SUCCESS + ":" + ART + "#" + VRS, true);
	}
	
	@Test
	public void failTest() {		
		test( GRP_FAIL + ":" + ART + "#" + VRS, false);
	}
	
	private String collate( List<String> str) {
		return str.stream().collect(Collectors.joining( ","));
	}
	
	private void validate( Solution terminal, boolean expected) {
		String buildPartialPath = RepositoryReflectionHelper.getArtifactFilesystemLocation(repo.getAbsolutePath(), terminal);
		File mavenMetaDataFile = new File( buildPartialPath, "maven-metadata-braintribe.Base.xml");
		if (!mavenMetaDataFile.exists()) {
			Assert.fail("expected file [" + mavenMetaDataFile.getAbsolutePath() + "] doesn't exist");
		}
		// 
		try {
			Document document = DomParser.load().from( mavenMetaDataFile);
			Element documentElement = document.getDocumentElement();
			NodeList childNodes = documentElement.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				if (node.getNodeType() == Node.COMMENT_NODE) {
					String comment = node.getTextContent();
					if (expected) {
						if (!comment.startsWith( " retrieved")) {
							Assert.fail("expected retrieval message, but found [" + comment + "]");
						}
					}
					else {
						if (!comment.startsWith( " injected")) {
							Assert.fail("expected injection message, but found [" + comment + "]");
						}
					}
					
				}
			}
		} catch (DOMException e) {
			Assert.fail("cannot find comment in [" + mavenMetaDataFile.getAbsolutePath() + "]");
		} catch (DomParserException e) {
			Assert.fail("cannot load [" + mavenMetaDataFile.getAbsolutePath() + "]");
		}
		
	}

}
