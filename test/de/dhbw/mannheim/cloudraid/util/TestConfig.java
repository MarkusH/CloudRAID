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

package de.dhbw.mannheim.cloudraid.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Markus Holtermann
 */
public class TestConfig {

	private static final String password = "CloudRAID-Config-Password";
	private static final String configpath = System
			.getProperty("java.io.tmpdir") + "/config.xml";
	private static Config config;

	@BeforeClass
	public static void oneTimeSetUp() {
		File f = new File(configpath);
		if (f.exists()) {
			f.delete();
		}
		config = Config.getInstance();
		Config.setConfigPath(configpath);
		config.init(password);
	}

	@AfterClass
	public static void oneTimeTeadDown() {
		File f = new File(configpath);
		if (f.exists()) {
			f.delete();
		}
		f = new File(configpath + ".new");
		if (f.exists()) {
			f.delete();
		}
		config = Config.getInstance();
		Config.setConfigPath(configpath);
		config.init(password);
	}

	@Test
	public void testBoolean() throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		config.put("plain.boolean.correct", true);
		config.put("plain.boolean.false", false);

		assertTrue(config.getBoolean("plain.boolean.correct", false));
		assertFalse(config.getBoolean("plain.boolean.false", true));

		config.put("encrypted.boolean.correct", true, true);
		config.put("encrypted.boolean.false", false, true);

		assertTrue(config.getBoolean("encrypted.boolean.correct", false));
		assertFalse(config.getBoolean("encrypted.boolean.false", true));
	}

	@Test
	public void testInt() throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		config.put("plain.int.correct", 42);

		assertEquals(42, config.getInt("plain.int.correct", 0));
		assertEquals(1234, config.getInt("plain.int.false", 1234));

		config.put("encrypted.int.correct", 1337, true);

		assertEquals(1337, config.getInt("encrypted.int.correct", 0));
		assertEquals(19, config.getInt("encrypted.int.false", 19));
	}

	@Test
	public void testLong() throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		config.put("plain.long.correct", 9876543210l);

		assertEquals(9876543210l, config.getLong("plain.long.correct", 0));
		assertEquals(1l, config.getLong("plain.long.false", 1l));

		config.put("encrypted.long.correct", 963852741l, true);

		assertEquals(963852741l, config.getLong("encrypted.long.correct", 0));
		assertEquals(1l, config.getLong("encrypted.long.false", 1l));
	}

	@Test
	public void testFloat() throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		config.put("plain.float.correct", 1.0 / 11.0);

		assertEquals(1.0f / 11.0f,
				config.getFloat("plain.float.correct", 1.0f), 0.00001);
		assertEquals(1.0f, config.getFloat("plain.float.false", 1.0f), 0.00001);

		config.put("encrypted.float.correct", 1.0 / 19.0, true);

		assertEquals(1.0f / 19.0f,
				config.getFloat("encrypted.float.correct", 1.0f), 0.00001);
		assertEquals(1.0f, config.getFloat("encrypted.float.false", 1.0f),
				0.00001);
	}

	@Test
	public void testDouble() throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		config.put("plain.double.correct", 1.0 / 7.0);

		assertEquals(1.0 / 7.0, config.getDouble("plain.double.correct", 1.0),
				0.0000000001);
		assertEquals(1.0, config.getDouble("plain.double.false", 1.0),
				0.0000000001);

		config.put("encrypted.double.correct", 1.0 / 15.0, true);

		assertEquals(1.0 / 15.0,
				config.getDouble("encrypted.double.correct", 1.0), 0.0000000001);
		assertEquals(1.0, config.getDouble("encrypted.double.false", 1.0),
				0.0000000001);
	}

	@Test
	public void testString() throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		config.put("plain.string.correct", "Correct");

		assertEquals("Correct", config.getString("plain.string.correct", "foo"));
		assertEquals("bar", config.getString("plain.string.false", "bar"));

		config.put("encrypted.string.correct", "Correct", true);

		assertEquals("Correct",
				config.getString("encrypted.string.correct", "foo"));
		assertEquals("buz", config.getString("encrypted.string.false", "buz"));
	}

	@Test
	public void testSave() throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		config.save();
		config.reload();
		testBoolean();
		testInt();
		testLong();
		testFloat();
		testDouble();
		testString();

		/*
		 * Changing the config file and reloading the data, the following tests
		 * are just the inverse of the above and therefore fail in a positive
		 * manner.
		 */
		Config.setConfigPath(configpath + ".new");
		config.reload();

		// Test plain data
		assertFalse(config.getBoolean("plain.boolean.correct", false));

		assertEquals(1.0, config.getDouble("plain.double.correct", 1.0),
				0.0000000001);

		assertEquals(1.0f, config.getFloat("plain.float.correct", 1.0f),
				0.00001);

		assertEquals(0, config.getInt("plain.int.correct", 0));

		assertEquals(0, config.getLong("plain.long.correct", 0));

		assertEquals("foo", config.getString("plain.string.correct", "foo"));

		// Test encrypted data
		assertFalse(config.getBoolean("encrypted.boolean.correct", false));

		assertEquals(1.0, config.getDouble("encrypted.double.correct", 1.0),
				0.0000000001);

		assertEquals(1.0f, config.getFloat("encrypted.float.correct", 1.0f),
				0.00001);

		assertEquals(0, config.getInt("encrypted.int.correct", 0));

		assertEquals(0, config.getLong("encrypted.long.correct", 0));

		assertEquals("foo", config.getString("encrypted.string.correct", "foo"));
	}

}
