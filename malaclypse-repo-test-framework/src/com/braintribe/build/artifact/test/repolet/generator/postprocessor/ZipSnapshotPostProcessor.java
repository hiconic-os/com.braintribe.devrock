// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet.generator.postprocessor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.function.Predicate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.build.artifact.test.repolet.generator.RepoletContentGeneratorException;
import com.braintribe.build.artifact.test.repolet.generator.filter.SnapshotDirectoryFilter;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * @author pit
 *
 */
public class ZipSnapshotPostProcessor extends AbstractZipPostProcessor {
	private SimpleDateFormat mavenLastUpdatedformat = new SimpleDateFormat("YYYYMMddHHmmss");
	private SimpleDateFormat mavenTimestampformat = new SimpleDateFormat("YYYYMMdd.HHmmss");
	private Predicate<File> directoryFilter = new SnapshotDirectoryFilter();
	private Random random = new Random();

	@Override
	protected void postProcess(File directory) {	
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				if (directoryFilter.test( file)) {
					// modify the maven-metadata
					Date now = new Date();
					File mavenMetadataFile = new File( file, "maven-metadata.xml");					
					String versionString;
				
						Document doc;
						try {
							doc = DomParser.load().from(mavenMetadataFile);
						} catch (Exception e) {
							System.err.println("cannot load document [" + mavenMetadataFile + "]");
							return;
						}
						Element versioningE = DomUtils.getElementByPath( doc.getDocumentElement(), "versioning", false);
						Element lastUpdatedE = DomUtils.getElementByPath(versioningE, "lastUpdated", false);
						// last updated time stamp
						String lastUpdated = mavenLastUpdatedformat.format(now);
						lastUpdatedE.setTextContent( lastUpdated);
						
						// overwrite snapshot
						Element snapshotE = DomUtils.getElementByPath(versioningE, "snapshot", false);
						String timestamp = mavenTimestampformat.format(now);
						DomUtils.setElementValueByPath(snapshotE, "timestamp", timestamp, false);
						int buildNumber = random.nextInt( 999999999);
						DomUtils.setElementValueByPath(snapshotE, "buildNumber", "" + buildNumber, false);
						
						
						
						// add snapshot version 						
						Element snapshotVersionsE = DomUtils.getElementByPath(versioningE, "snapshotVersions", false);
						
						Element snapshotVersionE = doc.createElement( "snapshotVersion");
						snapshotVersionsE.appendChild(snapshotVersionE);
						DomUtils.setElementValueByPath(snapshotVersionE, "extension", "jar", true);
						DomUtils.setElementValueByPath(snapshotVersionE, "updated", lastUpdated, true);
						
						String name = DomUtils.getElementValueByPath(doc.getDocumentElement(), "version", false);						
						String artifact = DomUtils.getElementValueByPath(doc.getDocumentElement(), "artifactId", false);
						String snapshotPrefix = name.substring(0, name.indexOf("-SNAPSHOT"));
						versionString = artifact + "-" + snapshotPrefix + "-" + timestamp + "-" + buildNumber;
						DomUtils.setElementValueByPath(snapshotVersionE, "value", snapshotPrefix + "-" + timestamp + "-" + buildNumber, true);
						
						try {
							DomParser.write().from(doc).to(mavenMetadataFile);
						} catch (Exception e) {
							System.err.println("cannot save file [" + mavenMetadataFile.getAbsolutePath() + "]");
							return;							
						}
												
						String oldValue = artifact + "-" + DomUtils.getElementValueByPath(versioningE, "snapshotVersions/snapshotVersion/value", false);
						// generate n names 
						for (File payload : file.listFiles()) {
							String payloadName = payload.getName();
							if (payloadName.equalsIgnoreCase("maven-metadata.xml"))
								continue;						
							String remainder = payloadName.substring( oldValue.length());
							File newFile = new File( file, versionString + remainder);
							FileTools.copyFile(payload, newFile);
							if (newFile.getName().endsWith(".pom")) {
								try {
									Document pom = DomParser.load().from(newFile);
									DomUtils.setElementValueByPath( pom.getDocumentElement(), "properties/snapshotTag", snapshotPrefix + "-" + timestamp + "-" + buildNumber, true); 						
									DomParser.write().from(pom).to(newFile);
								} catch (DomParserException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
						}
				}
				else { 
					postProcess(file);
				}
			}			
		}
	}
	
	public static void main( String [] args) {
		ZipSnapshotPostProcessor postProcessor = new ZipSnapshotPostProcessor();
		if (args.length % 2 != 0) {
			System.out.println("Usage : <in file> <outfile>");
			return;
		}
		
		for (int i = 0; i <= args.length - 2; i=i+2) {
			File in = new File( args[i]);
			File out = new File( args[i+1]);									
			try {	
				postProcessor.postProcess(in, out);
			} catch (RepoletContentGeneratorException e) {
				System.err.println("cannot postprocess " + e);
			}
		}		
	}
	
	
}
