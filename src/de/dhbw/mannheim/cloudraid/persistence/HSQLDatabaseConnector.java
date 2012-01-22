package de.dhbw.mannheim.cloudraid.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * An implementation of the {@link DatabaseConnector} for the HSQL database
 * system.
 * 
 * @author Florian Bausch
 * 
 */
public class HSQLDatabaseConnector extends DatabaseConnector {
	private Connection con;
	private PreparedStatement insertStatement, updateStatement, findStatement,
			deleteStatement;
	private Statement statement;

	private final static String DB_PATH = System.getProperty("os.name")
			.contains("windows") ? System.getenv("APPDATA")
			+ "\\cloudraid\\filedb" : System.getProperty("user.home")
			+ "/.config/cloudraid/filedb";

	public boolean connect() {
		try {
			con = DriverManager.getConnection("jdbc:hsqldb:file:" + DB_PATH
					+ ";shutdown=true", "SA", "");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean disconnect() {
		try {
			statement.execute("SHUTDOWN COMPACT;");
			con.commit();
		} catch (SQLException e) {
		}
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void initialize() {
		try {
			statement = con.createStatement();
			// statement.execute("DROP TABLE IF EXISTS cloudraid_files;");
			String createTable = "CREATE CACHED TABLE IF NOT EXISTS cloudraid_files "
					+ "( path_name VARCHAR(512) NOT NULL, hash_name VARCHAR(256) NOT NULL, "
					+ "last_mod TIMESTAMP NOT NULL, PRIMARY KEY ( path_name ) );";
			statement.execute(createTable);

			con.commit();

			insertStatement = con
					.prepareStatement("INSERT INTO cloudraid_files VALUES ( ?, ?, ? );");
			updateStatement = con
					.prepareStatement("UPDATE cloudraid_files SET last_mod = ? WHERE path_name = ? ;");
			findStatement = con
					.prepareStatement("SELECT * FROM cloudraid_files WHERE path_name = ? ;");
			deleteStatement = con
					.prepareStatement("DELETE FROM cloudraid_files WHERE path_name = ? ;");
		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	public void insert(String path, String hash, long lastMod) {
		try {
			findStatement.setString(1, path);
			ResultSet resSet = findStatement.executeQuery();

			if (resSet.next()) {
				System.out.println("UPDATE");
				updateStatement.setTimestamp(1, new Timestamp(lastMod));
				updateStatement.setString(2, path);
				updateStatement.execute();
			} else {
				System.out.println("INSERT");
				insertStatement.setString(1, path);
				insertStatement.setString(2, hash);
				insertStatement.setTimestamp(3, new Timestamp(lastMod));
				insertStatement.execute();
			}
			con.commit();

			if (statement.execute("SELECT * FROM cloudraid_files;")) {
				ResultSet rs = statement.getResultSet();
				while (!rs.isLast()) {
					rs.next();
					System.out.println(rs.getString(1) + " : "
							+ rs.getString(2) + " : " + rs.getTimestamp(3));
				}
			}

		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	public String getHash(String path) {
		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT * FROM cloudraid_files WHERE path_name = ?;");
			ps.setString(1, path);
			ps.execute();
			ResultSet rs = ps.getResultSet();
			rs.next();
			return rs.getString("hash_name");
		} catch (SQLException e) {
		}
		return null;
	}

	public long getLastMod(String path) {
		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT * FROM cloudraid_files WHERE path_name = ?;");
			ps.setString(1, path);
			ps.execute();
			ResultSet rs = ps.getResultSet();
			rs.next();
			return rs.getTimestamp("last_mod").getTime();
		} catch (SQLException e) {
		}
		return -1L;
	}

	public String getName(String hash) {
		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT * FROM cloudraid_files WHERE hash_name = ?;");
			ps.setString(1, hash);
			ps.execute();
			ResultSet rs = ps.getResultSet();
			rs.next();
			return rs.getString("path_name");
		} catch (SQLException e) {
		}
		return null;
	}

	public boolean delete(String path) {
		try {
			deleteStatement.setString(1, path);
			deleteStatement.execute();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

}
