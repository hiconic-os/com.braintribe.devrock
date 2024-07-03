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
package com.braintribe.devrock.mc.core.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.testing.category.KnownIssue;

/**
 * simple test bed to analyze an issue with the YAML marshaller :
 * constructs like this fail : 
 *   ?*11: &31 !com.braintribe.model.artifact.analysis.AnalysisArtifact
 *   
 *   property is a map of 
 * @author pit
 *
 */
@Category( KnownIssue.class)
public class YamlMarshallerIssuesLab implements HasCommonFilesystemNode {
	protected File input;
	protected File output;
			
	{
		Pair<File,File> pair = filesystemRoots("basics/yaml");
		input = pair.first;
		output = pair.second;								
	}

	@Test
	public void test() {
		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setV2(true);
		
		AnalysisArtifactResolution resolution = null;
		try (InputStream in = new FileInputStream( new File( input, "map.issue.yaml"))) {
			resolution = (AnalysisArtifactResolution) marshaller.unmarshall(in);
		}
		catch (Exception e) {
			Assert.fail("may not fail at this time, still : " + e.getLocalizedMessage());
		}
		System.out.println( resolution);	
	}
}
