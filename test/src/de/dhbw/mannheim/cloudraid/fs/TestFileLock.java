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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dhbw.mannheim.cloudraid.config.impl.Config;
import de.dhbw.mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw.mannheim.cloudraid.core.impl.fs.FileLock;
import de.dhbw.mannheim.cloudraid.core.impl.fs.FileManager;

public class TestFileLock {

	private static ICloudRAIDConfig config;

	@BeforeClass
	public static void oneTimeSetUp() {
		config = new Config();
		config.setCloudRAIDHome(System.getProperty("java.io.tmpdir")
				+ File.separator + "cloudraid");
		config.init("CloudRAID-unitTests");
	}

	@AfterClass
	public static void oneTimeTearDown() {
		config.delete();
	}

	@Test
	public void testLock() {
		FileManager fm1 = new FileManager(config);
		FileManager fm2 = new FileManager(config);

		assertTrue(FileLock.lock("string", fm1));

		assertFalse(FileLock.lock("string", fm2));

		assertFalse(FileLock.unlock("string", fm2));

		assertTrue(FileLock.unlock("string", fm1));

		assertTrue(FileLock.lock("string", fm2));

		assertTrue(FileLock.lock("another-string", fm1));

		assertTrue(FileLock.unlock("string", fm2));

		assertFalse(FileLock.unlock("string", fm2));
	}

}