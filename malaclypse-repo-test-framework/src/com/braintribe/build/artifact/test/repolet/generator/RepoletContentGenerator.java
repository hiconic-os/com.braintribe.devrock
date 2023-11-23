// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet.generator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.test.repolet.generator.filter.DirectoryNameFilter;
import com.braintribe.build.artifact.test.repolet.generator.filter.FileNameFilter;
import com.braintribe.build.artifacts.mc.wire.pomreader.contract.ConfigurablePomReaderExternalContract;
import com.braintribe.build.artifacts.mc.wire.pomreader.contract.PomReaderContract;
import com.braintribe.build.artifacts.mc.wire.pomreader.external.contract.PomReaderExternalContract;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cfg.Configurable;
import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.crypto.hash.md5.MD5HashGenerator;
import com.braintribe.crypto.hash.sha1.Sha1HashGenerator;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * creates a zip containing a subsection of a local repository to mimic a remote repository. The zip contains artifact and solution level maven-metadata.xml, and hashes for all parts.
 * <br/>
 * main call <br/>
 * {@code <group>:<artifact>[#version][[(<prefix>[,prefix]][:<suffix>[suffix],[<group>:<artifact>[#version]];=<zip file>}
 * <br/>
 * example:<br/> 
 * com.braintribe.gm:root-model=res/content/RootModel.repo.zip<br/>
 * com.braintribe.devrock:artifact-model=res/content/ArtifactModel.repo.zip<br/>
 * tribefire.cortex:basic-deployment-model#1.0;tribefire.cortex:basic-deployment-model#1.1=res/content/BasicDeploymentModel-1.x.repo.zip<br/>
 * <br/>
 * com.braintribe.gm:root-model(.updated,maven-metadata:-sources.jar)=res/content/RootModel.repo.zip<br/>
 * <br/>
 * <br/>
 * packs a full maven repo compatible subsection as a zip, starting at {@link RepoletContentGenerator#root}. Any hashes, maven-metadata files are created on the file.
 * <br/>
 * using a file : -<path to file>
 * <br/>
 * @author pit
 *
 */
public class RepoletContentGenerator {
	private static final String PROFILE_USECASE = "PROFILE_USECASE";
	private static final String SNAPSHOT = "-SNAPSHOT";
	private static final String MAVEN_METADATA_XML = "maven-metadata.xml";	
	private SimpleDateFormat mavenLastUpdatedformat = new SimpleDateFormat("YYYYMMddHHmmss");
	private SimpleDateFormat mavenTimestampformat = new SimpleDateFormat("YYYYMMdd.HHmmss");
	private Set<File> packedPoms;
	private Map<String, String> renamedFilesMap;
	private Random random = new Random();
	private File root;
	private MavenSettingsReader mavenSettingsReader;

	@Configurable
	public void setRoot(File root) {
		this.root = root;
	}
		
	/**
	 * recursively prepares the content, i.e. generates all required hashes (MD5, SHA1) and maven metadata files
	 * @param directory - the start point 
	 * @param prefixesToFilter - prefixes of files that should be excluded
	 * @param suffixesToFilter - suffixes of files that should be excluded 
	 * @return - true if the directory to process has no child directory (is a leaf)
	 * @throws RepoletContentGeneratorException - arrgh
	 */
	private boolean prepareContent( File directory, String [] prefixesToFilter, String [] suffixesToFilter) throws RepoletContentGeneratorException {
		
		File [] filesToProcess = directory.listFiles();
		boolean noMoreDirectories = true;
		boolean childHasNoMoreDirectories = false;
		List<File> files = new ArrayList<File>();
		List<File> directories = new ArrayList<File>();
		
		if (filesToProcess == null) {
			throw new IllegalStateException( "no files found within [" + directory.getAbsolutePath() + "]");
		}
		
		for (File file : filesToProcess) {
			if (file.isDirectory()) {
				childHasNoMoreDirectories = prepareContent( file, prefixesToFilter, suffixesToFilter);
				directories.add( file);
				noMoreDirectories = false;
			}
			else {
				boolean noPack = false;
				if (prefixesToFilter != null && prefixesToFilter.length > 0) {
					for (String prefix : prefixesToFilter) {
						if (file.getName().startsWith( prefix)) {
							noPack = true;
							break; 
						}
					}
				}
				if (suffixesToFilter != null && suffixesToFilter.length > 0) {
					for (String suffix : suffixesToFilter) {
						System.out.println( file.getName() + ":" + suffix + "->" + file.getName().endsWith(suffix));
						if (file.getName().endsWith(suffix)) {
							noPack = true;
							break;
						}
					}
				}
				if (!noPack) {
					files.add(file);
				}
			}
		}
		// end point - solution level 
		if (noMoreDirectories) {		
			for (File file : files) {
				// 
				if (file.getName().endsWith( ".pom")) {
					// mark
					packedPoms.add( file);
				}
				// hashes
				createHashes( file);
				System.out.println("create hash for [" + file.getAbsolutePath() + "]");
			}
			// metadata
			System.out.println("create solution metadata for [" + directory.getAbsolutePath() + "]");
			File metaDataFile = createSolutionMetaData( directory);
			createHashes( metaDataFile);
		}		
		else {
			if (childHasNoMoreDirectories) {
				System.out.println("create artifact metadata for [" + directory.getAbsolutePath() + "]");
				File metaDataFile = createArtifactMetaData( directory, directories);
				createHashes( metaDataFile);
			}
		}
		return noMoreDirectories;
	}
	
	/**
	 * create the two hash files for the file passed 
	 * @param file - file to create the hashes for 
	 * @throws RepoletContentGeneratorException - arrgh
	 */
	private void createHashes( File file) throws RepoletContentGeneratorException{
		if (file.getName().endsWith(".md5") || file.getName().endsWith(".sha1")) {
			return;
		}
		byte [] bytes = FileTools.readBytesFromFile( file);
		
		try {
			String md5Hash = MD5HashGenerator.MD5(bytes);
			File hashFile = new File( file.getAbsolutePath() + ".md5");
			IOTools.spit( hashFile, md5Hash, "UTF-8", false);
			hashFile.deleteOnExit();
		} catch (CryptoServiceException e) {
			String msg = "cannot create md5 hash for [" + file.getAbsolutePath() + "]";
			throw new RepoletContentGeneratorException(msg, e);
		} catch (IOException e) {		
			String msg = "cannot save md5 hash for [" + file.getAbsolutePath() + "]";
			throw new RepoletContentGeneratorException(msg, e);
		}
		
		try {
			String sha1Hash = Sha1HashGenerator.SHA1(bytes);
			File hashFile = new File( file.getAbsolutePath() + ".sha1");
			IOTools.spit( hashFile, sha1Hash, "UTF-8", false);
			hashFile.deleteOnExit();
		} catch (CryptoServiceException e) {
			String msg = "cannot create sha1 hash for [" + file.getAbsolutePath() + "]";
			throw new RepoletContentGeneratorException(msg, e);
		} catch (IOException e) {		
			String msg = "cannot create sha1 hash for [" + file.getAbsolutePath() + "]";
			throw new RepoletContentGeneratorException(msg, e);
		}
		
	}
	
	/**
	 * helper class: an artifact 
	 * @author pit
	 *
	 */
	private class Artifact {
		public String grp;
		public String artifact;		
	}
	
	/**
	 * a helper class: a solution (derives from {@link Artifact})
	 * @author pit
	 *
	 */
	private class Solution extends Artifact {
		public String version;
	}
	
	/**
	 * extract the {@link Solution} from directory name 
	 * @param directory - {@link File} to extract the solution from  
	 * @return - the generated {@link Solution}
	 */
	private Solution extractSolution( File directory){
		// <grp part>[/<grp part>]/<artifact part>/<version part>
		Solution solution = new Solution();
		String name = directory.getAbsolutePath().replace('\\', '/');
		name = name.substring( root.getAbsolutePath().length() + 1);
		int vd = name.lastIndexOf('/');
		solution.version = name.substring(vd+1);
		
		String remainder = name.substring(0, vd);
		int ad = remainder.lastIndexOf('/');
		solution.artifact = remainder.substring( ad+1);
		
		remainder = remainder.substring(0, ad);		
		solution.grp = remainder.replace( '/', '.');
		
		return solution;
	}
	
	
	
	/**
	 * create maven metadata for an {@link Solution}
	 * @param directory - the {@link File} the create the metadata for 
	 * @return - the written meta data file 
	 * @throws RepoletContentGeneratorException - arrgh
	 */
	private File  createSolutionMetaData( File directory) throws RepoletContentGeneratorException{	
		try {
			Solution solution = extractSolution(directory);
			Document document = DomParser.create().makeItSo();
			Element metadataE = document.createElement( "metadata");
			document.appendChild(metadataE);
			
			Element grpE = document.createElement("groupId");
			grpE.setTextContent( solution.grp);
			metadataE.appendChild(grpE);
			
			Element artE = document.createElement("artifactId");
			artE.setTextContent( solution.artifact);
			metadataE.appendChild(artE);
			
			Element vrsE = document.createElement("version");
			vrsE.setTextContent( solution.version);
			metadataE.appendChild(vrsE);
			
			String name = directory.getName();
			
			Date now = new Date();
			
			// if the version's a snapshot, we must add special data to the maven meta data 
			if (name.endsWith( SNAPSHOT)) {
				Element versioningE = document.createElement("versioning");
				metadataE.appendChild(versioningE);
				
				Element lastUpdatedE = document.createElement("lastUpdated");
				versioningE.appendChild(lastUpdatedE);
				lastUpdatedE.setTextContent( mavenLastUpdatedformat.format(now));
				
				
				Element snapshotE = document.createElement( "snapshot");
				versioningE.appendChild(snapshotE);
				
				Element timestampE = document.createElement( "timestamp");
				snapshotE.appendChild(timestampE);
				String timestamp = mavenTimestampformat.format(now);
				timestampE.setTextContent( timestamp);
				
				Element buildNumberE = document.createElement( "buildNumber");
				snapshotE.appendChild(buildNumberE);
				int buildNumber = random.nextInt( 999999999);
				
				buildNumberE.setTextContent( "" + buildNumber);
				
				// add a fake snapshot version directory 
				Element snapshotVersionsE = document.createElement( "snapshotVersions");
				versioningE.appendChild( snapshotVersionsE);
				
				Element snapshotVersionE = document.createElement( "snapshotVersion");
				snapshotVersionsE.appendChild( snapshotVersionE);
				
				Element extensionE = document.createElement("extension");
				extensionE.setTextContent("jar");
				snapshotVersionE.appendChild(extensionE);
				
				String snapshotPrefix = name.substring(0, name.indexOf(SNAPSHOT));
				String versionString = snapshotPrefix + "-" + timestamp + "-" + buildNumber;
				Element valueE = document.createElement("value");
				valueE.setTextContent( versionString);
				snapshotVersionE.appendChild(valueE);
				
				
				
				Element updatedE = document.createElement("updated");
				updatedE.setTextContent( mavenLastUpdatedformat.format(now) );
				snapshotVersionE.appendChild(updatedE);
				
				// modify the files in the directory 
				for (File file : directory.listFiles()) {
					if (file.getName().startsWith( "maven-metadata"))
						continue;
					String oldName = file.getName();
					String remainder = oldName.substring( solution.artifact.length() + name.length()+1);
					String newName = solution.artifact + "-" + versionString + remainder;
					
					File newFile =  new File( directory, newName);
					renamedFilesMap.put( newFile.getAbsolutePath(), file.getAbsolutePath());
					
					if (oldName.endsWith( ".pom")){
						packedPoms.remove(file);
						file.renameTo( newFile);
						packedPoms.add( newFile);
						
						Document pom = DomParser.load().from(newFile);
						DomUtils.setElementValueByPath( pom.getDocumentElement(), "properties/snapshotTag", snapshotPrefix + "-" + timestamp + "-" + buildNumber, true); 						
						DomParser.write().from(pom).to(newFile);
					}							
					else {
						file.renameTo( newFile);
					}
				}					
														
			}
			
			
			
			File metadataFile = new File( directory, MAVEN_METADATA_XML);
			DomParser.write().from(document).to( metadataFile);
			metadataFile.deleteOnExit();
			return metadataFile;
			
		} catch (DOMException e) {
			String msg ="cannot manipulate DOM";
			throw new RepoletContentGeneratorException(msg, e);
		} catch (DomParserException e) {
			String msg ="cannot manipulate document";
			throw new RepoletContentGeneratorException(msg, e);
		}
	}
	
	/**
	 * extract the artifact from the directory 
	 * @param directory - {@link File} directory to get the artifact from 
	 * @return - the generated {@link Artifact}
	 */
	private Artifact extractArtifact( File directory) {
		Artifact artifact = new Artifact();
		String name = directory.getAbsolutePath().replace('\\', '/');
		name = name.substring( root.getAbsolutePath().length() + 1);		
			
		int ad = name.lastIndexOf('/');
		artifact.artifact = name.substring( ad+1);
		
		String remainder = name.substring(0, ad);		
		artifact.grp = remainder.replace( '/', '.');
		
		return artifact;
	}
	
	/**
	 * create a maven metadata file for an {@link Artifact}
	 * @param directory - {@link File} of the artifact 
	 * @param directories - all version directories with the solutions
	 * @return - the written meta data file 
	 * @throws RepoletContentGeneratorException - arrgh
	 */
	private File createArtifactMetaData( File directory, List<File> directories) throws RepoletContentGeneratorException {
		Date now = new Date();
		String lastUpdated = mavenLastUpdatedformat.format( now);
		
		
		try {
			Artifact artifact = extractArtifact(directory);
			Document document = DomParser.create().makeItSo();
			Element metadataE = document.createElement( "metadata");
			document.appendChild(metadataE);
			
			Element grpE = document.createElement("groupId");
			grpE.setTextContent( artifact.grp);
			metadataE.appendChild(grpE);
			
			Element artE = document.createElement("artifactId");
			artE.setTextContent( artifact.artifact);
			metadataE.appendChild(artE);
			
			Element versioningE = document.createElement("versioning");
			metadataE.appendChild(versioningE);
			
			Element versionsE = document.createElement("versions");
			versioningE.appendChild(versionsE);			
			
			for (File suspect : directories) {
				String name = suspect.getName();
				Element versionE = document.createElement("version");
				versionE.setTextContent( name);
				versionsE.appendChild(versionE);
				
			
			}
			
			// 
			
			Element lastUpdateE = document.createElement( "lastUpdated");
			lastUpdateE.setTextContent( lastUpdated);
			versioningE.appendChild(lastUpdateE);
			File file = new File( directory, MAVEN_METADATA_XML);
			DomParser.write().from(document).to( file);
			file.deleteOnExit();
			return file;
			
		} catch (DOMException e) {
			String msg ="cannot manipulate DOM";
			throw new RepoletContentGeneratorException(msg, e);
		} catch (DomParserException e) {
			String msg ="cannot manipulate document";
			throw new RepoletContentGeneratorException(msg, e);
		}
		
	}
	
	/**
	 * pack the zip, starting at {@link RepoletContentGenerator#root}, but until reaching the beef only take up the directories 
	 * @param target - the zip file 
	 * @param sources - the directories to include 
	 * @throws RepoletContentGeneratorException - arrgh
	 */
	private void packContent( File target, FileNameFilter fileNameFilter, File ... sources) throws RepoletContentGeneratorException{
		try {

			Predicate<File> disjunction = null;
			
			// build directory files 
			for (File source : sources) {
				if (disjunction == null) {
					disjunction = new DirectoryNameFilter(source); 
				} else {
					disjunction = disjunction.or(new DirectoryNameFilter(source));
				}
			}
			if (disjunction == null) {
				disjunction = f -> false; //Implementor's note: the original code would have an empty array of filters, which would return false (like this one). 
			}
			
			Predicate<File> filter = fileNameFilter.and(disjunction);

			// pack
			Archives.zip().pack(root, filter).to(target).close();
		} catch (ArchivesException e) {
			String msg = "cannot pack [" + target.getAbsolutePath() + "]";
			throw new RepoletContentGeneratorException(msg, e);
		}
	}
		
	
	/**
	 * generate the contents of the zip file 
	 * @param archive - {@link File} to write to 
	 * @param artifactsToPackage - the artifacts to pack into it
	 * @throws RepoletContentGeneratorException - arrgh
	 */
	public void generateRepositoryContents( File archive,  String ...artifactsToPackage) throws RepoletContentGeneratorException {
		
		setup();
		
		List<File> files = new ArrayList<File>( artifactsToPackage.length);
		Map<String, List<String>> artifactToVersionsMap = new HashMap<String, List<String>>();
		packedPoms = CodingSet.createHashSetBased( new FileWrapperCodec());
		renamedFilesMap = new HashMap<String, String>();

		// build the file names and note the explicit solutions (if any)
		for (String file : artifactsToPackage) {
			int filterP = file.indexOf( '(');
			String filters = null;
			if (filterP > 0) {
				filters = file.substring(filterP);
				file = file.substring(0, filterP);
			}
			// condensed 
			int pGrp = file.indexOf(':');
			String grp = file.substring(0, pGrp);
			int pVrs = file.indexOf( '#');
			String art, vrs;
			if (pVrs > 0) {
				art = file.substring( pGrp+1, pVrs);
				vrs = file.substring( pVrs+1);
			}
			else {
				art = file.substring( pGrp+1);
				vrs = null;
			}
			File processFile;
			if (vrs != null) {
				 processFile = new File( root, grp.replace('.', File.separatorChar) + File.separator + art + File.separator + vrs);
				files.add( processFile);
				List<String> vrss = artifactToVersionsMap.get( grp + ":" + art);
				if (vrss == null) {
					vrss = new ArrayList<String>();
					artifactToVersionsMap.put( grp + ":" + art, vrss);
				}
				vrss.add( vrs);				 
			} else {
				processFile= new File( root, grp.replace('.', File.separatorChar) + File.separator + art);
				files.add( processFile);
			}			
			//
			List<String> prefixes = new ArrayList<String>();
			prefixes.add( ".updated");
			prefixes.add( "maven-metadata");			
			List<String> suffixes = new ArrayList<String>();
			
			if (filters != null) {
				filters = filters.substring(1, filters.length() -1); // get rid of parenthesis
				String [] headsAndTails = filters.split(":");
				String [] heads = headsAndTails[0].split(",");
				prefixes.addAll(Arrays.asList( heads));
				if (headsAndTails.length == 2) {					
					String [] tails = headsAndTails[1].split(",");
					suffixes.addAll( Arrays.asList( tails));
				}
			}
				
			Object [] filterValues = new Object[2];
			filterValues[0] = prefixes;
			filterValues[1] = suffixes;			
			
			prepareContent( processFile, prefixes.toArray( new String[0]), suffixes.toArray( new String[0]));
		}	
		// create hashes, meta data etc 

		// post process - if any solutions have been passed, we must create their artifact's meta data  
		for (Entry<String, List<String>> entry : artifactToVersionsMap.entrySet()) {
			String art = entry.getKey();
			String [] parts = art.split( ":");
			File location = new File( root, parts[0].replace('.', File.separatorChar) + File.separator + parts[1]);
			List<File> directories = new ArrayList<File>();
			for (String vrs : entry.getValue()) {
				File file = new File( location, vrs);
				directories.add(file);
			}
			createArtifactMetaData(location, directories);			
		}
		File listingFile = new File( root, "artifacts.lst");
		for (File file : packedPoms) {
			try {
				Document document = DomParser.load().from( file);
				String groupId = DomUtils.getElementValueByPath( document.getDocumentElement(), "groupId", false);
				String artifactId = DomUtils.getElementValueByPath( document.getDocumentElement(), "artifactId", false);
				String version = DomUtils.getElementValueByPath( document.getDocumentElement(), "version", false);				
				IOTools.spit(listingFile, groupId + ":" + artifactId + "#" + version + "\n", "UTF-8", true);
			} catch (Exception e) {
				throw new RepoletContentGeneratorException( "cannot create listing file", e);				
			} 						
		}
		FileNameFilter filter = new FileNameFilter();
		packContent( archive, filter, files.toArray( new File[0]));
		listingFile.delete();
		
		// rename the files back (snapshot stuff) 
		for (Entry<String, String> entry : renamedFilesMap.entrySet()) {
			new File( entry.getKey()).renameTo( new File( entry.getValue()));
		}
	}
	
	public static PomReaderContract getPomReaderContract(String usecase) {
		ConfigurablePomReaderExternalContract ec = new ConfigurablePomReaderExternalContract();
		
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		if (usecase != null) {
			ove.addEnvironmentOverride( PROFILE_USECASE, usecase);
		}
		ec.setVirtualEnvironment( ove);
			
		
		WireContext<PomReaderContract> wireContext = Wire.context( PomReaderContract.class)
				.bindContracts("com.braintribe.build.artifacts.mc.wire.pomreader")
				.bindContract( PomReaderExternalContract.class, ec)
				.build();				

		return wireContext.contract();
	}
	
	private void setup() {
		if (root != null)
			return;
		
		PomReaderContract pomReaderContract = getPomReaderContract(null);
		mavenSettingsReader = pomReaderContract.settingsReader();
		
		root = new File(mavenSettingsReader.getLocalRepository(null));
		if (root.exists() == false) {
			throw new IllegalStateException( "cannot determine location of local repository");
		}
		
	}

	private void run(String arg) {
		String [] values = arg.split( "=");
		if (values.length < 2) {
			System.err.println("usage <artifact/solution>[;artifact/solution]=<zip file>");
			return;
		}
		String [] startArtifacts = values[0].split( ";");
						
		File out = new File( values[1]);
		try {
			generateRepositoryContents(out, startArtifacts);
		} catch (RepoletContentGeneratorException e) {
			System.err.println( "cannot generate content for [" + arg + "] as " + e.getMessage());
		}
	}
	
	private void run(File file) {
		try {
			String cmd = IOTools.slurp( file, "UTF-8");
			String [] tokens = cmd.split( "\n");
			List<String> artifacts = new ArrayList<String>();
			File out = null;
			for (String token : tokens) {
				if (token.startsWith( "=")) {
					out = new File( token.substring(1).trim());			
				}
				else {
					artifacts.add( token.trim());
				}
			}
			if (out == null) {
				System.err.println("no output file passed");
				return;
			}
			try {
				generateRepositoryContents(out, artifacts.toArray( new String[0]));
			} catch (RepoletContentGeneratorException e) {
				System.err.println( "cannot generate content for [" + file.getAbsolutePath() + "] as " + e.getMessage());
			}
			
		} catch (IOException e) {
			System.err.println( "cannot generate parameters from[" + file.getAbsolutePath() + "] as " + e.getMessage());
		}
	}
	
	
	public static void main(String [] args){
		RepoletContentGenerator generator = new RepoletContentGenerator();
		
		for (String arg : args) {
			if (arg.startsWith( "!"))
				continue;
			if (arg.startsWith("-")) {
				File file = new File( arg.substring(1));
				generator.run( file);
			}
			else {
				generator.run(arg);
			}
		}
	}
	
	
}
 
