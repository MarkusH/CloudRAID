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
	public void testSetPlain() {
		config.put("plain.boolean.correct", true);
		config.put("plain.boolean.false", false);

		config.put("plain.double.correct", 1.0 / 7.0);
		config.put("plain.double.false", 1.0 / 9.0);

		config.put("plain.float.correct", 1.0 / 11.0);
		config.put("plain.float.false", 1.0 / 13.0);

		config.put("plain.int.correct", 42);
		config.put("plain.int.false", 1337);

		config.put("plain.long.correct", 9876543210l);
		config.put("plain.long.false", 99999999999l);

		config.put("plain.string.correct", "Correct");
		config.put("plain.string.false", "False");
	}

	@Test
	public void testPlain() throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		assertTrue(config.getBoolean("plain.boolean.correct", false));
		assertFalse(config.getBoolean("plain.boolean.false", true));

		assertEquals(1.0 / 7.0, config.getDouble("plain.double.correct", 1.0),
				0.0000000001);
		assertFalse(1.0 == config.getDouble("plain.double.false", 1.0));

		assertEquals(1.0f / 11.0f,
				config.getFloat("plain.float.correct", 1.0f), 0.00001);
		assertFalse(1.0f == config.getFloat("plain.float.false", 1.0f));

		assertEquals(42, config.getInt("plain.int.correct", 0));
		assertFalse(1234 == config.getInt("plain.int.false", 1234));

		assertEquals(9876543210l, config.getLong("plain.long.correct", 0));
		assertFalse(1l == config.getLong("plain.long.false", 1l));

		assertEquals("Correct", config.getString("plain.string.correct", "foo"));
		assertFalse("bar".equals(config.getString("plain.string.false", "bar")));
	}

	@Test
	public void testSetEncrypted() {
		config.put("encrypted.boolean.correct", true, true);
		config.put("encrypted.boolean.false", false, true);
		config.put("encrypted.double.correct", 1.0 / 15.0, true);
		config.put("encrypted.double.false", 1.0 / 17.0, true);
		config.put("encrypted.float.correct", 1.0 / 19.0, true);
		config.put("encrypted.float.false", 1.0 / 21.0, true);
		config.put("encrypted.int.correct", 12, true);
		config.put("encrypted.int.false", 34, true);
		config.put("encrypted.long.correct", 963852741l, true);
		config.put("encrypted.long.false", 1472583690l, true);
		config.put("encrypted.string.correct", "Correct", true);
		config.put("encrypted.string.false", "False", true);
	}

	@Test
	public void testEncrypted() throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		assertTrue(config.getBoolean("encrypted.boolean.correct", false));
		assertFalse(config.getBoolean("encrypted.boolean.false", true));

		assertEquals(1.0 / 15.0,
				config.getDouble("encrypted.double.correct", 1.0), 0.0000000001);
		assertFalse(1.0 == config.getDouble("encrypted.double.false", 1.0));

		assertEquals(1.0f / 19.0f,
				config.getFloat("encrypted.float.correct", 1.0f), 0.00001);
		assertFalse(1.0f == config.getFloat("encrypted.float.false", 1.0f));

		assertEquals(12, config.getInt("encrypted.int.correct", 0));
		assertFalse(1 == config.getInt("encrypted.int.false", 1));

		assertEquals(963852741l, config.getLong("encrypted.long.correct", 0));
		assertFalse(1l == config.getLong("encrypted.long.false", 1l));

		assertEquals("Correct",
				config.getString("encrypted.string.correct", "foo"));
		assertFalse("buz".equals(config.getString("encrypted.string.false",
				"buz")));
	}

	@Test
	public void testSave() throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		config.save();
		config.reload();
		testPlain();
		testEncrypted();

		/*
		 * Changing the config file and reloading the data, the following tests
		 * are just the inverse of the above and therefore fail in a positive
		 * manner.
		 */
		Config.setConfigPath(configpath + ".new");
		config.reload();

		// Test plain data
		assertFalse(config.getBoolean("plain.boolean.correct", false));
		assertTrue(config.getBoolean("plain.boolean.false", true));
		assertFalse(1.0 / 7.0 == config.getDouble("plain.double.correct", 1.0));
		assertTrue(1.0 == config.getDouble("plain.double.false", 1.0));
		assertFalse(1.0f / 11.0f == config
				.getFloat("plain.float.correct", 1.0f));
		assertTrue(1.0f == config.getFloat("plain.float.false", 1.0f));
		assertFalse(42 == config.getInt("plain.int.correct", 0));
		assertTrue(1234 == config.getInt("plain.int.false", 1234));
		assertFalse(9876543210l == config.getLong("plain.long.correct", 0));
		assertTrue(1l == config.getLong("plain.long.false", 1l));
		assertFalse("Correct".equals(config.getString("plain.string.correct",
				"foo")));
		assertTrue("bar".equals(config.getString("plain.string.false", "bar")));

		// Test encrypted data
		assertFalse(config.getBoolean("encrypted.boolean.correct", false));
		assertTrue(config.getBoolean("encrypted.boolean.false", true));
		assertFalse(1.0 / 15.0 == config.getDouble("encrypted.double.correct",
				1.0));
		assertTrue(1.0 == config.getDouble("encrypted.double.false", 1.0));
		assertFalse(1.0f / 19.0f == config.getFloat("encrypted.float.correct",
				1.0f));
		assertTrue(1.0f == config.getFloat("encrypted.float.false", 1.0f));
		assertFalse(12 == config.getInt("encrypted.int.correct", 0));
		assertTrue(1 == config.getInt("encrypted.int.false", 1));
		assertFalse(963852741l == config.getLong("encrypted.long.correct", 0));
		assertTrue(1l == config.getLong("encrypted.long.false", 1l));
		assertFalse("Correct".equals(config.getString(
				"encrypted.string.correct", "foo")));
		assertTrue("buz".equals(config.getString("encrypted.string.false",
				"buz")));
	}

}
