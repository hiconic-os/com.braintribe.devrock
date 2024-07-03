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
package com.braintribe.test.meta;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.FsBasedModelArtifactBuilder;

public class PublishingTest {
	@Test
	public void manifest() throws Exception {
		// create and fill manifest

		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, "schnallo");
		manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, "prello");
		manifest.getMainAttributes().putValue("Created-By", "foobar");

		// write the manifest to the archive
		manifest.write(System.out);

	}
	@Test
	public void publishing() throws Exception {

		Model model = GMF.getTypeReflection().getModel("com.braintribe.gm:value-descriptor-model");

		GmMetaModel metaModel = model.getMetaModel();

		FsBasedModelArtifactBuilder publishing = new FsBasedModelArtifactBuilder();
		publishing.setUser("dummyUser");

		File folder = new File("test-output");
		folder.mkdirs();
		publishing.setModel(metaModel);
		publishing.setVersionFolder(folder);
		publishing.publish();

	}

}
