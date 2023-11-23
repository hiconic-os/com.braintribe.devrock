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
package com.braintribe.model.artifact.processing.version;

import java.util.Comparator;
import java.util.List;

import com.braintribe.model.artifact.version.AlphaNumericVersionPart;
import com.braintribe.model.artifact.version.NumericVersionPart;

public class ListComparator implements Comparator<List<?>> {
	
	@Override
	public int compare(List<?> l1, List<?> l2) {
        int count1 = l1.size();
        int count2 = l2.size();
        int count = Math.min(count1, count2);
        
        for (int i = 0; i < count; i++) {
            Object o1 = l1.get(i);
            Object o2 = l2.get(i);
            
            if (o1 == o2) continue;
            else if (o1 == null) return -1;
            else if (o2 == null) return 1;
            else if (o1.getClass() == o2.getClass() && o1 instanceof Comparable<?> && o2 instanceof Comparable<?>) {
            	@SuppressWarnings("unchecked")
            	Comparable<Object> c1 = (Comparable<Object>)o1;
            	@SuppressWarnings("unchecked")
            	Comparable<Object> c2 = (Comparable<Object>)o2;
            	
            	int cmp = c1.compareTo(c2);
                if (cmp != 0) return cmp;
            }
            else {
            	if ((o1 instanceof AlphaNumericVersionPart) && (o2 instanceof AlphaNumericVersionPart)) {
            		String s1 = ((AlphaNumericVersionPart)o1).getValue();
                    String s2 = ((AlphaNumericVersionPart)o2).getValue();
                    int cmp = s1.compareTo(s2);
                    if (cmp != 0) return cmp;
            	}
            	if ((o1 instanceof NumericVersionPart) && (o2 instanceof NumericVersionPart)) {
            		Integer s1 = ((NumericVersionPart)o1).getValue();
                    Integer s2 = ((NumericVersionPart)o2).getValue();
                    int cmp = s1.compareTo(s2);
                    if (cmp != 0) return cmp;	
            	}
                
            }
        }
        return count1 - count2;
	}
}
