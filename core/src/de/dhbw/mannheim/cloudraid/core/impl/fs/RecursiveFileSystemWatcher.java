/*
 * Copyright 2011 - 2012 by the CloudRAID Team
 * see AUTHORS for more details
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.dhbw.mannheim.cloudraid.core.impl.fs;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.directory.InvalidAttributeValueException;

import de.dhbw.mannheim.cloudraid.core.impl.fs.FileQueue.FileAction;

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
	 * 
	 * @param pathToWatch
	 *            The path to be watched.
	 * @throws InvalidAttributeValueException
	 *             Thrown if the pathToWatch is null
	 */
	public RecursiveFileSystemWatcher(String pathToWatch)
			throws InvalidAttributeValueException {
		if (pathToWatch == null) {
			throw new InvalidAttributeValueException("Invalid path");
		}
		dir = new File(pathToWatch);
		System.out.println("Watching directory " + dir.getAbsolutePath());
		this.setPriority(MIN_PRIORITY);
		this.setName("RecursiveFileSystemWatcher");
	}

	/**
	 * Creates a RecursiveFileSystemWatcher that runs in the given interval.
	 * 
	 * @param pathToWatch
	 *            Specifies the path that will be watched for changes.
	 * @param sleepTime
	 *            The sleeping time in ms.
	 * @throws InvalidAttributeValueException
	 *             Thrown if the pathToWatch is null
	 */
	public RecursiveFileSystemWatcher(String pathToWatch, long sleepTime)
			throws InvalidAttributeValueException {
		this(pathToWatch);
		this.sleepTime = sleepTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
				FileQueue.add(k, FileAction.DELETE);
				fileMap.remove(k);
			}

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
		System.err.println("The file system watcher is stopped");
	}

	/**
	 * Runs through the list of files in the given directory and handles the
	 * files according to their type.
	 * 
	 * @param dir
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
				FileQueue.add(file.getAbsolutePath(), FileAction.MODIFY);
			}
			keySet.remove(name);
		} else {
			// a new file is found
			// System.out.println(file.getAbsolutePath() + " is a new file.");
			fileMap.put(file.getAbsolutePath(), file.lastModified());
			FileQueue.add(file.getAbsolutePath(), FileAction.CREATE);
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
}
