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
package com.braintribe.test.framework.resultbuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * retrieves the dependency list as defined by maven (from the dist directory), iterates of the local repository to 
 * build a map of jar-file vs pom-file, and compiles a file with the <group>:<artifact>#version from it. 
 * @author pit
 *
 */
public class WalkResultBuilder {
	private File localRepositoryDirectory;
	private ResultTuple [] resultTuples;
	
	public void setLocalRepositoryDirectory(File localRepositoryDirectory) {
		this.localRepositoryDirectory = localRepositoryDirectory;
	}	
	public void setResultTuples(ResultTuple ... resultTuples) {
		this.resultTuples = resultTuples;
	}
	
	private Map<File, File> enumerateLocalRepository() {
		return enumerate(localRepositoryDirectory);
	}
	
	private Map<File, File> enumerate(File directory) {
		Map<File, File> result = CodingMap.createHashMapBased( new FileWrapperCodec());
		if (!directory.exists())
			return result;
		File pomFile = null;
		File jarFile = null;
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				result.putAll( enumerate(file));
			}
			if (file.getName().endsWith( ".jar")) {
				jarFile = file;
				continue;
			}
			if (file.getName().endsWith( ".pom")) {
				pomFile = file;
				continue;
			}				
		}
		result.put(jarFile, pomFile);		
		return result;
	}
	
	public void map() {
		Map<File, File> map = enumerateLocalRepository();
		for (ResultTuple tuple : resultTuples) {
			File input = tuple.getInput();
			File [] files = input.listFiles();
			List<String> lines = new ArrayList<String>(); 
			for (File file : files) {
				String fileName = file.getName();
				// sometimes a pom's found in the distribution (TribefireServicesDeps)
				if (fileName.endsWith( ".pom")) {
					try {
						Document doc = DomParser.load().from( file);
						String grp = DomUtils.getElementValueByPath( doc.getDocumentElement(), "groupId", false);
						String art = DomUtils.getElementValueByPath( doc.getDocumentElement(), "artifactId", false);
						String vrs = DomUtils.getElementValueByPath( doc.getDocumentElement(), "version", false);
						String name = grp + ":" + art + "#" + vrs;
						if (name.equalsIgnoreCase( tuple.getTerminal())) { 
							continue;
						}						
						lines.add(name);
					} catch (DomParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					continue;
				}
				if (fileName.endsWith(".jar")) {
					File pomFile = map.get( file);
					try {
						Document doc = DomParser.load().from(pomFile);
						String grp = DomUtils.getElementValueByPath( doc.getDocumentElement(), "groupId", false);
						if (grp == null) {			
							Element parent = DomUtils.getElementByPath( doc.getDocumentElement(), "parent", false);
							if (parent != null) {
								grp = DomUtils.getElementValueByPath(parent, "groupId", false);								
							}
							if (grp == null) {
								String name = pomFile.getParentFile().getParentFile().getAbsolutePath();
								grp = name.substring( localRepositoryDirectory.getAbsolutePath().length()+1).replace( "\\", ".");							
								System.out.println("Warning: no group found in pom, using parent directory [" + grp + "] for [" + fileName+ "]");
							}
							else {
								System.out.println("Warning: no group found in pom, using parent's group [" + grp + "] for [" + fileName+ "]");	
							}
						}
						String artifactId = DomUtils.getElementValueByPath( doc.getDocumentElement(), "artifactId", false);
						if (artifactId == null) {
							artifactId = pomFile.getParentFile().getParentFile().getName();
							System.out.println("Warning: no artifact id found in pom, using parent directory [" + artifactId + "] for [" + fileName+ "]");
						}
						
						String jarName = fileName.substring( 0, fileName.indexOf(".jar"));
						String version = jarName.substring( artifactId.length()+1);
						String name = grp + ":" + artifactId + "#" + version;
						if (name.equalsIgnoreCase( tuple.getTerminal())) { 
							continue;
						}						
						lines.add(name);						
					} catch (DomParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}	
			lines.sort( new Comparator<String>() {
				@Override
				public int compare(String arg0, String arg1) {					
					return arg0.compareTo(arg1);
				}
			});
			
			StringBuilder builder = new StringBuilder();
			for (String line : lines) {
				if (builder.length() > 0) {
					builder.append( System.lineSeparator());
				}
				builder.append( line);
			}
			File ouput = tuple.getOutput();
			try {
				IOTools.spit(ouput, builder.toString(), "UTF-8", false);
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
	}
	
	public static void main( String [] args) {
		File workingCopyRoot = new File( System.getenv( "BT__ARTIFACTS_HOME"));
		
		WalkResultBuilder builder = new WalkResultBuilder();
		builder.setLocalRepositoryDirectory( new File( System.getenv( "M2_REPO")));
		String localSvn = System.getenv( "BT__ARTIFACTS_HOME");
		builder.setResultTuples( 
					new ResultTuple(
								"com.braintribe.build.artifacts:Malaclypse#2.0",
								new File("res/walk/results/Malaclypse#2.0.launch.txt"),
								new File( localSvn + "/com/braintribe/build/artifacts/Malaclypse/2.0/dist/lib")
							),
					new ResultTuple(
							"com.braintribe.product.tribefire:TribefireServicesDeps#1.1",
							new File("res/walk/results/TribefireServicesDeps#1.1.launch.txt"),
							new File( localSvn + "/com/braintribe/product/tribefire/TribefireServicesDeps/1.1/dist/lib")
					),
					new ResultTuple(
							"com.braintribe.product.tribefire:TribefireServicesDeps#2.0",
							new File("res/walk/results/TribefireServicesDeps#2.0.launch.txt"),
							new File( localSvn + "/com/braintribe/product/tribefire/TribefireServicesDeps/2.0/dist/lib")
					),
					new ResultTuple(
							"com.braintribe.utils:TikaMimeTools#1.1",
							new File("res/walk/results/TikaMimeTools#1.1.launch.txt"),
							new File( localSvn + "/com/braintribe/utils/TikaMimeTools/1.1/dist/lib")
					)
				);
		builder.map();
	}
}
