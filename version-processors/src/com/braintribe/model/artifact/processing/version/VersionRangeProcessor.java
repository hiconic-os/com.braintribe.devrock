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

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;

/**
 * 05.09.2017 : swapped logic to resemble mathematically notation: <br/>
 * 
 * open interval : boundaries are *not* included<br/>
 * closed interval : boundaries are included<br/>
 * 
 * @author pit
 *
 */
public class VersionRangeProcessor {

	//private static Logger log = Logger.getLogger(VersionRangeProcessor.class);
	
	private final static String closedLowerChar = "[";
	private final static String openLowerChar = "(";
	private final static String closedUpperChar = "]";
	private final static String openUpperChar =")";
	
	
	public static VersionRange createVersionRange() {
		VersionRange versionRange = VersionRange.T.create();
		versionRange.setInterval( true);
		versionRange.setOpenLower( false);
		versionRange.setOpenUpper( false);
		versionRange.setUndefined( true);
		
		return versionRange;
	}
	
	public static boolean contains(VersionRange container, VersionRange containee) {
		Version lb = containee.lowerBound();
		Version ub = containee.upperBound();
		
		int res = getLowerBoundComparison(container).apply(lb);
		
		if (res == 0) {
			boolean c = container.lowerBoundOpen(), e = containee.lowerBoundOpen();
			
			if (c && !e) 
				return false;
		}
		else if (res > 0) {
			return false;
		}
		
		res = getUpperBoundComparison(container).apply(ub);
		
		if (res == 0) {
			boolean c = container.upperBoundOpen(), e = containee.upperBoundOpen();

			if (c && !e) 
				return false;
		}
		
		return res > 0;
	}
	
	public static Predicate<VersionRange> containsPredicate(VersionRange container) {
		return containee -> contains(container, containee); 
	}
	
	public static Predicate<VersionRange> isContainedPredicate(VersionRange containee) {
		return container -> contains(container, containee); 
	}
	
	public static VersionRange extend(VersionRange range, Version version) {
		int res = compare(range, version);

		if (res < 0) {
			VersionRange extendedRange = VersionRange.T.create();
			extendedRange.setMaximum(range.getMaximum());
			extendedRange.setOpenUpper(range.getOpenUpper());
			extendedRange.setMinimum(version);
			extendedRange.setOpenLower(false);
			return extendedRange;
		}
		else if (res > 0) {
			VersionRange extendedRange = VersionRange.T.create();
			extendedRange.setMaximum(range.getMaximum());
			extendedRange.setOpenUpper(range.getOpenUpper());
			extendedRange.setMinimum(version);
			extendedRange.setOpenLower(false);
			return extendedRange;
		}
		else {
			return range;
		}
	}
	
	public static Function<Version, Integer> getUpperBoundComparison(VersionRange range) {
		if (!range.getInterval()) {
			Version version = range.getDirectMatch();
			return v -> VersionProcessor.comparator.compare(version, v);
		}
		else {
			Version version = range.getMaximum();
			
			if (range.getOpenUpper()) {
				return v -> {
					int res = VersionProcessor.comparator.compare(version, v);
					if (res == 0) {
						res = 1;
					}
					return res;
				};
			}
			else {
				return v -> VersionProcessor.comparator.compare(version, v);
			}
		}		
	}
	
	public static Function<Version, Integer> getLowerBoundComparison(VersionRange range) {
		if (!range.getInterval()) {
			Version version = range.getDirectMatch();
			return v -> VersionProcessor.comparator.compare(version, v);
		}
		else {
			Version version = range.getMinimum();
			
			if (range.getOpenLower()) {
				return v -> {
					int res = VersionProcessor.comparator.compare(version, v);
					if (res == 0) {
						res = -1;
					}
					return res;
				};
			}
			else {
				return v -> VersionProcessor.comparator.compare(version, v);
			}
		}		
	}
	
	public static VersionRange createFromString( String range) throws VersionProcessingException{
		
		
		VersionRange versionRange = createVersionRange();
		
		versionRange.setOriginalVersionRange(range);
		
		if ( 
				(range == null) ||
				(range.equalsIgnoreCase( "^"))
		   ){ // set an undefined version range
			versionRange.setUndefined(true);
			return versionRange;
		} else 
			versionRange.setUndefined( false);

		if (range.equalsIgnoreCase( "LATEST")) {
			versionRange.setUndefined( true);
			versionRange.setSymbolicLatest( true);
			return versionRange;
		}
		
		if (range.equalsIgnoreCase( "RELEASE")) {
			versionRange.setUndefined( true);
			versionRange.setSymbolicRelease( true);
			return versionRange;
		}
		
		// fuzzy must be turned into interval 
		if (range.endsWith( "^")) {
			int lastDot = range.lastIndexOf('.');
			range = range.substring(0, lastDot);
			return autoRangify(createFromString(range));
		}
		
		
		String lowerDelChar = range.substring(0, 1);
		if (lowerDelChar.equalsIgnoreCase( closedLowerChar))
			versionRange.setOpenLower( false);
		else
			if (lowerDelChar.equalsIgnoreCase( openLowerChar))
				versionRange.setOpenLower( true);
			else {				
				versionRange.setInterval(false);
				versionRange.setDirectMatch( VersionProcessor.createFromString( range));
				return versionRange;
			}
		
		String upperDelChar = range.substring( range.length()-1);
		if (upperDelChar.equalsIgnoreCase( closedUpperChar))
			versionRange.setOpenUpper( false);
		else
			versionRange.setOpenUpper( true);
		
		range = range.substring( 1, range.length()-1);
		int comma = range.indexOf( ",");
		if (comma < 0) {			
			Version createFromString = VersionProcessor.createFromString( range);
			versionRange.setDirectMatch( createFromString);
			versionRange.setInterval( false);
			versionRange.setMinimum(createFromString);
			versionRange.setMaximum(createFromString);
			return versionRange;
		} 
		
		// lower bounds
		if (comma > 0) {
			versionRange.setMinimum( VersionProcessor.createFromString( range.substring(0, comma)));
		}
		// upper bounds
		if (comma < range.length()-1)  
			versionRange.setMaximum( VersionProcessor.createFromString( range.substring( comma + 1)));
		
		return versionRange;
	}
	
	public static VersionRange createfromVersion( Version version) throws VersionProcessingException {
		VersionRange versionRange = createVersionRange();
		if (VersionProcessor.isFuzzy( version) == false) {
			versionRange.setDirectMatch( version);
			versionRange.setInterval( false);
			versionRange.setUndefined( false);		
		} else {
			// 
			versionRange.setInterval( true);
			versionRange.setUndefined( false);
			// minimum?
			versionRange.setMinimum( VersionProcessor.getMinimumOfVersion(version));
			// maximum?
			versionRange.setMaximum( VersionProcessor.getMaximumOfVersion(version)); 
		}
		
		return versionRange;		
	}
	
	
	
	public static String toString( VersionRange versionRange){
		String versionRangeAsString =versionRange.getOriginalVersionRange();
		
		if (versionRangeAsString == null) {
			versionRangeAsString = toStringRepresentation(versionRange);
		}
		return versionRangeAsString;
						
	}
	
	public static String toStringRepresentation(VersionRange versionRange) {
		if (versionRange.getUndefined()){
			if (versionRange.getSymbolicLatest()) { 				
				return "LATEST";
			}
			if (versionRange.getSymbolicRelease())  {
				return "RELEASE";
			}			
			return "UNDEFINED";
		}
		
		if (!Boolean.TRUE.equals( versionRange.getInterval())) {
			return VersionProcessor.toString( versionRange.getDirectMatch());
		} 
		StringBuilder builder = new StringBuilder();
		if (versionRange.getOpenLower())
			builder.append( openLowerChar);
		else
			builder.append( closedLowerChar);
			
		Version directMatch = versionRange.getDirectMatch();
		if ( directMatch != null) {					
			builder.append( VersionProcessor.toString(directMatch));
			if (versionRange.getOpenUpper())
				builder.append( openUpperChar);
			else
				builder.append( closedUpperChar);
			return builder.toString();
		} 
		Version minimum = versionRange.getMinimum();
		if (minimum != null)
			builder.append( VersionProcessor.toString( minimum));
		
		builder.append(",");
		
		Version maximum = versionRange.getMaximum();
		if (maximum != null)
			builder.append( VersionProcessor.toString( maximum));
		
		if (versionRange.getOpenUpper())
			builder.append( openUpperChar);
		else
			builder.append( closedUpperChar);
		
		return builder.toString();	
	}
	
	 
	
	public static boolean hardMatches( VersionRange versionRange, Version version) {
		return matches( versionRange, version);
	}
	
	public static boolean matches( VersionRange versionRange, Version version){
		if (versionRange.getUndefined())
			return false;
		Version directMatch = versionRange.getDirectMatch();
		if ( directMatch != null) {
			return VersionProcessor.matches( directMatch, version);
		}
	
		Version minimum = versionRange.getMinimum();
		if (minimum != null) {
			boolean isGreater = VersionProcessor.isLess( minimum, version);
			boolean isLess = VersionProcessor.isHigher( minimum, version);
			// if it's not greater, we need to check the border
			if (isGreater == false) { 
				// 
				if (versionRange.getOpenLower()) {
					return false;											
				}
				else 
					// may simple not be smaller 
					if (isLess) {
						return false;
				}				
			}
		}
		
		Version maximum = versionRange.getMaximum();
		if (maximum != null) {
			boolean isSmaller = VersionProcessor.isHigher( maximum, version);
			boolean isGreater = VersionProcessor.isLess( maximum, version);
			
			if (isSmaller == false) {
					
				if (versionRange.getOpenUpper()) {
					return false;					
				}
				else {
					if (isGreater) {
						return false;
					}
				}
			}
					
		}
		return true;

	}
	
	/**
	 * compare a {@link VersionRange} with a {@link Version}
	 * @param versionRange
	 * @param version
	 * @return
	 */
	public static int compare( VersionRange versionRange, Version version){
		if (getLowerBoundComparison(versionRange).apply(version) < 0)
			return -1;
		else if (getUpperBoundComparison(versionRange).apply(version) > 0) {
			return 1;
		}
		else
			return 0;
	}
	
	/**
	 * compares two {@link VersionRange}, both intervals and collapsed.
	 * @param r1
	 * @param r2
	 * @return
	 */
	public static int compare( VersionRange r1, VersionRange r2) {
		if (equals(r1, r2)) {
			return 0;
		}
		if (isHigher(r1, r2)) {
			return 1;
		}
		else {
			return -1;
		}		
	}
	
	
	public static boolean equals( VersionRange range1, VersionRange range2)  {
		if (
				(range1 == null) &&
				(range2 == null)
			)
			return true;
		
		if (
				(
					(range1 == null) &&
					(range2 != null)
				) ||
				(
					(range1 != null) &&
					(range2 == null)
				)
			)
			return false;
					
		String str1 = "";
		String str2 = "";
	
		str1 = toString( range1);
		str2 = toString( range2);
		
		return str1.equalsIgnoreCase(str2);
				
	}
	
	public static boolean matches( VersionRange versionRange, String versionExpression) throws VersionProcessingException {
		Version version = VersionProcessor.createFromString( versionExpression);
		return matches( versionRange, version);
	}
	
	/**
	 * returns the highest possible match from a SORTED list of versions
	 * 
	 * @param versions - the list of versions, sorted ascending in their versions
	 * @return - the last matching number 
	 */
	public static Version bestMatches( VersionRange versionRange, List<Version> versions){
		Version match = null;
		boolean oneMatched = false;
		for (Version version : versions) {
			if (matches( versionRange, version)) {
				match = version;
				oneMatched = true;
			} else {
				if (oneMatched)
					return match;
				continue;
			}
		}
		return match;
	}
	
	public static Version hardMatches( VersionRange versionRange, List<Version> versions){
		for (Version version : versions) {
			if (hardMatches( versionRange, version)) {
				return version;
			} 
		}
		return null;
	}
	
	
	public static boolean isFuzzy( VersionRange versionRange) {
		Version directMatch = versionRange.getDirectMatch();
		Version minimum = versionRange.getMinimum();
		Version maximum = versionRange.getMaximum();
		
		return (
				(versionRange.getUndefined()) ||
				( (directMatch != null) && ( VersionProcessor.isFuzzy( directMatch)) )|| 
				( (minimum != null) && (VersionProcessor.isFuzzy( minimum)) ) || 
				( (maximum != null) && (VersionProcessor.isFuzzy(maximum)) )
			   );
	}
	
	/**
	 * a enum to distinguish the two means of metric comparison 
	 * @author Pit
	 *
	 */
	private enum Comparison { higher, less}
	enum Boundary { closed, open, none}
	
	/**
	 * returns a guess of what might be the best data to compare a {@link VersionRange} to another<br/>
	 * if the {@link VersionRange}'s a simple version (collapsed range) then this is returned, 
	 * otherwise, depending on the {@link Comparison} value, either the minimum for {@link Comparison#less} or
	 * the maximum for {@link Comparison#higher}
	 * @param versionRange - the {@link VersionRange} in question
	 * @param comparison - the {@link Comparison} value 
	 * @return - a {@link Version}, see above, may be null if not defined  
	 */
	private static ComparisonTuple getBestGuess( VersionRange versionRange, Comparison comparison) throws VersionProcessingException {
		
		if (versionRange.getUndefined()) {
			/*
			try {
				return new ComparisonTuple(VersionProcessor.createFromString("^"));
			} catch (VersionProcessingException e) {
				return new ComparisonTuple(VersionProcessor.createVersion());
			}*/
			throw new VersionProcessingException("passed versionrange is undefined");
		}
		Version directMatch = versionRange.getDirectMatch();
		if (directMatch != null)
			return new ComparisonTuple(directMatch);
		switch ( comparison) {
			case higher :				
				return new ComparisonTuple(versionRange.getMaximum(), versionRange.getOpenUpper() ? Boundary.open : Boundary.closed);
			default:
			case less:
				return new ComparisonTuple(versionRange.getMinimum(), versionRange.getOpenLower() ? Boundary.open : Boundary.closed);
		}		
	}
	
	/**
	 * compare two ranges and return true if the first one is higher than the other one 	
	 */
	public static boolean isHigher( VersionRange range1, VersionRange range2) {
		/*
		if (range1.getUndefined()) {
			throw new VersionProcessingException( "first version range is undefined");
		}
		if (range2.getUndefined()) {
			throw new VersionProcessingException( "second version range is undefined");
		}
		*/
		
		ComparisonTuple tuple1 = getBestGuess( range1, Comparison.higher);
		if (tuple1.version == null)
			return true;
		ComparisonTuple tuple2 = getBestGuess( range2, Comparison.higher);
		if (tuple2.version == null) 
			return false;
		// if direct matching: check boundaries 
		if (VersionProcessor.matches( tuple1.version, tuple2.version)) {
			switch (tuple1.boundary) {
				case closed:
					switch (tuple2.boundary) {
						case closed:
							return false;						
						case open:
							return true;
						case none:
						default:
							return false;
					}
				case open:				
					switch (tuple2.boundary) {
						case closed:
							return false;						
						case open:
							return true;
						case none:
						default:
							return false;
					}
				case none:
					switch (tuple2.boundary) {
						case closed:
							return false;						
						case open:
							return true;
						case none:
						default:
							return false;
					}				
			}
		}
		return VersionProcessor.isHigher(tuple1.version, tuple2.version);
	}
	
	public static boolean isLess( VersionRange range1, VersionRange range2) {
		ComparisonTuple tuple1 = getBestGuess( range1, Comparison.less);
		if (tuple1.version == null)
			return true;
		ComparisonTuple tuple2 = getBestGuess( range2, Comparison.less);
		if (tuple2.version == null)
			return false;
		// if direct matching: check boundaries 
				if (VersionProcessor.matches( tuple1.version, tuple2.version)) {
					switch (tuple1.boundary) {
						case closed:
							switch (tuple2.boundary) {
								case closed:
									return false;						
								case open:
									return true;
								case none:
								default:
									return false;
							}
						case open:				
							switch (tuple2.boundary) {
								case closed:
									return true;						
								case open:
									return false;
								case none:
								default:
									return false;
							}
						case none:
							switch (tuple2.boundary) {
								case closed:
									return false;						
								case open:
									return true;
								case none:
								default:
									return false;
							}				
					}
				}
		return VersionProcessor.isLess( tuple1.version, tuple2.version);
	}
	
	/**
	 * merges two version ranges 
	 * @param inRange1 - the first {@link VersionRange}
	 * @param inRange2  - the second {@link VersionRange}
	 * @return - the resulting {@link VersionRange}
	 * @throws VersionProcessingException -
	 */
	public static VersionRange merge( VersionRange inRange1, VersionRange inRange2) throws VersionProcessingException {
		//
		// test if it it's fuzzy		
		//
		VersionRange range1 = inRange1;
		if (isFuzzy(range1)) {
			range1 = createfromVersion( range1.getDirectMatch());
		}
		VersionRange range2 = inRange2;
		if (isFuzzy(inRange2)) {
			range2 = createfromVersion( range2.getDirectMatch());
		}
		if (equals( range1, range2))
			return range1;
		
		// check if the borders are identical, and then check their open/close status
		if (
			(range1.getInterval()) &&
			(range2.getInterval())
		) {
			return doMerge( range1, range2);
		} else {
			if (
					(range1.getInterval()) &&
					(range2.getInterval() == false)
				) {
				if (matches( range1,range2.getDirectMatch()))
						return range2;
				else
					return null;
			} else { 
				if (
						(range1.getInterval() == false) &&
						(range2.getInterval())
					) {
					if (matches( range2,range1.getDirectMatch()))
							return range1;
					else
						return null;
				} 
			}					
		}
		return null;
	}
	
	/**
	 * merges two intervall'd version ranges 
	 * @param range1 - the first {@link VersionRange}
	 * @param range2 - the second {@link VersionRange}
	 * @return - the resulting {@link VersionRange}
	 */
	private static VersionRange doMerge( VersionRange range1, VersionRange range2){
		Version minimum1 = range1.getMinimum();
		Version maximum1 = range1.getMaximum();
		
		Version minimum2 = range2.getMinimum();
		Version maximum2 = range2.getMaximum();
	
		// test where the borders match, but the open/closed feature makes them disjunctive 
		if (VersionProcessor.matches( maximum1, minimum2)) { // first one lower
			if (range1.getOpenUpper()) {
				return null;
			}
		}
		else if (VersionProcessor.matches( minimum1, maximum2)) { // second one lower
			if (range2.getOpenUpper()) {
				return null;
			}
		}
		
		//
		// determine the new minimum - the higher wins 
		// 
		Version minimum = null;
		boolean isOpenLower = false;
		if (
				(minimum1 == null) &&
				(minimum2 != null)
			) {
			minimum = minimum2;
			isOpenLower = range2.getOpenLower();
		} else
			if (
					(minimum1 != null) &&
					(minimum2 == null)
				) {
				minimum = minimum1;
				isOpenLower = range1.getOpenLower();
			} else
				if (
						(minimum1 != null) &&
						(minimum2 != null)
					) {
					if (VersionProcessor.isHigher(minimum1, minimum2)) {
						minimum = minimum1;
						isOpenLower = range1.getOpenLower();
					} else {
						minimum = minimum2;
						isOpenLower = range2.getOpenLower();
					}
				}
		
		//
		// determine maximumn 
		//
		Version maximum = null;
		boolean isOpenUpper = false;
		if (
				(maximum1 == null) &&
				(maximum2 != null)
			) {
			maximum = maximum2;
			isOpenUpper = range2.getOpenUpper();
		} else
			if (
					(maximum1 != null) &&
					(maximum2 == null)
				) {
				maximum = maximum1;
				isOpenUpper = range1.getOpenUpper();
			} else
				if (
						(maximum1 != null) &&
						(maximum2 != null)
					) {
					if (VersionProcessor.isLess(maximum1, maximum2)) {
						maximum = maximum1;
						isOpenUpper = range1.getOpenUpper();
					} else {						
						maximum = maximum2;
						isOpenUpper = range2.getOpenUpper();
					}
				}
					
		//
		//
		// 		
		VersionRange range = createVersionRange();
		range.setUndefined(false);
		
		if (VersionProcessor.hardMatches( minimum, maximum)) {
			if (				
					(isOpenLower) ||
					(isOpenUpper)
				) 
		    { // open interval -> allowed 
				range.setDirectMatch(minimum);
				range.setOriginalVersionRange( VersionRangeProcessor.toString(range));
				return range;
		    } else { 
		    	// closed interval -> not allowed
		    	return null;
		    }
		}
		   
		range.setMinimum(minimum);
		range.setMaximum(maximum);

		range.setInterval( true);
		range.setOpenLower( isOpenLower);
		range.setOpenUpper( isOpenUpper);
		
		//
		// sanity check 
		//
		// a) minimum must be smaller (or equal) as maximum 
		if (VersionProcessor.isHigher(minimum, maximum)) {
			return null;
		}


		if (VersionProcessor.isLess( maximum, minimum))
			return null;
		
		range.setOriginalVersionRange( VersionRangeProcessor.toString(range));
		return range;	
	}
	
	
	/**
	 * turn a direct matching range (single version range) into a interval range<br/>
	 * lower limit is major and minor from direct match version, upper limit is major and minor+1,  
	 * lower limit is open, upper limit is closd. 
	 * @param range - the {@link VersionRange} to rangify 
	 * @return - the unmodified range if already an interval or the rangified range.
	 * @throws VersionProcessingException - if anything goes wrong
	 */
	public static VersionRange autoRangify( VersionRange range) throws VersionProcessingException{
		return autoRangify(range, false);
	}
	
	/**
	 * turn a direct matching range (single version range) into a interval range<br/>
	 * lower limit is major and minor from direct match version, upper limit is major and minor+1,  
	 * lower limit is open, upper limit is closd. 
	 * @param range - the {@link VersionRange} to rangify
	 * @param orUseGivenRevision - if true and the given version range is a direct match and has a revision part then the range will be unmodified 
	 * @return - the unmodified range if already an interval or the rangified range.
	 * @throws VersionProcessingException - if anything goes wrong
	 */
	public static VersionRange autoRangify(VersionRange range, boolean orUseGivenRevision) throws VersionProcessingException{
		if (Boolean.TRUE.equals(range.getInterval()))			
			return range;
		
		Version directMatch = range.getDirectMatch();
		
		VersionMetricTuple tuple = VersionProcessor.getVersionMetric(directMatch, orUseGivenRevision);
		
		if (tuple.revision != null && orUseGivenRevision) {
			return range;
		}
		
		Version minimumVersion = VersionProcessor.createFromString(tuple.major + "." + tuple.minor);
		range.setMinimum(minimumVersion);				
		range.setOpenLower(false);
		
		// calculate maximum - same major, increase minor by one, cut off rest 
		Version maximumVersion = VersionProcessor.createFromString(tuple.major + "." + (tuple.minor + 1));
		range.setMaximum(maximumVersion);		
		range.setOpenUpper( true);
		
		range.setInterval( true);
		
		// reset
		range.setDirectMatch( null);
		range.setOriginalVersionRange( null);
		range.setOriginalVersionRange( VersionRangeProcessor.toString(range));
		
		range.setOriginalVersionRange( toStringRepresentation( range));
		
		return range;
	}
	
	public static VersionRange hotfixRange(Version lowerBound) {
		VersionMetricTuple versionMetric = VersionProcessor.getVersionMetric(lowerBound);
		VersionMetricTuple normalizedVersionMetric = new VersionMetricTuple();
		normalizedVersionMetric.major = versionMetric.major;
		normalizedVersionMetric.minor = versionMetric.minor + 1;
		normalizedVersionMetric.revision = null;
		
		Version upperBound = Version.T.create();
		VersionProcessor.setVersionMetric(upperBound, normalizedVersionMetric);
		
		VersionRange normalizedVersionRange = VersionRange.T.create();
		normalizedVersionRange.setMinimum(lowerBound);
		normalizedVersionRange.setOpenLower(false);
		normalizedVersionRange.setMaximum(upperBound);
		normalizedVersionRange.setOpenUpper(true);
		normalizedVersionRange.setInterval(true);

		return normalizedVersionRange;
	}
	
	/**
	 * The method ensures a hotfix range up to the next minor version if the versionRange was not already an interval.
	 * The original VersionRange instance is not touched under any circumstances.
	 * In case that the VersionRanges changes a new VersionRange instance is being created and returned otherwise the original instance
	 * is being returned. 
	 */
	public static VersionRange addHotfixRangeIfMissing(VersionRange versionRange) {
		if (versionRange.getInterval())
			return versionRange;
		
		Version lowerBound = versionRange.getDirectMatch();
		
		return hotfixRange(lowerBound);
	}
	

}
