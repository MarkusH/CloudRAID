package de.dhbw.mannheim.cloudraid.net.connector;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Defines the methods to be implemented by classes that are used to connect to
 * cloud services.
 * 
 * @author Florian Bausch, Markus Holtermann
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
	 * Create a new instance of the <code>connector</code>.
	 * 
	 * @param parameter
	 *            The given HashMap contains the parameters as key-value pairs
	 *            that a <code>connector</code> should use during
	 *            initialization.
	 * 
	 * @return Returns a new initialized instance of the <code>connector</code>.
	 */
	public IStorageConnector create(HashMap<String, String> parameter);

	/**
	 * Deletes a file on a cloud service.
	 * 
	 * In case that the requested file <b>does not exist</b> (HTTP 404) or that
	 * the removal <b>was successful</b> (HTTP 200), the implementation has to
	 * return <code>true</code>!
	 * 
	 * @return true, if the file could be deleted; false, if not.
	 */
	public boolean delete(String resource);

	/**
	 * Gets a file from a cloud service.
	 * 
	 * @return An InputStream to the regarding file.
	 */
	public InputStream get(String resource);

	/**
	 * Returns meta data for a resource.
	 * 
	 * @return The meta data.
	 */
	public String head(String resource);

	/**
	 * Returns the options available for a resource.
	 * 
	 * @return The options.
	 */
	public String[] options(String resource);

	/**
	 * Sends a file to a cloud service.
	 * 
	 * @return The link to the new file on the cloud service.
	 */
	public String post(String resource, String parent);

	/**
	 * Changes a file on a cloud service.
	 * 
	 * @return true, if the file could be changed; false, if not.
	 */
	public boolean put(String resource);

}
