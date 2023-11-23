// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.VirtualPart;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;

public class VirtualPartExpert extends AbstractProcessingInstructionExpert {
	
	public static void read(Dependency dependency, String piData) {
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
		
		VirtualPart virtualPart = VirtualPart.T.create();
		virtualPart.setType( PartTupleProcessor.fromString(partType));
		virtualPart.setPayload(payload);
		
		dependency.getMetaData().add(virtualPart);
	}

}
