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
package com.braintribe.devrock.mc.core.declared;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.declared.DeclaredGroupExtractionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.declared.group.DeclaredGroupExtractor;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.declared.DeclaredGroup;

public abstract class AbstractGroupExtractionTest implements HasCommonFilesystemNode {
	protected File input;
	protected File output;
	protected YamlMarshaller marshaller;
	{
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	protected File root = new File( "f:/works/dev-envs/standard/git");
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/declared/group");
		input = pair.first;
		output = pair.second;			
	}

		
	protected DeclaredGroup runGroupExtractionLab(DeclaredGroupExtractionContext context) {
		// 

		DeclaredGroupExtractor extractor = new DeclaredGroupExtractor();
		Maybe<DeclaredGroup> extractedGroup = extractor.extractGroup(context);
		
		if (extractedGroup.isEmpty()) {
			//Assert.fail("no extraction happened");
			DeclaredGroup dg = DeclaredGroup.T.create();
			dg.setFailure(extractedGroup.whyUnsatisfied());
			return dg;
		}
		else if (extractedGroup.isIncomplete()) {
			System.out.println("incomplete " + extractedGroup.whyUnsatisfied().asFormattedText());
			if (extractedGroup.hasValue()) {
				DeclaredGroup declaredGroup = extractedGroup.value();
				dump( declaredGroup.getGroupId(), declaredGroup);
				return declaredGroup;
			}
		} else if (extractedGroup.isSatisfied()) {			
			//System.out.println("successful extraction");			
			DeclaredGroup declaredGroup = extractedGroup.get();
			dump(declaredGroup.getGroupId(), declaredGroup);
			return declaredGroup;
		}
		else {
			Assert.fail("unknown state of returned maybe");
		}
		return null;
	}

	private void dump(String groupName, DeclaredGroup extractedGroup) {
		// validate
		try (OutputStream out = new FileOutputStream( new File( output, groupName + ".yaml"))) {
			marshaller.marshall(out, extractedGroup);
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail("cannot dump group data");
		}
	}
		
	/**
	 * @param declaredGroup
	 * @param expectations
	 */
	protected void validate( DeclaredGroup declaredGroup, Map<String, String> expectations) {
		Validator validator = new Validator();
		
		if (declaredGroup == null) {
			Assert.fail("extraction failed catastrophically");
			return;
		}
		validator.assertTrue("extraction has issues", !declaredGroup.hasFailed());	
		
		Map<String,String> groupDependencies = declaredGroup.getGroupDependencies();
		
		validate(validator, groupDependencies, expectations);			
		validator.assertResults();
	}
	
	private void validate( Validator validator, Map<String, String> founds, Map<String, String> expecteds) {	
		Map<String,String> matches = new HashMap<>();
		Map<String,String> mismatch = new HashMap<>();
		List<String> missing = new ArrayList<>();
		List<String> excess = new ArrayList<>(); 
		
		for (Map.Entry<String, String> entry : founds.entrySet()) {
			String expected = expecteds.get(entry.getKey());
			if (expected == null) {
				excess.add( entry.getKey());
				continue;
			}
			if (expected.equals( entry.getValue())) {
				matches.put(entry.getKey(), entry.getValue());
			}
			else {
				mismatch.put(entry.getKey(), entry.getValue());
			}
		}
		missing.addAll( expecteds.keySet());
		missing.removeAll( matches.keySet());
		
		validator.assertTrue("missing groups [" + missing.stream().collect( Collectors.joining(",")) + "]", missing.size() == 0);
		validator.assertTrue("excess groups [" + excess.stream().collect( Collectors.joining(",")) + "]", excess.size() == 0);
		
		validator.assertTrue("mismatchs [" + dump( mismatch, expecteds) + "]", mismatch.size() == 0);
	}

	private String dump(Map<String,String> found, Map<String,String> expected) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : found.entrySet()) {
			if (sb.length() != 0) {
				sb.append(",");
			} 
			sb.append( entry.getKey() + " -> " + entry.getValue() + " != " + expected.get(entry.getKey()));
		}
		return sb.toString();
	}

}
