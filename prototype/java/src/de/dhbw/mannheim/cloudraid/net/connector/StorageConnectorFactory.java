package de.dhbw.mannheim.cloudraid.net.connector;

import java.util.HashMap;

public class StorageConnectorFactory {

	public static IStorageConnector create(String name,
			HashMap<String, String> parameter) {
		try {
			Class<?> klass = Class.forName(name);
			IStorageConnector connector = (IStorageConnector) klass
					.newInstance();
			connector.create(parameter);
			return connector;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
