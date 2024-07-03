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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.utils.IOTools;

public interface ZipTools {
	static void unzip(InputStreamProvider inputStreamProvider, File targetFolder) {
		try (ZipInputStream zin = new ZipInputStream(inputStreamProvider.openInputStream())) {
			ZipEntry zipEntry = null;
			
			while ((zipEntry = zin.getNextEntry()) != null) {
				String slashedPathName = zipEntry.getName();
				
				File targetFile = new File(targetFolder, slashedPathName);
				
				if (zipEntry.isDirectory()) {
					// create directory because it maybe empty and it would be an information loss otherwise
					targetFile.mkdirs();
				}
				else {
					targetFile.getParentFile().mkdirs();
					try (OutputStream out = new FileOutputStream(targetFile)) {
						IOTools.transferBytes(zin, out, IOTools.BUFFER_SUPPLIER_64K);
					} catch (Exception ex) {
						throw Exceptions.unchecked(ex, "Error while transferring zip entry " + slashedPathName + " to " + targetFile);
					}
				}
				
				zin.closeEntry();
			}
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while unpacking zip");
		}
	}

}
