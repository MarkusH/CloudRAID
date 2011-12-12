package de.dhbw.mannheim.cloudraid.net.model.amazons3;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.dhbw.mannheim.cloudraid.net.model.VolumeModel;

public class AmazonS3VolumeModel extends VolumeModel {

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
				this.addMetadata(tag, text);
			}
		}
	}

	@Override
	public String toString() {
		return String.format("@AmazonS3VolumeModel(name=%s)", this.getName());
	}

}
