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
package com.braintribe.devrock.mc.core.artifactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.model.artifactory.FolderInfo;

public class ArtifactoryJsonTest implements HasCommonFilesystemNode {	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("artifactoryJson");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}

	@Before
	public void before() {
		TestUtils.ensure(output);
	}
	
	@Test
	public void test() throws IOException{
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		GmDeserializationOptions options = GmDeserializationOptions.defaultOptions.derive().setInferredRootType( FolderInfo.T).build();
		try (InputStream in = new FileInputStream( new File( input, "artifactory.json"))) {
			FolderInfo fi = (FolderInfo) marshaller.unmarshall(in, options);
			System.out.println(fi);
		}
		 
		
	}
}
