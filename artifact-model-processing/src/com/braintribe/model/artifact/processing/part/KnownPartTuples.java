// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
