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
package com.braintribe.devrock.mc.core.repository.index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ArtifactIndex {
	private Map<String, ArtifactIndexEntry> entryByArtifact = new LinkedHashMap<>();
	private NavigableMap<Integer, ArtifactIndexEntry> entryBySequenceNum = null;
	private int sequence;
	
	public ArtifactIndex(boolean indexBySequenceNum) {
		if (indexBySequenceNum) entryBySequenceNum = new TreeMap<>();
	}
	
	public ArtifactIndexEntry get(String artifact) {
		return entryByArtifact.get(artifact);
	}
	
	public List<String> getArtifacts() {
		return new ArrayList<>(entryByArtifact.keySet());
	}
	
	public int getLastSequenceNumber() {
		return sequence - 1;
	}
	
	public void update(String artifact, ArtifactIndexOperation operation) {
		int sequenceNumber = sequence++;
		ArtifactIndexEntry newEntry = new ArtifactIndexEntry(sequenceNumber, artifact, operation);
		
		ArtifactIndexEntry oldEntry = entryByArtifact.put(artifact, newEntry);
		
		if (entryBySequenceNum != null) {
			if (oldEntry != null)
				entryBySequenceNum.remove(oldEntry.getSequenceNumber());
			
			entryBySequenceNum.put(sequenceNumber, newEntry);
		}
	}
	
	public void update(String artifact) {
		update(artifact, ArtifactIndexOperation.UPDATED);
	}

	public void delete(String artifact) {
		update(artifact, ArtifactIndexOperation.DELETED);
	}

	private void register(ArtifactIndexEntry entry) {
		entryByArtifact.put(entry.getArtifact(), entry);
		
		if (entryBySequenceNum != null) {
			entryBySequenceNum.put(entry.getSequenceNumber(), entry);
		}
		
		sequence = Math.max(sequence, entry.getSequenceNumber() + 1);
	}
	
	public static ArtifactIndex read(InputStream in, boolean withSequenceIndex) throws IOException {
		return read(in, withSequenceIndex, -1);
	}
	
	public static ArtifactIndex read(InputStream in, boolean withSequenceIndex, int sinceSequenceNum) throws IOException {
		ArtifactIndex index = new ArtifactIndex(withSequenceIndex);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		
		String line = null;
		
		while ((line = reader.readLine()) != null) {
			StringTokenizer tokenizer = new StringTokenizer(line, " ");
			
			int sequenceNum = Integer.parseInt(tokenizer.nextToken());
			
			if (sinceSequenceNum != -1 && sinceSequenceNum >= sequenceNum)
				return index;
			
			String artifact = tokenizer.nextToken();
			ArtifactIndexOperation operation = ArtifactIndexOperation.fromShorthand(tokenizer.nextToken().charAt(0));
			
			ArtifactIndexEntry entry = new ArtifactIndexEntry(sequenceNum, artifact, operation);
			index.register(entry);
		}
		return index;
	}
	
	public void write(OutputStream out) throws IOException {
		final Collection<ArtifactIndexEntry> sortedEntries;
		if (entryBySequenceNum == null) {
			Comparator<ArtifactIndexEntry> comparator = Comparator.comparing(ArtifactIndexEntry::getSequenceNumber).reversed();
			sortedEntries = entryByArtifact.values().stream().sorted(comparator).collect(Collectors.toList());
		}
		else {
			sortedEntries = entryBySequenceNum.descendingMap().values();
		}
		
		Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

		boolean first = true;
		
		for (ArtifactIndexEntry entry: sortedEntries) {
			if (first) {
				first = false;
			}
			else {
				writer.write('\n');
			}
			writer.write(String.valueOf(entry.getSequenceNumber()));
			writer.write(' ');
			writer.write(entry.getArtifact());
			writer.write(' ');
			writer.write(entry.getLastOperation().shorthand());
		}
		
		writer.flush();
	}
}
