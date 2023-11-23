
package com.braintribe.build.artifact.walk.multi.filters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.artifact.Dependency;

/**
 * a simple tag rule parser and matcher 
 * 
 * @author pit
 *
 */
public class TagRuleFilter implements DependencyFilter{
	private static final String RULE_ALL = "*";
	private static final String RULE_NONE = "!*";
	
	private static final String NEGATION = "!";
	private static final String DELIM = ",";
	
	private static final String EXCEPTION = "classpath";
	
	private String rule;
	private Set<String> positives;
	private Set<String> negatives;
	
		
	private RuleFilterPassMode globalTagMode = RuleFilterPassMode.ignore;
	
	private boolean initialized = false;

	/**
	 * build the internal (hopefully faster) representation of the rule 
	 * @param rule - the rule as a string 
	 */
	@Configurable
	public void setRule(String rule) {
		this.rule = rule;
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
	 * filter a single dependency 
	 * @param dependency - the {@link Dependency} to filter
	 * @return - true if it passes the filter, false otherwise 
	 */
	public boolean filterDependency(Dependency dependency) {
		// no rule set: anything matches 
		initializeRule();
		
		List<String> tags = dependency.getTags();
		
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
	
	
	
}
