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
package com.braintribe.devrock.mc.core.declared.group;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.declared.DeclaredGroupExtractionContext;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.devrock.model.mc.reason.InvalidGroupReferenceDependencyVersionType;
import com.braintribe.devrock.model.mc.reason.InvalidParentReference;
import com.braintribe.devrock.model.mc.reason.MalformedDependency;
import com.braintribe.devrock.model.mc.reason.MalformedGroupReason;
import com.braintribe.devrock.model.mc.reason.PomValidationReason;
import com.braintribe.devrock.model.mc.reason.UnresolvedProperty;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.artifact.declared.DeclaredGroup;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionRange;

/**
 * looks at the sources of a group and extracts some info about it: the members and references to other groups, 
 * see {@link DeclaredGroup}
 * 
 * following assumptions: 
 * only one parent level (no grand parents)
 * only one parent per group
 * properties only in current pom and the parent.
 *  
 * @author pit
 *
 */
public class DeclaredGroupExtractor {
	private static Logger log = Logger.getLogger(DeclaredGroupExtractor.class);
	private Reason umbrellaReason;
	private List<Pair<String,Map<String,String>>> mgtDependencies;
	
	public Maybe<DeclaredGroup> extractGroup( DeclaredGroupExtractionContext context) {
		mgtDependencies = new ArrayList<>(100);
		File groupDirectory = new File(context.getGroupLocation());
		
		if (!groupDirectory.exists() || !groupDirectory.isDirectory()) {
			String msg = "passed location [" + context.getGroupLocation() + "] either doesn't exist or is no directory";
			log.error( msg);
			return Reasons.build( NotFound.T).text( msg).toMaybe();
		}
		
		// enumerate all artifacts 
		File [] artifactDirectories = groupDirectory.listFiles( new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				// filter hidden directories, i.e. '.git'
				if (pathname.getName().startsWith("."))
					return false;

				if (pathname.isDirectory())
					return true;
				return false;
			}
		});
		
		if (artifactDirectories == null || artifactDirectories.length == 0) {			
			String msg = "passed location [" + context.getGroupLocation() + "] has no sub directories, i.e. no member artifacts";
			log.error(msg);
			return Reasons.build( NotFound.T).text( msg).toMaybe();	
		}
		
		// prep return value
		DeclaredGroup declaredGroup = DeclaredGroup.T.create();
		declaredGroup.setLocation( context.getGroupLocation());
		
		List<DeclaredArtifact> members = new ArrayList<>( artifactDirectories.length);
		
		// load all poms into DeclaredArtifacts
		DeclaredArtifactMarshaller marshaller = new DeclaredArtifactMarshaller();
		for (File artifactDirectory : artifactDirectories) {
			File pomFile = new File( artifactDirectory, "pom.xml");
			if (pomFile.exists()) {
				Maybe<DeclaredArtifact> declaredArtifactMaybe = DeclaredArtifactIdentificationExtractor.readArtifact(() -> new FileInputStream(pomFile) , pomFile.getAbsolutePath());
				if (declaredArtifactMaybe.isUnsatisfied()) {
					acquireUmbrellaReason(context).getReasons().add(declaredArtifactMaybe.whyUnsatisfied());
					continue;
				}
					
				DeclaredArtifact declaredArtifact = declaredArtifactMaybe.get();
				members.add(declaredArtifact);
			}
		}
		
		// member count
		declaredGroup.setMemberCount( members.size());
		
		// find parent
		DeclaredArtifact groupParent = null;
		for (DeclaredArtifact da : members) {
			if (da.getArtifactId().equals("parent")) {				
				groupParent = da;
				// transfer the groupId
				declaredGroup.setGroupId( da.getGroupId());
				
				// transfer the major / minor of the parent 
				Maybe<Pair<Integer, Integer>> versionMaybe = extractVersionFromParent( da);				
				if (versionMaybe.isSatisfied()) {
					Pair<Integer, Integer> versionPair = versionMaybe.get();
					declaredGroup.setMajor( versionPair.first);
					declaredGroup.setMinor( versionPair.second);
				}
				else {
					String msg = "cannot determine version of parent [" + da.getResource().getName() + "] as [" + versionMaybe.whyUnsatisfied().stringify();
					log.error(msg);
					Reason failReason = Reasons.build( InvalidParentReference.T).text(msg).toReason();
					acquireUmbrellaReason(context).getReasons().add(failReason);			
				}			
				break;
			}
		}		
		// no parent -> no continue
		if (groupParent == null) {
			String msg = "group in  [" + context.getGroupLocation() + "] has no group-parent named 'parent'";
			log.error(msg);
			Reason failReason = Reasons.build( MalformedGroupReason.T).text( msg).toReason();
			acquireUmbrellaReason(context).getReasons().add(failReason);
			return Maybe.incomplete( declaredGroup, umbrellaReason);	
		}
		
		Map<String,String> parentProperties = groupParent.getProperties();
		
		// extract group dependencies
		Map<String,List<DeclaredDependency>> groupDependencies = new HashMap<>();		
		for (DeclaredArtifact da : members) {
			Map<String,String> mgtDepVersionMap = new HashMap<>();
			
			List<DeclaredDependency> dependencies = da.getDependencies();
			for (DeclaredDependency dependency : dependencies) {
				
				// resolve
				String ve = dependency.getVersion();
				String groupId = dependency.getGroupId();
				
				// exclusion filter has precedence (default filter via context is negate all - passthru)
				if (context.exclusionFilter().test( groupId)) {
					log.info("excluded group [" + groupId + "] as it's excluded by exclusion filter");
					continue;
				}
				// inclusion filter comes next (default filter via context is allow all - passthru)
				if (!context.inclusionFilter().test(groupId))  {
					log.info("excluded group [" + groupId + "] as it's not included by inclusion filter");
					continue;
				}
				
				if (ve == null) {
					// dept mgt section
					Maybe<String> versionPerManaged = resolveDependencyVersionPerDependencyManagement( groupParent, dependency, da);
					if (versionPerManaged.isSatisfied()) {
						ve = versionPerManaged.get();
					mgtDepVersionMap.put(groupId + ":" + dependency.getArtifactId(), ve);
						//mgtDepVersionMap.put(dependency.asString(), ve);
					}
					else {
						String msg = "dependency [" + dependency.asString() + "] in [" + da.asString() + "] is incomplete";
						log.warn(msg);
						Reason dependencyProcessFailureReason = Reasons.build( MalformedDependency.T) //
								.text(msg) //
								.cause(versionPerManaged.whyUnsatisfied()).toReason(); //
							acquireUmbrellaReason(context).getReasons().add(dependencyProcessFailureReason);							
							continue;		
					}
				}
				Maybe<String> resolved = resolve( ve, parentProperties, da.getProperties());
				if (!resolved.isSatisfied()) {
					Reason dependencyProcessFailureReason = Reasons.build( MalformedDependency.T) //
																.text("dependency [" + dependency.asString() + "] in [" + da.asString() + "] is incomplete") //
																.cause(resolved.whyUnsatisfied()).toReason(); //
					acquireUmbrellaReason(context).getReasons().add(dependencyProcessFailureReason);
					continue;
				}
				dependency.setVersion(resolved.get());
				
				List<DeclaredDependency> dependenciesOfGroup = groupDependencies.computeIfAbsent( groupId, k -> new ArrayList<>());
				dependenciesOfGroup.add(dependency);				
			}			
			// 
			mgtDependencies.add( Pair.of( da.asString(), mgtDepVersionMap));
		}	
		context.setManagementDependentDependencies(mgtDependencies);
						
		// collate all dependencies to a group into a single one (merged? honking if no match?)
		Map<String,String> groupVersionsMap = new HashMap<>();
		for (Map.Entry<String, List<DeclaredDependency>> entry : groupDependencies.entrySet()) {
			String groupId = entry.getKey();
			List<DeclaredDependency> dependencies = entry.getValue();
			VersionExpression commonVersionExpression = null;
			
			for (DeclaredDependency dependency : dependencies) {
				String actualVersion = dependency.getVersion();
				// filter versions that couldn't be properly resolved -> error is already in umbrella
				if (actualVersion.contains("$")) {
					continue;
				}
				// extract version 
				VersionExpression versionExpression = VersionExpression.parse(actualVersion);
				// compare it to the common version expression
				if (commonVersionExpression == null) {
					commonVersionExpression = versionExpression;
				}
				else {					
					String commonAsString = commonVersionExpression.asString();
					String parsedAsString = versionExpression.asString();
					// check if matches
					if (!commonAsString.equals( parsedAsString)) {
						// reason: differing access..
						String msg = "group [" + groupId + "] is accessed by members of [" + declaredGroup.getGroupId() + "] in differing version expressions : " + commonAsString + " vs " + parsedAsString + ", retaining first";
						log.warn( msg);
						Reason diveringGroupVersions = Reasons.build(MalformedGroupReason.T)
														.text( msg)
														.toReason();
						acquireUmbrellaReason(context).getReasons().add(diveringGroupVersions);
					}					
				}				
			}
			groupVersionsMap.put(groupId, commonVersionExpression.asString());
		}

		//
		// post process
		//
		
		// if no self-references are wanted, just remove them 
		if (!context.includeSelfreferences()) {
			groupVersionsMap.remove( declaredGroup.getGroupId());
		}
		
		
		// include parent
		if (context.includeParent()) {
			declaredGroup.setGroupParent(groupParent);
		}
		// include members
		if (context.includeMembers())  {
			if (context.sort()) {
				members.sort( new Comparator<DeclaredArtifact>() {
					@Override
					public int compare(DeclaredArtifact o1, DeclaredArtifact o2) {					
						return o1.compareTo(o2);
					}					
				});
			}
			declaredGroup.setMembers(members);			
		}
		
		
		// enforce ranges 
		if (context.enforceRanges()) {
			for (Map.Entry<String, String> entry : groupVersionsMap.entrySet()) {
				String groupId = entry.getKey();
				String groupVersionExpression = entry.getValue();
				VersionExpression versionExpression = VersionExpression.parse(groupVersionExpression);
				if (
						versionExpression instanceof VersionRange == false && 
						versionExpression instanceof FuzzyVersion == false
					) {
					Reason reason = TemplateReasons.build(InvalidGroupReferenceDependencyVersionType.T) //
							.assign( InvalidGroupReferenceDependencyVersionType::setGroupId, groupId) //
							.assign( InvalidGroupReferenceDependencyVersionType::setVersionExpression, versionExpression) //
							.toReason();
					acquireUmbrellaReason(context).getReasons().add(reason);
				}
			}
		}
		
		// simplify range to lower border
		if (context.simplifyRangeToLowerBoundary()) {
			for (Map.Entry<String, String> entry : groupVersionsMap.entrySet()) {				
				String groupVersionExpression = entry.getValue();
				VersionExpression versionExpression = VersionExpression.parse(groupVersionExpression);
				if (versionExpression instanceof VersionRange) {
					VersionRange range = (VersionRange) versionExpression;
					entry.setValue( range.getLowerBound().asString());
				}
				else if (versionExpression instanceof FuzzyVersion) {
					FuzzyVersion fuzzy = (FuzzyVersion) versionExpression;
					entry.setValue( fuzzy.getMajor() + "." + fuzzy.getMinor());
				}
			}
		}
		
		// sort result
		if (context.sort()) {
			LinkedHashMap<String, String> result = new LinkedHashMap<>();
			List<String> sortedKeys = new ArrayList<>( groupVersionsMap.keySet());
			sortedKeys.sort( String::compareTo);
			for( String key : sortedKeys) {
				result.put( key, groupVersionsMap.get(key));
			}
			declaredGroup.setGroupDependencies(result);
		}
		else {
			declaredGroup.setGroupDependencies(groupVersionsMap);
		}
		
		
		// validate 
		if (umbrellaReason != null) {
			declaredGroup.setFailure(umbrellaReason);
			return Maybe.incomplete( declaredGroup, umbrellaReason);
		}
		
		return Maybe.complete( declaredGroup);
	}

	private Maybe<Pair<Integer, Integer>> extractVersionFromParent(DeclaredArtifact da) {
		String ve = da.getVersion();
		Maybe<String> resolved = resolve( ve, new HashMap<>(), da.getProperties());
		if (resolved.isSatisfied()) {
			Version version = Version.parse( resolved.get());
			return Maybe.complete( Pair.of( version.getMajor(), version.getMinor()));
		}
		else {
			return Maybe.empty( resolved.whyUnsatisfied());
		}	
	}

	private Maybe<String> resolveDependencyVersionPerDependencyManagement(DeclaredArtifact groupParent, DeclaredDependency dependency, DeclaredArtifact owner) {
		if (groupParent.getManagedDependencies().size() == 0) {
			String msg = "parent [" + groupParent.asString() + "] has no managed deps, but [" + dependency.asString() + "] in [" + owner.asString() + "] requires it";
			log.warn(msg);
			return Maybe.empty( Reasons.build(NotFound.T).text(msg).toReason());
		}
		for (DeclaredDependency managed : groupParent.getManagedDependencies()) {
			if ( matches( dependency, managed)) {
				return Maybe.complete( managed.getVersion());
			}				
		}
		
		String msg = "parent [" + groupParent.asString() + "] has no matching managed deps, for [" + dependency.asString() + "] in [" + owner.asString() + "]";
		log.warn(msg);
		return Maybe.empty( Reasons.build(NotFound.T).text(msg).toReason());
	}

	private boolean matches(DeclaredDependency dependency, DeclaredDependency managed) {
		String dG = dependency.getGroupId(); 
		String mG = managed.getGroupId();
		String dA = dependency.getArtifactId();
		String mA = dependency.getArtifactId();
		
		String dC = dependency.getClassifier();
		String mC = managed.getClassifier();
		
		String dT = dependency.getType();
		String mT = managed.getType();
		
		// group & artifact id must match
		if (!dG.equals( mG) || !dA.equals(mA) )
			return false;
		
		// classifier must also match if present (or not)
		if (dC != null) {
			if (mC == null)
				return false;
			if (!dC.equals(mC))
				return false;			
		}
		else if (mC != null){
			return false;							
		}
		// types must also match if present (or not) 
		if (dT != null) {
			if (mT == null)
				return false;
			if (!dT.equals(mT))
				return false;			
		}
		else if (mT != null){
			return false;							
		}
		
		return true;
	}

	/**
	 * just checks if the expression contains ${..} somehow
	 * @param expression - the string to check 
	 * @return - true if a variable reference is in the string and false otherwise 
	 */
	protected boolean requiresEvaluation(String expression) {
		String extract = extract(expression);
		return !extract.equalsIgnoreCase(expression);
	}

	/**
	 * extracts the first variable in the expression 
	 * @param expression - the {@link String} to extract the variable from 
	 * @return - the first variable (minus the ${..} stuff)
	 */
	protected String extract(String expression) {
		int p = expression.indexOf( "${");
		if (p < 0)
			return expression;
		int q = expression.indexOf( "}", p+1);
		return expression.substring(p+2, q);	
	}

	/**
	 * replaces any occurrence of the variable by its value 
	 * @param variable - without ${..}, it will be added 
	 * @param value - the value of the variable
	 * @param expression - the expression to replace in 
	 * @return - the expression after the replace 
	 */
	protected String replace(String variable, String value, String expression) {
		return expression.replace( "${" + variable + "}", value);
	}
	 
	private Maybe<String> resolve(String expression, Map<String, String> parentProperties, Map<String, String> localProperties) {
		List<Reason> reasons = new ArrayList<>();
		// extract
		while (requiresEvaluation( expression)) {
			String variable = extract( expression);
			String value = localProperties.get(variable);
			if (value == null) {
				value = parentProperties.get(variable);
			}
			if (value == null) {
				value ="<not resolveable>";			
				String msg = "variable " + variable + " cannot be resolved";
				log.warn(msg);
				Reason reason = TemplateReasons.build( UnresolvedProperty.T).assign(UnresolvedProperty::setPropertyName, variable).toReason();
				
				reasons.add(reason);			
			}
			expression = replace(variable, value, expression);
		}	
		if (reasons.size() > 0) {
			Reason umbrella = Reasons.build( PomValidationReason.T).text("expression in pom is invalid").causes( reasons).toReason();
			return Maybe.incomplete(expression, umbrella);
		}
		return Maybe.complete( expression);
	}

	private Reason acquireUmbrellaReason(DeclaredGroupExtractionContext context) {
		if (umbrellaReason != null) {
			return umbrellaReason;
		}
		umbrellaReason = Reason.T.create();
		umbrellaReason.setText( "group extraction of [" + context.getGroupLocation() + "] failed");
		return umbrellaReason;
	}
	
	public static void main(String[] args) {
		DeclaredGroupExtractor ex = new DeclaredGroupExtractor();
		Map<String,String> map1 = new HashMap<>();
		map1.put("map1-value", "map1");
		
		Map<String,String> map2 = new HashMap<>();
		map2.put("map2-value", "map2");
		
		List<String> expressions = new ArrayList<>();
		//expressions.add("no value");
		//expressions.add("value is ${map1-value}");
		//expressions.add( "value is ${map2-value}");
	    expressions.add("value is ${map1-value}${map2-value}");
	    expressions.add("value is ${map1-value}${map3-value}");
	
		for (String arg : expressions) {
			Maybe<String> maybe = ex.resolve(arg, map1, map2);
			if (maybe.isSatisfied()) {
				System.out.println( arg + "->" + maybe.get());
			}
			else {
				System.out.println( arg + "->" + maybe.get() + " : " + maybe.whyUnsatisfied().stringify());	
			}
		}
	
	}
	
	
}
