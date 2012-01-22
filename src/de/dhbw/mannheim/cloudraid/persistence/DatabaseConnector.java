package de.dhbw.mannheim.cloudraid.persistence;

/**
 * An abstract class for defining an interface to access various database types.
 * 
 * @author Florian Bausch
 * 
 */
public abstract class DatabaseConnector {

	/**
	 * Creates a DatabaseConnector for a specific database type via reflection.
	 * 
	 * @param className
	 *            The full class name (with packages) of the class that extends
	 *            this abstract class and implements the database type specific
	 *            things.
	 * @return An instance of the DatabaseConnector or, if there was an error
	 *         creating the instance, an {@link HSQLDatabaseConnector} instance
	 *         as fall-back.
	 */
	public static DatabaseConnector getDatabaseConnector(String className) {
		try {
			return (DatabaseConnector) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new HSQLDatabaseConnector();
	}

	/**
	 * Creates a connection to a database.
	 * 
	 * @return true, if the connection could be opened; false, if not.
	 */
	public abstract boolean connect();

	/**
	 * Closes the connection to the database opened by {@link connect()}.
	 * 
	 * @return true, if the connection could be closed.
	 */
	public abstract boolean disconnect();

	/**
	 * Creates the database schemas.
	 */
	public abstract void initialize();

	/**
	 * Inserts a data set into the database.
	 * 
	 * @param path
	 *            The path of a file.
	 * @param hash
	 *            The hash of the file name.
	 * @param lastMod
	 *            The last modification date.
	 */
	public abstract void insert(String path, String hash, long lastMod);

	/**
	 * Looks up the hash value of an entry in the database.
	 * 
	 * @param path
	 *            The path of the file (identifies the data set).
	 * @return The hash value. Or <code>null</code>, if the path does not exist
	 *         in the database.
	 */
	public abstract String getHash(String path);

	/**
	 * Looks up the last modification date of a file.
	 * 
	 * @param path
	 *            The path of the file.
	 * @return The last modification date. Or <code>-1L</code>, if the path does
	 *         not exist in the database.
	 */
	public abstract long getLastMod(String path);

	/**
	 * Looks up a file name for a given hash value.
	 * 
	 * @param hash
	 *            The hash value.
	 * @return The path of the file. Or <code>null</code>, if the hash does not
	 *         exist in the database.
	 */
	public abstract String getName(String hash);

	/**
	 * Deletes a data set in the database defined by the path.
	 * 
	 * @param path
	 *            The path of the file.
	 * @return true, if the data set could be deleted.
	 */
	public abstract boolean delete(String path);

	public static void main(String[] args) {
		DatabaseConnector dbc = DatabaseConnector
				.getDatabaseConnector("de.dhbw.mannheim.cloudraid.persistence.HSQLDatabaseConnector");
		dbc.connect();
		dbc.initialize();
		dbc.insert("path", "hash", System.currentTimeMillis());
		dbc.insert("path", "hash", System.currentTimeMillis());
		dbc.insert("path3", "hash3", System.currentTimeMillis());
		dbc.delete("path3");
		dbc.insert("path2", "hash2", System.currentTimeMillis());
		System.out.println(dbc.getName("hash") + " : " + dbc.getHash("path")
				+ " : " + dbc.getLastMod("path"));
		System.out.println(dbc.getName("hash2") + " : " + dbc.getHash("path2")
				+ " : " + dbc.getLastMod("path2"));

		dbc.disconnect();
	}
}
