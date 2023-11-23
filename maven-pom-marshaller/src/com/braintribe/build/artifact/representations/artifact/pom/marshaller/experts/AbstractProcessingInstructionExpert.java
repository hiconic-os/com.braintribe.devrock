package com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts;

import java.util.function.Predicate;

/**
 * abstract base for both 'complex' PI experts
 * @author pit
 *
 */
public abstract class AbstractProcessingInstructionExpert {
	protected static Predicate<Character> whitespacePredicate = Character::isWhitespace;
	protected static Predicate<Character> nonWhitespacePredicate = whitespacePredicate.negate();

	protected static int findNextOccurrence(String expression, int startIndex, Predicate<Character> predicate) {
		int len = expression.length();
		for (int i = startIndex; i < len; i++) {
			char c = expression.charAt(i);
			if (predicate.test(c))
				return i;
		}
		
		return -1;
	}
}
