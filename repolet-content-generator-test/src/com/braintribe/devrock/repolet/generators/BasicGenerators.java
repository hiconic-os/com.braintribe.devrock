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
package com.braintribe.devrock.repolet.generators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepoletContentGenerator;
import com.braintribe.devrock.repolet.parser.RepoletContentParser;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.util.Lists;

@Category(KnownIssue.class)
public class BasicGenerators {

	private File contents = new File( "res/generators");
	private static YamlMarshaller marshaller = new YamlMarshaller();
	
 
	
	private void generateRepoContents(File inputParam, File output) {	
		try (InputStream in = new FileInputStream(inputParam)) {
			RepoletContent content = RepoletContentParser.INSTANCE.parse(in);
			RepoletContentGenerator.INSTANCE.generate(output, content);
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e.getMessage() + "] thrown");
		}
	}
	
	public static void transposeTextToYaml(File inputParam, File output) {	
		try (InputStream in = new FileInputStream(inputParam)) {
			RepoletContent content = RepoletContentParser.INSTANCE.parse(in);			
			String name = inputParam.getName().substring( 0, inputParam.getName().lastIndexOf( '.')) + ".yaml";
			try ( 
					OutputStream out = new FileOutputStream( new File( output, name));
				) {					
					marshaller.marshall(out, content);
			}
			catch (IOException e) {
				e.printStackTrace();
				Assert.fail("exception [" + e.getMessage() + "] thrown");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e.getMessage() + "] thrown");
		}
	}
	
	
	@Test
	public void contentForParallelBuildResolverTest() {
		File tcontents = new File( contents,"parallelBuilder");
		File input = new File( tcontents, "parallel-builder-test.txt");
		File output = new File( tcontents, "result");
		TestUtils.ensure(output);
		generateRepoContents( input, output);
		
		
	}
	
	@Test
	public void contentFor_COREDR_30() {
		File tcontents = new File( contents,"COREDR-30");
	
		File input = new File( tcontents, "input");
		File output = new File( tcontents, "output");
		TestUtils.ensure(output);
		
		File rules = new File( input, "remoteRepoB.txt");
		File produce = new File( output, "remoteRepoB");
		generateRepoContents( rules, produce);
		
		rules = new File( input, "remoteRepoC.txt");
		produce = new File( output, "remoteRepoC");
		generateRepoContents( rules, produce);
		
	}
	
	List<String> dataToProduceForCoreDr_31;
	{
		dataToProduceForCoreDr_31 = new ArrayList<>();
		dataToProduceForCoreDr_31.add( "directSelfReference");
		dataToProduceForCoreDr_31.add( "rangedSelfReference");
		dataToProduceForCoreDr_31.add( "directSelfReferenceAsParent");
		dataToProduceForCoreDr_31.add( "directSelfReferenceAsImport");
		dataToProduceForCoreDr_31.add( "cycleViaParent");
		dataToProduceForCoreDr_31.add( "cycleViaImport");
		dataToProduceForCoreDr_31.add( "cycleViaStandardDependency");
		dataToProduceForCoreDr_31.add( "cycleViaRelocation");
	}
	@Test
	public void contentFor_COREDR_31() {
		File tcontents = new File( contents,"COREDR-31");
	
		File input = new File( tcontents, "input");
		File output = new File( tcontents, "output");
		TestUtils.ensure(output);
		
		for (String data : dataToProduceForCoreDr_31) {
			File rules = new File( input, data + ".txt");
			File produce = new File( output, data);
			generateRepoContents( rules, produce);
		}
		
	}
	
	List<Pair<String,List<String>>> dataToProduceForCoreDr_10;
	{
		dataToProduceForCoreDr_10 = new ArrayList<>();
		
		dataToProduceForCoreDr_10.add( Pair.of("simpleTree", null));
		dataToProduceForCoreDr_10.add( Pair.of("commonBranches", null));
		dataToProduceForCoreDr_10.add( Pair.of("commonBranchesWithExclusions", null));
		dataToProduceForCoreDr_10.add( Pair.of("simpleStandardTree", null));
		dataToProduceForCoreDr_10.add( Pair.of("simpleReferenceTree", Lists.list("simpleReferenceTree", "simpleReferenceTree.parent")));
		dataToProduceForCoreDr_10.add( Pair.of("simpleImportingReferenceTree", Lists.list("simpleImportingReferenceTree", "simpleImportingReferenceTree.parent.import")));		
		dataToProduceForCoreDr_10.add( Pair.of("redirectionTree", Lists.list("redirectionTree", "redirectiontree.redirect")));
		
	}
	
	@Test
	public void contentFor_COREDR_10() {
		File tcontents = new File( contents,"COREDR-10");
		
		File input = new File( tcontents, "input");
		File output = new File( tcontents, "output");

		TestUtils.ensure(output);
		for (Pair<String,List<String>> data : dataToProduceForCoreDr_10) {
			File rules = new File( input, data.first + ".txt");

			if (rules.exists()) {
				System.out.println("creating content specified by [" + rules.getName() + "]");
				File produce = new File( output, data.first);
				generateRepoContents( rules, produce);
			}
			else {
				System.out.println("no rule file [" + rules.getName() + "] found");
			}
			
			List<String> validations = data.second;
			if (validations == null) {
				File validation = new File( input, data.first + ".validation.txt");
				if (validation.exists()) {
					System.out.println("creating rulebased validation specified by [" + validation.getName() + "]");
					transposeTextToYaml(validation, output);
				}
				else {
					System.out.println("no validation file [" + validation.getName() + "] found");
				}
			}
			else {
				for (String val : validations) {					
					File validation = new File( input, val + ".validation.txt");
					if (validation.exists()) {
						System.out.println("creating specific validation specified by [" + validation.getName() + "]");
						transposeTextToYaml(validation, output);
					}
					else {
						System.out.println("no validation file [" + validation.getName() + "] found");
					}					
				}
			}			
		}		
	}
	
	private List<String> inputs = new ArrayList<>();
	{
		inputs.add( "simpleTree.txt");
		inputs.add( "contents.one.txt");
		inputs.add( "contents.two.txt");
	}
	
	@Test
	public void transpose() {
		File base = new File( contents, "COREDR-10");
		File input = new File( base, "input");
		for (String in : inputs) {
			File text = new File( input, in);
			transposeTextToYaml(text, new File( base, "output"));
		}
	}
	
	@Test
	public void createDataForDiscoveryTest() {
		File discovery = new File( contents, "discovery");
		File input = new File( discovery, "input");		
		File output = new File( discovery, "output");
		TestUtils.ensure(output);
		File inputFile = new File( input, "discovery.txt");
		File outputFile = new File( output, "content");
		generateRepoContents(inputFile, outputFile);
	}
	
	List<String> dataToProduceForCoreDr_50;
	{
		dataToProduceForCoreDr_50 = new ArrayList<>();
		dataToProduceForCoreDr_50.add( "content.definition");
		
	}
	@Test
	public void contentFor_COREDR_50() {
		File tcontents = new File( contents,"COREDR-50");
	
		File input = new File( tcontents, "input");
		File output = new File( tcontents, "output");
		TestUtils.ensure(output);
		
		for (String data : dataToProduceForCoreDr_50) {
			File rules = new File( input, data + ".txt");
			File produce = new File( output, data);
			generateRepoContents( rules, produce);
		}
		
	}

}
