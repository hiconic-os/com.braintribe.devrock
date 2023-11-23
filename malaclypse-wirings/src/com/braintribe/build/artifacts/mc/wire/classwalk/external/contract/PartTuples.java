package com.braintribe.build.artifacts.mc.wire.classwalk.external.contract;

import java.lang.reflect.Array;

import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;

/**
 * helper to determine the 'usual suspects when it comes to part tuples' 
 * 
 * @author pit
 *
 */
public class PartTuples {
	
	/**
	 * exposes the standard part tuples which are mostly ok for classpath walks
	 * @return - an {@link Array} of {@link PartTuple}
	 */
	public static PartTuple [] standardPartTuples() {
		return new PartTuple[] {
				PartTupleProcessor.createPomPartTuple(), // pom
				PartTupleProcessor.createJarPartTuple(), // jar
				PartTupleProcessor.create( PartType.JAVADOC), // javadoc in its variation
				PartTupleProcessor.create( PartType.SOURCES), // sources 
				PartTupleProcessor.fromString("classes", "jar"), // in case such files exist
			  };			
	}
}
