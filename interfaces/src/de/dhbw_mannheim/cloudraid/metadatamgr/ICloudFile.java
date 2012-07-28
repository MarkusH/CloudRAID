package de.dhbw_mannheim.cloudraid.metadatamgr;

/**
 * An interface for the representation of a file stored by CloudRAID.
 * 
 * @author Florian Bausch
 * 
 */
public interface ICloudFile {

	/**
	 * Returns the file ID.
	 * 
	 * @return The file ID.
	 */
	public int getFileId();

	/**
	 * Returns the hashed file name.
	 * 
	 * @return The hashed file name.
	 */
	public String getHash();

	/**
	 * Returns the date (milliseconds since 01/01/1970) of the last modification
	 * of a file.
	 * 
	 * @return The last modification date.
	 */
	public long getLastMod();

	/**
	 * Returns the file name.
	 * 
	 * @return The file name.
	 */
	public String getName();

	/**
	 * Returns the file's status.
	 * 
	 * @return The status.
	 */
	public String getStatus();

	/**
	 * Returns the user ID the file belongs to.
	 * 
	 * @return The user ID.
	 */
	public int getUserId();
}
