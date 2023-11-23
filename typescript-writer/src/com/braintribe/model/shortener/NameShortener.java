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

import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.last;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newTreeMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmType;

/**
 * 
 * 
 * 
 * @author peter.gazdik
 */
public class NameShortener<T> {

	/**
	 * For a given collection of Ts (say {@link GmType}s or {@link GenericModelType}s) and a function which can resolve the name(signature), creates a
	 * {@link ShortNames} object.
	 * <p>
	 * The goal is group types where they have a unique name within a given namespace, and the name of the namespace is derived based on the package
	 * of the type, while being as short as possible.
	 */
	public static <T> ShortNames<T> shortenNames(Collection<T> values, Function<T, String> fullNameResolver) {
		return new NameShortener<T>(values, fullNameResolver).doItYouGoof();
	}

	private final Map<String, Group> groups = newMap();

	private NameShortener(Collection<T> values, Function<T, String> fullNameResolver) {
		for (T value : values) {
			String[] path = fullNameResolver.apply(value).split("\\.");
			NamedElement entry = new NamedElement(value, path);
			String simpleName = last(path);

			Group group = groups.computeIfAbsent(simpleName, sn -> new Group(0, ""));
			group.elements.add(entry);
		}
	}

	private ShortNames<T> doItYouGoof() {
		ShortNames<T> result = new ShortNames<>();

		for (NameShortener<T>.Group group : groups.values())
			group.resultEntries().forEach( //
					re -> acquireList(result.paths, re.prefix).add(re.toShortNameEntry()));

		result.paths.values().forEach(this::sortNameEntries);

		return result;
	}

	private void sortNameEntries(List<ShortNameEntry<T>> entries) {
		entries.sort((e1, e2) -> e1.simpleName.compareTo(e2.simpleName));
	}

	// Group consists of type with the same simple name  
	private class Group {

		private final int offset;
		private final String prefix;
		private final List<NamedElement> elements = newList();

		public Group(int offset, String prefix) {
			this.offset = offset;
			this.prefix = prefix;
		}

		public Stream<ResultElement> resultEntries() {
			return elements.size() == 1 ? singleResult() : disambiguate();
		}

		private Stream<ResultElement> singleResult() {
			return Stream.of(new ResultElement(prefix, first(elements)));
		}

		private Stream<ResultElement> disambiguate() {
			// index of first position with differences, i.e. number of path parts that are same (within this group)
			int nonCommonIndex = skipCommonPrefix();

			Map<String, Group> groups = newMap();

			for (NamedElement e : elements) {
				boolean isEnd = e.path.length == nonCommonIndex + 1;
				String subGroupName = isEnd ? "" : e.path[nonCommonIndex];
				String subGroupPrefix = concat(prefix, subGroupName);

				Group group = groups.computeIfAbsent(subGroupName, sn -> new Group(nonCommonIndex + 1, subGroupPrefix));
				group.elements.add(e);
			}

			return groups.values().stream() //
					.flatMap(Group::resultEntries);
		}

		private String concat(String s1, String s2) {
			if (s1.isEmpty())
				return s2;
			if (s2.isEmpty())
				return s1;
			return s1 + "." + s2;
		}

		private int skipCommonPrefix() {
			int i = offset;
			while (isCommonPrefix(i))
				i++;

			return i;
		}

		private boolean isCommonPrefix(int i) {
			String part = null;
			for (NamedElement e : elements) {
				if (e.path.length == i)
					return false;

				String thisPart = e.path[i];
				if (part == null)
					part = thisPart;
				else if (!part.equals(thisPart))
					return false;
			}
			return true;
		}

	}

	private class NamedElement {
		public final T value;
		public final String[] path;

		public NamedElement(T value, String[] path) {
			this.value = value;
			this.path = path;
		}
	}

	private class ResultElement {
		public final String prefix;
		public final NamedElement element;

		public ResultElement(String prefix, NamedElement element) {
			this.prefix = prefix;
			this.element = element;
		}

		public ShortNameEntry<T> toShortNameEntry() {
			return new ShortNameEntry<>(last(element.path), element.value);
		}
	}

	public static class ShortNames<T> {
		// map: prefix -> entries for given prefix (prefix is derived from package path)
		public final Map<String, List<ShortNameEntry<T>>> paths = newTreeMap();
	}

	public static class ShortNameEntry<T> {
		public final String simpleName;
		public final T value;

		public ShortNameEntry(String simpleName, T value) {
			this.simpleName = simpleName;
			this.value = value;
		}

	}

}
