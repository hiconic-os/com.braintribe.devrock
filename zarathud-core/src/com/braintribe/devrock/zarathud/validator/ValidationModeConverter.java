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

import com.braintribe.model.denotation.zarathud.ValidationMode;

/**
 * converts the high level enum to low level values for binary mask checks
 * 
 * @author pit
 *
 */
public class ValidationModeConverter {
	public static int STANDARD = 0;
	public static int CONTAINMENT = 1;
	public static int MODEL = 2;
	public static int PERSISTENCE = 4;
	public static int QUICKCONTAINMENT = 8;

	public static int validationModeToBinary( ValidationMode mode) {
		switch (mode) {			
			case model : 
				return MODEL;
			case containment :
				return MODEL + CONTAINMENT;
			case persistence : 
				return MODEL + PERSISTENCE;
			case quickContainment:
				return MODEL + QUICKCONTAINMENT;
			default:
			case standard:
				return STANDARD;
		}
	}
}
