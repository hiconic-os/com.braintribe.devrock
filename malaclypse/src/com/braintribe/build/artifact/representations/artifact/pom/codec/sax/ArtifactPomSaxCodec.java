// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.codec.sax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.model.artifact.Solution;
import com.braintribe.utils.xml.parser.sax.SaxParser;
import com.braintribe.utils.xml.parser.sax.SaxParserException;
import com.braintribe.utils.xml.parser.sax.builder.SaxContext;
import com.braintribe.xml.parser.ContentHandler;

public class ArtifactPomSaxCodec {

	private SaxArtifactPomExpertRegistry registry;
	private ContentHandler<Solution> handler;
	private URL xsdUrl = ArtifactPomSaxCodec.class.getResource( "/maven-4.0.0.xsd");
	boolean validating = false;
	
	
	public void setValidating(boolean validating) {
		this.validating = validating;
	}
	@Configurable @Required
	public void setRegistry(SaxArtifactPomExpertRegistry registry) {
		this.registry = registry;
	}
	
	private SaxContext setup() {
		handler = new ContentHandler<Solution>();						
		return SaxParser.parse().setValidating(false).setHandler(handler);		
	}

	public synchronized Solution decode( File file) throws CodecException {	
		try {
			SaxContext saxContext = setup();
			handler.setRegistry(registry);	
			if (validating && xsdUrl != null) {		
				try (InputStream stream = xsdUrl.openStream())  { 
					saxContext.schema( stream).parse( file);
				} catch (IOException e) {
					// if this doesn't work, do it without validation
					;
				}
			}
			else {
				saxContext.parse( file);
			}
			Solution decodedValue = handler.getResult();
			return decodedValue;
		} catch (SaxParserException e) {
			throw new CodecException(e);
		}
	}
	
	public  synchronized Solution decode( InputStream stream) throws CodecException {
		try {
			SaxContext saxContext = setup();
			if (validating && xsdUrl != null) {		
				try (InputStream schema = xsdUrl.openStream())  { 
					saxContext.schema( schema).parse( stream);
				} catch (IOException e) {
					// if this doesn't work, do it without validation
					;
				}
			}
			else {
				saxContext.parse( stream);
			}			
			Solution decodedValue = handler.getResult();
			return decodedValue;
		} catch (SaxParserException e) {
			throw new CodecException(e);
		}	
	}
	
	public  synchronized Solution decode( String contents) throws CodecException{
		try {
			SaxContext saxContext = setup();
			if (validating && xsdUrl != null) {		
				try (InputStream stream = xsdUrl.openStream())  { 
					saxContext.schema( stream).parse( contents);
				} catch (IOException e) {
					// if this doesn't work, do it without validation
					;
				}
			}
			else {
				saxContext.parse( contents);
			}
			saxContext.parse( contents);
			Solution decodedValue = handler.getResult();
			return decodedValue;
		} catch (SaxParserException e) {
			throw new CodecException(e);
		}		
	}
}
