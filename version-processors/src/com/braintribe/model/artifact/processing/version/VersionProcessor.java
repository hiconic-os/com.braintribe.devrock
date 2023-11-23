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
package com.braintribe.model.artifact.processing.version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.version.AlphaNumericVersionPart;
import com.braintribe.model.artifact.version.DashedDelimiterVersionPart;
import com.braintribe.model.artifact.version.DelimiterVersionPart;
import com.braintribe.model.artifact.version.DottedDelimiterVersionPart;
import com.braintribe.model.artifact.version.NumericVersionPart;
import com.braintribe.model.artifact.version.UnderlineDashedDelimiterVersionPart;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionPart;

public class VersionProcessor{
	
	private final static Logger log = Logger.getLogger(VersionProcessor.class);
	private static final String VERSION_WILDCARD = "^";
	private static final String VERSION_DASH = "-";
	private static ListComparator listComparator = new ListComparator();
	public final static int UNDEFINED = -1; 
	private final static String VERSION_SNAPSHOT = "-SNAPSHOT";	
	private enum Inside { Undefined, Numeric, AlphaNumeric }

	public static Comparator<Version> comparator = VersionProcessor::compare; 

	private static Map<String, Integer> adornmentToValueMap;
	
	static {
		adornmentToValueMap = new HashMap<String, Integer>();
			
		adornmentToValueMap.put( "PC", -4);
		adornmentToValueMap.put( "M", -3);
		adornmentToValueMap.put( "RC", -2);
		adornmentToValueMap.put( "RELEASE", 0);
		adornmentToValueMap.put( "GA", 0);
		adornmentToValueMap.put( "FINAL", 0);		
	}
	
	public static Version createVersion() {
		Version version = Version.T.create();
		version.setUndefined( true);
		return version;
	}
	
	public static Version createFromString( String inVersionAsString) throws VersionProcessingException{
		String versionAsString = inVersionAsString.trim();
		try {			
			Version version = createVersion();
			version.setOriginalVersionString( versionAsString);
			
			// first, let's cut-out the snapshot identifier.. "-SNAPSHOT"
			int index = versionAsString.indexOf( VERSION_SNAPSHOT);
			if (index > 0) {
				String fp = versionAsString.substring(0, index);
				String sp = versionAsString.substring( index + VERSION_SNAPSHOT.length());
				versionAsString = fp + sp;
				version.setSnapshot( true);
			}
			
			List<VersionPart> data = new ArrayList<VersionPart>();
			String classifier = null;
			
			/*
			int dashIndex = versionAsString.indexOf( VERSION_DASH);
			if (dashIndex > 0) {
				String suffix = versionAsString.substring( dashIndex+1);
				classifier = suffix;
				versionAsString = versionAsString.substring(0, dashIndex);
			}
			*/
			
			
			Inside inside = Inside.Undefined;
			String expression = "";
			for (int i = 0; i < versionAsString.length(); i++) {
				String c = versionAsString.substring( i, i+1);
				
				if (c.matches("[\\.\\-_]")) {
					//
					switch( inside) {
						case Numeric : {
							try {
								Integer value = new Integer( expression);
								data.add( createNumericVersionPart(value));
							} catch (Exception e) {
								data.add( createAlphaNumericVersionPart( expression));
							}
							break;
						}
						case AlphaNumeric: {
							data.add( createAlphaNumericVersionPart( expression));
							break;
						}		
						default:
							break;
					}					
					if (c.matches( "\\."))
						data.add( createDottedDelimiterVersionPart());
					else
						if (c.matches("\\-"))
							data.add( createDashedDelimiterVersionPart());
						else
							data.add( createUnderlineDashedDelimiterVersionPart());
					
					expression = "";
					inside = Inside.Undefined;
					continue;
				}	
				
				
				expression += c;
				
				if (inside == Inside.Undefined) {
					if (c.matches( "[0-9]")) {					
						inside = Inside.Numeric;
					} else {
						inside = Inside.AlphaNumeric;
					}
				}								
			}
			
			switch (inside) {
				case Numeric: {
					try {
						Integer value = new Integer( expression);
						data.add( createNumericVersionPart(value));
					} catch (Exception e) {
						data.add( createAlphaNumericVersionPart( expression));
					}
					break;
				}
				case AlphaNumeric: {
					data.add( createAlphaNumericVersionPart( expression));
					break;
				}
				default: {
					if (log.isDebugEnabled()) {
						log.warn( String.format("a this point in version parsing, the value [%s] is unexpected", inside.toString()));
					}
				}
			}
			
			
			/*
			 String number = "";
			 String delimiter = "";
			for (int i = 0; i < versionAsString.length(); i++) {
				String c = versionAsString.substring( i, i+1);
				// is a delimiter
				if (c.matches("\\.")) {
					data.add( createDelimiterVersionPart());
					continue;
				}				
				// is a numerical digit
				if (c.matches( "[0-9]")) {
					number += c;
					if (delimiter.length() > 0) {
						data.add( createAlphaNumericVersionPart(delimiter));
						delimiter = "";
					}
					continue;
				} 				
				// is something else.. 
				delimiter += c;
				if (number.length() > 0) {
					Integer value = new Integer( number);
					data.add( createNumericVersionPart(value));
					number = "";
				}								
			}
						
			
			// remainder's a number
			if (number.length() > 0)
				data.add( createNumericVersionPart( new Integer(number)));
			
			// remainder's a delimiter.. 
			if (delimiter.length() > 0)
				data.add( createAlphaNumericVersionPart( delimiter));
			*/
			
			
		
			version.setClassifier( classifier);
			version.setVersionData( data);
			version.setUndefined( false);
			return version;
				
		} catch (Exception e) {			
			String msg = "can't extract version from [" + versionAsString + "] as " + e;
			log.error( msg, e);
			throw new VersionProcessingException( msg, e);
		}		
	}

	public static int hash(Version version) {
		return toString(version).hashCode();
	}

	public static String toString(Version version) {
		  
		String versionAsString = version.getOriginalVersionString();
		if (versionAsString != null)
			return versionAsString;
		
		if (version.getUndefined() == true)			
			return "";
		
		StringBuilder retval = new StringBuilder();
		
		retval.append( toString( version.getVersionData()));
		
		if  (version.getSnapshot() == true)		
			retval.append( VERSION_SNAPSHOT);
			
		if (version.getClassifier() != null)
			retval.append( VERSION_DASH + version.getClassifier());
		
		return retval.toString();
	}
	
	private static String toString( List<VersionPart> versionData) {
		StringBuilder retval = new StringBuilder();
		for (VersionPart obj : versionData) {
			
			if (obj instanceof NumericVersionPart) {
				retval.append("" + ((NumericVersionPart) obj).getValue());
				continue;
			}			
			if (obj instanceof DottedDelimiterVersionPart) {
				retval.append(".");
				continue;
			}
			if (obj instanceof DashedDelimiterVersionPart) {
				retval.append("-");
				continue;
			}
			if (obj instanceof UnderlineDashedDelimiterVersionPart) {
				retval.append("_");
				continue;
			}
			
			
			retval.append(((AlphaNumericVersionPart)obj).getValue());
		}
		return retval.toString();
	}
	
	
	public static boolean isFuzzy( Version version) {
		List<VersionPart> data = version.getVersionData();
		Object last = data.get( data.size()-1);
		if (last instanceof AlphaNumericVersionPart) {
			String lastDel = ((AlphaNumericVersionPart) last).getValue();
			if (lastDel.substring( lastDel.length() -1).equalsIgnoreCase( VERSION_WILDCARD)) {
				return true;
			}
		}
		return false;
	}
	
	public static Version getMinimumOfVersion( Version version) throws VersionProcessingException {
		if (isFuzzy(version) == false)
			return version;
		// 
		List<VersionPart> partsOne = convertVersionDataToComparableVersionData( version.getVersionData());
		String versionAsString = toString(partsOne);
		return createFromString(versionAsString);
	}
	
	public static Version getMaximumOfVersion( Version version) throws VersionProcessingException{
		if (isFuzzy(version) == false)
			return version;
		// 
		List<VersionPart> partsOne = convertVersionDataToComparableVersionData( version.getVersionData());
		VersionPart lastPart = partsOne.get( partsOne.size()-1);
		if (lastPart instanceof NumericVersionPart) {
			int value = ((NumericVersionPart) lastPart).getValue();
			VersionPart replacementPart = createNumericVersionPart( value + 1);
			partsOne.remove( lastPart);
			partsOne.add( replacementPart);
		}
		String versionAsString = toString(partsOne);
		return createFromString(versionAsString);
	}
	
	
	public static String regexpFromVersion( Version version) {
		StringBuilder retval = new StringBuilder();
		List<VersionPart> data = version.getVersionData();
		ArrayList<VersionPart> regExpValue = new ArrayList<VersionPart>( data.size());
		regExpValue.addAll( data);

		//
		if (isFuzzy( version)) {
			regExpValue.remove( data.size()-1);
			regExpValue.add( createAlphaNumericVersionPart(".*"));
		}
	
		//		
		for (Object obj : regExpValue) {
			if (obj instanceof NumericVersionPart) {
				retval.append( "" + ((NumericVersionPart) obj).getValue());
				continue;
			}
			if (obj instanceof AlphaNumericVersionPart) {
				retval.append(((AlphaNumericVersionPart)obj).getValue());
			}
			if (obj instanceof DottedDelimiterVersionPart) {
				retval.append( ".");
			}
			if (obj instanceof DashedDelimiterVersionPart) {
				retval.append("-");
			}
			if (obj instanceof UnderlineDashedDelimiterVersionPart) {
				retval.append("_");
				continue;
			}
		}
		
		if  (version.getSnapshot() == true)			
			retval.append(VERSION_SNAPSHOT);
			
		if (version.getClassifier() != null)
			retval.append(VERSION_DASH + version.getClassifier());
		
		return retval.toString();
	}
	
	private static int compareClassifiers( Version version1, Version version2) {
		String c1 = version1.getClassifier();
		String c2 = version2.getClassifier();
		
		if (
				(c1 == null) &&
				(c2 == null)
			)
			return 0;
		
		if (
				(c1 == null) &&
				(c2 != null)
			)
			return -1;
		
		if (
				(c1 != null) &&
				(c2 == null)
			)
			return 1;
		
		return c1.compareToIgnoreCase( c2);						
	}
	
	
	/**
	 * normalizes the size of two version strings by adding "neutral" version values
	 * the 0 is your friend when it comes to versions.. 
	 * @param list - the version data list 
	 * @param size - the size you need.
	 * @return - the expanded list.
	 */
	private static List<VersionPart> expandToSize( List<VersionPart> list, int size) {		
		boolean flip = list.get( list.size()-1) instanceof DelimiterVersionPart ? true : false;
		for (int i = list.size(); i < size; i+= 1) {
			if (flip) {
				NumericVersionPart numericVersionPart = NumericVersionPart.T.create();
				numericVersionPart.setValue( 0);
				list.add(numericVersionPart);
				flip = false;
				continue;
			}				
			DelimiterVersionPart delimiterVersionPart = DelimiterVersionPart.T.create();
			list.add( delimiterVersionPart);
			flip = true;
		}
		return list;
	}
	/**
	 * compare two versions 
	 * @param one - first {@link Version}
	 * @param two - second rabbit 
	 * @return - 0 if both are equivalent, 1 if first's higher, -1 if first's lower
	 */
	private static int _compare( Version one, Version two) {
							
		if  (isUndefined( one) && isUndefined( two))
			return 0;
		
		if (isUndefined( one) && !isUndefined( two))
			return 1;
		if (!isUndefined( two) && isUndefined( two))
			return -1;
		
		//
		// convert version data into comparable data.. 
		List<VersionPart> partsOne = convertVersionDataToComparableVersionData( one.getVersionData());
		List<VersionPart> partsTwo = convertVersionDataToComparableVersionData( two.getVersionData());

		// normalize - only if none of the is fuzzy 
		if (
				(VersionProcessor.isFuzzy(one) == false) &&
				(VersionProcessor.isFuzzy( two) == false)
			) {
			int sizeOne = partsOne.size();
			int sizeTwo = partsTwo.size();
			if (sizeOne != sizeTwo) {
				if (sizeOne > sizeTwo)
					partsTwo = expandToSize(partsTwo, sizeOne);
				else
					partsOne = expandToSize( partsOne, sizeTwo);			
			}
			//			
		} 
		
		
		// now compare.. (only up to the comparable parts)
		int retval = listComparator.compare( partsOne, partsTwo);
		if (retval != 0)
			return retval;
	
		boolean snapshotOne = false;
		if (one.getSnapshot() == true)			
			snapshotOne = true;
		
		boolean snapshotTwo = false;
		if (two.getSnapshot() == true)			
			snapshotTwo = true;
	
		if (
				(snapshotOne) &&
				(!snapshotTwo)
			)
			return -1;
		
		if (
				(!snapshotOne) &&
				(snapshotTwo)
			)
			return 1;
		
		return 0;
		
	}
	
	/**
	 * translates the version parts into a list of parts that really are comparable.. 
	 * @param versionParts - a {@link List} of {@link VersionPart} as stored in a version
	 * @return - a {@link List} of {@link VersionPart} that is normalized to be compared 
	 */
	private static List<VersionPart> convertVersionDataToComparableVersionData( List<VersionPart> versionParts) {
		List<VersionPart> result = new ArrayList<VersionPart>();
		for (VersionPart part : versionParts) {
			// 
			if (
					(part instanceof NumericVersionPart) ||
					(part instanceof DelimiterVersionPart)
				){
				result.add( part);
				continue;
			}
			// must disect alpha numeric part to numeric parts.. 
			if (part instanceof AlphaNumericVersionPart) {
				AlphaNumericVersionPart apart = (AlphaNumericVersionPart) part;
				String suspect = apart.getValue();
				//
				// if it's a fuzzy marker, abort here.  
				//				
				if (suspect.equalsIgnoreCase( VersionProcessor.VERSION_WILDCARD)) {
					if (
							(result.size() > 0) && 							
							(result.get( result.size() - 1) instanceof DelimiterVersionPart)
						)
						result.remove( result.size()-1);
					return result;
				}
				//
				// otherwise, analyze.. 
				//
				Inside inside = Inside.Undefined;
				String expression = "";
				for (int i = 0; i < suspect.length(); i++) {
					String c = suspect.substring(i, i+1);
					
					if (c.matches( "[0-9]")) {
						if (inside == Inside.Undefined) 
							inside = Inside.Numeric;
						
						switch ( inside) {									
							case Numeric :
								expression += c;
								break;						
							case AlphaNumeric:
								Integer value = getIntegerValueForAlphaNumericStatement( expression);
								
								if (
										(result.size() > 0) &&
										(result.get( result.size()-1) instanceof DelimiterVersionPart == false)
									)
									result.add( createDottedDelimiterVersionPart());
								if (value == null) {
									result.add( createAlphaNumericVersionPart( expression));
								} else 
									result.add( createNumericVersionPart(value));								
								expression = c;
								inside=Inside.Numeric;
								break;
							default:
								break;
						}
					} else {
						if (inside == Inside.Undefined) 
							inside = Inside.AlphaNumeric;
						switch (inside) {						
							case Numeric: {
								
								if (
										(result.size() > 0) &&
										(result.get( result.size()-1) instanceof DelimiterVersionPart == false)
									)
									result.add( createDottedDelimiterVersionPart());
								Integer value = Integer.valueOf( expression);
								result.add( createNumericVersionPart(value));								
								expression = c;
								inside=Inside.AlphaNumeric;
								break;
							}													
							case AlphaNumeric: {
								expression += c;
								break;
							}
							default:
								break;
						}
					}					
				}
			
				//
				if (expression.length() > 0) {
					//typical fuck up - a single string without digits as a version, such as "Smood"
					if (result.size() == 0) {
						result.add( createAlphaNumericVersionPart( expression));
					}
					if (result.get( result.size()-1) instanceof DelimiterVersionPart == false)
						result.add( createDottedDelimiterVersionPart());
					switch (inside) {						
						case Numeric: {
							Integer value = Integer.valueOf( expression);
							result.add( createNumericVersionPart(value));
							break;
						}	
						case AlphaNumeric: {
							Integer value = getIntegerValueForAlphaNumericStatement( expression);
							if (value == null) {
								value = -1;
							}
							result.add( createNumericVersionPart(value));						
							break;
						}
						default:
							break;
					}
				}
			}
		}
		//
		//
		//
		//System.out.println( toString( versionParts) + "->" + toString(result));
		return result;
	}
	
	private static Integer getIntegerValueForAlphaNumericStatement( String expression) {
		
		for (Entry<String, Integer> entry : adornmentToValueMap.entrySet()) {
			if (expression.toUpperCase().matches( entry.getKey())) {
				return entry.getValue();
			}
		}
		return 0;
	}
	
	/**
	 * @param version1 - the one that must be higher 
	 * @param version2 - the one that must be lower
	 * @return - true if as expected 
	 */
	public static boolean isLess(Version version1, Version version2){
		if (version1 == null)
			return false;
		
		int retval = _compare( version1, version2);
	
		if (retval == 0) {
			return compareClassifiers(version1, version2) < 0;
		} else 
			return  retval < 0;
	}
	
	/**
	 * @param version1 - the one that must be lower 
	 * @param version2 - the one that must be higher 
	 * @return true if as expected 
	 */
	public static boolean isHigher( Version version1, Version version2) {
		if (version2 == null)
			return true;
		int retval = _compare( version1, version2);
		if (retval == 0)
			return compareClassifiers(version1, version2) > 0;
		return  retval > 0;
	}
	
	/**
	 * matches two versions<br/>
	 * if both versions do have a original string stored as a string, they are compared as strings, otherwise
	 * the metric is used to compare - i.e. they're converted into string via the internal representation
	 * @param version1 - the first {@link Version}
	 * @param version2 - the second {@link Version}
	 * @return - true if they match, false otherwise 	
	 */
	public static boolean matches( Version version1, Version version2){
		// classically undefined
		if  (	
				(isUndefined( version1)) || 
				(isUndefined( version2))
		  	)
			return true;

		if (version1.getOriginalVersionString() != null && version2.getOriginalVersionString() != null) {
			return matches( version1.getOriginalVersionString(), version2.getOriginalVersionString());
		}
		
		int retval = _compare( version1, version2);
		if (
				(retval == 0) && 				
			    (compareClassifiers(version1, version2) == 0)
			) 
			return true;
			    			
		String regExp = regexpFromVersion( version1);
		String test = toString( version2); 
		return test.matches( regExp);
		
		
	}
	
	/**
	 * match two versions<br/>
	 * if the first version has an original string stored, a string comparison is done 
	 * @param version - the {@link Version} to compare 
	 * @param versionAsString - the second versions as String to compare to 
	 * @return - true if the match, false otherwise 
	 * @throws VersionProcessingException - 
	 */
	public static boolean matches( Version version, String  versionAsString) throws VersionProcessingException{
		if (version.getOriginalVersionString() != null) {
			return matches( version.getOriginalVersionString(), versionAsString);
		}
		Version version2 = createFromString(versionAsString);
		return matches( version, version2);
	}
	
	/**
	 * hard match (no fuzzy, no frills)<br/>
	 * if both versions have an original string stored, then comparison on this string is done, 
	 * otherwise the versions are converted to string representation and then tested. 
	 * @param version1 - the first {@link Version}
	 * @param version2 - the second {@link Version}
	 * @return - true if they match, false otherwise 
	 */
	public static boolean hardMatches( Version version1, Version version2) {
		
		if (version1.getOriginalVersionString() != null && version2.getOriginalVersionString() != null) {
			return version1.getOriginalVersionString().equalsIgnoreCase(version2.getOriginalVersionString());
		}
		
		String thisVersion = toString( version1);
		String thatVersion = toString( version2);
		if (thisVersion.equalsIgnoreCase( thatVersion))
			return true;
		return false;
	}
	
	/**
	 * hard match (no fuzzy, no frills) <br/>
	 * if both versions have an original string stored, then comparison on this string is done, 
	 * otherwise the versions are converted to string representation and then tested. 
	 * @param version1 - the {@link Version}
	 * @param version2 - the version as {@link String}
	 * @return - true if they match, false otherwise 
	 */
	public static boolean hardMatches( Version version1, String version2){
		if (version1.getOriginalVersionString() != null)
			return version1.getOriginalVersionString().equalsIgnoreCase(version2);
		
		String thisVersion = toString( version1);
		if (thisVersion.equalsIgnoreCase( version2))
			return true;
		return false;
	}
	/**
	 * returns the part of the version string that is relevant, i.e. cuts away the fuzzy dacherl.
	 * @param version - the version as {@link String}
	 * @return - the part of the string preceding the dacherl.
	 */
	private static String getRelevantVersionString( String version) {			
		int p1 = version.indexOf( VERSION_WILDCARD);
		if (p1 > 0) {
			return version.substring(0, p1);
		}
		return version;
	}
	
	/**
	 * matches to versions as strings <br/>
	 * if any of the versions sport a dacherl, only the relevant part of the string is compared 
	 * @param version1 - the first versions as a {@link String}
	 * @param version2 - the second version as {@link String}
	 * @return - true if they match, false otherwise 	
	 */
	private static boolean matches( String version1, String version2){
		
			String v1 = version1;
			String v2 = version2;
			
			// check if we have dacherl anywhere 
			boolean wc_1 = v1.contains( VERSION_WILDCARD);
			boolean wc_2 = v2.contains( VERSION_WILDCARD);
			
			// if any dacherls are found, we must modify the version strings to compare only the rest. 
			if (wc_1 || wc_2) {
				if (wc_1)
					v1 = getRelevantVersionString(version1);
				if (wc_2)
					v2 = getRelevantVersionString(version1);
										
				if (v2.length() > v1.length()) {
					v2 = v2.substring(0, v1.length());
				} else
					if (v1.length() > v2.length()) {
						v1 = v1.substring(0, v2.length());
					}
			}
			
			return v1.equalsIgnoreCase(v2);
	}
	
	/**
	 * checks if the version is to be deemed undefined, i.e. has no data 
	 * @param version - the {@link Version} to check
	 * @return - true if the version's deemed not to be defined
	 */
	public static boolean isUndefined(Version version){
	
		if (
				(version.getVersionData() == null) || 
				(version.getVersionData().size() == 0) ||
				(VersionProcessor.toString(version).equalsIgnoreCase( "^"))				
			) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true iff "first" denotes a higher version than "second". Equivalent to
	 * {@code compare(first, second) > 0}, but makes user code easier to read.
	 */
	public static boolean isFirstHigher(Version first, Version second) {
		return compare(first, second) > 0;
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	
	public static int compare(Version one, Version two) {
		if (isUndefined( one) && (isUndefined( two)))
			return 0;
		if (
			(isUndefined( one) == false) &&
			(isUndefined( two) == true)
			)
			return 1;
		
		if (
			(isUndefined( one) == true) &&
			(isUndefined( two) == false)
			)
			return -1;
					
		if (hardMatches( one, two))
			return 0;
		if (isHigher(one, two))
			return 1;
		if (isLess(one, two))
			return -1;
		
		return 0;
	}
	
	private static AlphaNumericVersionPart createAlphaNumericVersionPart( String delimiter) {
		AlphaNumericVersionPart delimiterVersionPart = AlphaNumericVersionPart.T.create();
		delimiterVersionPart.setValue(delimiter);
		return delimiterVersionPart;
	}
	
	private static NumericVersionPart createNumericVersionPart( Integer value) {
		NumericVersionPart integerVersionPart = NumericVersionPart.T.create();
		integerVersionPart.setValue( value);
		return integerVersionPart;
	}
	
	private static DelimiterVersionPart createDottedDelimiterVersionPart(){
		return DottedDelimiterVersionPart.T.create(); 
	}
	
	private static DelimiterVersionPart createDashedDelimiterVersionPart(){
		return DashedDelimiterVersionPart.T.create(); 
	}

	private static DelimiterVersionPart createUnderlineDashedDelimiterVersionPart(){
		return UnderlineDashedDelimiterVersionPart.T.create(); 
	}

	public static String getAdornment(Version version) {
		List<VersionPart> data = version.getVersionData();
		
		if (data.size() < 1)
			return null;
		VersionPart part = data.get( data.size()-1);
		if (part instanceof AlphaNumericVersionPart) {
			AlphaNumericVersionPart alphaNumericVersionPart = (AlphaNumericVersionPart) part;
			return alphaNumericVersionPart.getValue();
		}
		return null;
	}


	
	/**
	 * extracts a {@link VersionMetricTuple} from a {@link Version}<br/>
	 * any missing values of the tuple are set to 0. 
	 * @param version - the {@link Version} to extract from 
	 * @return - the {@link VersionMetricTuple} extracted 
	 * @throws VersionProcessingException - if nothing meaningful can be extracted
	 */
	public static VersionMetricTuple getVersionMetric( Version version) throws VersionProcessingException {
		return getVersionMetric(version, false);
	}
	
	/**
	 * extracts a {@link VersionMetricTuple} from a {@link Version}<br/>
	 * any missing values of the tuple are set to 0. 
	 * @param version - the {@link Version} to extract from
	 * @param useOnlyGivenParts if true only those parts of the tuple will be filled with numbers that really come from the version otherwise 0s will be used to fill up the missing parts. 
	 * @return - the {@link VersionMetricTuple} extracted 
	 * @throws VersionProcessingException - if nothing meaningful can be extracted
	 */
	public static VersionMetricTuple getVersionMetric( Version version, boolean useOnlyGivenParts) throws VersionProcessingException {
		VersionMetricTuple metricTuple = useOnlyGivenParts? new VersionMetricTuple(null, null, null): new VersionMetricTuple();
		
		List<VersionPart> data = version.getVersionData();
		if (data == null || data.size() == 0) {
			String msg ="version doesn't contain any useful data";
			log.error( msg);
			throw new VersionProcessingException( msg);
		}
		int index = 0;
		for (VersionPart part : data ) {
			if (part instanceof DelimiterVersionPart) {
				continue;
			}
			Integer value = null;
			if (part instanceof NumericVersionPart) {
				NumericVersionPart nvp = (NumericVersionPart) part;
				value = nvp.getValue();
				
			}
			if (part instanceof AlphaNumericVersionPart) {
				AlphaNumericVersionPart avp = (AlphaNumericVersionPart) part;
				String avpValue = avp.getValue();
				value = 0;
				if (!avpValue.toLowerCase().equals( "pc")) {			
					String msg = "unexpected: alpha numeric version part detected [" + avpValue + "]";
					log.warn( msg);
				}
			}
			if (value == null) {
				String msg = "unexpected: no value extracted from non-delimiter version part";
				log.warn( msg);				
			}
			switch ( index) {
				case 0:
					metricTuple.major = value;
					break;
				case 1:
					metricTuple.minor = value;
					break;
				case 2:
					metricTuple.revision = value;
					break;
				default: // we're done 
					return metricTuple;
			}
			index++;
		}
		
		return metricTuple;		
	}
	
	/**
	 * set the version as specified in the {@link VersionMetricTuple} passed <br/>
	 * any parts of the version that are following the last entry in the {@link VersionMetricTuple} are preserved <br/>
	 * @param version - the {@link Version} that needs to be changed 
	 * @param metricTuple - the {@link VersionMetricTuple} with the new data to be set 
	 */
	public static void setVersionMetric( Version version, VersionMetricTuple metricTuple){		
		List<VersionPart> data = version.getVersionData();
		
		List<VersionPart> newData = new ArrayList<VersionPart>();
		
		NumericVersionPart nvp = NumericVersionPart.T.create();
		nvp.setValue( metricTuple.major);
		newData.add(nvp);
		
		DottedDelimiterVersionPart dvp = DottedDelimiterVersionPart.T.create();
		newData.add( dvp);
		
		nvp = NumericVersionPart.T.create();
		nvp.setValue( metricTuple.minor);
		newData.add(nvp);
		
		Integer revision = null;
		if ((revision = metricTuple.revision) != null) {
			dvp = DottedDelimiterVersionPart.T.create();
			newData.add( dvp);
			nvp = NumericVersionPart.T.create();
			nvp.setValue( revision);
			newData.add(nvp);
		}
		
	
		// transfer the rest of the version.. 
		if (data.size() > 5) {
			for (int i = 5; i< data.size(); i++) {
				newData.add( data.get(i));
			}
		}
		
		version.setVersionData(newData);
		String newString = toString(newData);
		version.setOriginalVersionString( newString);
	}
	
	public static Version createFromMetrics( VersionMetricTuple metric) {
		Version version = createVersion();
		setVersionMetric(version, metric);
		return version;
	}
	
	
	public static List<Version> filterMajorMinorWinners(Collection<Version> versions) {	
		Map<String, Version> winnerMap = new HashMap<>();
		for (Version version : versions) {
			VersionMetricTuple tuple = getVersionMetric(version);
			String key = String.format( "%d.%d", tuple.major, tuple.minor);
			Version categoryWinner = winnerMap.get(key);
			if (
					categoryWinner == null ||
					isHigher(version, categoryWinner)
				) {
				winnerMap.put(key, version);				
			}
			
		}
		return new ArrayList<Version>( winnerMap.values());
	}
}

	

