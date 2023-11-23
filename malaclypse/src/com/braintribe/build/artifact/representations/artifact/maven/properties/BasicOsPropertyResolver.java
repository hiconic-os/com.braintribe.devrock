package com.braintribe.build.artifact.representations.artifact.maven.properties;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a cheap copy of the os-maven-plugin - see https://github.com/trustin/os-maven-plugin.git 
 * 
 * @author pit
 *
 */
public class BasicOsPropertyResolver implements OsPropertyResolver {

	private static final String OS_VERSION = "os.version";
	private static final String OS_ARCH = "os.arch";
	private static final String OS_NAME = "os.name";

    private static final Pattern VERSION_REGEX = Pattern.compile("((\\d+)\\.(\\d+)).*");
  
    
	private Map<String, String> properties;

	private Map<String, Predicate<String>> nameFilters;
	private Map<String, Predicate<String>> archFilters;
	
	{
		// filter map for os name
		nameFilters = new HashMap<>();
		nameFilters.put( "aix", (v) -> v.startsWith( "aix"));
		nameFilters.put( "hpux", (v) -> v.startsWith( "hpux"));
		nameFilters.put( "linux", (v) -> v.startsWith( "linux"));
		nameFilters.put( "osx", (v) -> v.startsWith( "macosx") || v.startsWith("osx"));
		nameFilters.put( "freebsd", (v) -> v.startsWith( "freebsd"));
		nameFilters.put( "openbsd", (v) -> v.startsWith( "openbsd"));
		nameFilters.put( "netbsd", (v) -> v.startsWith( "netbsd"));
		nameFilters.put( "sunos", (v) -> v.startsWith( "sunos"));
		nameFilters.put( "windows", (v) -> v.startsWith( "windows") || v.startsWith( "Windows 10"));
		nameFilters.put( "zos", (v) -> v.startsWith( "zos"));
		 
		nameFilters.put( "os400", (v) -> {
			if (v.startsWith( "os400")) {
				if (v.length() > 5) {
					char c = v.charAt( v.length()-1);
					return !(Character.isDigit(c));
				}
				return true;
			}
			return false;
		});
		
		// filter map for os arch
		archFilters = new HashMap<>();
		archFilters.put( "x86_32", (v) -> isAnyOf( v, "x8632", "x86", "i386", "i486", "i586", "i686", "ia32", "x32"));
		archFilters.put( "x86_64", (v) -> isAnyOf( v, "x8664", "amd64", "ia32e", "em64t", "x64"));
		archFilters.put( "itanium_32", (v) -> v.equalsIgnoreCase("ia64n"));
		archFilters.put( "itanium_64", (v) -> isAnyOf( v, "ia64", "ia64w", "itanium64"));
		archFilters.put( "sparc_32", (v) -> isAnyOf( v, "sparc", "sparc32"));
		archFilters.put( "sparc_64", (v) -> isAnyOf( v, "sparcv9", "sparc64"));
		archFilters.put( "arm_32", (v) -> isAnyOf( v, "arm", "arm32"));
		archFilters.put( "aarch_64", (v) -> v.equalsIgnoreCase("aarch64"));
		archFilters.put( "mips_32", (v) -> isAnyOf( v, "-mips", "mips32"));
		archFilters.put( "mips_64", (v) -> v.equalsIgnoreCase("-mips64"));
		archFilters.put( "mipsel_32", (v) -> isAnyOf( v, "-mipsel", "mips32el"));
		archFilters.put( "mipsel_64", (v) -> v.equalsIgnoreCase("-mips64el"));
		archFilters.put( "ppc_32", (v) -> isAnyOf( v, "-ppc", "ppc32"));
		archFilters.put( "ppc_64", (v) -> v.equalsIgnoreCase("ppc64"));
		archFilters.put( "ppcle_32", (v) -> isAnyOf( v, "-ppcle", "ppc32le"));
		archFilters.put( "ppcle_64", (v) -> v.equalsIgnoreCase("ppc64le"));
		archFilters.put( "s390_32", (v) -> v.equalsIgnoreCase("s390"));
		archFilters.put( "s390_64", (v) -> v.equalsIgnoreCase("s390x"));
		
	}
	
	
	/**
	 * lill' helper 
	 * @param suspect
	 * @param values
	 * @return
	 */
	private static boolean isAnyOf( String suspect, String ... values) {
		for (String value : values) {
			if (suspect.equalsIgnoreCase(value))
				return true;
		}
		return false;
	}
		

	private Object osPropertyResolverInitializerMonitor = new Object();

	/**
	 * lazy instantiation / resolving 
	 * @return - a {@link Map} of 'standard key' and actual value 
	 */
	private Map<String,String> getProperties() {
		if (properties != null)
			return properties;
		
		synchronized ( osPropertyResolverInitializerMonitor) { 
			if (properties != null)
				return properties;
			
			Properties allProps = new Properties(System.getProperties());
			
			Map<String,String> _properties = new HashMap<>();
			
			// os name
			String osName = normalize(allProps.getProperty(OS_NAME));
			boolean found = false;
			for (Entry<String, Predicate<String>> entry : nameFilters.entrySet()) {
				if (entry.getValue().test(osName)) {
					_properties.put( DETECTED_NAME, entry.getKey());
					found = true;
					break;
				}				
			}
			if (!found) {
				_properties.put( DETECTED_NAME, "UNKNOWN");
			}
			
			// os arch
			String osArch = normalize( allProps.getProperty(OS_ARCH));
			found = false;
			for (Entry<String, Predicate<String>> entry : archFilters.entrySet()) {
				if (entry.getValue().test(osArch)) {
					_properties.put( DETECTED_ARCH, entry.getKey());
					found = true;
				}
			}
			if (!found) {
				_properties.put( DETECTED_ARCH, "UNKNOWN");
			}
			
			
			// os version
			String osVersion = normalize(allProps.getProperty(OS_VERSION));			
			Matcher versionMatcher = VERSION_REGEX.matcher(osVersion);
			found = false;
		    if (versionMatcher.matches()) {
		    	
		    	_properties.put( DETECTED_VERSION, versionMatcher.group(1));
		        _properties.put( DETECTED_VERSION_MAJOR, versionMatcher.group(2));
		        _properties.put( DETECTED_VERSION_MINOR, versionMatcher.group(3));
		     }
		    else {
		    	_properties.put( DETECTED_VERSION, "UNKNOWN");
		        _properties.put( DETECTED_VERSION_MAJOR, "UNKNOWN");
		        _properties.put( DETECTED_VERSION_MINOR, "UNKNOWN");				
			}
		    
		    
		    // detected classifier
		    StringBuilder detectedClassifierBuilder = new StringBuilder();
	        detectedClassifierBuilder.append( _properties.get( DETECTED_NAME));
	        detectedClassifierBuilder.append('-');
	        detectedClassifierBuilder.append( _properties.get( DETECTED_ARCH));
	        //
	        // here would need to go the code for the linux stuff - see https://github.com/trustin/os-maven-plugin.git
	        //
	        _properties.put( DETECTED_CLASSIFIER, detectedClassifierBuilder.toString());
	        
	        
	        // assign 
			properties = _properties;
			
			return properties;
		}
			
	}
	
	private static String normalize(String value) {
		if (value == null) {
		    return "";
		}
		return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
	}
	

	@Override
	public String expand(String variable) {
		return getProperties().get(variable);
	}

}
