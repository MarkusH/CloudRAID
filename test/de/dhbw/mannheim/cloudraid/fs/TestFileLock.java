/*
 * Copyright 2011 by the CloudRAID Team, see AUTHORS for more details.
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFileLock {
	@BeforeClass
	public static void oneTimeSetUp() {

	}

	@AfterClass
	public static void oneTimeTearDown() {

	}

	@Test
	public void testLock() {
		FileManager fm1 = new FileManager();
		FileManager fm2 = new FileManager();

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