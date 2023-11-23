// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.launcher.gradle;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.build.commons.launcher.BuildException;
import com.braintribe.logging.Logger;

public class CommandLauncher {

	private static Logger log = Logger.getLogger(CommandLauncher.class);

	public static void main(String[] args) throws BuildException {
		log.info("Launching " + CommandLauncher.class.getName() + " with: " + Stream.of(args).collect(Collectors.joining(" ")));
		com.braintribe.build.commons.launcher.CommandLauncher.main(args);
	}
}
