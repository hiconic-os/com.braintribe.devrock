package com.braintribe.build.artifact.retrieval.multi.coding;

import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;

public class PartTupleWrapperCodec extends HashSupportWrapperCodec<PartTuple>{
	
	public PartTupleWrapperCodec() {
		super( true);
	}

	@Override
	protected int entityHashCode(PartTuple e) {
		return PartTupleProcessor.toString(e).hashCode();
	}

	@Override
	protected boolean entityEquals(PartTuple e1, PartTuple e2) {		
		return PartTupleProcessor.equals(e1, e2);
	}

	
}
