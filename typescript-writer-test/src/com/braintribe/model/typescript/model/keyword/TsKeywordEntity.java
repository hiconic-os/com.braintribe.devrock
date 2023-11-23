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
package com.braintribe.model.typescript.model.keyword;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.typescript.model.keyword.with.TsKeywordPackageEntity;

/**
 * @author peter.gazdik
 */
public interface TsKeywordEntity extends GenericEntity {

	EntityType<TsKeywordEntity> T = EntityTypes.T(TsKeywordEntity.class);

	TsKeywordPackageEntity getKeywordPackage();
	void setKeywordPackage(TsKeywordPackageEntity keywordPackage);

	String getArguments();
	void setArguments(String value);

	String getAwait();
	void setAwait(String value);

	String getBreak();
	void setBreak(String value);

	String getCase();
	void setCase(String value);

	String getCatch();
	void setCatch(String value);

	String getConst();
	void setConst(String value);

	String getContinue();
	void setContinue(String value);

	String getDebugger();
	void setDebugger(String value);

	String getDefault();
	void setDefault(String value);

	String getDelete();
	void setDelete(String value);

	String getDo();
	void setDo(String value);

	String getElse();
	void setElse(String value);

	String getEnum();
	void setEnum(String value);

	String getEval();
	void setEval(String value);

	String getExport();
	void setExport(String value);

	String getExtends();
	void setExtends(String value);

	String getFalse();
	void setFalse(String value);

	String getFinally();
	void setFinally(String value);

	String getFor();
	void setFor(String value);

	String getFunction();
	void setFunction(String value);

	String getIf();
	void setIf(String value);

	String getImplements();
	void setImplements(String value);

	String getImport();
	void setImport(String value);

	String getIn();
	void setIn(String value);

	String getInstanceof();
	void setInstanceof(String value);

	String getInterface();
	void setInterface(String value);

	String getLet();
	void setLet(String value);

	String getNew();
	void setNew(String value);

	String getNull();
	void setNull(String value);

	String getPackage();
	void setPackage(String value);

	String getPrivate();
	void setPrivate(String value);

	String getProtected();
	void setProtected(String value);

	String getPrototype();
	void setPrototype(String value);

	String getPublic();
	void setPublic(String value);

	String getReturn();
	void setReturn(String value);

	String getStatic();
	void setStatic(String value);

	String getSuper();
	void setSuper(String value);

	String getSwitch();
	void setSwitch(String value);

	String getThis();
	void setThis(String value);

	String getThrow();
	void setThrow(String value);

	String getTrue();
	void setTrue(String value);

	String getTry();
	void setTry(String value);

	String getTypeof();
	void setTypeof(String value);

	String getVar();
	void setVar(String value);

	String getVoid();
	void setVoid(String value);

	String getWhile();
	void setWhile(String value);

	String getWith();
	void setWith(String value);

	String getYield();
	void setYield(String value);

	String getYield_();
	void setYield_(String value);
}
