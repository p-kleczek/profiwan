package pkleczek.profiwan.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import pkleczek.profiwan.model.PhraseEntry;

public class DBUtils {

	private static Connection c = null;

	public static PreparedStatement insertPhraseEntry = null;
	public static PreparedStatement updatePhraseEntry = null;
	public static PreparedStatement deletePhraseEntry = null;

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

		try {
			c = DriverManager.getConnection("jdbc:sqlite:profiwan.db");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

		String insertPhraseEntryQuery = "INSERT INTO Phrase (idPhrase,lang1,lang2,inRevision) "
				+ "VALUES (NULL, ?, ?, ?);";

		String updatePhraseEntryQuery = "UPDATE Phrase SET lang1 = ?, lang2 = ?, inRevision = ? WHERE idPhrase = ?;";

		String deletePhraseEntryQuery = "DELETE FROM Phrase WHERE idPhrase= ?;";

		try {
			insertPhraseEntry = getConnection().prepareStatement(
					insertPhraseEntryQuery, Statement.RETURN_GENERATED_KEYS);

			updatePhraseEntry = getConnection().prepareStatement(
					updatePhraseEntryQuery);

			deletePhraseEntry = getConnection().prepareStatement(
					deletePhraseEntryQuery);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private DBUtils() {
	}

	public static Connection getConnection() {
		return c;
	}

	public static List<PhraseEntry> getDictionary() throws SQLException {
		List<PhraseEntry> dict = new ArrayList<PhraseEntry>();

		String query = "SELECT * FROM Phrase;";

		Connection conn = DBUtils.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		try {

			while (rs.next()) {
				int id = rs.getInt("idPhrase");
				String lang1 = rs.getString("lang1");
				String lang2 = rs.getString("lang2");
				boolean inRevision = rs.getBoolean("inRevision");

				PhraseEntry entry = new PhraseEntry();
				entry.setId(id);
				entry.setRusText(lang1);
				entry.setPlText(lang2);
				entry.setInRevisions(inRevision);

				dict.add(entry);
			}

		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return dict;
	}

	public static void execute(String query) throws SQLException {

		Connection conn = DBUtils.getConnection();
		Statement stmt = conn.createStatement();

		try {
			stmt.executeUpdate(query);
			// conn.commit();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void recreateTables() throws SQLException {

		String query = "CREATE TABLE IF NOT EXISTS Phrase "
				+ "(idPhrase 		INTEGER PRIMARY KEY	AUTOINCREMENT,"
				+ " lang1			TEXT	NOT NULL, " + " lang2			TEXT	NOT NULL, "
				+ " inRevision		BOOLEAN)";

		Connection conn = DBUtils.getConnection();
		Statement stmt = conn.createStatement();

		try {
			stmt.executeUpdate("DROP TABLE IF EXISTS Phrase");
			stmt.executeUpdate(query);
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
