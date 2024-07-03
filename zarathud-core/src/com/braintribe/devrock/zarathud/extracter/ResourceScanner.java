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