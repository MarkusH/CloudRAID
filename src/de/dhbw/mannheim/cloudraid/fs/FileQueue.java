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

import java.util.LinkedList;

/**
 * The FileQueue containing files to be split or deleted.
 * 
 * @author Florian Bausch
 * 
 */
public class FileQueue {

	/**
	 * The {@link LinkedList} representing the queue.
	 */
	private static LinkedList<FileQueueEntry> ll = new LinkedList<FileQueueEntry>();

	/**
	 * Appends a new entry at the end of the queue.
	 * 
	 * @param fileName
	 *            The filename of the new entry.
	 * @param fileAction
	 *            The file action of the new entry.
	 * @return true (as specified by Collection.add)
	 */
	public static boolean add(String fileName, FileAction fileAction) {
		return ll.add(new FileQueueEntry(fileName, fileAction));
	}

	/**
	 * Pops the first entry from the queue.
	 * 
	 * @return the element at the front of the queue (which is the top of the
	 *         stack represented by the queue)
	 */
	public static FileQueueEntry get() {
		return ll.pop();
	}

	/**
	 * Indicates, of the queue is empty.
	 * 
	 * @return true, if the queue contains no element.
	 */
	public static boolean isEmpty() {
		return ll.isEmpty();
	}

	/**
	 * This enum represents the three different file actions CREATE, DELETE,
	 * MODIFY.
	 * 
	 * @author Florian Bausch
	 * 
	 */
	public enum FileAction {
		CREATE, DELETE, MODIFY;
	}

}
