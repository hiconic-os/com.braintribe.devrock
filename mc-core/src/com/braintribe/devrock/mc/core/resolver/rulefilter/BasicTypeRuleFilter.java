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
package com.braintribe.devrock.mc.core.resolver.rulefilter;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.PartIdentification;

/**
 * a filter that filters dependencies (within the walker) according the rules
 * @author pit
 *
 */
public class BasicTypeRuleFilter implements TypeRuleFilter {
	
	private static final String DEFAULT_TYPE_RULE = "jar,pom";
	private static final String DEFAULT_TYPE = "jar";
	private String typeRule = DEFAULT_TYPE_RULE;
	private boolean initialized = false;
	private RuleFilterPassMode passMode = RuleFilterPassMode.ignore;
	private List<PartIdentification> partTuples; 
	
	public BasicTypeRuleFilter() {
	}
	
	public BasicTypeRuleFilter(String typeRule, RuleFilterPassMode passMode, List<PartIdentification> partTuples) {
		super();
		this.typeRule = typeRule;
		this.passMode = passMode;
		this.partTuples = partTuples;
		this.initialized = true;
	}


	@Configurable
	public void setRule(String typeFilter) {
		this.typeRule = typeFilter;
	}
	
	/**
	 * @param typeRule
	 * @return
	 */
	public static Maybe<BasicTypeRuleFilter> parse(String typeRule) {
		RuleFilterPassMode passMode;
		
		List<PartIdentification> partTuples = null;
		if (typeRule == null || typeRule.equalsIgnoreCase("*")) {
			passMode = RuleFilterPassMode.ignore;
		} else {
			passMode = RuleFilterPassMode.filter;
			partTuples = new ArrayList<>();
			String [] tuples = typeRule.split(",");
			for (String tuple : tuples) {
				PartIdentification pi = PartIdentification.parse(tuple);				
				partTuples.add(pi);
			}
		}
		
		return Maybe.complete(new BasicTypeRuleFilter(typeRule, passMode, partTuples));
	}
	
	private void initializeRule() {
			if (initialized) {
				return;			
			}
			initialized = true;
			
			if (typeRule == null || typeRule.equalsIgnoreCase("*")) {
				passMode = RuleFilterPassMode.ignore;
				return;
			}			
			passMode = RuleFilterPassMode.filter;
			
			partTuples = new ArrayList<>();
			String [] tuples = typeRule.split(",");
			for (String tuple : tuples) {
				PartIdentification pi = PartIdentification.parse(tuple);				
				partTuples.add(pi);
			}
	}


	@Override
	public boolean test(AnalysisDependency dependency) {
		initializeRule();
		if (passMode == RuleFilterPassMode.ignore || partTuples == null) 
			return true;

		String classifier = dependency.getClassifier();
		String type = dependency.getType();
		if (type == null) {
			type = DEFAULT_TYPE;
		}
		
		for (PartIdentification tuple : partTuples) {			
			String tClassifier = tuple.getClassifier();
			String tType = tuple.getType();
				
			if (tType != null && tType.length() > 0) {	
				if (type.equalsIgnoreCase( tType)) {
					// classifiers may be added 
					if (tClassifier != null) {
						// and if we expect one, it must match 
						if (classifier != null && classifier.equalsIgnoreCase(tClassifier))
							return true;
					}
					else {
						// no classifier expected
						return true;
					}
				}
			}
			else {
				// classifiers may be added 
				if (tClassifier != null) {
					// and if we expect one, it must match 
					if (classifier != null && classifier.equalsIgnoreCase(tClassifier))
						return true;
				}
				else {
					// no classifier expected
					return true;
				}
			}
				
			
		}		
		return false;				
	}
	
	

}
