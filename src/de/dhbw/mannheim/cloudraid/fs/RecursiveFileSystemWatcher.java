package de.dhbw.mannheim.cloudraid.fs;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import de.dhbw.mannheim.cloudraid.fs.FileQueue.FileAction;

/**
 * Watches a directory recursively and writes newly created, modified and
 * deleted files into the {@link FileQueue}.
 * 
 * @author Florian Bausch
 * 
 */
public class RecursiveFileSystemWatcher extends Thread {

	private File dir;

	/**
	 * A map containing all known files.
	 */
	private static ConcurrentHashMap<String, Long> fileMap = new ConcurrentHashMap<String, Long>();

	private Vector<String> keySet = new Vector<String>();

	private long sleepTime = 10000;

	/**
	 * Creates a RecursiveFileSystemWatcher that runs every 10s.
	 */
	public RecursiveFileSystemWatcher() {
		dir = new File(System.getProperty("user.home") + "/Dropbox/");
		System.out.println("Watching directory " + dir.getAbsolutePath());
		this.setPriority(MIN_PRIORITY);
		this.setName("RecursiveFileSystemWatcher");
	}

	/**
	 * Creates a RecursiveFileSystemWatcher that runs in the given interval.
	 * 
	 * @param sleepTime
	 *            The sleeping time in ms.
	 */
	public RecursiveFileSystemWatcher(long sleepTime) {
		this();
		this.sleepTime = sleepTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		while (!isInterrupted()) {
			keySet = new Vector<String>(fileMap.keySet());

			if (!this.dir.exists()) {
				System.err.println("The watch directory does not exist");
				break;
			} else {
				this.checkDir(this.dir);
			}

			// all files still in "keySet" were not found, this means they were
			// deleted
			for (String k : keySet) {
				FileQueue.add(new FileQueueEntry(k, FileAction.DELETE));
				fileMap.remove(k);
			}

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.err.println("The file system watcher is stopped");
	}

	/**
	 * Runs through the list of files in the given directory and handles the
	 * files according to their type.
	 * 
	 * @param file
	 *            The directory to be handled.
	 */
	private void checkDir(File dir) {
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				if (this.isSymlink(f)) {
					System.err.println("I do not handle the symbolic link at "
							+ f.getAbsolutePath());
				} else if (f.isDirectory()) {
					this.checkDir(f);
				} else if (f.isFile()) {
					this.checkFile(f);
				} else {
					System.err
							.println("Whoops! I don't know how to handle the file "
									+ f.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Checks the given file and handles it according to the status.
	 * 
	 * @param file
	 *            The file to be handled.
	 */
	private void checkFile(File file) {
		String name = file.getAbsolutePath();
		if (fileMap.containsKey(name)) {
			if (file.lastModified() == fileMap.get(name)) {
				// nothing to do, file already indexed
				// System.out.println(file.getAbsolutePath() +
				// " already exists.");
			} else {
				// the file changed
				// System.out.println(file.getAbsolutePath() + " was changed.");
				fileMap.put(file.getAbsolutePath(), file.lastModified());
				FileQueue.add(new FileQueueEntry(file.getAbsolutePath(),
						FileAction.MODIFY));
			}
			keySet.remove(name);
		} else {
			// a new file is found
			// System.out.println(file.getAbsolutePath() + " is a new file.");
			fileMap.put(file.getAbsolutePath(), file.lastModified());
			FileQueue.add(new FileQueueEntry(file.getAbsolutePath(),
					FileAction.CREATE));
		}
	}

	/**
	 * Checks, if a file is a symbolic link.
	 * 
	 * From Apache Commons (modified)
	 * https://svn.apache.org/viewvc/commons/proper
	 * /io/trunk/src/main/java/org/apache/commons/io/FileUtils.java?view=markup <br>
	 * This is for Java 1.6 compatibility
	 * 
	 * @param file
	 *            The file to be checked.
	 * @return true, if it is a symbolic link
	 */
	private boolean isSymlink(File file) {
		try {
			if (file == null) {
				throw new NullPointerException("File must not be null");
			}
			File fileInCanonicalDir = null;
			if (file.getParent() == null) {
				fileInCanonicalDir = file;
			} else {
				File canonicalDir = file.getParentFile().getCanonicalFile();
				fileInCanonicalDir = new File(canonicalDir, file.getName());
			}

			if (fileInCanonicalDir.getCanonicalFile().equals(
					fileInCanonicalDir.getAbsoluteFile())) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			return false;
		}
	}

	public static void main(String[] args) {
		new RecursiveFileSystemWatcher(60000).start();
		int proc = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of available CPUs: " + proc);
		FileManager[] fma = new FileManager[proc];
		for (int i = 0; i < proc; i++) {
			fma[i] = new FileManager(i);
			fma[i].start();
		}
	}
}
