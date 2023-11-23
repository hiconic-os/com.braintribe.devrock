// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt.testrun;

import java.util.Collection;

import com.braintribe.build.gwt.AsmClassDepScanner;
import com.braintribe.build.gwt.AsmClassDepScanner.ClassFromSignatureExtractor;

/**
 * 
 */
public class ClassFromSignatureExtractorTest {

	static ClassFromSignatureExtractor extractor = new AsmClassDepScanner.ClassFromSignatureExtractor();

	public static void main(String[] args) {
		run("Ljava/lang/Object;");
		run("Ljava/lang/Map<Ljava/lang/Object;Ljava/lang/String;>;");
		run("Ljava/lang/Map<Ljava/lang/Object;Ljava/lang/String;>;");
		run("Ljava/lang/Map<Ljava/lang/Object;Ljava/lang/String;>;BZLjava/lang/Integer;");
		run("Ljava/lang/Map<Ljava/util/HashMap<Ljava/lang/NestedKey;Ljava/lang/NestedValue;>;Ljava/lang/String;>;");
		run("Ljava/lang/Map<Ljava/lang/Object;Ljava/lang/String;>.Entry<Lentry/Key;Lentry/Value;>;B");
		
		run("Lcom/braintribe/model/processing/session/impl/managed/merging/ContinuableMerger<TM;>.ListVisitor<Ljava/lang/Object;>;");
	}

	private static void run(String internalName) {
		Collection<String> extractClasses = extractor.extractClasses(internalName);

		System.out.println("##############");
		System.out.println(internalName);
		for (String s: extractClasses)
			System.out.println(s);

		System.out.println("");
	}
}
