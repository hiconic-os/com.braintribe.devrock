// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.SimpleType;

public interface OptionParser {
	static <T extends GenericEntity> T parse(EntityType<T> entityType, String args[]) {
		List<String> remainingArguments = new ArrayList<>();
		
		T options = entityType.create();
		
		Function<String, Object> decoder = null;
		Consumer<Object> assigner = null; 
		for (int i = 0; i < args.length; i++) {
			String value = args[i];
			
			if (value.startsWith("-")) {
				String name = value.substring(0);
				Property currentProperty = entityType.findProperty(name);
				
				if (currentProperty != null) {
					GenericModelType propertyType = currentProperty.getType();
	
					switch (propertyType.getTypeCode()) {
						case booleanType:
						case decimalType:
						case doubleType:
						case enumType:
						case floatType:
						case integerType:
						case longType:
						case stringType:
							ScalarType scalarType = (SimpleType)propertyType;
							decoder = scalarType::instanceFromString;
							assigner = v -> currentProperty.set(options, v);
							break;
						case listType:
						case setType:
							LinearCollectionType linearCollectionType = (LinearCollectionType)propertyType;
							GenericModelType elementType = linearCollectionType.getCollectionElementType();
							if (elementType.isScalar()) {
								ScalarType scalarElementType = (ScalarType)elementType;
								decoder = scalarElementType::instanceFromString;
								Collection<Object> collection = currentProperty.get(options);
								assigner = collection::add;
								break;
							}
							//$FALL-THROUGH$
						default:
							assigner = null;
							decoder = null;
							break;
					}
				}
			}
			else {
				if (assigner != null) {
					Object v = decoder.apply(value);
					assigner.accept(v);
				}
				else {
					remainingArguments.add(value);
				}
			}
		}
		
		return options;
	}
}
