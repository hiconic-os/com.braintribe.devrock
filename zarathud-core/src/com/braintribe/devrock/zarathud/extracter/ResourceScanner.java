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
package com.braintribe.devrock.zarathud.extracter;

import java.io.File;
import java.io.IOException;

import com.braintribe.devrock.zarathud.extracter.scanner.ScannerResult;

public interface ResourceScanner {

	/**
	 * scan the jar file and extract the classes found within 
	 * @param file - the {@link File} that represents the jar 
	 * @return - a {@link ScannerResult}
	 * @throws IOException
	 */
	ScannerResult scanJar(File file) throws IOException;

	/**
	 * scans a file for the gwt module name - only one gwt file is expected within the jar!
	 * @param file - the {@link File} that represents the jar 
	 * @return - the {@link ScannerResult} (only module, no classes added)
	 * @throws IOException 
	 */
	ScannerResult scanJarForGwt(File file) throws IOException;

	/**
	 * scan a folder for class files 
	 * @param folder - the {@link File} that contains the class files
	 * @return - a {@link ScannerResult} with the classes 
	 * @throws IOException 
	 */
	ScannerResult scanFolder(File folder) throws IOException;

	/**
	 * scan the folder for the gwt module name 
	 * @param folder - the {@link File} pointing to the directory (of classes plus the module file)
	 * @return - a {@link ScannerResult} with only the last found module name 
	 */
	ScannerResult scanFolderForGwt(File folder);

}