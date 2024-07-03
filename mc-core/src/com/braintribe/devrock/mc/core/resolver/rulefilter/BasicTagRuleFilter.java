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
package com.braintribe.devrock.mc.core.resolver.rulefilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.mc.api.transitive.ArtifactPathElement;
import com.braintribe.devrock.mc.api.transitive.DependencyPathElement;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisDependency;

/**
 * a simple tag rule parser and matcher 
 * 
 * @author pit
 *
 */

public class BasicTagRuleFilter implements TagRuleFilter {
	public static final String RULE_ALL = "*";
	public static final String RULE_NONE = "!*";
	
	public static final String NEGATION = "!";
	public static final String DELIM = ",";
	
	private static final String EXCEPTION = "classpath";
	
	private String rule;
	private Set<String> positives;
	private Set<String> negatives;
	
		
	private RuleFilterPassMode globalTagMode = RuleFilterPassMode.ignore;
	
	private boolean initialized = false;
	
	public BasicTagRuleFilter() {
		
	}

	public BasicTagRuleFilter(String rule, Set<String> positives, Set<String> negatives, RuleFilterPassMode globalTagMode) {
		super();
		this.rule = rule;
		this.positives = positives;
		this.negatives = negatives;
		this.globalTagMode = globalTagMode;
		this.initialized = true;
	}


	/**
	 * build the internal (hopefully faster) representation of the rule 
	 * @param rule - the rule as a string 
	 */
	@Configurable
	public void setRule(String rule) {
		this.rule = rule;
	}
	
	
	public static Maybe<BasicTagRuleFilter> parse(String rule) {
		RuleFilterPassMode globalTagMode;
		Set<String> positives = null;
		Set<String> negatives = null;
		
		if (rule.equalsIgnoreCase(RULE_ALL)) {
			globalTagMode = RuleFilterPassMode.all;
		}
		else if (rule.equalsIgnoreCase(RULE_NONE)) {
			globalTagMode = RuleFilterPassMode.none;
		}
		else {
			globalTagMode = RuleFilterPassMode.filter;
			positives = new HashSet<>();
			negatives = new HashSet<>();
			String [] tags = rule.split(DELIM);
			for (String tag : tags) {
				if (tag.startsWith(NEGATION)) 
					negatives.add(tag.substring(1));
				else
					positives.add(tag);
			}				
		}

		return Maybe.complete(new BasicTagRuleFilter(rule, positives, negatives, globalTagMode));
	}
	
	private void initializeRule() {
		if (initialized) {
			return;			
		}
		initialized = true;
		if (rule == null) {
			globalTagMode = RuleFilterPassMode.ignore;
			return;
		}
		if (rule.equalsIgnoreCase(RULE_ALL)) {
			globalTagMode = RuleFilterPassMode.all;
			return;
		}
		else if (rule.equalsIgnoreCase(RULE_NONE)) {
			globalTagMode = RuleFilterPassMode.none;
			return;
		}
		globalTagMode = RuleFilterPassMode.filter;
		
		positives = new HashSet<>();
		negatives = new HashSet<>();
		String [] tags = rule.split(DELIM);
		for (String tag : tags) {
			if (tag.startsWith(NEGATION)) 
				negatives.add(tag.substring(1));
			else
				positives.add(tag);
		}				
	}
	
	/**
	 * a helper to call a filter on the dependencies - not used by the framework
	 * @param dependencies
	 * @return
	 */
	public List<AnalysisDependency> filter( List<AnalysisDependency> dependencies) {	
		List<AnalysisDependency> filteredList = new ArrayList<>( dependencies); 
		Iterator<AnalysisDependency> iterator = filteredList.iterator();
		while (iterator.hasNext()) {
			AnalysisDependency dependency = iterator.next();
			if (!test( dependency)) {
				iterator.remove();
			}
		}
		return filteredList;
	}
	
	/**
	 * filter a single dependency - matches the interface for a dependency filter anywhere in the traversion
	 * @param dependency - the {@link AnalysisDependency} to filter
	 * @return - true if it passes the filter, false otherwise 
	 */
	public boolean test(AnalysisDependency dependency) {
		// no rule set: anything matches 
		initializeRule();
		
		Set<String> tags = dependency.getOrigin().getTags();
		
		int tagSize = tags.size();
		switch (globalTagMode) {
			case all: // any dependencies with tags pass, dependencies without don't pass
				return tagSize == 0 ? false : true;
			case none: // no dependencies with tags pass, dependencies without do pass, unless it contains a special tag, then that's fine too
				return tagSize > 0 && !tags.contains( EXCEPTION) ? false : true;
			case ignore: // everthing passes 
				return true;
			case filter:
			default:
				break;			
		}
		// check on positive listing.. 
		// if any are listed, *only* dependencies with matching tags are allowed, 
		// and no tags means fail.
		if (positives != null && positives.size() > 0) {
			if (tagSize == 0)
				return false;		
			if (positives.stream().filter( t -> {return !tags.contains(t);}).findFirst().isPresent()) {
				return false;
			}
		}
		// check on negatives
		// a dependency with no tag are allowed, others may not match the list.
		if (tagSize == 0 || negatives == null)
			return true;		
		return !tags.stream().filter( t -> {return negatives.contains(t);}).findFirst().isPresent();		
	}
		
	/**
	 * filter a dependency ONLY when a direct dependency to the terminal
	 * @param dpe - the {@link DependencyPathElement}
	 * @return - true if it passes the filter, false otherwise 
	 */
	public boolean test(DependencyPathElement dpe) {
		//System.out.println( dpe.asPathString());
		// determine if we're a dependency of the terminal 
		ArtifactPathElement artifactPathElement = dpe.getParent();
		DependencyPathElement parent = artifactPathElement.getParent();
		// terminal has no dependency pointing to it
		if (parent == null || parent.getParent() == null) { 
			AnalysisDependency dependency = dpe.getDependency();
			return test( dependency);
		}
		else {
			return true;
		}		
	}
}
