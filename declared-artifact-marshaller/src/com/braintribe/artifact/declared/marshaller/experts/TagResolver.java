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
package com.braintribe.artifact.declared.marshaller.experts;

import java.util.function.Supplier;

import com.braintribe.model.artifact.declared.ProcessingInstruction;

/**
 * just an extract to build our tags, either from the PI or now also from a COMMENT
 * 
 * @author pit
 *
 */
public class TagResolver {

	/**
	 * standard way to create a {@link ProcessingInstruction} from an XML processing instruction  
	 * @param piTarget - the target, 'tag' mostly 
	 * @param piData - the data (the rest after the tag)
	 * @param supplier - a supplier to get the {@link ProcessingInstruction} GE instance
	 * @return - the new {@link ProcessingInstruction}
	 */
	public static ProcessingInstruction fromProcessingInstruction(String piTarget, String piData, Supplier<ProcessingInstruction> supplier) {
		ProcessingInstruction pi = supplier.get();
		pi.setTarget(piTarget);
		pi.setData( piData.trim());		
		return pi;
	}
	
	/**
	 * alternative way to create a {@link ProcessingInstruction} from an XML comment 
	 * @param comment - the comment 
	 * @param supplier - a supplier to get the {@link ProcessingInstruction} GE instance
	 * @return - the new {@link ProcessingInstruction}
	 */
	public static ProcessingInstruction fromComment(String comment, Supplier<ProcessingInstruction> supplier) {

		int c = comment.indexOf( ':');
		String target = null;
		String data = null;
		if (c < 0) {			
			for (int i = 0; i < comment.length(); i++) {
				char ch = comment.charAt( i);
				if (Character.isWhitespace(ch)) {
					target = comment.substring(0, i);
					data = comment.substring(i+1);
					break;
				}
			}		
		}
		else {
			target = comment.substring(0, c);
			data = comment.substring(c+1);						
		}
			
		ProcessingInstruction pi = supplier.get();						
		pi.setTarget( target.trim()); // just drop whitespace arround the tag and it's ':'
		pi.setData( data.trim()); // data is just the remainder

		return pi;
	}
}
