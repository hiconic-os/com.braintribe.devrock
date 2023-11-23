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
package com.braintribe.model.processing.meta;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.build.UpdateModelDeclaration;
import com.braintribe.model.build.UpdateModelDeclaration.ModelDescriptor;
import com.braintribe.model.io.metamodel.GmSourceWriter;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.itw.synthesis.java.JavaTypeSynthesisException;
import com.braintribe.model.processing.itw.synthesis.java.jar.JavaTypeSerializer;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;

/**
 * the {@link AbstractModelArtifactBuilder} can turn a {@link GmMetaModel} into a full fledged jar <br/>
 * if you want to use it to create a jar, simple call it that way: <br/>
 * {@code
 * 		ModelArtifactBuilder artifactBuilder = new ModelArtifactBuilder();
 * }<br/>
 * {@code
 * 		artifactBuilder.setModel(model);
 * }<br/>
 * {@code
 *		artifactBuilder.setVersionFolder(file);
 * }<br/>
 * {@code
 *		artifactBuilder.publish();
 *}<br/>
 * <br/>
 * 
 * @author pit, dirk
 *
 *         autoranges a model dependency when it creates a pom dependency from it, currently it's a standard range, i.e. a major/minor range.
 *
 */
public abstract class AbstractModelArtifactBuilder {

	protected GmMetaModel model;
	protected ModelDescriptor modelDescriptor;
	private Map<String, String> sourceMap;
	private String user;

	@Required
	public void setModel(GmMetaModel model) {
		this.model = model;
	}

	@Configurable
	public void setUser(String user) {
		this.user = user;
	}

	public void publish() {
		try {
			prime();
			writePom();
			writeSources();
			writeJar();

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("error while building parts for model " + model.getName(), e);
		}
	}

	private void writeJar() throws Exception {
		ZipOutputStream out = new ZipOutputStream(partOutputStream(".jar"));

		try {
			writeClasses(out);
			writeManifest(out);
			writeModelDeclaration(out);
		} finally {
			out.finish();
			closePartOutputStream(out);
		}
	}

	private void writeManifest(ZipOutputStream out) throws IOException {
		out.putNextEntry(new ZipEntry("META-INF/MANIFEST"));

		// create and fill manifest

		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, modelDescriptor.name);
		manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, modelDescriptor.version);

		if (user != null)
			manifest.getMainAttributes().putValue("Created-By", user);

		// write the manifest to the archive
		manifest.write(out);
	}

	private void prime() {
		buildDescriptor();
	}

	private void buildDescriptor() {
		modelDescriptor = new ModelDescriptor();

		ModelArtifact modelArtifact = modelArtifactFor(model);

		modelDescriptor.name = model.getName();

		modelDescriptor.groupId = modelArtifact.groupId;
		modelDescriptor.artifactId = modelArtifact.artifactId;
		modelDescriptor.version = modelArtifact.version;

		model.getDependencies().stream() //
				.map(m -> m.getName()) //
				.collect(Collectors.toCollection(() -> modelDescriptor.dependencies));

		model.getTypes().stream() //
				.map(t -> t.getTypeSignature()) //
				.collect(Collectors.toCollection(() -> modelDescriptor.declaredTypes));
	}

	private void writeModelDeclaration(ZipOutputStream out) throws Exception {
		out.putNextEntry(new ZipEntry("model-declaration.xml"));
		UpdateModelDeclaration.writeDescriptor(out, modelDescriptor);
	}

	private void writeClasses(ZipOutputStream out) throws JavaTypeSynthesisException {
		modelDescriptor.hash = JavaTypeSerializer.serializeAndGenerateHash(model, out, sourceMap);
	}

	private void writeSources() throws IOException {
		ZipOutputStream out = new ZipOutputStream(partOutputStream("-sources.jar"));

		try {
			writeSources(out);
			writeManifest(out);
		} finally {
			out.finish();
			closePartOutputStream(out);
		}
	}

	private void writeSources(ZipOutputStream out) {
		GmSourceWriter sourceWriter = new GmSourceWriter();
		sourceWriter.setOutputStream(out);
		sourceWriter.setGmMetaModel(model);
		sourceWriter.enableWritingSourcesForExistingClasses();

		// do the actual source generation and store the source map for later usage in writeClasses()
		sourceMap = sourceWriter.writeMetaModelToStream();
	}

	private void writePom() throws Exception {
		List<ModelArtifact> modelDependenciesBindings = new ArrayList<>();

		for (GmMetaModel modelDependency : model.getDependencies()) {
			modelDependenciesBindings.add(modelArtifactFor(modelDependency));
		}

		Document document = DomParser.create().setNamespaceAware(true).makeItSo();
		Element projectElement = document.createElementNS("http://maven.apache.org/POM/4.0.0", "project");
		projectElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation",
				"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd");

		document.appendChild(projectElement);

		Element modelVersionElement = document.createElement("modelVersion");
		projectElement.appendChild(modelVersionElement);
		modelVersionElement.setTextContent("4.0.0");

		writeArtifactIdentification(projectElement, modelDescriptor.groupId, modelDescriptor.artifactId, modelDescriptor.version);

		Element dependenciesElement = document.createElement("dependencies");
		projectElement.appendChild(dependenciesElement);

		for (ModelArtifact dependency : modelDependenciesBindings) {
			Element dependencyElement = document.createElement("dependency");
			dependenciesElement.appendChild(dependencyElement);
			writeArtifactIdentification(dependencyElement, dependency.groupId, dependency.artifactId, rangifyModelVersion(dependency.version));
		}

		DomParser.write().from(document).to(partOutputStream(".pom"));
	}

	public static String rangifyModelVersion(String versionString) {
		Version version = Version.parse(versionString);
		return FuzzyVersion.from(version).asString();
	}

	protected abstract OutputStream partOutputStream(String extension);

	protected abstract void closePartOutputStream(OutputStream out) throws IOException;

	private void writeArtifactIdentification(Element projectElement, String groupId, String artifactId, String version) {
		DomUtils.setElementValueByPath(projectElement, "groupId", groupId, true);
		DomUtils.setElementValueByPath(projectElement, "artifactId", artifactId, true);
		DomUtils.setElementValueByPath(projectElement, "version", version, true);
	}

	public static ModelArtifact modelArtifactFor(GmMetaModel model) {
		String name = model.getName();

		int index = name.lastIndexOf(':');

		ModelArtifact modelArtifact = new ModelArtifact();
		modelArtifact.version = model.getVersion();

		if (index != -1) {
			modelArtifact.groupId = name.substring(0, index);
			modelArtifact.artifactId = name.substring(index + 1);
		} else {
			modelArtifact.groupId = name;
			modelArtifact.artifactId = name;
		}

		return modelArtifact;
	}

	public static class ModelArtifact {
		public String groupId;
		public String artifactId;
		public String version;

		public String partFileName(String extension) {
			return artifactId + "-" + version + extension;
		}
	}
}
