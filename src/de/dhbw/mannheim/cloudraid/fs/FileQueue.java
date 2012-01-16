package de.dhbw.mannheim.cloudraid.fs;

import java.util.LinkedList;

public class FileQueue {

	private static LinkedList<FileQueueEntry> ll = new LinkedList<FileQueueEntry>();

	public static boolean add(FileQueueEntry newEntry) {
		return ll.add(newEntry);
	}

	public static FileQueueEntry get() {
		return ll.pop();
	}

	public static boolean isEmpty() {
		return ll.isEmpty();
	}

	public enum FileAction {
		CREATE, DELETE, MODIFY;
	}

}
