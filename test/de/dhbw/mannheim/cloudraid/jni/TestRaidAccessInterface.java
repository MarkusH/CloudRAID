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

package de.dhbw.mannheim.cloudraid.jni;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Markus Holtermann
 */
public class TestRaidAccessInterface {

	private static File in, out;
	private static String hash;
	private static char[] content_array;
	private static String content;

	private static final int CONTENT_LENGTH = 1024 * 20;
	private static final String tmpPath = System.getProperty("java.io.tmpdir")
			+ File.separator;
	private static final String KEY = "CloudRAID";
	private static final int KEY_LENGTH = KEY.length();

	@BeforeClass
	public static void oneTimeSetUp() throws IOException {
		int i;
		in = new File(tmpPath, "TestRaidAccessInterface.in");
		out = new File(tmpPath, "TestRaidAccessInterface.out");
		FileWriter fw = new FileWriter(in);
		content_array = new char[CONTENT_LENGTH];

		for (i = 0; i < CONTENT_LENGTH; i++) {
			content_array[i] = (char) (i % 256);
		}

		content = new String(content_array);

		fw.write(content);
		fw.close();
	}

	@AfterClass
	public static void oneTimeTearDown() {
		in.delete();
		new File(tmpPath, hash + ".0").delete();
		new File(tmpPath, hash + ".1").delete();
		new File(tmpPath, hash + ".2").delete();
		new File(tmpPath, hash + ".m").delete();
		out.delete();
	}

	@Test
	public void testSplit() throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		hash = RaidAccessInterface.splitInterface(in.getAbsolutePath(),
				tmpPath, KEY, KEY_LENGTH);

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		byte[] expected = digest.digest(in.getAbsolutePath().getBytes("UTF-8"));
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < expected.length; i++) {
			sb.append(Integer.toString((expected[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		assertEquals(sb.toString(), hash);
	}

	@Test
	public void testMerge() {
		int i = RaidAccessInterface.mergeInterface(tmpPath, hash,
				out.getAbsolutePath(), KEY, KEY_LENGTH);
		assertEquals(0x02, i);
	}

	@Test
	public void testContent() throws IOException {
		FileReader fr;
		char[] buff;
		fr = new FileReader(in);
		buff = new char[CONTENT_LENGTH];
		fr.read(buff, 0, CONTENT_LENGTH);
		assertEquals(content, new String(buff));
		fr.close();

		fr = new FileReader(out);
		buff = new char[CONTENT_LENGTH];
		fr.read(buff, 0, CONTENT_LENGTH);
		assertEquals(content, new String(buff));
		fr.close();
	}
}
