// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.artifact.processing.part;

import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;

/**
 * @author peter.gazdik
 */
public interface KnownPartTuples {

	PartTuple jarType = PartTupleProcessor.create(PartType.JAR);
	PartTuple javadocType = PartTupleProcessor.create(PartType.JAVADOC);
	PartTuple metaType = PartTupleProcessor.create(PartType.META);
	PartTuple global_metaType = PartTupleProcessor.create(PartType.GLOBAL_META);
	PartTuple pomType = PartTupleProcessor.create(PartType.POM);
	PartTuple sourcesType = PartTupleProcessor.create(PartType.SOURCES);
	PartTuple projectType = PartTupleProcessor.create(PartType.PROJECT);
	PartTuple md5Type = PartTupleProcessor.create(PartType.MD5);
	PartTuple sha1Type = PartTupleProcessor.create(PartType.SHA1);
	PartTuple exclusionsType = PartTupleProcessor.create(PartType.EXCLUSIONS);
	PartTuple antType = PartTupleProcessor.create(PartType.ANT);
	PartTuple ascType = PartTupleProcessor.create(PartType.ASC);
	PartTuple modelType = PartTupleProcessor.create(PartType.MODEL);

}
