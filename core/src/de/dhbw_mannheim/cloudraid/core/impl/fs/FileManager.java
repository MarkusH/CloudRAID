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

package de.dhbw_mannheim.cloudraid.core.impl.fs;

import java.io.File;
import java.util.NoSuchElementException;

import de.dhbw_mannheim.cloudraid.core.impl.jni.RaidAccessInterface;
import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;

/**
 * Reads as a thread the {@link FileQueue} and handles the files according to
 * the {@link FileQueueEntry}.
 * 
 * @author Florian Bausch
 * 
 */
public class FileManager extends Thread {

	private final static String KEY = "key";

	private int interval = 2000;
	private ICloudRAIDConfig config;

	/**
	 * Creates a FileManager thread with minimal priority.
	 * 
	 * @param config
	 *            An instance of a {@link ICloudRAIDConfig}
	 */
	public FileManager(ICloudRAIDConfig config) {
		this.setPriority(MIN_PRIORITY);
		this.config = config;
	}

	/**
	 * Creates a FileManager thread with minimal priority. Sets the name of the
	 * Thread to "FileManager-" + i and sets the interval of scanning the
	 * {@link FileQueue} to (i+1)*2s.
	 * 
	 * @param config
	 *            An instance of a {@link ICloudRAIDConfig}
	 * @param i
	 *            The number of the thread.
	 */
	public FileManager(ICloudRAIDConfig config, int i) {
		this(config);
		this.setName("FileManager-" + i);
		this.interval = (i + 1) * 2000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		boolean wasFull = false;
		long startTime = 0;
		while (!this.isInterrupted()) {
			if (FileQueue.isEmpty()) {
				if (wasFull)
					System.err.println("Time: "
							+ (System.currentTimeMillis() - startTime));
				wasFull = false;
				try {
					sleep(this.interval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
			} else {
				if (!wasFull)
					startTime = System.currentTimeMillis();
				wasFull = true;
				FileQueueEntry fqe = null;
				try {
					fqe = FileQueue.get();
				} catch (NoSuchElementException e) {
					System.err.println("Empty queue!");
					continue;
				}

				if (FileLock.lock(fqe.getFileName(), this)) {
					switch (fqe.getFileAction()) {
					case CREATE:
						System.out.println("Upload new file "
								+ fqe.getFileName());
						splitFile(fqe.getFileName());
						break;

					case DELETE:
						System.out.println("Send delete order for "
								+ fqe.getFileName());
						break;

					case MODIFY:
						System.out.println("Upload updated file "
								+ fqe.getFileName());
						splitFile(fqe.getFileName());
						break;
					default:
						System.err.println("This should not happen.");
						break;
					}
					FileLock.unlock(fqe.getFileName(), this);
				} else {
					System.err.println("File " + fqe.getFileName()
							+ " already locked");
					try {
						sleep(this.interval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
				}
			}
		}
		System.err.println("FileManager thread stopped");
	}

	/**
	 * Splits the file and merges it again.
	 * 
	 * @param filename
	 *            The file to be merged and split.
	 */
	private void splitFile(String filename) {

		// System.out.println("Start splitting " + filename);
		// Split the file into three RAID5 redundant files.
		if (!new File(filename).exists()) {
			System.err.println("The file " + filename
					+ " is not existing anymore");
			return;
		}

		String splitInputDir, splitOutputDir;
		// String mergeInputDir, mergeOutputDir;
		try {
			// mergeInputDir = config.getString("merge.input.dir", null);
			// mergeOutputDir = config.getString("merge.output.dir", null);
			splitInputDir = config.getString("split.input.dir", null);
			splitOutputDir = config.getString("split.output.dir", null);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		System.err.println(filename);
		System.err.println(splitInputDir);
		System.err.println(splitOutputDir);

		// TODO Update file status to SPLITTING
		String hashedFilename = RaidAccessInterface.splitInterface(
				splitInputDir, filename.substring(splitInputDir.length()),
				splitOutputDir, KEY);
		System.err.println(hashedFilename);

		// TODO Update file status to SPLITTED
		// TODO Can we add the splitted files to the Queue to handle their
		// upload??
		// TODO Update file status to UPLOADING
		// TODO Upload the files
		// TODO Update file status to UPLOADED

		// String name = new File(filename).getName();
		// RaidAccessInterface.mergeInterface(mergeInputDir, hashedFilename,
		// mergeOutputDir + name, KEY);

		/* Do something fancy. */

		// Delete the split files.
		new File(splitOutputDir + hashedFilename + ".0").delete();
		new File(splitOutputDir + hashedFilename + ".1").delete();
		new File(splitOutputDir + hashedFilename + ".2").delete();
		new File(splitOutputDir + hashedFilename + ".m").delete();
		new File(filename).delete();
		// new File(mergeOutputDir + name).delete();
		// TODO Update file status to READY
	}

}
