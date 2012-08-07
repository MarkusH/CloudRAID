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

package de.dhbw_mannheim.cloudraid.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.junit.BeforeClass;
import org.junit.Test;

import de.dhbw_mannheim.cloudraid.api.impl.RestApiUrlMapping;
import de.dhbw_mannheim.cloudraid.api.impl.RestApiUrlMapping.MatchResult;
import de.dhbw_mannheim.cloudraid.api.responses.IRestApiResponse;

/**
 * @author Markus Holtermann
 * 
 */
public class TestRestApi {

	private static ArrayList<RestApiUrlMapping> mappings = new ArrayList<RestApiUrlMapping>();

	@BeforeClass
	public static void oneTimeSetUp() throws IllegalArgumentException,
			SecurityException, NoSuchMethodException {
		mappings.add(new RestApiUrlMapping(Pattern.compile("^/test0/$"),
				RestApiUrlMapping.findFunction(TestRestApi.class, "function")));
		mappings.add(new RestApiUrlMapping("^/test1/$", RestApiUrlMapping
				.findFunction(TestRestApi.class, "function")));
		mappings.add(new RestApiUrlMapping(Pattern.compile("^/test2/$"),
				TestRestApi.class, "function"));
		mappings.add(new RestApiUrlMapping("^/test3/$", TestRestApi.class,
				"function"));

		mappings.add(new RestApiUrlMapping(Pattern.compile("^/test4/$"), "GET",
				RestApiUrlMapping.findFunction(TestRestApi.class, "function")));
		mappings.add(new RestApiUrlMapping("^/test5/$", "GET",
				RestApiUrlMapping.findFunction(TestRestApi.class, "function")));
		mappings.add(new RestApiUrlMapping(Pattern.compile("^/test6/$"), "GET",
				TestRestApi.class, "function"));
		mappings.add(new RestApiUrlMapping("^/test7/$", "GET",
				TestRestApi.class, "function"));

	}

	@Test
	public void testToString() {
		assertEquals("^/test0/$ (*) --> function", mappings.get(0).toString());
		assertEquals("^/test1/$ (*) --> function", mappings.get(1).toString());
		assertEquals("^/test2/$ (*) --> function", mappings.get(2).toString());
		assertEquals("^/test3/$ (*) --> function", mappings.get(3).toString());
		assertEquals("^/test4/$ (GET) --> function", mappings.get(4).toString());
		assertEquals("^/test5/$ (GET) --> function", mappings.get(5).toString());
		assertEquals("^/test6/$ (GET) --> function", mappings.get(6).toString());
		assertEquals("^/test7/$ (GET) --> function", mappings.get(7).toString());
	}

	@Test
	public void testRequestMatch() {
		String paths[] = {"/test0/", "/test0/bar", "/test1/", "/test1/bar",
				"/test2/", "/test2/bar", "/test3/", "/test3/bar", "/test4/",
				"/test4/bar", "/test5/", "/test5/bar", "/test6/", "/test6/bar",
				"/test7/", "/test7/bar"};
		String methods[] = {"GET", "gEt", "PosT", "put"};
		MatchResult mr;
		for (int p = 0; p < paths.length; p++) {
			for (int m = 0; m < methods.length; m++) {
				for (int r = 0; r < mappings.size(); r++) {
					// Total number of iterations: 512
					mr = mappings.get(r).match(paths[p], methods[m]);
					if (p % 2 == 1) {
						// This covers every 2. path from the above array.
						// They do not match (coverage: 256)
						assertNull(mr);
						continue;
					}
					if (p <= 7 && r <= 3 && (p == r * 2 || p == r * 2 + 1)) {
						// This covers the first 8 paths and only the first 4
						// mappings. Independent of the method, they must match
						// They do match (coverage: 16)
						assertNotNull(mr);
						continue;
					} else {
						if (p >= 8 && r >= 4 && m <= 1
								&& (p == r * 2 || p == r * 2 + 1)) {
							// Covers the least 8 paths, and least 4 mappings,
							// but only if the method is similar to "GET"
							// They do match (coverage: 8)
							assertNotNull(mr);
							continue;
						} else {
							// All other combinations are invalid and must
							// return null too
							// They do not match (coverage: 232)
							assertNull(mr);
							continue;
						}
					}
				}
			}
		}
		assertEquals("^/test0/$ (*) --> function", mappings.get(0).toString());
		assertEquals("^/test1/$ (*) --> function", mappings.get(1).toString());
		assertEquals("^/test2/$ (*) --> function", mappings.get(2).toString());
		assertEquals("^/test3/$ (*) --> function", mappings.get(3).toString());
		assertEquals("^/test4/$ (GET) --> function", mappings.get(4).toString());
		assertEquals("^/test5/$ (GET) --> function", mappings.get(5).toString());
		assertEquals("^/test6/$ (GET) --> function", mappings.get(6).toString());
		assertEquals("^/test7/$ (GET) --> function", mappings.get(7).toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoPattern() throws IllegalArgumentException,
			SecurityException, NoSuchMethodException {
		new RestApiUrlMapping((Pattern) null, "GET",
				RestApiUrlMapping.findFunction(TestRestApi.class, "function"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoFunction() {
		new RestApiUrlMapping(Pattern.compile("^/foo/$"), "GET", null);
	}

	/**
	 * Dummy function - Only needs to exist
	 * 
	 * @param req
	 * @param resp
	 * @param args
	 */
	public static void function(HttpServletRequest req, IRestApiResponse resp,
			ArrayList<String> args) {

	}
}
