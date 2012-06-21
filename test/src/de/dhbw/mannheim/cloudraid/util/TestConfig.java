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

package de.dhbw.mannheim.cloudraid.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.NoSuchElementException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dhbw.mannheim.cloudraid.config.impl.Config;
import de.dhbw.mannheim.cloudraid.config.ICloudRAIDConfig;
import de.dhbw.mannheim.cloudraid.config.exceptions.InvalidConfigValueException;
import de.dhbw.mannheim.cloudraid.config.exceptions.MissingConfigValueException;

/**
 * @author Markus Holtermann
 */
public class TestConfig {

	private static final String password = "CloudRAID-Config-Password";
	private static final String cloudraidHome1 = System
			.getProperty("java.io.tmpdir") + File.separator + "cloudraid";
	private static final String cloudraidHome2 = System
			.getProperty("java.io.tmpdir")
			+ File.separator
			+ "cloudraid-new"
			+ File.separator;
	private static ICloudRAIDConfig config;

	@BeforeClass
	public static void oneTimeSetUp() {
		File f = new File(cloudraidHome1);
		if (f.exists()) {
			f.delete();
		}
		config = new Config();
		config.setCloudRAIDHome(System.getProperty("java.io.tmpdir")
				+ File.separator + "cloudraid");
		config.init("CloudRAID-unitTests");
		config.init(password);
	}

	@AfterClass
	public static void oneTimeTearDown() {
		File f = new File(cloudraidHome1 + File.separator + "config.xml");
		if (f.exists()) {
			f.delete();
		}
		f = new File(cloudraidHome1);
		if (f.exists()) {
			f.delete();
		}
		f = new File(cloudraidHome2);
		if (f.exists()) {
			f.delete();
		}
	}

	@Test
	public void testCloudRAIDHome() {
		config.setCloudRAIDHome(cloudraidHome2);
		assertEquals(cloudraidHome2, config.getCloudRAIDHome());
		config.setCloudRAIDHome(cloudraidHome1);
		assertEquals(cloudraidHome1 + File.separator, config.getCloudRAIDHome());
	}

	@Test
	public void testConfigPath() {
		config.setCloudRAIDHome(cloudraidHome1);
		assertEquals(cloudraidHome1 + File.separator + "config.xml",
				config.getConfigPath());
	}

	@Test
	public void testBoolean() throws MissingConfigValueException,
			InvalidConfigValueException {
		config.put("plain.boolean.correct", true);

		assertTrue(config.keyExists("plain.boolean.correct"));
		assertFalse(config.keyExists("plain.boolean.false"));
		assertTrue(config.getBoolean("plain.boolean.correct", false));
		assertTrue(config.getBoolean("plain.boolean.false", true));

		config.put("encrypted.boolean.correct", true, true);

		assertTrue(config.keyExists("encrypted.boolean.correct"));
		assertFalse(config.keyExists("encrypted.boolean.false"));
		assertTrue(config.getBoolean("encrypted.boolean.correct", false));
		assertTrue(config.getBoolean("encrypted.boolean.false", true));
	}

	@Test
	public void testInt() throws MissingConfigValueException,
			InvalidConfigValueException {
		config.put("plain.int.correct", 42);

		assertTrue(config.keyExists("plain.int.correct"));
		assertFalse(config.keyExists("plain.int.false"));
		assertEquals(42, config.getInt("plain.int.correct", 0));
		assertEquals(1234, config.getInt("plain.int.false", 1234));

		config.put("encrypted.int.correct", 1337, true);

		assertTrue(config.keyExists("encrypted.int.correct"));
		assertFalse(config.keyExists("encrypted.int.false"));
		assertEquals(1337, config.getInt("encrypted.int.correct", 0));
		assertEquals(19, config.getInt("encrypted.int.false", 19));
	}

	@Test
	public void testLong() throws NoSuchElementException,
			InvalidConfigValueException {
		config.put("plain.long.correct", 9876543210l);

		assertTrue(config.keyExists("plain.long.correct"));
		assertFalse(config.keyExists("plain.long.false"));
		assertEquals(9876543210l, config.getLong("plain.long.correct", 0l));
		assertEquals(1l, config.getLong("plain.long.false", 1l));

		config.put("encrypted.long.correct", 963852741l, true);

		assertTrue(config.keyExists("encrypted.long.correct"));
		assertFalse(config.keyExists("encrypted.long.false"));
		assertEquals(963852741l, config.getLong("encrypted.long.correct", 0l));
		assertEquals(1l, config.getLong("encrypted.long.false", 1l));
	}

	@Test
	public void testFloat() throws MissingConfigValueException,
			InvalidConfigValueException {
		config.put("plain.float.correct", 1.0f / 11.0f);

		assertTrue(config.keyExists("plain.float.correct"));
		assertFalse(config.keyExists("plain.float.false"));
		assertEquals(1.0f / 11.0f,
				config.getFloat("plain.float.correct", 1.0f), 0.00001f);
		assertEquals(1.0f, config.getFloat("plain.float.false", 1.0f), 0.00001f);

		config.put("encrypted.float.correct", 1.0f / 19.0f, true);

		assertTrue(config.keyExists("encrypted.float.correct"));
		assertFalse(config.keyExists("encrypted.float.false"));
		assertEquals(1.0f / 19.0f,
				config.getFloat("encrypted.float.correct", 1.0f), 0.00001f);
		assertEquals(1.0f, config.getFloat("encrypted.float.false", 1.0f),
				0.00001f);
	}

	@Test
	public void testDouble() throws MissingConfigValueException,
			InvalidConfigValueException {
		config.put("plain.double.correct", 1.0 / 7.0);

		assertTrue(config.keyExists("plain.double.correct"));
		assertFalse(config.keyExists("plain.double.false"));
		assertEquals(1.0 / 7.0, config.getDouble("plain.double.correct", 1.0),
				0.0000000001);
		assertEquals(1.0, config.getDouble("plain.double.false", 1.0),
				0.0000000001);

		config.put("encrypted.double.correct", 1.0 / 15.0, true);

		assertTrue(config.keyExists("encrypted.double.correct"));
		assertFalse(config.keyExists("encrypted.double.false"));
		assertEquals(1.0 / 15.0,
				config.getDouble("encrypted.double.correct", 1.0), 0.0000000001);
		assertEquals(1.0, config.getDouble("encrypted.double.false", 1.0),
				0.0000000001);
	}

	@Test
	public void testString() throws NoSuchElementException,
			InvalidConfigValueException {
		config.put("plain.string.correct", "Correct");

		assertTrue(config.keyExists("plain.string.correct"));
		assertFalse(config.keyExists("plain.string.false"));
		assertEquals("Correct", config.getString("plain.string.correct", "foo"));
		assertEquals("bar", config.getString("plain.string.false", "bar"));

		config.put("encrypted.string.correct", "Correct", true);

		assertTrue(config.keyExists("encrypted.string.correct"));
		assertFalse(config.keyExists("encrypted.string.false"));
		assertEquals("Correct",
				config.getString("encrypted.string.correct", "foo"));
		assertEquals("buz", config.getString("encrypted.string.false", "buz"));
	}

	@Test
	public void testSave() throws MissingConfigValueException,
			InvalidConfigValueException {
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
		config.setCloudRAIDHome(cloudraidHome2);
		config.reload();

		// Test plain data
		assertFalse(config.keyExists("plain.boolean.correct"));
		assertFalse(config.keyExists("plain.boolean.false"));
		assertFalse(config.getBoolean("plain.boolean.correct", false));

		assertFalse(config.keyExists("plain.double.correct"));
		assertFalse(config.keyExists("plain.double.false"));
		assertEquals(1.0, config.getDouble("plain.double.correct", 1.0),
				0.0000000001);

		assertFalse(config.keyExists("plain.float.correct"));
		assertFalse(config.keyExists("plain.float.false"));
		assertEquals(1.0f, config.getFloat("plain.float.correct", 1.0f),
				0.00001f);

		assertFalse(config.keyExists("plain.int.correct"));
		assertFalse(config.keyExists("plain.int.false"));
		assertEquals(0, config.getInt("plain.int.correct", 0));

		assertFalse(config.keyExists("plain.long.correct"));
		assertFalse(config.keyExists("plain.long.false"));
		assertEquals(0, config.getLong("plain.long.correct", 0l));

		assertFalse(config.keyExists("plain.string.correct"));
		assertFalse(config.keyExists("plain.string.false"));
		assertEquals("foo", config.getString("plain.string.correct", "foo"));

		// Test encrypted data
		assertFalse(config.keyExists("encrypted.boolean.correct"));
		assertFalse(config.keyExists("encrypted.boolean.false"));
		assertFalse(config.getBoolean("encrypted.boolean.correct", false));

		assertFalse(config.keyExists("encrypted.double.correct"));
		assertFalse(config.keyExists("encrypted.double.false"));
		assertEquals(1.0, config.getDouble("encrypted.double.correct", 1.0),
				0.0000000001);

		assertFalse(config.keyExists("encrypted.float.correct"));
		assertFalse(config.keyExists("encrypted.float.false"));
		assertEquals(1.0f, config.getFloat("encrypted.float.correct", 1.0f),
				0.00001f);

		assertFalse(config.keyExists("encrypted.int.correct"));
		assertFalse(config.keyExists("encrypted.int.false"));
		assertEquals(0, config.getInt("encrypted.int.correct", 0));

		assertFalse(config.keyExists("encrypted.long.correct"));
		assertFalse(config.keyExists("encrypted.long.false"));
		assertEquals(0, config.getLong("encrypted.long.correct", 0l));

		assertFalse(config.keyExists("encrypted.string.correct"));
		assertFalse(config.keyExists("encrypted.string.false"));
		assertEquals("foo", config.getString("encrypted.string.correct", "foo"));
	}

	@Test
	public void testEncryptionStatusChange() throws NoSuchElementException,
			InvalidConfigValueException {
		config.put("key1", "Test1", false);
		assertEquals("Test1", config.getString("key1", "Default1"));
		config.put("key1", "Test2", true);
		assertEquals("Test2", config.getString("key1", "Default2"));

		config.put("key2", "Test3", true);
		assertEquals("Test3", config.getString("key2", "Default3"));
		config.put("key2", "Test4", false);
		assertEquals("Test4", config.getString("key2", "Default4"));
	}
}
