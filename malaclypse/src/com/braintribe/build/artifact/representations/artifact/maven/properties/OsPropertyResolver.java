package com.braintribe.build.artifact.representations.artifact.maven.properties;

public interface OsPropertyResolver {
	 static final String DETECTED_NAME = "os.detected.name";
	 static final String DETECTED_ARCH = "os.detected.arch";
	 static final String DETECTED_VERSION = "os.detected.version";
	 static final String DETECTED_VERSION_MAJOR = DETECTED_VERSION + ".major";
	 static final String DETECTED_VERSION_MINOR = DETECTED_VERSION + ".minor";
	 static final String DETECTED_CLASSIFIER = "os.detected.classifier";
	 	 
	String expand( String variable);	
}
