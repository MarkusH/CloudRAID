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

package de.dhbw.mannheim.cloudraid.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dhbw.mannheim.cloudraid.util.Config;

/**
 * This JUnit test tests the {@link HSQLDatabaseConnector}.
 * 
 * @author Florian Bausch
 * 
 */
public class TestHSQLDatabaseConnector {

	private static DatabaseConnector dbc;
	private static final String CONNECTOR_CLASS = "de.dhbw.mannheim.cloudraid.persistence.HSQLDatabaseConnector";
	private static final String DATABASE_FILE = "testfiledb";
	private static final String PATH = "path", PATH2 = "path2";
	private static final String HASH = "hash", HASH2 = "hash2";
	private static final long TIME = 100000L, TIME2 = 200000L;

	@BeforeClass
	public static void oneTimeSetUp() {
		dbc = DatabaseConnector.getDatabaseConnector(CONNECTOR_CLASS);
		assertTrue(dbc.connect());
		assertTrue(dbc.disconnect());
		assertTrue(dbc.connect(Config.CONFIG_HOME + DATABASE_FILE));
		assertTrue(dbc.initialize());
	}

	@AfterClass
	public static void oneTimeTearDown() {
		assertTrue(dbc.disconnect());
		new File(Config.getCloudRAIDHome() + DATABASE_FILE + ".data").delete();
		new File(Config.getCloudRAIDHome() + DATABASE_FILE + ".properties").delete();
		new File(Config.getCloudRAIDHome() + DATABASE_FILE + ".script").delete();
	}

	@Test
	public void testInsert() {
		assertTrue(dbc.insert(PATH, HASH, TIME));

		assertTrue(PATH.equals(dbc.getName(HASH)));
		assertTrue(HASH.equals(dbc.getHash(PATH)));
		assertTrue(TIME == dbc.getLastMod(PATH));

		assertTrue(dbc.insert(PATH, HASH, TIME2));

		assertTrue(PATH.equals(dbc.getName(HASH)));
		assertTrue(HASH.equals(dbc.getHash(PATH)));
		assertTrue(TIME2 == dbc.getLastMod(PATH));

		assertTrue(dbc.insert(PATH2, HASH2, TIME2));

		assertTrue(PATH2.equals(dbc.getName(HASH2)));
		assertTrue(HASH2.equals(dbc.getHash(PATH2)));
		assertTrue(TIME2 == dbc.getLastMod(PATH2));

		assertTrue(PATH.equals(dbc.getName(HASH)));
		assertTrue(HASH.equals(dbc.getHash(PATH)));
		assertTrue(TIME2 == dbc.getLastMod(PATH));
	}

	@Test
	public void testDelete() {
		long time = System.currentTimeMillis();
		String path = "path3";
		String hash = "hash3";
		assertTrue(dbc.insert(path, hash, time));
		dbc.delete(path);

		assertTrue(dbc.getName(hash) == null);
		assertTrue(dbc.getHash(path) == null);
		assertTrue(dbc.getLastMod(path) == -1L);

		assertTrue(dbc.delete(path));
	}

	@Test
	public void testAccessToClosedConnection() {
		assertTrue(dbc.disconnect());

		assertTrue(dbc.disconnect());
		assertTrue(dbc.getHash(PATH) == null);
		assertTrue(dbc.getName(HASH) == null);
		assertTrue(dbc.getLastMod(PATH) == -1L);
		assertFalse(dbc.insert(PATH, HASH, TIME));
		assertFalse(dbc.initialize());
		assertFalse(dbc.delete(PATH));

		assertTrue(dbc.connect(Config.CONFIG_HOME + DATABASE_FILE));
		assertTrue(dbc.initialize());
	}
}
