package de.dhbw.mannheim.cloudraid.net.connector;

import java.io.InputStream;

/**
 * Defines the methods to be implemented by classes that are used to connect do
 * cloud services.
 * 
 * @author Florian Bausch
 * 
 */
public interface IStorageConnector {

	/**
	 * Connects to a cloud service
	 * 
	 * @return true, if the connection could be established; false, if not.
	 */
	public boolean connect();

	/**
	 * Sends a file to a cloud service.
	 * 
	 * @return true, if the file could be send; false, if not.
	 */
	public boolean put();

	/**
	 * Gets a file from a cloud service.
	 * 
	 * @return An InputStream to the regarding file.
	 */
	public InputStream get();

}
