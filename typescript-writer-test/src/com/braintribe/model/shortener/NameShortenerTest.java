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
package com.braintribe.model.shortener;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.model.shortener.NameShortener.ShortNameEntry;
import com.braintribe.model.shortener.NameShortener.ShortNames;

/**
 * @author peter.gazdik
 */
public class NameShortenerTest {

	private ShortNames<Name> result;

	@Test
	public void testUnambiguous() throws Exception {
		Name x = new Name("com.bt.X");
		Name y = new Name("com.bt.Y");

		shorten(x, y);

		assertThat(result.paths).hasSize(1);

		assertNamesForPrefix("", "X", x, "Y", y);
	}

	@Test
	public void testSimmpleAmbiguous() throws Exception {
		Name x = new Name("X");
		Name aaX = new Name("aa.X");
		Name aabbX = new Name("aa.bb.X");

		shorten(x, aaX, aabbX);

		assertThat(result.paths).hasSize(3);

		assertNamesForPrefix("", "X", x);
		assertNamesForPrefix("aa", "X", aaX);
		assertNamesForPrefix("aa.bb", "X", aabbX);
	}

	@Test
	public void testAmbiguous() throws Exception {
		Name x = new Name("com.bt.X");
		Name aaX = new Name("com.bt.aa.foo.X");
		Name bbX = new Name("com.bt.bb.foo.X");
		Name fooX = new Name("com.bt.foo.aa.X");

		shorten(x, aaX, bbX, fooX);

		assertThat(result.paths).hasSize(4);

		assertNamesForPrefix("", "X", x);
		assertNamesForPrefix("aa", "X", aaX);
		assertNamesForPrefix("bb", "X", bbX);
		assertNamesForPrefix("foo", "X", fooX);
	}

	@Test
	public void testMix() throws Exception {
		Name x = new Name("com.bt.X");
		Name y = new Name("com.bt.Y");
		Name fooX = new Name("com.bt.foo.X");

		shorten(x, y, fooX);

		assertThat(result.paths).hasSize(2);

		assertNamesForPrefix("", "X", x, "Y", y);
		assertNamesForPrefix("foo", "X", fooX);
	}

	private void shorten(Name... names) {
		result = NameShortener.shortenNames(asList(names), n -> n.s);
	}

	private void assertNamesForPrefix(String prefix, Object... expectedNames) {
		Map<String, Name> expectedMap = asMap(expectedNames);

		assertThat(result.paths).containsKey(prefix);

		List<ShortNameEntry<Name>> entries = result.paths.get(prefix);
		assertThat(entries).isNotNull();

		Map<String, Name> shortNames = entries.stream() //
				.collect(Collectors.toMap(e -> e.simpleName, e -> e.value));

		assertThat(shortNames) //
				.as("Set of short-names is smaller than set of entries, implying some short names among the entries are the same.") //
				.hasSameSizeAs(entries);

		assertThat(shortNames).isEqualTo(expectedMap);
	}

	private static class Name {
		private final String s;

		public Name(String s) {
			this.s = s;
		}

	}
}
