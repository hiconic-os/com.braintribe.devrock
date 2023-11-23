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
