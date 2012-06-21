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

package de.dhbw.mannheim.cloudraid.fs;

import static de.dhbw.mannheim.cloudraid.core.impl.fs.FileQueue.FileAction.CREATE;
import static de.dhbw.mannheim.cloudraid.core.impl.fs.FileQueue.FileAction.DELETE;
import static de.dhbw.mannheim.cloudraid.core.impl.fs.FileQueue.FileAction.MODIFY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.naming.directory.InvalidAttributeValueException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dhbw.mannheim.cloudraid.config.Config;
import de.dhbw.mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw.mannheim.cloudraid.core.impl.fs.FileManager;
import de.dhbw.mannheim.cloudraid.core.impl.fs.FileQueue;
import de.dhbw.mannheim.cloudraid.core.impl.fs.FileQueueEntry;
import de.dhbw.mannheim.cloudraid.core.impl.fs.RecursiveFileSystemWatcher;

public class TestFileSystemUtilities {

	private static File file1, file2, file3, file4;
	private static String splitInputDir, mergeOutputDir;
	private static ICloudRAIDConfig config;

	@BeforeClass
	public static void oneTimeSetUp() throws IOException {
		String TMP = System.getProperty("java.io.tmpdir") + File.separator
				+ "cloudraid-test" + File.separator;
		config = new Config();
		config.setCloudRAIDHome(System.getProperty("java.io.tmpdir")
				+ File.separator + "cloudraid");
		config.init("CloudRAID-unitTests");
		config.put("merge.input.dir", TMP);
		config.put("merge.output.dir", TMP + "out" + File.separator);
		config.put("split.input.dir", TMP);
		config.put("split.output.dir", TMP);

		try {
			mergeOutputDir = config.getString("merge.output.dir", TMP + "out"
					+ File.separator);
			splitInputDir = config.getString("split.input.dir", TMP);
		} catch (Exception e) {
			e.printStackTrace();
		}

		new File(splitInputDir).mkdirs();
		new File(splitInputDir + "subdir" + File.separator).mkdirs();
		new File(mergeOutputDir).mkdirs();
		file1 = new File(TMP + "file1");
		file1.createNewFile();
		file2 = new File(TMP + "file2");
		file2.createNewFile();
		file3 = new File(TMP + "subdir" + File.separator + "file3");
		file3.createNewFile();
	}

	@AfterClass
	public static void oneTimeTearDown() {
		file3.delete();
		file2.delete();
		file1.delete();
		new File(mergeOutputDir).delete();
		new File(splitInputDir + "subdir" + File.separator).delete();
		new File(splitInputDir).delete();
		config.delete();
	}

	@Test
	public void testRecursiveFileSystemWatcher() throws InterruptedException,
			IOException, InvalidAttributeValueException {
		RecursiveFileSystemWatcher rfsw = new RecursiveFileSystemWatcher(
				splitInputDir, 500);
		rfsw.start();
		Thread.sleep(1000);
		FileQueueEntry fqe;
		for (int i = 0; i < 3; i++) {
			assertFalse(FileQueue.isEmpty());
			fqe = FileQueue.get();
			String filename = fqe.getFileName();
			if (filename.endsWith("file4")) {
				FileQueue.add(fqe.getFileName(), CREATE);
				continue;
			}
			boolean isValidName = filename.equals(file1.getAbsolutePath())
					|| filename.equals(file2.getAbsolutePath())
					|| filename.equals(file3.getAbsolutePath());
			assertTrue(isValidName);
			assertTrue(fqe.getFileAction().equals(CREATE));
		}

		file4 = new File(splitInputDir + File.separator + "subdir"
				+ File.separator + "file4");
		file4.createNewFile();
		Thread.sleep(1000);

		assertFalse(FileQueue.isEmpty());
		fqe = FileQueue.get();
		assertTrue(fqe.getFileName().equals(file4.getAbsolutePath()));
		assertTrue(fqe.getFileAction().equals(CREATE));
		assertTrue(FileQueue.isEmpty());

		file4.delete();
		file4.createNewFile();
		Thread.sleep(1000);

		assertFalse(FileQueue.isEmpty());
		fqe = FileQueue.get();
		assertTrue(fqe.getFileName().equals(file4.getAbsolutePath()));
		assertTrue(fqe.getFileAction().equals(MODIFY));
		assertTrue(FileQueue.isEmpty());

		file4.delete();
		Thread.sleep(1000);

		assertFalse(FileQueue.isEmpty());
		fqe = FileQueue.get();
		assertTrue(fqe.getFileName().equals(file4.getAbsolutePath()));
		assertTrue(fqe.getFileAction().equals(DELETE));
		assertTrue(FileQueue.isEmpty());

		rfsw.interrupt();
		rfsw.join();

		assertTrue(FileQueue.isEmpty());
	}

	@Test
	public void testFileManager() throws InterruptedException {
		FileQueue.add(file1.getAbsolutePath(), CREATE);
		FileQueue.add(file2.getAbsolutePath(), MODIFY);
		FileQueue.add(file3.getAbsolutePath(), DELETE);

		assertFalse(FileQueue.isEmpty());

		FileManager fm = new FileManager(config, 0);
		fm.start();
		Thread.sleep(1000);
		fm.interrupt();
		fm.join();
		assertTrue(FileQueue.isEmpty());
	}

}
