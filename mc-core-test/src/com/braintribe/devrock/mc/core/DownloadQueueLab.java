// ============================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
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
package com.braintribe.devrock.mc.core;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.event.EventBroadcasterAttribute;
import com.braintribe.devrock.mc.api.event.EventContext;
import com.braintribe.devrock.mc.api.event.EventHub;
import com.braintribe.devrock.mc.api.transitive.ResolutionPathElement;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloadEnqueued;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloadProcessed;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloadProcessing;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloaded;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloading;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class DownloadQueueLab {
	static JFrame dlg = new JFrame();
	static DefaultListModel<String> downloadingModel = new DefaultListModel<>();
	static DefaultListModel<String> processingModel = new DefaultListModel<>();
	static JList<String> downloadingListView = new JList(downloadingModel);
	static JList<String> processingListView = new JList(processingModel);
	
	
	public static void main(String[] args) {
		
		dlg.setSize(new Dimension(800, 1000));
		
		
		GridBagLayout layout = new GridBagLayout();
		
		GridBagConstraints processingConstraints = new GridBagConstraints();
		processingConstraints.gridx = 0;
		processingConstraints.gridy = 0;
		processingConstraints.weightx = 1.0;
		processingConstraints.weighty = .5;
		processingConstraints.fill = GridBagConstraints.BOTH;
		GridBagConstraints downloadConstraints = new GridBagConstraints();
		downloadConstraints.gridx = 0;
		downloadConstraints.gridy = 1;
		downloadConstraints.weightx = 1.0;
		downloadConstraints.weighty = .5;
		downloadConstraints.fill = GridBagConstraints.BOTH;

		dlg.setLayout(layout);
		
		JScrollPane sp1 = new JScrollPane(processingListView);
		JScrollPane sp2 = new JScrollPane(downloadingListView);
		dlg.add(sp1, processingConstraints);
		dlg.add(sp2, downloadConstraints);
		
		dlg.setVisible(true);

		File outputDir = new File("download-output");
		File configFile = new File(outputDir, "repository-configuration.yaml");
		File localRepoDir = new File(outputDir, "artifacts-local-repository");
		
		if (localRepoDir.exists()) {
			try {
				FileTools.deleteDirectoryRecursively(localRepoDir);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		
		OverridingEnvironment overridingEnvironment = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		
		overridingEnvironment.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, configFile.getAbsolutePath());
		
		EventHub eventHub = new EventHub();
		
		AttributeContext attributeContext = AttributeContexts.derivePeek() //
		.set(EventBroadcasterAttribute.class, eventHub) //
		.build(); //
		
		AttributeContexts.push(attributeContext);


		eventHub.addListener(OnPartDownloading.T, DownloadQueueLab::onDownloading);
		eventHub.addListener(OnPartDownloaded.T, DownloadQueueLab::onDownloaded);
		eventHub.addListener(OnPartDownloadProcessing.T, DownloadQueueLab::onProcessing);
		eventHub.addListener(OnPartDownloadProcessed.T, DownloadQueueLab::onProcessed);
		
		try (WireContext<TransitiveResolverContract> wireContext = Wire.contextBuilder(TransitiveResolverWireModule.INSTANCE).bindContract(VirtualEnvironmentContract.class, () -> overridingEnvironment).build()) {
			
			PartEnrichingContext enrichingContext = PartEnrichingContext.build() //
				.enrichPart(PartIdentification.create("war"))
				.enrichPart(PartIdentification.create("asset", "man"))
				.enrichPart(PartIdentification.create("resources", "zip"))
				.enrichPart(PartIdentification.create("data", "man"))
				.enrichPart(PartIdentification.create("model", "man"))
				.done();
			
			
			
			TransitiveResolutionContext resolutionContext = TransitiveResolutionContext.build().enrich(enrichingContext).dependencyPathFilter(DownloadQueueLab::filter).done();
			
			
			AnalysisArtifactResolution resolution = wireContext.contract().transitiveDependencyResolver().resolve(resolutionContext, CompiledDependencyIdentification.create("tribefire.cortex.assets", "tribefire-standard-aggregator", FuzzyVersion.from(Version.create(2, 0))));
		}
		finally {
			AttributeContexts.pop();
		}
		
		dlg.dispose();
	}
	
	private static Set<CompiledPartIdentification> downloads = HashComparators.compiledPartIdentification.newHashSet();
	private static Set<CompiledPartIdentification> enqueued = HashComparators.compiledPartIdentification.newHashSet();

	private static void inUiThread(Runnable runnable) {
		try {
			EventQueue.invokeAndWait(runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void onProcessing(EventContext eventContext, OnPartDownloadProcessing event) {
		inUiThread(() -> {
			processingModel.addElement(event.getPart().asString());
		});
	}
	
	public static void onProcessed(EventContext eventContext, OnPartDownloadProcessed event) {
		inUiThread(() -> {
			processingModel.removeElement(event.getPart().asString());
		});
	}
	
	public static void onDownloading(EventContext eventContext, OnPartDownloading event) {

		inUiThread(() -> {
			if (downloads.add(event.getPart())) {
				downloadingModel.addElement(event.getPart().asString());
			}
	    });
	}

	private static void removeElement(DefaultListModel<String> model, String s) {
		int index = model.indexOf(s);
		if (index != -1)
			model.removeElementAt(index);
	}
	
	public static void onDownloaded(EventContext eventContext, OnPartDownloaded event) {
		
		inUiThread(() -> {
			if (downloads.remove(event.getPart())) {
				removeElement(downloadingModel, event.getPart().asString());
			}
		});
	}
	
	private static boolean filter(ResolutionPathElement e) {
		int i = 0;
		
		while (e != null) {
			e = e.getParent();
			i++;
		}
		
		return i < 10;
	}
}
