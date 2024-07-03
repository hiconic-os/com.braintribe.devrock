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
package com.braintribe.devrock.repolet.parser;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Paths;

import com.braintribe.logging.Logger;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.stream.ReaderInputStream;

public interface ResourceParsers {
	Logger log = Logger.getLogger(ResourceParsers.class);

	static Resource parse(String expression) {
		return Resource.createTransient(() -> new ReaderInputStream(new StringReader(expression)));
	}

	static FileResource parseFileResource(String expression) {

		File file = Paths.get(expression).toFile();
		if (!file.exists()) {
			log.warn("Could not resolve file with path '" + expression + "'.\nCheck if file exists: " + file.getAbsolutePath());
		}
		FileResource resource = FileResource.T.create();
		resource.setPath(expression);
		return resource;
	}
}
