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

/**
 * @author Florian Bausch, Markus Holtermann
 */
public class RaidAccessInterface {
	
	static {
		System.loadLibrary("cloudraid");
	}

	/**
	 * @param out
	 *            The output file
	 * @param in0
	 *            Input file that simulates device 0
	 * @param in1
	 *            Input file that simulates device 1
	 * @param in2
	 *            Input file that simulates device 2
	 * @return The success or error return value
	 */
	public static int mergeBitInterface(String out, String in0, String in1,
			String in2) {
		return new RaidAccessInterface().splitInterface(out, in0, in1, in2, true);
	}

	/**
	 * @param out
	 *            The output file
	 * @param in0
	 *            Input file that simulates device 0
	 * @param in1
	 *            Input file that simulates device 1
	 * @param in2
	 *            Input file that simulates device 2
	 * @return The success or error return value
	 */
	public static int mergeByteInterface(String out, String in0, String in1,
			String in2) {
		return new RaidAccessInterface().splitInterface(out, in0, in1, in2, false);
	}

	/**
	 * @param in
	 *            The input file
	 * @param out0
	 *            Output file that simulates device 0
	 * @param out1
	 *            Output file that simulates device 1
	 * @param out2
	 *            Output file that simulates device 2
	 * @return The success or error return value
	 */
	public static int splitBitInterface(String in, String out0, String out1,
			String out2) {
		return new RaidAccessInterface().splitInterface(in, out0, out1, out2, true);
	}

	/**
	 * @param in
	 *            The input file
	 * @param out0
	 *            Output file that simulates device 0
	 * @param out1
	 *            Output file that simulates device 1
	 * @param out2
	 *            Output file that simulates device 2
	 * @return The success or error return value
	 */
	public static int splitByteInterface(String in, String out0, String out1,
			String out2) {
		return new RaidAccessInterface().splitInterface(in, out0, out1, out2, false);
	}

	/**
	 * @param out
	 *            The output file
	 * @param in0
	 *            Input file that simulates device 0
	 * @param in1
	 *            Input file that simulates device 1
	 * @param in2
	 *            Input file that simulates device 2
	 * @param bits
	 *            Set to true if the split process should use bit level and not
	 *            block level
	 * @return The success or error return value
	 */
	private native int mergeInterface(String out, String in0, String in1,
			String in2, boolean bits);

	/**
	 * @param in
	 *            The input file
	 * @param out0
	 *            Output file that simulates device 0
	 * @param out1
	 *            Output file that simulates device 1
	 * @param out2
	 *            Output file that simulates device 2
	 * @param bits
	 *            Set to true if the split process should use bit level and not
	 *            block level
	 * @return The success or error return value
	 */
	private native int splitInterface(String in, String out0, String out1,
			String out2, boolean bits);
}
