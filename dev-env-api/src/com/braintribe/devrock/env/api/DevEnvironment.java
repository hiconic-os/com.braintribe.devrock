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
package com.braintribe.devrock.env.api;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.braintribe.common.attribute.TypeSafeAttribute;

public interface DevEnvironment extends TypeSafeAttribute<DevEnvironment> {
	File getRootPath();
	
	default File resolveRelativePath(String path) {
		File file;
		try {
			file = new File(path).getCanonicalFile();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		if (file.isAbsolute())
			return file;
		
		return new File(getRootPath(), path);
	}
}
