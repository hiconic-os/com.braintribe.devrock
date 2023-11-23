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
