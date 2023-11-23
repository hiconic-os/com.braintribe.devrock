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
