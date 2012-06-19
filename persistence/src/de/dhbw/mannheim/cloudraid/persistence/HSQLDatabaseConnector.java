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

package de.dhbw.mannheim.cloudraid.persistence;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Random;

/**
 * An implementation of the {@link IDatabaseConnector} for the HSQL database
 * system.
 * 
 * @author Florian Bausch, Markus Holtermann
 * 
 */
public class HSQLDatabaseConnector implements IDatabaseConnector {

	/**
	 * 
	 */
	private Connection con;

	/**
	 * 
	 */
	private PreparedStatement fileAddStmnt = null;

	/**
	 * 
	 */
	private PreparedStatement fileUpdateStmnt = null;

	/**
	 * 
	 */
	private PreparedStatement fileGetStmnt = null;

	/**
	 * 
	 */
	private PreparedStatement fileDeleteStmnt = null;

	/**
	 * 
	 */
	private PreparedStatement findNameStatement = null;

	/**
	 * 
	 */
	private PreparedStatement listFiles = null;

	/**
	 * 
	 */
	private PreparedStatement addUserStatement = null;

	/**
	 * 
	 */
	private PreparedStatement authUserStatement = null;

	/**
	 * 
	 */
	private PreparedStatement getUserSaltStatement = null;

	/**
	 * 
	 */
	private Statement statement = null;

	@Override
	public synchronized boolean connect(String database) {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			con = DriverManager.getConnection("jdbc:hsqldb:file:" + database
					+ ";shutdown=true", "SA", "");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public synchronized boolean disconnect() {
		try {
			if (statement != null) {
				statement.execute("SHUTDOWN COMPACT;");
			}
			if (con != null) {
				con.commit();
			}
		} catch (SQLException e) {
		}

		statement = null;
		fileGetStmnt = null;
		fileAddStmnt = null;
		fileDeleteStmnt = null;
		fileUpdateStmnt = null;
		findNameStatement = null;
		listFiles = null;
		addUserStatement = null;
		authUserStatement = null;
		getUserSaltStatement = null;

		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public synchronized boolean initialize() {
		try {
			statement = con.createStatement();
			String createTable = "CREATE CACHED TABLE IF NOT EXISTS cloudraid_users ("
					+ "id INTEGER IDENTITY, "
					+ "username VARCHAR(32) NOT NULL, "
					+ "password BLOB(32) NOT NULL, "
					+ "salt BLOB(8) NOT NULL"
					+ ");";
			statement.execute(createTable);

			createTable = "CREATE CACHED TABLE IF NOT EXISTS cloudraid_files ("
					+ "id INTEGER IDENTITY, "
					+ "path_name VARCHAR(512) NOT NULL, "
					+ "hash_name VARCHAR(256) NOT NULL, "
					+ "last_mod TIMESTAMP NOT NULL, "
					+ "status VARCHAR(32) NOT NULL, "
					+ "user_id INTEGER NULL, "
					+ "FOREIGN KEY ( user_id ) REFERENCES cloudraid_users ( id ),"
					+ "UNIQUE ( user_id, path_name ) );";
			statement.execute(createTable);

			con.commit();

			fileAddStmnt = con
					.prepareStatement("INSERT INTO cloudraid_files VALUES (NULL, ?, ?, ?, ?, ? );");
			fileUpdateStmnt = con
					.prepareStatement("UPDATE cloudraid_files SET last_mod = ? WHERE path_name = ? AND user_id = ? ;");
			fileGetStmnt = con
					.prepareStatement("SELECT * FROM cloudraid_files WHERE path_name = ? AND user_id = ?;");
			fileDeleteStmnt = con
					.prepareStatement("DELETE FROM cloudraid_files WHERE path_name = ? AND user_id = ? ;");
			findNameStatement = con
					.prepareStatement("SELECT * FROM cloudraid_files WHERE hash_name = ? AND user_id = ?;");

			listFiles = con
					.prepareStatement("SELECT * FROM cloudraid_files WHERE user_id = ?;");

			addUserStatement = con
					.prepareStatement("INSERT INTO cloudraid_users VALUES (NULL, ?, ?, ? );");
			authUserStatement = con
					.prepareStatement("SELECT * FROM cloudraid_users WHERE username = ? AND password = ?");
			getUserSaltStatement = con
					.prepareCall("SELECT salt FROM cloudraid_users WHERE username = ?");

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public synchronized boolean insert(String path, String hash, long lastMod,
			int userId) {
		try {
			fileGetStmnt.setString(1, path);
			fileGetStmnt.setInt(2, userId);
			ResultSet resSet = fileGetStmnt.executeQuery();

			if (resSet.next()) {
				fileUpdateStmnt.setTimestamp(1, new Timestamp(lastMod));
				fileUpdateStmnt.setString(2, path);
				fileUpdateStmnt.setInt(3, userId);
				fileUpdateStmnt.execute();
			} else {
				fileAddStmnt.setString(1, path);
				fileAddStmnt.setString(2, hash);
				fileAddStmnt.setTimestamp(3, new Timestamp(lastMod));
				fileAddStmnt.setString(4,
						IDatabaseConnector.FILE_STATUS.UPLOADED.toString());
				fileAddStmnt.setInt(5, userId);
				fileAddStmnt.execute();
			}
			con.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public synchronized String getHash(String path, int userId) {
		try {
			fileGetStmnt.setString(1, path);
			fileGetStmnt.setInt(2, userId);
			fileGetStmnt.execute();
			ResultSet rs = fileGetStmnt.getResultSet();
			if (rs.next()) {
				return rs.getString("hash_name");
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public synchronized long getLastMod(String path, int userId) {
		try {
			fileGetStmnt.setString(1, path);
			fileGetStmnt.setInt(2, userId);
			fileGetStmnt.execute();
			ResultSet rs = fileGetStmnt.getResultSet();
			if (rs.next()) {
				return rs.getTimestamp("last_mod").getTime();
			}
			return -1L;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1L;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return -1L;
		}
	}

	@Override
	public synchronized String getName(String hash, int userId) {
		try {
			findNameStatement.setString(1, hash);
			findNameStatement.setInt(2, userId);
			findNameStatement.execute();
			ResultSet rs = findNameStatement.getResultSet();
			if (rs.next()) {
				return rs.getString("path_name");
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public synchronized int fileDelete(String path, int userId) {
		try {
			fileDeleteStmnt.setString(1, path);
			fileDeleteStmnt.setInt(2, userId);
			fileDeleteStmnt.execute();
			return fileDeleteStmnt.getUpdateCount();
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public boolean fileNew(String path, String hash, long lastMod, int userId) {
		try {
			fileAddStmnt.setString(1, path);
			fileAddStmnt.setString(2, hash);
			fileAddStmnt.setTimestamp(3, new Timestamp(lastMod));
			fileAddStmnt.setString(4,
					IDatabaseConnector.FILE_STATUS.UPLOADED.toString());
			fileAddStmnt.setInt(5, userId);
			fileAddStmnt.execute();
			con.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean fileUpdate(String path, String hash, long lastMod, int userId) {
		try {
			fileUpdateStmnt.setTimestamp(1, new Timestamp(lastMod));
			fileUpdateStmnt.setString(2, path);
			fileUpdateStmnt.setInt(3, userId);
			fileUpdateStmnt.execute();
			con.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public synchronized ResultSet fileGet(String path, int userId) {
		try {
			fileGetStmnt.setString(1, path);
			fileGetStmnt.setInt(2, userId);
			fileGetStmnt.execute();
			ResultSet rs = fileGetStmnt.getResultSet();
			if (rs.next()) {
				return rs;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public synchronized ResultSet fileList(int userId) {
		try {
			listFiles.setInt(1, userId);
			listFiles.execute();
			return listFiles.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.dhbw.mannheim.cloudraid.persistence.IDatabaseConnector#authUser(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public int authUser(String username, String password) {
		try {
			getUserSaltStatement.setString(1, username);
			getUserSaltStatement.execute();
			ResultSet rs = getUserSaltStatement.getResultSet();
			if (rs.next()) {
				byte[] salt = rs.getBytes("salt");
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				digest.reset();
				digest.update(salt);
				byte[] pwdigest = digest.digest(password.getBytes("UTF-8"));
				authUserStatement.setString(1, username);
				authUserStatement.setBytes(2, pwdigest);
				authUserStatement.execute();
				rs = authUserStatement.getResultSet();
				if (rs.next()) {
					return rs.getInt("id");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.dhbw.mannheim.cloudraid.persistence.IDatabaseConnector#addUser(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public boolean addUser(String username, String password) {
		try {
			getUserSaltStatement.setString(1, username);
			getUserSaltStatement.execute();
			ResultSet rs = getUserSaltStatement.getResultSet();
			if (!rs.next()) {
				Random rnd = new Random(System.nanoTime());
				byte[] salt = new byte[8];
				byte[] pwdigest;
				rnd.nextBytes(salt);
				try {
					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					digest.reset();
					digest.update(salt);
					pwdigest = digest.digest(password.getBytes("UTF-8"));
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					return false;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return false;
				}
				addUserStatement.setString(1, username);
				addUserStatement.setBytes(2, pwdigest);
				addUserStatement.setBytes(3, salt);
				addUserStatement.execute();
				con.commit();
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}

}
