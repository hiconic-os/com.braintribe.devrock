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
package com.braintribe.model.typescript.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.typescript.model.sub.TsSub;

/**
 * @author peter.gazdik
 */
public interface TsJoat extends TsSub, AbsenceInformation {

	EntityType<TsJoat> T = EntityTypes.T(TsJoat.class);

	Object getObject();
	void setObject(Object object);

	// ################################################
	// ## . . . . . . . . Primitives . . . . . . . . ##
	// ################################################

	int getPrimitiveInteger();
	void setPrimitiveInteger(int primitiveInteger);

	long getPrimitiveLong();
	void setPrimitiveLong(long primitiveLong);

	float getPrimitiveFloat();
	void setPrimitiveFloat(float primitiveFloat);

	double getPrimitiveDouble();
	void setPrimitiveDouble(double primitiveDouble);

	boolean getPrimitiveBoolean();
	void setPrimitiveBoolean(boolean primitiveBoolean);

	// ################################################
	// ## . . . . . . . . Wrappers . . . . . . . . . ##
	// ################################################

	Integer getWrapperInteger();
	void setWrapperInteger(Integer Integer);

	Long getWrapperLong();
	void setWrapperLong(Long _long);

	Float getWrapperFloat();
	void setWrapperFloat(Float _float);

	Double getWrapperDouble();
	void setWrapperDouble(Double _double);

	Boolean getWrapperBoolean();
	void setWrapperBoolean(Boolean _boolean);

	// ################################################
	// ## . . . . . . . Other Simple . . . . . . . . ##
	// ################################################

	String getString();
	void setString(String string);

	Date getDate();
	void setDate(Date date);

	BigDecimal getDecimal();
	void setDecimal(BigDecimal decimal);

	// ################################################
	// ## . . . . . . . . . Custom . . . . . . . . . ##
	// ################################################

	TsEnum getTsEnum();
	void setTsEnum(TsEnum tsEnum);

	TsJoat getEntity();
	void setEntity(TsJoat entity);

	TsSub getOtherNamespaceEntity();
	void setOtherNamespaceEntity(TsSub otherNamespaceEntity);

	// ################################################
	// ## . . . . . . . . Collections . . . . . . . .##
	// ################################################

	List<String> getListOfStrings();
	void setListOfStrings(List<String> listOfStrings);

	Set<String> getSetOfStrings();
	void setSetOfStrings(Set<String> setOfStrings);

	Map<String, String> getMapOfStrings();
	void setMapOfStrings(Map<String, String> mapOfStrings);

	// ################################################
	// ## . . . . . . . . . MetaData . . . . . . . . ##
	// ################################################

	@Mandatory
	@Unique
	String getEnriched();
	void setEnriched(String enriched);

	// ################################################
	// ## . . . . . . . . . Eval . . . . . . . . . . ##
	// ################################################

	EvalContext<? extends List<String>> eval(Evaluator<ServiceRequest> evaluator);
}
