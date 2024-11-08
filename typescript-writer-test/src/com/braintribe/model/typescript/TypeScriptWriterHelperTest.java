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
package com.braintribe.model.typescript;

import static com.braintribe.model.typescript.TypeScriptWriterHelper.npmPackageFullName;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Tests for {@link TypeScriptWriterHelper}
 */
public class TypeScriptWriterHelperTest {

	@Test
	public void testNpmPackageFullName() throws Exception {
		assertFullName("com.braintribe.gm", "root-model", "@dev.hiconic/gm_root-model");
		assertFullName("tribefire.cortex", "some-model", "@dev.hiconic/tf.cortex_some-model");
		assertFullName("tribefire.cortex.assets", "some-model", "@dev.hiconic/tf.cortex.assets_some-model");
		assertFullName("tribefire.extension.js", "js-model", "@dev.hiconic/tf.extension.js_js-model");
		assertFullName("hiconic.core.js", "js-model", "@dev.hiconic/core.js_js-model");
		assertFullName("dev.hicocnic.js", "js-model", "@dev.hicocnic/js_js-model");
	}

	private void assertFullName(String groupId, String packageSuffix, String expected) {
		String actual = npmPackageFullName(groupId, packageSuffix);
		assertThat(actual).isEqualTo(expected);
	}

}
