package de.dhbw.mannheim.cloudraid.fs;

import de.dhbw.mannheim.cloudraid.fs.FileQueue.FileAction;

/**
 * This class maps a filename to an according action.
 * 
 * @author Florian Bausch
 * 
 */
public class FileQueueEntry {

	/**
	 * The filename
	 */
	private String fileName;

	/**
	 * The action to be done
	 */
	private FileAction fileAction;

	/**
	 * Creates a new instance of FileQueueEntry that can be written into the
	 * {@link FileQueue}.
	 * 
	 * @param file
	 *            The filename
	 * @param fileAction
	 *            The action
	 */
	public FileQueueEntry(String file, FileAction fileAction) {
		this.fileName = file;
		this.fileAction = fileAction;
	}

	/**
	 * Returns the filename of the entry.
	 * 
	 * @return The filename as String.
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * Returns the file action of the entry.
	 * 
	 * @return The action.
	 */
	public FileAction getFileAction() {
		return this.fileAction;
	}
}
