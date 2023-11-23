package com.braintribe.build.artifact.walk.multi.filters;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.PartTuple;

/**
 * a filter that filters dependencies (within the walker) according the rules
 * @author pit
 *
 */
public class TypeRuleFilter implements DependencyFilter{
	
	private static final String DEFAULT_TYPE_RULE = "JAR,POM";
	private static final String DEFAULT_TYPE = "jar";
	private String typeRule = DEFAULT_TYPE_RULE;
	private boolean initialized = false;
	private RuleFilterPassMode passMode = RuleFilterPassMode.ignore;
	private List<PartTuple> partTuples; 
	
	@Configurable
	public void setRule(String typeFilter) {
		this.typeRule = typeFilter;
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
				PartTuple partTuple = PartTuple.T.create();
				String [] parts = tuple.split(":");
				if (parts.length == 1) {
					if (tuple.endsWith(":")) {
						partTuple.setClassifier( parts[0]);
					}
					else {
						partTuple.setType( parts[0]);
					}
				}
				else {
					partTuple.setClassifier( parts[0]);
					partTuple.setType( parts[1]);
				}
				partTuples.add(partTuple);
			}
	}


	@Override
	public boolean filterDependency(Dependency dependency) {
		initializeRule();
		if (passMode == RuleFilterPassMode.ignore || partTuples == null) 
			return true;

		String classifier = dependency.getClassifier();
		String type = dependency.getType();
		if (type == null) {
			type = DEFAULT_TYPE;
		}
		
		for (PartTuple tuple : partTuples) {			
			String tClassifier = tuple.getClassifier();
			String tType = tuple.getType();
				
			if (tType != null) {	
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
