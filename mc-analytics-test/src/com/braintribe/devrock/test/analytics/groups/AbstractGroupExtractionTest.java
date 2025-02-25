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
package com.braintribe.devrock.test.analytics.groups;

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
import com.braintribe.devrock.mc.core.declared.group.DeclaredGroupExtractor;
import com.braintribe.devrock.test.analytics.commons.utils.HasCommonFilesystemNode;
import com.braintribe.devrock.test.analytics.commons.validator.Validator;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.declared.DeclaredGroup;

public abstract class AbstractGroupExtractionTest implements HasCommonFilesystemNode {
	protected YamlMarshaller marshaller = new YamlMarshaller();
	
	protected File input;
	protected File output;		
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
			System.out.println("incomplete " + extractedGroup.whyUnsatisfied().stringify());
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
