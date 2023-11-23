// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.codec.stax.staged;

import java.io.File;
import java.io.InputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.model.artifact.Solution;
import com.braintribe.xml.stagedstax.parser.ContentHandler;
import com.braintribe.xml.stagedstax.parser.StagedStaxModelParser;

public class ArtifactPomStagedStaxCodec {

	private StagedStaxArtifactPomExpertRegistry registry;
	private ContentHandler<Solution> handler;
	private StagedStaxModelParser staxParser = new StagedStaxModelParser();

	@Configurable @Required
	public void setRegistry(StagedStaxArtifactPomExpertRegistry registry) {
		this.registry = registry;
	}
	


	public  Solution decode( File file) throws CodecException {	
		try {
			handler = new ContentHandler<>();
			handler.setRegistry(registry);	
			staxParser.read(file, handler);
			Solution decodedValue = handler.getResult();
			return decodedValue;
		} catch (Exception e) {
			throw new CodecException(e);
		}
	}
	
	public   Solution decode( InputStream stream) throws CodecException {
		try {
			handler = new ContentHandler<>();
			handler.setRegistry(registry);	
			staxParser.read(stream, handler);
			Solution decodedValue = handler.getResult();
			return decodedValue;
		} catch (Exception e) {
			throw new CodecException(e);
		}	
	}
	
	public   Solution decode( String contents) throws CodecException{
		try {
			handler = new ContentHandler<>();
			handler.setRegistry(registry);	
			staxParser.read( contents, handler);
			Solution decodedValue = handler.getResult();
			return decodedValue;
		} catch (Exception e) {
			throw new CodecException(e);
		}		
	}
}
