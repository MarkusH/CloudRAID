package de.dhbw.mannheim.cloudraid.fs;

import java.util.HashMap;

/**
 * Handles the locking of files.
 * 
 * @author Florian Bausch
 * 
 */
public class FileLock {

	private static HashMap<String, FileManager> lockedFiles = new HashMap<String, FileManager>();

	/**
	 * Lock a file. You <b>MUST</b> unlock it later.
	 * 
	 * @param filename
	 *            The file to be locked.
	 * @return true, if you got the lock, false, if not
	 */
	public static synchronized boolean lock(String filename,
			FileManager fileManager) {
		if (lockedFiles.containsKey(filename)) {
			return false;
		} else {
			lockedFiles.put(filename, fileManager);
			return true;
		}

	}

	/**
	 * Unlock a file.
	 * 
	 * @param filename
	 *            The file to be unlocked.
	 * @return true, if the file could be unlocked.
	 */
	public static synchronized boolean unlock(String filename,
			FileManager fileManager) {
		if (lockedFiles.containsKey(filename)) {
			if (lockedFiles.get(filename) == fileManager) {
				lockedFiles.remove(filename);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
