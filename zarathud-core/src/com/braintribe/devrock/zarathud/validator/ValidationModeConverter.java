// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
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
