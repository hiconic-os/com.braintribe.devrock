// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet.generator;

import java.io.File;

import com.braintribe.cc.lcd.HashSupportWrapperCodec;


public class FileWrapperCodec extends HashSupportWrapperCodec<File> {

	@Override
	protected int entityHashCode(File e) {
		
		return e.getAbsolutePath().hashCode();
	}

	@Override
	protected boolean entityEquals(File e1, File e2) {
		return e1.getAbsolutePath().equals( e2.getAbsolutePath());
	}

	
}
