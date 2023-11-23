// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.enriching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;


/**
 * an implementation of the {@link RelevantPartTupleFactory} <br/>
 * standard tuples are : <br/>
 *  POM, JAR, SOURCES, JAVADOC, see {@link PartType}
 *  
 * @author pit
 *
 */
public class RelevantPartTupleFactoryImpl implements RelevantPartTupleFactory {
	private PartTuple [] relevantPartTuples;

	@Override
	public PartTuple[] get() throws RuntimeException {
		if (relevantPartTuples == null) {
			relevantPartTuples = createStandardPartTuples(); 
		}
		return relevantPartTuples;		
	}
	
	/**
	 * creates the standard tuples 	
	 */
	private PartTuple[] createStandardPartTuples() {
		return new PartTuple[] {
				PartTupleProcessor.createPomPartTuple(),
				PartTupleProcessor.createJarPartTuple(),
				PartTupleProcessor.create( PartType.JAVADOC),
				PartTupleProcessor.create( PartType.SOURCES),
				PartTupleProcessor.fromString("classes", "jar"), // in case such files exist
			  };			
	}

	@Override
	public void setRelevantPartTuples(PartTuple[] tuples) {
		relevantPartTuples = tuples;
		
	}

	@Override
	public void addRelevantPartTuples(PartTuple[] tuples) {
		List<PartTuple> mergedTuples = new ArrayList<PartTuple>();

		PartTuple[] standard = relevantPartTuples;
		if (standard == null) {
			mergedTuples.addAll(Arrays.asList(createStandardPartTuples()));
		}
		else {
			mergedTuples.addAll( Arrays.asList( relevantPartTuples));
		}
		mergedTuples.addAll( Arrays.asList( tuples));
		relevantPartTuples = mergedTuples.toArray( new PartTuple[0]);		
		
	}

	@Override
	public void addRelevantPartTuple(PartTuple tuple) {
		List<PartTuple> mergedTuples = new ArrayList<PartTuple>();

		PartTuple[] standard = relevantPartTuples;
		if (standard == null) {
			mergedTuples.addAll(Arrays.asList(createStandardPartTuples()));
		}
		else {
			mergedTuples.addAll( Arrays.asList( relevantPartTuples));
		}
		mergedTuples.add( tuple);
		relevantPartTuples = mergedTuples.toArray( new PartTuple[0]);						
	}
	

}
