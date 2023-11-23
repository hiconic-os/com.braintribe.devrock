// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt.testrun;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import com.braintribe.build.gwt.AsmClassDepScanner;

public class AsmDepScannerTest {
	public static void main(String[] args) {
		
		try {
			Set<String> classes = new TreeSet<String>();
			
			File dir = new File("C:\\svn\\artifacts\\com\\braintribe\\gwt\\GwtWebGmRpcClient\\1.0\\bin\\com\\braintribe\\gwt\\gmrpc\\web\\client");
			
			for (File file: dir.listFiles()) {
				if (!Arrays.asList("..", ".").contains(file.getName())) {
					classes.addAll(AsmClassDepScanner.getClassDependencies(file));
				}
						
			}
			
			for (String clazz: classes) {
				System.out.println(clazz);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
