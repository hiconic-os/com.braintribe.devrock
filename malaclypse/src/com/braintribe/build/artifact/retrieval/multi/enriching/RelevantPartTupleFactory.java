// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.enriching;

import java.util.function.Supplier;

import com.braintribe.model.artifact.PartTuple;

/**
 * a factory to create the part tuples that are relevant for the solution enricher 
 * @author pit
 *
 */
public interface RelevantPartTupleFactory extends Supplier<PartTuple[]>{
	/**
	 * set the {@link PartTuple}s to use
	 * @param tuples - {@link PartTuple}s to override the standard
	 */
	void setRelevantPartTuples( PartTuple[] tuples);
	/**
	 * add these {@link PartTuple}s to ones set (if none, standard is used, see impl)
	 * @param tuples - the {@link PartTuple}s to add 
	 */
	void addRelevantPartTuples( PartTuple[] tuples);
	/**
	 * add a single {@link PartTuple} to the ones set (if none are set, standard is used, see impl)
	 * @param tuple - the {@link PartTuple} to add 
	 */
	void addRelevantPartTuple( PartTuple tuple);
}
