// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.codec.stax.staged;

import java.util.function.Predicate;

import org.xml.sax.Attributes;

import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.VirtualPart;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.xml.stagedstax.parser.experts.AbstractContentExpert;
import com.braintribe.xml.stagedstax.parser.experts.ContentExpert;

/**
 * adds support for {@link VirtualPart} on {@link Dependency} declaration,
 * format is a follows:
 * <pre>{@code
 * 		<?part asset:man
 * 			$natureType = com.braintribe.model.asset.natures.ModelInitializer
 * 		?>
 * }</pre>
 * @author pit
 *
 */
public class VirtualPartExpert extends AbstractContentExpert implements ContentExpert {
	private static Logger log = Logger.getLogger(VirtualPartExpert.class);
	private GenericEntity parentEntity;

	@Override
	public void startElement(ContentExpert parent, String uri, String localName, String qName, Attributes atts)  {
		parentEntity = parent.getInstance();
	
	}

	private int findNextOccurrence(String expression, int startIndex, Predicate<Character> predicate) {
		int len = expression.length();
		for (int i = startIndex; i < len; i++) {
			char c = expression.charAt(i);
			if (predicate.test(c))
				return i;
		}
		
		return -1;
	}
	
	@Override
	public void endElement(ContentExpert parent, String uri, String localName, String qName) {
	
		// inject first line into expression 
		String expression = buffer.toString();
	
		Predicate<Character> whitespacePredicate = Character::isWhitespace;
		Predicate<Character> nonWhitespacePredicate = whitespacePredicate.negate();
		
		int startOfPartType = findNextOccurrence(expression, 0, nonWhitespacePredicate);
		
		if (startOfPartType == -1)
			throw new IllegalStateException("missing expected part type on <?part classifier:type payload ?> processing instruction");
		
		int endOfPartType = findNextOccurrence(expression, startOfPartType, whitespacePredicate);
		
		String partType, payload;
		
		if (endOfPartType == -1) {
			partType = expression.substring(startOfPartType);
			payload = "";
		}
		else {
			partType = expression.substring(startOfPartType, endOfPartType);
			payload = expression.substring(endOfPartType + 1);
		}
		
		VirtualPart virtualPart = VirtualPart.T.create();
		virtualPart.setType( PartTupleProcessor.fromString(partType));
		virtualPart.setPayload(payload);
		
		if (parentEntity instanceof HasMetaData) {
			HasMetaData hasMetaData = (HasMetaData) parentEntity;
			hasMetaData.getMetaData().add( virtualPart);
		}
		else {
			String msg = "entity is of type[" + parentEntity.getClass().getName() + "] which has the part processing instruction attached doesn derive from  [" + HasMetaData.class.getName() + "]";
			log.error( msg);
			throw new IllegalStateException(msg);
		}
	}

	@Override
	public void attach(ContentExpert child) {	
	}

	@Override
	public Object getPayload() {
		return null;
	}

	@Override
	public GenericEntity getInstance() {	
		return null;
	}

	@Override
	public EntityType<GenericEntity> getType() {
		return null;
	}
	
	


}
