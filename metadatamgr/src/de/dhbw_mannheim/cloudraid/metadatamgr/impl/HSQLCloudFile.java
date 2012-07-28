package de.dhbw_mannheim.cloudraid.metadatamgr.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

import de.dhbw_mannheim.cloudraid.metadatamgr.ICloudFile;

/**
 * An implementation of {@link ICloudFile} that can read data from the
 * {@link HSQLMetadataManager} database.
 * 
 * @author Florian Bausch
 * 
 */
public class HSQLCloudFile implements ICloudFile {

	/**
	 * Creates from a {@link ResultSet} a Collection of {@link ICloudFile}s. The
	 * cursor must be set before the first row.
	 * 
	 * @param rs
	 *            A SQL {@link ResultSet}.
	 * @return The Collection.
	 * @throws SQLException
	 */
	protected static Collection<ICloudFile> createFileList(ResultSet rs)
			throws SQLException {
		Vector<ICloudFile> ret = new Vector<ICloudFile>();
		while (rs.next()) {
			ret.add(new HSQLCloudFile(rs));
		}
		return ret;
	}

	private String path;
	private String hash;
	private int userid;
	private String status;
	private long lastMod;
	private int fileid;

	/**
	 * Creates a {@link ICloudFile} from a {@link ResultSet}. The cursor must be
	 * set on a row.
	 * 
	 * @param rs
	 *            The {@link ResultSet}.
	 * @throws SQLException
	 */
	protected HSQLCloudFile(ResultSet rs) throws SQLException {
		this.path = rs.getString("path_name");
		this.hash = rs.getString("hash_name");
		this.userid = rs.getInt("user_id");
		this.status = rs.getString("status");
		this.lastMod = rs.getTimestamp("last_mod").getTime();
		this.fileid = rs.getInt("id");
	}

	@Override
	public int getFileId() {
		return this.fileid;
	}

	@Override
	public String getHash() {
		return this.hash;
	}

	@Override
	public long getLastMod() {
		return this.lastMod;
	}

	@Override
	public String getName() {
		return this.path;
	}

	@Override
	public String getStatus() {
		return this.status;
	}

	@Override
	public int getUserId() {
		return this.userid;
	}

}
