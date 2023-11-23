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
package com.braintribe.devrock.repolet.common;

import java.util.LinkedHashMap;
import java.util.Map;

import com.braintribe.common.lcd.Pair;

public class RepoletCommons {
	public static Map<String, Pair<String,String>> hashAlgToHeaderKeyAndExtension = new LinkedHashMap<>();
	static {
		hashAlgToHeaderKeyAndExtension.put( "MD5", Pair.of("X-Checksum-Md5", "md5"));
		hashAlgToHeaderKeyAndExtension.put( "SHA-1", Pair.of( "X-Checksum-Sha1", "Sha1"));
		hashAlgToHeaderKeyAndExtension.put( "SHA-256", Pair.of( "X-Checksum-Sha256", "Sha256"));
	}
	
	public static Pair<String, String> extractArtifactExpression( String path) {
		String [] tokens = path.split( "/");
		
		String partName = tokens[ tokens.length - 1]; // part name
		String version = tokens[ tokens.length - 2]; // version
		String artifactId = tokens[ tokens.length - 3]; // artfactid
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tokens.length - 3; i++) {
			if (sb.length() > 0)
				sb.append( '.');
			sb.append( tokens[i]);
					
		}
		return Pair.of( sb.toString() + ":" + artifactId + "#" + version, partName);		
	}

}
