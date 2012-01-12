package de.dhbw.mannheim.cloudraid.fs;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import de.dhbw.mannheim.cloudraid.jni.RaidAccessInterface;

public class RecursiveFileSystemWatcher extends Thread {

	private File dir;
	private final static String TMP = System.getProperty("os.name")
			.toLowerCase().contains("windows") ? "C:\\temp\\cloudraid\\"
			: "/tmp/cloudraid/";
	private final static File TMP_FILE = new File(TMP);

	/**
	 * A map containing all known files.
	 */
	private static ConcurrentHashMap<String, Long> fileMap = new ConcurrentHashMap<String, Long>();

	private Vector<String> keySet = new Vector<String>();

	private long sleepTime = 10000;

	private int count = 0;

	/**
	 * Creates a RecursiveFileSystemWatcher that runs every 10s.
	 */
	public RecursiveFileSystemWatcher() {
		dir = new File(System.getProperty("user.home") + "/Dropbox/");
		System.out.println("Watching directory " + dir.getAbsolutePath());
		TMP_FILE.mkdirs();
		this.setPriority(MIN_PRIORITY);
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
			long startTime = System.currentTimeMillis();
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
				System.out.println(k + " was deleted.");
				fileMap.remove(k);
			}

			long endTime = System.currentTimeMillis();
			System.out.println("Splitting took " + (endTime - startTime));
			System.out.println("Files: " + count);
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
		count++;
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

				this.splitFile(file.getAbsolutePath());
			}
			keySet.remove(name);
		} else {
			// a new file is found
			// System.out.println(file.getAbsolutePath() + " is a new file.");
			fileMap.put(file.getAbsolutePath(), file.lastModified());

			this.splitFile(file.getAbsolutePath());
		}
	}

	private void splitFile(String filename) {

		// System.out.println("Start splitting " + filename);
		// Split the file into three RAID5 redundant files.
		String hashedFilename = RaidAccessInterface.splitInterface(filename,
				TMP, "key", 256);

		/* Do something fancy. */

		// Delete the split files.
//		new File(TMP + hashedFilename + ".0").delete();
//		new File(TMP + hashedFilename + ".1").delete();
//		new File(TMP + hashedFilename + ".2").delete();
//		new File(TMP + hashedFilename + ".m").delete();

		// System.out.println("done.");

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
	}
}
