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

package de.dhbw_mannheim.cloudraid.net.model.amazons3;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.dhbw_mannheim.cloudraid.net.model.VolumeModel;

/**
 * @author Markus Holtermann
 */
public class AmazonS3VolumeModel extends VolumeModel {

	/**
	 * @param node
	 *            A {@link Node} representation of all available meta data
	 */
	public AmazonS3VolumeModel(Node node) {
		NodeList nl = node.getChildNodes();
		String tag;
		String text;
		for (int i = 0; i < nl.getLength(); i++) {
			tag = nl.item(i).getNodeName();
			text = nl.item(i).getTextContent();
			if (tag.equalsIgnoreCase("name")) {
				this.setName(text);
			} else if (tag.equalsIgnoreCase("creationdate")) {
				this.metadata.put(tag, text);
			}
		}
	}

	@Override
	public String toString() {
		return String.format("@AmazonS3VolumeModel(name=%s)", this.getName());
	}

}
