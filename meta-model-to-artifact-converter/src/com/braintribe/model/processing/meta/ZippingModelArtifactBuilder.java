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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.utils.StringTools;

/**
 * File System based implementation of {@link AbstractModelArtifactBuilder}
 */
public class ZippingModelArtifactBuilder extends AbstractModelArtifactBuilder {

	private ZipOutputStream zos;
	private String filePrefix = "";

	@Configurable
	public void setFilePrefix(String filePrefix) {
		if (StringTools.isEmpty(filePrefix))
			return;

		if (!filePrefix.endsWith("/"))
			filePrefix += "/";

		this.filePrefix = filePrefix;
	}

	@Required
	public void setZos(ZipOutputStream zos) {
		this.zos = zos;
	}

	@Override
	protected OutputStream partOutputStream(String extension) {
		String artifactId = modelDescriptor.artifactId;
		String version = modelDescriptor.version;

		String fileName = artifactId + "-" + version + extension;
		ZipEntry ze = new ZipEntry(filePrefix + fileName);
		try {
			zos.putNextEntry(ze);
		} catch (IOException e) {
			throw new RuntimeException("Error while preparing next zip entry for file " + fileName, e);
		}

		return zos;
	}

	@Override
	protected void closePartOutputStream(OutputStream out) throws IOException {
		// NO OP
	}

}
