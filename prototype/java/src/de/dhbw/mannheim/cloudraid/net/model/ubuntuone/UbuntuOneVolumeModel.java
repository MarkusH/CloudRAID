package de.dhbw.mannheim.cloudraid.net.model.ubuntuone;

import org.json.JSONException;
import org.json.JSONObject;

import de.dhbw.mannheim.cloudraid.net.model.VolumeModel;

public class UbuntuOneVolumeModel extends VolumeModel {

	public UbuntuOneVolumeModel(JSONObject object) {
		String[] names = JSONObject.getNames(object);
		for (int i = 0; i < names.length; i++) {
			try {
				this.addMetadata(names[i], object.get(names[i]));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		this.setName(((String) this.getMetadata("path")).substring(2));
	}

	public UbuntuOneVolumeModel(String content) throws JSONException {
		this(new JSONObject(content));
	}

	@Override
	public String toString() {
		return String.format("@UbuntuOneVolume(name=%s)", this.getName());
	}
}
