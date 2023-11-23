// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt.testrun;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import com.braintribe.build.gwt.GwtModule;
import com.braintribe.build.gwt.ModuleScanner;

public class ModuleScannerTest {
	public static void main(String[] args) {
		try {
			ModuleScanner scanner = new ModuleScanner();
			scanner.setClasspath(Arrays.asList(new File("C:\\Java\\gwt-2.4.0\\gwt-user")));
			scanner.scanForModules();
			for (Map.Entry<String, GwtModule> entry: scanner.getModulesBySourcePackage().entries()) {
				System.out.println(entry.getKey() + " -> " + entry.getValue().getModuleName());
			}
			System.out.println("--------- super sources ---------------");
			
			for (Map.Entry<String, GwtModule> entry: scanner.getModulesBySuperSourcePackage().entries()) {
				System.out.println(entry.getKey() + " -> " + entry.getValue().getModuleName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
