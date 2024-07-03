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
