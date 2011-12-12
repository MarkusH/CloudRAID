package de.dhbw.mannheim.cloudraid.net.model;

import java.util.HashMap;

public abstract class VolumeModel {

	private HashMap<Object, Object> metadata = new HashMap<Object, Object>();
	private HashMap<String, EntryModel> entries = new HashMap<String, EntryModel>();

	private String name;

	public final void addEntry(EntryModel entry) {
		this.entries.put(entry.getName(), entry);
	}

	public final void addMetadata(HashMap<Object, Object> map) {
		this.metadata.putAll(map);
	}

	public final void addMetadata(Object key, Object value) {
		this.metadata.put(key, value);
	}

	public final HashMap<String, EntryModel> getEntries() {
		return this.entries;
	}

	public final EntryModel getEntry(String key) {
		return this.entries.get(key);
	}

	public final HashMap<Object, Object> getMetadata() {
		return this.metadata;
	}

	public final Object getMetadata(Object key) {
		return this.metadata.get(key);
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	@Override
	public abstract String toString();
}
