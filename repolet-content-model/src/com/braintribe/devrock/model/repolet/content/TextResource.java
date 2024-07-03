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
package com.braintribe.devrock.model.repolet.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;

import com.braintribe.model.resource.Resource;

/**
 * a dependency within the {@link RepoletContent}
 * @author pit
 *
 */
public interface TextResource extends Resource {
	
	String getContent();
	void setContent(String content);
	
	
	@Override
	default InputStream openStream() {
		try {
			// TODO: add a functional dependency to platform-api and use ReaderInputStream as a more stream like solution with less memory consumption 
			String content = getContent();
			
			byte data[] = content != null? content.getBytes("UTF-8"): new byte[] {}; 
			return new ByteArrayInputStream(data);
		} catch (UnsupportedEncodingException e) {
			throw new UncheckedIOException(e);
		}
	}
}
