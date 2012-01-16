package de.dhbw.mannheim.cloudraid.fs;

import java.util.HashSet;

/**
 * Handles the locking of files.
 * 
 * @author Florian Bausch
 * 
 */
public class FileLock {

	private static HashSet<String> lockedFiles = new HashSet<String>();

	/**
	 * Lock a file. You <b>MUST</b> unlock it later.
	 * 
	 * @param filename
	 *            The file to be locked.
	 * @return true, if you got the lock, false, if not
	 */
	public static synchronized boolean lock(String filename) {
		return lockedFiles.add(filename);

	}

	/**
	 * Unlock a file.
	 * 
	 * @param filename
	 *            The file to be unlocked.
	 * @return true, if the file could be unlocked.
	 */
	public static synchronized boolean unlock(String filename) {
		return lockedFiles.remove(filename);
	}

}
