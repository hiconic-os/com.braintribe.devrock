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
package com.braintribe.model.processing.meta;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import com.braintribe.cfg.Required;

/**
 * File System based implementation of {@link AbstractModelArtifactBuilder}
 */
public class FsBasedModelArtifactBuilder extends AbstractModelArtifactBuilder {

	private File versionFolder;

	@Required
	public void setVersionFolder(File versionFolder) {
		this.versionFolder = versionFolder;
	}

	@Override
	protected OutputStream partOutputStream(String extension) {
		String artifactId = modelDescriptor.artifactId;
		String version = modelDescriptor.version;

		File partFile = new File(versionFolder, artifactId + "-" + version + extension);

		try {
			return new BufferedOutputStream(new FileOutputStream(partFile));

		} catch (FileNotFoundException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	protected void closePartOutputStream(OutputStream out) throws IOException {
		out.close();
	}

}
