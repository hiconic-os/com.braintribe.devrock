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
