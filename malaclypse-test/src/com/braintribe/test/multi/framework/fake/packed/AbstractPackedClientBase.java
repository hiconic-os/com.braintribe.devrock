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
package com.braintribe.test.multi.framework.fake.packed;

import java.io.File;

import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;

public class AbstractPackedClientBase {
	private ZipContext zip;

	public AbstractPackedClientBase(File zipFile) {
		try {
			this.zip = Archives.zip().from(zipFile);
		} catch (ArchivesException e) {
			throw new IllegalArgumentException("cannot open Archives on [" + zipFile.getAbsolutePath() + "]");
		}
	}
	
	protected void getEntry() {		
	}

}
