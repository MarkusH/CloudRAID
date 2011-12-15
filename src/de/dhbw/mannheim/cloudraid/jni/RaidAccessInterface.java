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
 * @author Markus Holtermann
 */
public class RaidAccessInterface {

	@SuppressWarnings("javadoc")
	public static void main(String[] args) {
		if (args.length != 5 && args.length != 6) {
			System.out
					.println("You have to specify <split|merge> <infile|outfile> <dev0> <dev1> <dev2> [--bits|--bytes]");
			System.exit(1);
		}

		/*
		 * Depending on what the user passed as the last argument, we will
		 * either split and merge the input at bit or byte level.
		 */
		System.loadLibrary("cloudraid");
		if (args.length == 6 && args[5].equals("--bytes")) {
		} else {
			System.loadLibrary("raid5bits");
		}

		long startTime = System.currentTimeMillis();
		if (args[0].toLowerCase().equals("split"))
			if (args.length == 6 && args[5].equals("--bytes")) {
				new RaidAccessInterface().splitByteInterface(args[1], args[2], args[3],
						args[4]);
			} else {
				new RaidAccessInterface().splitBitInterface(args[1], args[2], args[3],
						args[4]);
			}
		else if (args[0].toLowerCase().equals("merge"))
			if (args.length == 6 && args[5].equals("--bytes")) {
				new RaidAccessInterface().mergeByteInterface(args[1], args[2], args[3],
						args[4]);
			} else {
				new RaidAccessInterface().mergeBitInterface(args[1], args[2], args[3],
						args[4]);
			}
		else {
			System.out
					.println("Unknown mode! Use either \"split\" or \"merge\"");
			System.exit(2);
		}
		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000 + " s.");
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
	 */
	private native void mergeBitInterface(String out, String in0, String in1,
			String in2);

	/**
	 * @param in
	 *            The input file
	 * @param out0
	 *            Output file that simulates device 0
	 * @param out1
	 *            Output file that simulates device 1
	 * @param out2
	 *            Output file that simulates device 2
	 */
	private native void splitBitInterface(String in, String out0, String out1,
			String out2);
	
	/**
	 * @param out
	 *            The output file
	 * @param in0
	 *            Input file that simulates device 0
	 * @param in1
	 *            Input file that simulates device 1
	 * @param in2
	 *            Input file that simulates device 2
	 */
	private native void mergeByteInterface(String out, String in0, String in1,
			String in2);

	/**
	 * @param in
	 *            The input file
	 * @param out0
	 *            Output file that simulates device 0
	 * @param out1
	 *            Output file that simulates device 1
	 * @param out2
	 *            Output file that simulates device 2
	 */
	private native void splitByteInterface(String in, String out0, String out1,
			String out2);
}
