package de.dhbw.mannheim.cloudraid.fs;

import java.io.File;
import java.nio.file.FileSystems;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class JNotifyFileSystemListener {

	private String dir;
	private final static String TMP = System.getProperty("java.io.tmpdir");
	private final static File TMP_FILE = new File(TMP);

	public JNotifyFileSystemListener() {
		dir = FileSystems.getDefault()
				.getPath(System.getProperty("user.home"), "").toString()
				+ "/tmp/";
		System.out.println("Watching directory "
				+ System.getProperty("user.home"));
	}

	public void run() {
		int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED
				| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED | JNotify.FILE_ANY;
		boolean watchSubtree = true;
		try {
			int watchID = JNotify.addWatch(dir, mask, watchSubtree,
					new JNotifyListener() {
						public void fileRenamed(int wd, String rootPath,
								String oldName, String newName) {
							System.out
									.println("JNotifyTest.fileRenamed() : wd #"
											+ wd + " root = " + rootPath + ", "
											+ oldName + " -> " + newName);
						}

						public void fileModified(int wd, String rootPath,
								String name) {
							System.out
									.println("JNotifyTest.fileModified() : wd #"
											+ wd
											+ " root = "
											+ rootPath
											+ ", "
											+ name);
						}

						public void fileDeleted(int wd, String rootPath,
								String name) {
							System.out
									.println("JNotifyTest.fileDeleted() : wd #"
											+ wd + " root = " + rootPath + ", "
											+ name);
						}

						public void fileCreated(int wd, String rootPath,
								String name) {
							System.out
									.println("JNotifyTest.fileCreated() : wd #"
											+ wd + " root = " + rootPath + ", "
											+ name);
						}
					});
			System.out.println("The watchID is: " + watchID);
		} catch (JNotifyException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new JNotifyFileSystemListener().run();
	}
}
