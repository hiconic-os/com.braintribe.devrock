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
package com.braintribe.model.processing.panther;

import java.io.File;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.braintribe.logging.Logger;
import com.braintribe.model.panther.ArtifactRepository;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.utils.xml.XmlTools;

public class MavenSettingsWriter {
	private static final Logger logger = Logger.getLogger(MavenSettingsWriter.class);
	private static VelocityEngine velocityEngine;

	static {
		try {
			Properties properties = new Properties();
			URL templateUrl = MavenSettingsWriter.class.getResource("settings.xml.vm");
			URL baseUrl = new URL(templateUrl, ".");

			properties.setProperty("input.encoding", "UTF-8");
			properties.setProperty("resource.loaders", "url");
			properties.setProperty("url.resource.loader.class", "org.apache.velocity.runtime.resource.loader.URLResourceLoader");

			properties.setProperty("url.resource.loader.root", baseUrl.toString());
			properties.setProperty("url.resource.loader.cache", "false");

			velocityEngine = new VelocityEngine();
			velocityEngine.init(properties);
		} catch (MalformedURLException e) {
			logger.error("Error while initializing velocity engine", e);
		}
	}

	public static void write(SourceRepository sourceRepository, File localRepository, String centralMirrorUrl, Writer writer) {
		VelocityContext context = new VelocityContext();
		context.put("localrepo", localRepository.getAbsolutePath());
		context.put("repos", sourceRepository.getLookupRepositories());
		context.put("updateReflectionRepositories", sourceRepository.getLookupRepositories().stream().filter(r -> r.getUpdateReflectionUrl() != null)
				.map(ArtifactRepository::getName).collect(Collectors.joining(",")));
		context.put("centralMirrorUrl", centralMirrorUrl);
		context.put("tools", Tool.INSTANCE);
		velocityEngine.mergeTemplate("settings.xml.vm", "UTF-8", context, writer);
	}

	public static class Tool {
		private static Tool INSTANCE = new Tool();

		public String esc(String s) {
			if (s == null)
				return null;
			return XmlTools.escape(s);
		}
	}
}
