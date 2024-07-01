// ============================================================================
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
package com.braintribe.devrock.mc.core.commons;

import java.util.function.Predicate;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.artifact.essential.PartIdentification;

/**
 * collection of function for processing XML 'processing information'
 * @author pit
 *
 */
public abstract class PiCommons {
	protected static Predicate<Character> whitespacePredicate = Character::isWhitespace;
	protected static Predicate<Character> nonWhitespacePredicate = whitespacePredicate.negate();

	/**
	 * finds the next occurrence of a subexpression - filtered by predicate 
	 * @param expression - the {@link String} to parse 
	 * @param startIndex - the starting index 
	 * @param predicate - a {@link Predicate} on {@link Character} to find the occurence
	 * @return - the position of the character within the string
	 */
	protected static int findNextOccurrence(String expression, int startIndex, Predicate<Character> predicate) {
		int len = expression.length();
		for (int i = startIndex; i < len; i++) {
			char c = expression.charAt(i);
			if (predicate.test(c))
				return i;
		}
		
		return -1;
	}
	
	/**
	 * parses a 'part' PI
	 * @param piData - the string of the 'processing instruction'
	 * @return - a {@link Pair} of the derived {@link PartIdentification} and the payload
	 */
	public static Pair<PartIdentification, String> parsePart(String piData) {
		// inject first line into expression 
		String expression = piData;
		
		int startOfPartType = findNextOccurrence(expression, 0, nonWhitespacePredicate);
		
		if (startOfPartType == -1)
			throw new IllegalStateException("missing expected part type on <?part classifier:type payload ?> processing instruction");
		
		int endOfPartType = findNextOccurrence(expression, startOfPartType, whitespacePredicate);
		
		String partType, payload;
		
		if (endOfPartType == -1) {
			partType = expression.substring(startOfPartType);
			payload = "";
		}
		else {
			partType = expression.substring(startOfPartType, endOfPartType);
			payload = expression.substring(endOfPartType + 1);
		}
		
		return Pair.of(PartIdentification.parse(partType), payload);
	}
}
