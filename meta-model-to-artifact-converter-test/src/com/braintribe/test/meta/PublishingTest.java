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
