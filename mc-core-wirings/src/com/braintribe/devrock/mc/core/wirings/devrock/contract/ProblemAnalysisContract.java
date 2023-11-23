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
package com.braintribe.devrock.mc.core.wirings.devrock.contract;

import java.io.File;

import com.braintribe.wire.api.space.WireSpace;

/**
 * a contract to support diverse aspects of problem analysis across different mc platforms 
 * 
 * standard location : ${user.home}/.devrock/processing-data-insight
 * 
 * @author pit
 *
 */
public interface ProblemAnalysisContract extends WireSpace {
	String folderName = "processing-data-insight";
	String environmentVariable = "devrock_home";
	String devrockFolderName = ".devrock";
	
	/**
	 * @return - the location of the current dump folder 
	 */
	File processingDataInsightFolder();
	
	default File defaultProcessingDataInsightFolder() {
		String devrockHome = System.getenv(environmentVariable);
		File devrockFolder;
		if (devrockHome == null) {
			String userHome = System.getProperty("user.home");
			devrockFolder = new File( userHome, devrockFolderName);
		}
		else {
			devrockFolder = new File( devrockHome);
		}
		return new File( devrockFolder, folderName);
	}
}
