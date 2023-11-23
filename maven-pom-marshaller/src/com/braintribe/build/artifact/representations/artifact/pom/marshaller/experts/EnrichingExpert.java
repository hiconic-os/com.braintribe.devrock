// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts;

import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulatorParser;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

public class EnrichingExpert extends AbstractProcessingInstructionExpert{
	private static MutableGmmlParserConfiguration configuration;

	public static void read(Dependency dependency, String piData) throws XMLStreamException{
		// inject first line into expression 
		String expression = piData;
		String globalId = UUID.randomUUID().toString();
		dependency.setGlobalId(globalId);
		String injected = "$entity=($entityType=" + dependency.entityType().getTypeSignature() + ")('"+ globalId + "')"; 
		expression = injected + "\n" + expression;
		
		// setup session 
		BasicManagedGmSession session = new BasicManagedGmSession();
		session.attach(dependency);		

		// lazy create configuration 
		if (configuration == null) {
			configuration = Gmml.configuration();
			//configuration.setParseSingleBlock(true);
			configuration.setLenient(true);
		}
		// call parser - who does something with the dependency in the session.. 
		try {
			ManipulatorParser.parse(expression, session, configuration);
		} catch (Exception e) {
			throw new XMLStreamException("ManipulationParser cannot process dependency's expression [" + expression + "]", e);
		}

	}

}
