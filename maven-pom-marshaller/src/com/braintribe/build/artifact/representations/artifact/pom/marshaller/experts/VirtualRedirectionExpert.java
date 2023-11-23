package com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts;

import com.braintribe.model.artifact.Dependency;

public class VirtualRedirectionExpert extends AbstractProcessingInstructionExpert {
	public static void read(Dependency dependency, String piData) {
		String expression = piData;
			
		int startOfPartType = findNextOccurrence(expression, 0, nonWhitespacePredicate);
		
		if (startOfPartType == -1)
			throw new IllegalStateException("missing expected part type on <?redirect <key> <condensed dependency> ?> processing instruction");
		
		int endOfPartType = findNextOccurrence(expression, startOfPartType, whitespacePredicate);
		
		if (endOfPartType == -1) {
			throw new IllegalStateException("missing expected part type on <?redirect <key> <condensed dependency> ?> processing instruction");
		}
		String key = expression.substring(startOfPartType, endOfPartType);
		String value = expression.substring(endOfPartType+1);
		
		dependency.getRedirectionMap().put(key, value);
	}
}
