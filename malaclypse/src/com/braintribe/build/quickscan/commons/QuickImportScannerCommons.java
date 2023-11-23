// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.quickscan.commons;

import java.io.File;
import java.net.URL;

import com.braintribe.build.quickscan.QuickImportScanException;
import com.braintribe.model.panther.SourceRepository;

public class QuickImportScannerCommons {

	public static String derivePath( File file, SourceRepository repository){
		// 
		String fullpath = sanitizeFilePath(file.getAbsolutePath());
		String repositoryPathAsUrl = repository.getRepoUrl();
		URL url;
		try {
			url = new URL( repositoryPathAsUrl);
		} catch (Exception e) {
			throw new QuickImportScanException( "cannot extract URL from [" + repositoryPathAsUrl + "]");
		}
		String repoPrefix = sanitizeFilePath(url.getFile());
		if (repoPrefix.startsWith( File.separator)) {
			repoPrefix = repoPrefix.substring(1);
		}
		int p = fullpath.indexOf(repoPrefix);
		if (p < 0) {
			throw new QuickImportScanException( "no match of [" + repoPrefix + "] in [" + fullpath + "]");
		}		
		String path = (repoPrefix.endsWith(File.separator)) ? fullpath.substring(p + repoPrefix.length()) : fullpath.substring(p + repoPrefix.length() +1); 
		return path;
	}

	
	public static String sanitizeFilePath( String path) {
		String result = path.replace("\\", "/");
		return result.replace( "/", File.separator);
	}


}
