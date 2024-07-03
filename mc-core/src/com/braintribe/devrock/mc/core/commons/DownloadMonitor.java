// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
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
package com.braintribe.devrock.mc.core.commons;

import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.console.output.ConsoleText;
import com.braintribe.devrock.mc.api.event.EntityEventListener;
import com.braintribe.devrock.mc.api.event.EventContext;
import com.braintribe.devrock.mc.api.event.EventEmitter;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloaded;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloading;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;

public class DownloadMonitor implements AutoCloseable {
	private boolean dynamicOutput = true;
	private final EventEmitter emitter;
	private final EntityEventListener<OnPartDownloading> downloadingListener = this::onDownloading;
	private final EntityEventListener<OnPartDownloaded> downloadedListener = this::onDownloaded;
	private final Map<EqProxy<CompiledPartIdentification>, DownloadInfo> downloadInfos = new HashMap<>();
	private int downloadCount;
	private int objectCount;
	private int downloadedVolume;
	private int downloadedVolumeInSlice;
	private int downloadRate;
	private long lastOutputTime;
	private ConsoleReprinting lastConsoleReprinting;
	private final Object sync = new Object();
	private final long startTime = System.currentTimeMillis();
	private final Timer timer;
	private boolean done;
	private ConsoleOutput indent;
	private boolean initialLinebreak;
	private List<DownloadMonitorPhase> phases = new ArrayList<>();
	private int currentPhase = -1;
	
	private static class DownloadInfo {
		CompiledPartIdentification part;
	}
	
	public DownloadMonitor(EventEmitter emitter) {
		this(emitter, false, 0, true);
	}
	
	public DownloadMonitor(EventEmitter emitter, boolean initialLinebreak, int indentSpaces, boolean dynamicOutput) {
		this.emitter = emitter;
		this.initialLinebreak = initialLinebreak;
		if (indentSpaces != 0)
			this.indent = ConsoleOutputs.spaces(indentSpaces);
		
		this.dynamicOutput = dynamicOutput;
		
		emitter.addListener(OnPartDownloading.T, downloadingListener);
		emitter.addListener(OnPartDownloaded.T, downloadedListener);
		
		timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				calcRate();
			}
		}, 1000, 1000);
	}
	

	private class DownloadMonitorPhase {
		private ConsoleOutput title;
		private int itemCount = -1;
		private int itemsDone = 0;
		private int percentage;
		private String progress = "";
		
		public DownloadMonitorPhase(ConsoleOutput title) {
			super();
			this.title = title;
		}
		
		public void setItemCount(int itemCount) {
			this.itemCount = itemCount;
		}
		
		public void increaseItemsDone(int count) {
			synchronized (this) {
				this.itemsDone += count;
				updateProgress();
			}
		}
		
		private void updateProgress() {
			final String progress;
			if (itemCount != -1) {
				progress = String.valueOf(itemsDone * 100 / itemCount) + "%";
			}
			else {
				progress = String.valueOf(itemCount);
			}
			
			if (!progress.equals(this.progress)) {
				this.progress = progress;
				doOutput();
			}
		}
		
		public ConsoleOutput getOutput() {
			return sequence(title, text(" "), text(progress)); 
		}
	}

	
	public void addPhase(ConsoleOutput title) {
		phases.add(new DownloadMonitorPhase(title));
	}
	
	public void nextPhase() {
		currentPhase++;
		doOutput();
	}
	
	public void nextPhase(int itemCount) {
		currentPhase++;
		phases.get(currentPhase).setItemCount(itemCount);
		doOutput();
	}
	
	public void increasePhaseItems(int amount) {
		phases.get(currentPhase).increaseItemsDone(amount);
	}
	
	@Configurable
	public void setInitialLinebreak(boolean initialLinebreak) {
		this.initialLinebreak = initialLinebreak;
	}
	
	@Configurable
	public void setIndent(int indentSpaces) {
		this.indent = ConsoleOutputs.spaces(indentSpaces);
	}
	
	@Configurable
	public void setDynamicOutput(boolean dynamicOutput) {
		this.dynamicOutput = dynamicOutput;
	}
	
	private void doOutputRegularily() {
		if (System.currentTimeMillis() - lastOutputTime > 100)
			doOutput();
	}
	
	private void doOutput() {
		if (initialLinebreak && lastConsoleReprinting == null)
			ConsoleOutputs.println("");
			
		lastConsoleReprinting = new ConsoleReprinting(lastConsoleReprinting, indent, done);

		for (int i = 0; i < phases.size(); i++) {
			ConfigurableConsoleOutputContainer phaseLine = ConsoleOutputs.configurableSequence();
			
			if (currentPhase == i) {
				phaseLine.append(yellow("-> "));
			}
			else if (currentPhase > i) {
				phaseLine.append(yellow("*  "));
			}
			else {
				phaseLine.append(text("   "));
			}
			
			phaseLine.append(phases.get(i).getOutput());

			lastConsoleReprinting.appendLine(phaseLine);
		}
		
		if (!done) {
			lastConsoleReprinting.appendLine(sequence( //
				text("Counting Objects to be downloaded: "), //
				text(String.valueOf(objectCount)) //
			));
		}
		
		lastConsoleReprinting.appendLine(sequence( //
			text("Downloaded Objects: "), //
			text(String.valueOf(downloadCount)) //
		));
		
		lastConsoleReprinting.appendLine(sequence( //
			text("Downloaded Data Volume: "), // 
			text(formatBytesOptimally(downloadedVolume)) //
		));

		if (done) {
			lastConsoleReprinting.appendLine(sequence( //
				text("Total Download Time: "), //
				text(formatMilliesOptimally(System.currentTimeMillis() - startTime)) //
			));
		}
		else {
			lastConsoleReprinting.appendLine(sequence( //
				text("Download Rate: "), //
				text(formatBytesOptimally(downloadRate)), //
				text("/s") //
			));	
		}
		
		lastConsoleReprinting.print();
		
		lastOutputTime = System.currentTimeMillis();
	}
	
	private void onDownloading(EventContext eventContext, OnPartDownloading event) {
		synchronized (sync) {
			DownloadInfo downloadInfo = downloadInfos.computeIfAbsent(HashComparators.compiledPartIdentification.eqProxy(event.getPart()), k -> new DownloadInfo());

			int dataAmound = event.getDataAmount();
			
			downloadedVolumeInSlice += dataAmound;
			downloadedVolume += dataAmound;
			
			if (downloadInfo.part == null) {
				objectCount++;
				downloadInfo.part = event.getPart();
				doOutput();
			}
			else {
				doOutputRegularily();
			}
		}
	}
	
	private void onDownloaded(EventContext eventContext, OnPartDownloaded event) {
		synchronized (sync) {
			downloadCount++;
			doOutput();
		}
	}
	
	private void calcRate() {
		synchronized (sync) {
			downloadRate = downloadedVolumeInSlice;
			downloadedVolumeInSlice = 0;
			doOutput();
		}
	}
	
	@Override
	public void close() {
		emitter.removeListener(OnPartDownloading.T, downloadingListener);
		emitter.removeListener(OnPartDownloaded.T, downloadedListener);
		timer.cancel();
		done = true;
		if (lastConsoleReprinting != null)
			doOutput();
	}
	
	public static String formatMilliesOptimally(long millies) {
		if (millies > 60_000) {
			return String.format(Locale.US, "%.1f min", millies / 60_000D) ;
		}
		else if (millies > 1000) {
			return String.format(Locale.US, "%.1f s", millies / 1000D) ;
		}
		else {
			return String.format(Locale.US, "%d ms", millies); 
		}
	}
	
	public static String formatBytesOptimally(long bytes) {
		if (bytes > 1048576) {
			return formatMebiBytes(bytes);
		}
		else if (bytes > 1024) {
			return formatKibiBytes(bytes);
		}
		else {
			return formatBytes(bytes);
		}
	}
	
	public static String formatMebiBytes(long bytes) {
		return String.format(Locale.US, "%.1f MiB", bytes / 1048576D) ; 
	}
	
	public static String formatKibiBytes(long bytes) {
		return String.format(Locale.US, "%.1f KiB", bytes / 1024D) ; 
	}
	
	public static String formatBytes(long bytes) {
		return String.format(Locale.US, "%d B", bytes); 
	}
	
	
}

class ConsoleReprinting {
	private final ConsoleReprinting predecessor;
	private final List<ConsoleOutput> lines = new ArrayList<>();
	private final boolean last;
	private final ConsoleOutput indent;
	
	public ConsoleReprinting(ConsoleReprinting predecessor, ConsoleOutput indent, boolean last) {
		super();
		this.predecessor = predecessor;
		this.indent = indent;
		this.last = last;
	}
	
	public void appendLine(ConsoleOutput output) {
		lines.add(output);
	}
	
	public void print() {
		ConfigurableConsoleOutputContainer container = ConsoleOutputs.configurableSequence().resetPosition(!last);
		
		List<ConsoleOutput> preLines = predecessor != null? predecessor.lines: Collections.emptyList();
		
		int len = Math.max(preLines.size(), lines.size());
		
		for (int i = 0; i < len; i++) {
			ConsoleOutput oldLine = getLine(preLines, i);
			ConsoleOutput newLine = getLine(lines, i);
			
			ConsoleOutput effectiveLine = getEffectiveLine(oldLine, newLine);
			
			if (indent != null)
				container.append(indent);
			
			container.append(effectiveLine);
			container.append("\n");
		}
		
		ConsoleOutputs.print(container);
	}
	
	private ConsoleOutput getLine(List<ConsoleOutput> lines, int i) {
		if (i < lines.size())
			return lines.get(i);
		
		return null;
	}

	private ConsoleOutput getEffectiveLine(ConsoleOutput oldLine, ConsoleOutput newLine) {
		if (oldLine == null)
			return newLine;
		
		int oldLen = getLength(oldLine);
		
		if (newLine == null)
			return ConsoleOutputs.spaces(oldLen);
		
		int newLen = getLength(newLine);
		
		if (newLen >= oldLen) 
			return newLine;
			
		return ConsoleOutputs.sequence(newLine, ConsoleOutputs.spaces(oldLen - newLen));
	}

	private static int getLength(ConsoleOutput output) {
		switch (output.kind()) {
		case container: return getLength((ConsoleOutputContainer)output);
		case text: return ((ConsoleText)output).getText().length();
		default: return 0;
		}
	}
		
	private static int getLength(ConsoleOutputContainer container) {
		int len = 0;
		
		for (int i = 0; i < container.size(); i++) {
			len += getLength(container.get(i));
		}
		
		return len;
	}
}
