// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.commons.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.build.artifact.api.DependencyResolver;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifacts.mc.wire.buildwalk.BuildDependencyResolvers;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.GeneralConfigurationContract;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.context.WireContext;

/**
 * {@link #main(String[]) Launches} commands with the help of the <code>BtBuildCommands</code> artifact. Based on the
 * passed arguments, it downloads the defined commands zip file and extracts it to the <code>BT__COMMANDS_HOME</code>
 * folder (default ${HOME}/.bt_cmd). Afterwards, it executes the command in a forked JVM while having the knowledge on
 * how to define the classpath. Command is specified by parameterizing its denotation type and passed as a
 * <code>JSON</code>.
 * </p>
 * The CommandLauncher depends on <code>Malaclypse</code> in order to download the commands zip file. Therefore it
 * expects a maven settings file which contains information about the necessary repositories. First, it checks in
 * <code>${HOME}/.m2/settings.xml</code>, then in <code>${maven.home}/conf/settings.xml</code> and finally it merges
 * their content with the user-specific settings.xml being dominant. One can overwrite these arguments by passing the
 * arguments (customUserHome, customMavenHome) and redefine the user home and the maven home.
 */
public class CommandLauncher {

	private static Logger log = Logger.getLogger(CommandLauncher.class);

	private static final String COMMAND_ARTIFACT = "commandArtifact";
	private static final String COMMAND_JSON = "command";
	private static final String PROFILE_USECASE = "PROFILE_USECASE";
	private static final String COMMAND_JVM_ARG = "jvmArg"; 
	private static final String CUSTOM_USER_HOME = "customUserHome"; 
	private static final String CUSTOM_MAVEN_HOME = "customMavenHome"; 
	private static final String DEBUG_LAUNCHER_PARAMETER = "DEBUG";
	private static Map<String, List<String>> arguments;

	private static String loaderParamFile = "command-classpath";
	private static String commandClass = "com.braintribe.build.CommandRunner";
	private static PartTuple commandTuple = PartTupleProcessor.fromString("cmd", "zip");
	private static String useCase = "DEVROCK";
	private static String loaderJar = "FileClasspathClassloader-1.0.jar";

	/**
	 * Launches the {@link CommandLauncher} with the following arguments which are expected as an array of
	 * <code>argumentName=argumentValue<code> (look example). Same argument might be passed more than one times.
	 * <ul>
	 * <li>commandArtifact : the artifact that contains the command to launch</li>
	 * <li>command : the command to run (specified as JSON)</li>
	 * <li>jvmArg : the jvm arguments passed to the forked JVM that launches the command (Optional)</li>
	 * <li>customUserHome : the user home path when we want to overwrite the existed one (Optional)</li>
	 * <li>customMavenHome : the maven home path when we want to overwrite the existed one (Optional)</li>
	 * <li>DEBUG : the DEBUG options to pass to the forked JVM that launches the command (Optional)</li>
	 * </ul>
	 * 
	 * </p>
	 * Arguments example:</br>
	 * <ul>
	 * <li>commandArtifact=com.braintribe.build:DummyCommands#1.0</li>
	 * <li>command={_type:"com.braintribe.build.cmd.dummy.declaration.DummyCommand", logMessage:"Hello there"}</li>
	 * <li>jvmArg=-Xms128M</li>
	 * <li>jvmArg=-Xmx1024M</li>
	 */
	public static void main(String[] args) throws com.braintribe.build.commons.launcher.BuildException {
		log.info("Launching " + CommandLauncher.class.getName() + " with: " + Stream.of(args).collect(Collectors.joining(" ")));
 
		arguments = parseLauncherArguments(args);
		arguments.forEach((k, v) -> log.info("KEY: " + k + " VALUE: " + v));

		Part commandPackage = resolveCommandPackage(arguments.get(COMMAND_ARTIFACT).get(0));

		String commandsHome = System.getenv("BT__COMMANDS_HOME");
		if (commandsHome == null) {
			String userHome = System.getProperty("user.home");
			commandsHome = userHome + String.format("%s.bt_cmd", File.separator);
		}
		File commandsHomeFile = new File(commandsHome);

		// prime loader - if required, copy file classpath loader to command directory,
		// otherwise just get the location
		primeLoader(commandsHomeFile, loaderJar);

		String commandPackageName = NameParser.buildName(commandPackage);
		String localName = commandPackageName.replace(":", ".");

		File unpackDir = new File(commandsHomeFile, localName + String.format("%scp%s", File.separator, File.separator));

		File paramFile = null;
		// check if command needs to be unpacked
		if (!unpackDir.exists()) {
			File zipFile = new File(commandPackage.getLocation());
			try {
				ZipContext zc = Archives.zip().from(zipFile);
				zc.unpack(unpackDir).close();
			} catch (ArchivesException e) {
				String msg = "cannot unpack [" + zipFile.getAbsolutePath() + "] to [" + unpackDir + "]";
				throw new BuildException(msg, e);
			}
			log.info(String.format("unpacked command to [%s]", unpackDir.getAbsolutePath()));
			// update parameter file
			paramFile = buildLoaderParameterFile(unpackDir, loaderParamFile);
		} else {
			paramFile = new File(unpackDir, loaderParamFile);
		}
		// the param file should have been built while unpacking or already present in
		// the unpack directory
		if (paramFile.exists() == false) {
			String msg = String.format("loader parameter file [%s] isn't present and couldn't have been created", paramFile.getAbsolutePath());
			throw new BuildException(msg);
		}

		String json = arguments.get(COMMAND_JSON).get(0);

		File tempFile = null;
		try {
			tempFile = File.createTempFile("command", ".json");
			FileTools.writeStringToFile(tempFile, json, "UTF-8");
			List<String> cmds = new ArrayList<String>();
			// setup java call
			cmds.add(getJreExecutable().getAbsolutePath());

			if(arguments.containsKey(DEBUG_LAUNCHER_PARAMETER)) {
				arguments.get(DEBUG_LAUNCHER_PARAMETER).stream().forEach(arg -> cmds.add(arg));
			}

			// class path
			cmds.add("-cp");
			String cp = String.format("%s%s%s", commandsHomeFile.getAbsolutePath().replace("\\", File.separator), File.separator, loaderJar);
			cmds.add(cp);
			cmds.add("-Djava.system.class.loader=com.braintribe.utils.classloader.FileClassPathClassLoader");
			cmds.add(String.format("-Dcom.braintribe.classpath.file=%s", paramFile.getAbsolutePath()));
			// parameters
			/* if (params.size() > 0) { for (LaunchParam param : params) { String name = param.getName(); if (name !=
			 * null && name.length() > 0) { cmds.add(param.getName()); } cmds.add(param.getValue()); } } */
			if (arguments.containsKey(COMMAND_JVM_ARG)) {
				arguments.get(COMMAND_JVM_ARG).stream().forEach(arg -> cmds.add(arg));
			}

			// main command class
			cmds.add(commandClass);
			// actual command (i.e. the json)
			/* if (commands.size() == 0) { throw new BuildException("no denotation type passed"); } else { */
			cmds.add("-f");
			cmds.add(tempFile.getAbsolutePath());
			// }
			// launch it
			int retval = launch(cmds);
			if (retval != 0) {
				String msg = String.format("java call failed with code [%d]", retval);
				throw new BuildException(msg);
			}

		} catch (FileNotFoundException e) {
			String msg = String.format("cannot determine the location of the JRE");
			throw new BuildException(msg);
		} catch (IOException e) {
			String msg = "cannot launch JRE";
			throw new BuildException(msg);
		} catch (InterruptedException e) {
			String msg = "cannot launch JRE";
			throw new BuildException(msg);
		} finally {
			// delete the temporary file
			if (tempFile != null)
				tempFile.deleteOnExit();
		}
	}

	/**
	 * Storing the passed arguments to a map. A key is the argument names and a value is a list with its respective
	 * values. Each one of those arguments is passed as a key-value pair separated by '='. Same argument might be passed
	 * more than one times. For example, the following arguments (See jvmArg is passed twice): </br>
	 * <ul>
	 * <li>commandArtifact=com.braintribe.build:DummyCommands#1.0</li>
	 * <li>command={_type:"com.braintribe.build.cmd.dummy.declaration.DummyCommand", logMessage:"Hello RATlab!!!"}</li>
	 * <li>jvmArg=-Xms128M</li>
	 * <li>jvmArg=-Xmx1024M</li>
	 * </ul>
	 * </p>
	 * will be stored as a map : </br>
	 * <ul>
	 * <li>commandArtifact -> ["com.braintribe.build:DummyCommands#1.0"]</li>
	 * <li>command -> ["{_type:"com.braintribe.build.cmd.dummy.declaration.DummyCommand", logMessage:"HelloRATlab!!!"}"]</li>
	 * <li>jvmArg -> ["-Xms128M", "Xmx1024M"]
	 * </ul>
	 */
	private static Map<String, List<String>> parseLauncherArguments(String[] args) {
		Map<String, List<String>> argsAsMap = Stream.of(args).map(arg -> arg.split("=", 2)).collect(
				Collectors.toMap(
						entry -> entry[0],
						entry -> {
							List<String> argsList = new ArrayList<String>();
							argsList.add(entry[1]);
							return argsList;
						},
						(key1, key2) -> {
			                 List<String> mergedArgsList = new ArrayList<>();
			                 mergedArgsList.addAll(key1);
			                 mergedArgsList.addAll(key2);
			                 return mergedArgsList;
			             })
				);
		return argsAsMap;
	}

	/**
	 * @param artifact
	 * @return
	 * @throws BuildException
	 */
	private static Part resolveCommandPackage(String artifact) throws BuildException {

		Dependency dependency;
		try {
			dependency = NameParser.parseCondensedDependencyName(artifact);
			dependency.setVersionRange(VersionRangeProcessor.addHotfixRangeIfMissing(dependency.getVersionRange()));
		} catch (NameParserException e1) {
			String msg = String.format("cannot extract dependency from passed artifact [%s]", artifact);
			throw new BuildException(msg, e1);
		}

		DependencyResolver resolver = primeAndRetrieveDependencyResolver();
		Set<Solution> resolvedSolutions = resolver.resolve(dependency);

		if (resolvedSolutions.size() == 0) {
			String msg = "no solution found for dependency [" + NameParser.buildName(dependency) + "]";
			throw new BuildException(msg);
		}

		Solution solution = resolvedSolutions.toArray(new Solution[0])[0];
		if (solution == null) {
			String msg = "no solution found for dependency [" + NameParser.buildName(dependency) + "]";
			throw new BuildException(msg);
		}

		Part commandPart = null;
		for (Part part : solution.getParts()) {
			if (PartTupleProcessor.compare(commandTuple, part.getType())) {
				commandPart = part;
				break;
			}
		}
		if (commandPart == null) {
			String msg = "no command part found in solution  [" + NameParser.buildName(solution) + "]";
			throw new BuildException(msg);
		}

		return commandPart;
	}

	private static DependencyResolver primeAndRetrieveDependencyResolver() {
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		ove.addEnvironmentOverride(PROFILE_USECASE, useCase);
		if(arguments.containsKey(CUSTOM_MAVEN_HOME)) {
			ove.addEnvironmentOverride("M2_HOME", arguments.get(CUSTOM_MAVEN_HOME).get(0));
		}
		if(arguments.containsKey(CUSTOM_USER_HOME)) {
			ove.addPropertyOverride("user.home", arguments.get(CUSTOM_USER_HOME).get(0));
		}		

		PlainOptimisticResolverConfiguration filterConfiguration = new PlainOptimisticResolverConfiguration();
		GeneralConfigurationContract ec = new GeneralConfigurationContract() {
			@Override
			public VirtualEnvironment virtualEnvironment() {
				return ove;
			}

			@Override
			public boolean lenient() {
				return false;
			}
		};

		WireContext<BuildDependencyResolutionContract> wireContext = BuildDependencyResolvers.standard(b -> {
			b.bindContract(GeneralConfigurationContract.class, ec);
			b.bindContract(FilterConfigurationContract.class, filterConfiguration);
		});

		DependencyResolver resolver = wireContext.beans().plainOptimisticDependencyResolver();
		return resolver;
	}

	private static File primeLoader(File directory, String loaderJar) throws BuildException {
		File target = new File(directory, loaderJar);
		if (target.exists())
			return target;
		// make sure the directory exists
		if (!directory.exists()) {
			directory.mkdirs();
		}
		URL resource = CommandLauncher.class.getClassLoader().getResource(loaderJar);
		if (resource == null) {
			String msg = String.format("cannot prime loader as resource [%s] cannot be found", loaderJar);
			throw new BuildException(msg);
		}
		try (InputStream in = resource.openStream(); FileOutputStream out = new FileOutputStream(target);) {
			IOTools.pump(in, out);
		} catch (Exception e) {
			String msg = String.format("cannot prime loader as resource [%s] cannot be copied from to [%s]", loaderJar, target.getAbsolutePath());
			throw new BuildException(msg);
		}
		return target;
	}

	private static File buildLoaderParameterFile(File unpackDir, String loaderParamFile) throws BuildException {
		if (unpackDir.exists() == false) {
			String msg = String.format("Unpack directory [%s] must exist", unpackDir.getAbsolutePath());
			throw new BuildException(msg);
		}
		File[] files = unpackDir.listFiles();
		StringBuilder builder = new StringBuilder();
		for (File file : files) {
			if (builder.length() > 0)
				builder.append(System.getProperty("line.separator"));
			builder.append(file.getAbsolutePath());
		}
		File paramFile = new File(unpackDir, loaderParamFile);
		try {
			FileTools.writeStringToFile(paramFile, builder.toString(), "UTF-8");
			return paramFile;
		} catch (Exception e) {
			String msg = String.format("Cannot write jar list to loader parameter file [%s]", paramFile.getAbsolutePath());
			throw new BuildException(msg);
		}
	}

	/**
	 * find the java executable (for the respective OS)
	 * 
	 * @return - the {@link File} that represents the java executable
	 * @throws FileNotFoundException
	 *             -
	 */
	private static File getJreExecutable() throws FileNotFoundException {
		String jreDirectory = System.getProperty("java.home");
		if (jreDirectory == null) {
			throw new IllegalStateException("java.home");
		}
		File exe;
		if (isWindows()) {
			exe = new File(jreDirectory, "bin/java.exe");
		} else {
			exe = new File(jreDirectory, "bin/java");
		}
		if (!exe.isFile()) {
			throw new FileNotFoundException(exe.toString());
		}
		return exe;
	}

	/**
	 * identifies if we're running on windows or something other .. (UNIX style)
	 * 
	 * @return - true if the os name starts with "windows"
	 */
	private static boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os == null) {
			throw new IllegalStateException("os.name");
		}
		os = os.toLowerCase();
		return os.startsWith("windows");
	}

	/**
	 * actually launch the java (or any other process)
	 * 
	 * @param cmdarray
	 *            - an {@link Array} of {@link String} with command parameters
	 * @return - what the process returns
	 * @throws IOException
	 *             -
	 * @throws InterruptedException
	 *             -
	 */
	private static int launch(List<String> cmdarray) throws IOException, InterruptedException {
		byte[] buffer = new byte[1024];

		log.info("Launching: " + cmdarray);
		ProcessBuilder processBuilder = new ProcessBuilder(cmdarray);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		InputStream in = process.getInputStream();
		while (true) {
			int r = in.read(buffer);
			if (r <= 0) {
				break;
			}
			System.out.write(buffer, 0, r);
		}
		return process.waitFor();
	}

}
