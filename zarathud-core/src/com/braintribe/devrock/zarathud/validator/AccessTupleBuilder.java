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
package com.braintribe.devrock.zarathud.validator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.braintribe.devrock.zarathud.ZarathudException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.MethodEntity;

/**
 * expert for the {@link AccessTuple}
 * 
 * @author pit
 *
 */
public class AccessTupleBuilder {
	
	/**
	 * checks whether a {@link MethodEntity} is assigned to an {@link AccessTuple}
	 * @param tuples - the {@link Set} of {@link AccessTuple} to scan 
	 * @param method - the {@link MethodEntity}
	 * @return - true if it's present in an {@link AccessTuple} or false if not
	 */
	private static boolean isPresent(Set<AccessTuple> tuples, MethodEntity method) {
		if (tuples == null || tuples.size() == 0)
			return false;
		for (AccessTuple tuple : tuples) {
			if (tuple.getGetter() == method || tuple.getSetter() == method)
				return true;			
		}
		return false;
	}
	
	/**
	 * extract all {@link MethodEntity} that could be assigned to {@link AccessTuple} - because their no getter/setters 
	 * @param tuples - the {@link Set} of {@link AccessTuple} created while acquiring 
	 * @param methods - a {@link Set} of {@link MethodEntity}, all from an {@link GenericEntity}
	 * @return - the {@link Set} of unassigned {@link MethodEntity}
	 */
	public static Set<MethodEntity> getUnassignedMethods( Set<AccessTuple> tuples, Set<MethodEntity> methods) {
		Set<MethodEntity> unAssignedMethods = new HashSet<MethodEntity>();
		if (methods == null)
			return unAssignedMethods;
		for (MethodEntity method : methods)  {
			if (isPresent(tuples, method) == false) {
				unAssignedMethods.add( method);
			}		
		}
		return unAssignedMethods;
	}
	
	/**
	 * acquires an {@link AccessTuple} for the {@link MethodEntity}, either by assigning a method to a matching {@link AccessTuple} or creating a new {@link AccessTuple}
	 * @param tuples - the growing {@link Set} of {@link AccessTuple}
	 * @param method - the {@link MethodEntity} to integrate to the {@link AccessTuple}
	 * @return - the {@link AccessTuple} assigned or created
	 */
	private static AccessTuple acquireAccessTuple( Set<AccessTuple> tuples, MethodEntity method) {
		if (tuples != null && tuples.size() > 0) {		
			for (AccessTuple tuple :tuples) {
				Boolean assigned = tuple.assign(method);
				if (assigned == true) {
					return tuple;
				}			
				if (assigned == null)
					return null;
			}
		}
		AccessTuple tuple = new AccessTuple();
		Boolean assigned = tuple.assign(method);
		if (assigned == null)
			return null;
		tuples.add( tuple);
		return tuple;
	}
	
	/**
	 * takes a set of {@link MethodEntity} and structures it into {@link AccessTuple} if possible  
	 * @param methods - a {@link Set} of {@link MethodEntity}, from a {@link AbstractClassEntity}
	 * @return - the {@link Set} of {@link AccessTuple} that were built from the methods 
	 * @throws ZarathudException - if any thing goes wrong
	 */
	@SuppressWarnings("unused")
	public static Set<AccessTuple> tuplizeMethods( Set<MethodEntity> methods) throws ZarathudException {
		Set<AccessTuple> result = new HashSet<AccessTuple>();
		if (methods == null)
			return result;		
		Iterator<MethodEntity> iterator = methods.iterator();
		while (iterator.hasNext()) {
			MethodEntity entity = iterator.next();
			acquireAccessTuple(result, entity);			
		}
		return result;
	}
	
}
