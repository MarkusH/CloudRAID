package de.dhbw.mannheim.cloudraid.net.model;

import java.util.HashMap;

public class DirectoryModel extends EntryModel {

	private HashMap<String, DirectoryModel> directories = new HashMap<String, DirectoryModel>();
	private HashMap<String, FileModel> files = new HashMap<String, FileModel>();

	public final void addEntry(EntryModel entry) {
		if (entry.getClass() == DirectoryModel.class) {
			this.directories.put(entry.getName(), (DirectoryModel) entry);
		} else {
			this.files.put(entry.getName(), (FileModel) entry);
		}
	}

	public final HashMap<String, DirectoryModel> getDirectories() {
		return this.directories;
	}

	public final EntryModel getEntry(String key) {
		return this.directories.get(key);
	}

}
