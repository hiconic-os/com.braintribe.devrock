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

import static com.braintribe.model.typescript.TypeScriptWriterHelper.createCustomGmTypeFilter;
import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.function.Predicate;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @author peter.gazdik
 */
public class AbstractWriterTest {

	protected static final Predicate<Class<?>> customGmTypeFilter = createCustomGmTypeFilter(TypeScriptWriterForClassesTest.class.getClassLoader());

	protected String output;

	protected GmMetaModel buildModel(EntityType<?>... types) {
		GmMetaModel result = new NewMetaModelGeneration().buildMetaModel("test:ts-test-model", asList(types));
		result.getDependencies().forEach(m -> m.setVersion("1.45.1"));
		return result;
	}

	protected String rangifyModelVersion(String versionString) {
		Version version = Version.parse(versionString);
		return FuzzyVersion.from(version).asString();
	}

	protected void mustContainOnce(String s) {
		mustContain(s);

		int first = output.indexOf(s);
		int last = output.lastIndexOf(s);
		if (first != last)
			Assertions.fail("Only one occurence is expected for snippet: " + s);
	}

	protected void mustContain(String s) {
		assertThat(output).contains(s);
	}

	protected void notContains(String s) {
		assertThat(output).doesNotContain(s);
	}

}
