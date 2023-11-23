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
package com.braintribe.devrock.mc.core.filters;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.braintribe.devrock.model.repository.filters.QualifiedArtifactFilter;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.utils.CommonTools;

/**
 * Expert implementation for {@link QualifiedArtifactFilter}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class QualifiedArtifactFilterExpert implements ArtifactFilterExpert {

	private Matcher<String> groupIdMatcher;
	private Matcher<String> artifactIdMatcher;
	private Matcher<Version> versionMatcher;
	private Matcher<String> classifierMatcher;
	private Matcher<String> typeMatcher;

	public QualifiedArtifactFilterExpert(QualifiedArtifactFilter filter) {
		groupIdMatcher = toStringValueMatcher(filter.getGroupId());
		artifactIdMatcher = toStringValueMatcher(filter.getArtifactId());
		versionMatcher = filter.getVersion() == null ? (Matcher<Version>) AllMatcher.instance
				: new VersionExpressionBasedMatcher(filter.getVersion());
		classifierMatcher = toStringValueMatcher(filter.getClassifier());
		typeMatcher = toStringValueMatcher(filter.getType());
	}

	@Override
	public boolean matchesGroup(String groupId) {
		boolean result = groupIdMatcher.matches(groupId);
		return result;
	}
	
	@Override
	public boolean matches(ArtifactIdentification identification) {
		boolean result = groupIdMatcher.matches(identification.getGroupId()) && artifactIdMatcher.matches(identification.getArtifactId());
		return result;
	}

	@Override
	public boolean matches(CompiledArtifactIdentification identification) {
		boolean result = groupIdMatcher.matches(identification.getGroupId()) && artifactIdMatcher.matches(identification.getArtifactId())
				&& versionMatcher.matches(identification.getVersion());
		return result;
	}

	@Override
	public boolean matches(CompiledPartIdentification identification) {
		boolean result = groupIdMatcher.matches(identification.getGroupId()) && artifactIdMatcher.matches(identification.getArtifactId())
				&& versionMatcher.matches(identification.getVersion())
				// classifier and type are not mandatory; null values must be handles as empty strings
				&& classifierMatcher.matches(identification.getClassifier() != null ? identification.getClassifier() : "")
				&& typeMatcher.matches(identification.getType() != null ? identification.getType() : "");
		return result;
	}

	private static Matcher<String> toStringValueMatcher(String filterValue) {
		final Matcher<String> result;
		if (filterValue != null) {
			if (filterValue.contains("*")) {
				result = new PatternBasedMatcher(filterValue);
			} else {
				result = new StringBasedMatcher(filterValue);
			}
		} else {
			result = (Matcher<String>) AllMatcher.instance;
		}
		return result;
	}

	// ************************************************************************
	// *************************** Matchers ***********************************
	// ************************************************************************

	/**
	 * A <code>Matcher</code> is used to check whether instances of a particular type {@link #matches(Object) match}.
	 *
	 * @param <T>
	 *            the type of the objects to check.
	 */
	private static interface Matcher<T> {
		boolean matches(T value);
	}

	private static class StringBasedMatcher implements Matcher<String> {
		private String string;

		StringBasedMatcher(String string) {
			this.string = string;
		}

		@Override
		public boolean matches(String value) {
			return string.equals(value);
		}
	}

	/**
	 * A {@link Matcher} which matches strings based on a pattern. The pattern can either be a simple pattern where
	 * <code>*</code> matches any substring (similar to <code>.*</code> in a regular expression) or it can be a string
	 * starting with {@value #REGEX_PREFIX} followed by a regular expression.
	 */
	public static class PatternBasedMatcher implements Matcher<String> {
		private static final String REGEX_PREFIX = "regex:";

		private Pattern pattern;

		public PatternBasedMatcher(String pattern) {
			if (pattern == null) {
				throw new IllegalArgumentException("The passed pattern must not be null!");
			}
			if (!isPattern(pattern)) {
				throw new IllegalArgumentException("The passed string '" + pattern + "' is not a pattern string!"
						+ " If the intention is to match only exactly this string, use a simple string comparison instead, since this is much faster.");
			}

			final String patternToBeCompiled;
			if (pattern.startsWith(REGEX_PREFIX)) {
				// regex pattern, e.g. "regex:abc.*def"
				patternToBeCompiled = pattern.substring(REGEX_PREFIX.length());
			} else {
				// simple pattern, e.g. "abc*def"
				patternToBeCompiled = pattern.replace("*", ".*");
			}
			try {
				this.pattern = Pattern.compile(patternToBeCompiled);
			} catch (PatternSyntaxException e) {
				throw Exceptions.contextualize(e, "The passed string '" + pattern + "' is not a valid pattern string!");
			}
		}

		@Override
		public boolean matches(String string) {
			return pattern.matcher(string).matches();
		}

		/**
		 * Checks whether the specified <code>string</code> is a pattern string, i.e. if it contains <code>*</code> or
		 * starts with {@value #REGEX_PREFIX}.
		 */
		public static boolean isPattern(String string) {
			return string != null && (string.startsWith(REGEX_PREFIX) || string.contains("*"));
		}

		/**
		 * Checks whether the specified <code>string</code> matches the given <code>pattern</code>. This is a
		 * convenience method which can be used, if one only wants to run a single check per pattern. Otherwise it is
		 * more efficient to create a new instance of {@link PatternBasedMatcher} and then use method
		 * {@link #matches(String)} multiple times.
		 */
		public static boolean matches(String string, String pattern) {
			return new PatternBasedMatcher(pattern).matches(string);
		}

		/**
		 * If the given <code>patternOrSearchedString</code> {@link #isPattern(String) is a pattern}, checks whether the
		 * specified <code>string</code> {@link #matches(String, String) matches}, otherwise checks whether the strings
		 * are equal (or both <code>null</code>).
		 */
		public static boolean matchesOrEqual(String string, String patternOrSearchedString) {
			boolean result;
			if (isPattern(patternOrSearchedString)) {
				result = matches(string, patternOrSearchedString);
			} else {
				result = CommonTools.equalsOrBothNull(string, patternOrSearchedString);
			}
			return result;
		}
	}

	private static class VersionExpressionBasedMatcher implements Matcher<Version> {
		private VersionExpression versionExpression;

		VersionExpressionBasedMatcher(String versionExpressionString) {
			versionExpression = VersionExpression.parse(versionExpressionString);
		}

		@Override
		public boolean matches(Version value) {
			return versionExpression.matches(value);
		}
	}

	private static class AllMatcher<T> implements Matcher<T> {
		private static final AllMatcher<?> instance = new AllMatcher<Object>();

		private AllMatcher() {
			// nothing to do
		}

		@Override
		public boolean matches(T value) {
			return true;
		}
	}

}
