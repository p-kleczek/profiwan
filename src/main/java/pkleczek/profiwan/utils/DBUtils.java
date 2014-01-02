package pkleczek.profiwan.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import org.sqlite.SQLiteConfig;

import pkleczek.Messages;
import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;

public class DBUtils {

	private static Connection c = null;

	public static PreparedStatement insertPhraseEntry = null;
	public static PreparedStatement updatePhraseEntry = null;
	public static PreparedStatement deletePhraseEntry = null;
	private static PreparedStatement selectRevisionEntry = null;
	public static PreparedStatement insertRevisionEntry = null;

	public static final String prodDb = "jdbc:sqlite:profiwan.db";
	public static final String debugDb = "jdbc:sqlite:profiwan_debug.db";

	static {
		try {
			Class.forName("org.sqlite.JDBC");

			SQLiteConfig conf = new SQLiteConfig();
			conf.enforceForeignKeys(true);
			c = DriverManager.getConnection(debugDb, conf.toProperties());
		} catch (ClassNotFoundException | SQLException e) {
			JOptionPane
					.showMessageDialog(
							null,
							Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(-1);
		}

		String insertPhraseEntryQuery = "INSERT INTO Phrase (idPhrase,lang1,lang2,inRevision,date,label) "
				+ "VALUES (NULL, ?, ?, ?, ?, ?);";

		String updatePhraseEntryQuery = "UPDATE Phrase SET lang1 = ?, lang2 = ?, inRevision = ?, label = ? WHERE idPhrase = ?;";

		String deletePhraseEntryQuery = "DELETE FROM Phrase WHERE idPhrase = ?;";

		String selectRevisionEntryQuery = "SELECT * FROM Revision WHERE Phrase_idPhrase = ?;";

		String insertRevisionEntryQuery = "INSERT INTO Revision (idRevision,date,mistakes,Phrase_idPhrase) "
				+ "VALUES (NULL, ?, ?, ?) ;";

		try {
			insertPhraseEntry = getConnection().prepareStatement(
					insertPhraseEntryQuery, Statement.RETURN_GENERATED_KEYS);

			updatePhraseEntry = getConnection().prepareStatement(
					updatePhraseEntryQuery);

			deletePhraseEntry = getConnection().prepareStatement(
					deletePhraseEntryQuery);

			selectRevisionEntry = getConnection().prepareStatement(
					selectRevisionEntryQuery);

			insertRevisionEntry = getConnection().prepareStatement(
					insertRevisionEntryQuery);
		} catch (SQLException e) {
			JOptionPane
					.showMessageDialog(
							null,
							Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
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

		String selectPhraseQuery = "SELECT * FROM Phrase;";

		Connection conn = DBUtils.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = null;

		try {

			rs = stmt.executeQuery(selectPhraseQuery);
			while (rs.next()) {
				int id = rs.getInt("idPhrase");
				String lang1 = rs.getString("lang1");
				String lang2 = rs.getString("lang2");
				boolean inRevision = rs.getBoolean("inRevision");
				Date date = rs.getDate("date");
				String label = rs.getString("label");

				PhraseEntry entry = new PhraseEntry();
				entry.setId(id);
				entry.setRusText(lang1);
				entry.setPlText(lang2);
				entry.setInRevisions(inRevision);
				entry.setCreationDate(date);
				entry.setLabel(label);

				dict.add(entry);
			}

			PreparedStatement pstmt = DBUtils.selectRevisionEntry;
			for (PhraseEntry entry : dict) {
				pstmt.setInt(1, entry.getId());
				rs = pstmt.executeQuery();

				while (rs.next()) {
					RevisionEntry re = new RevisionEntry();
					re.date = rs.getDate("date");
					re.mistakes = rs.getInt("mistakes");

					entry.getRevisions().add(re);
				}
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
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void recreateTables() throws SQLException {

		String queryPhrase = "CREATE TABLE IF NOT EXISTS Phrase "
				+ "(idPhrase 		INTEGER PRIMARY KEY	AUTOINCREMENT,"
				+ " lang1			TEXT	NOT NULL," + " lang2			TEXT	NOT NULL,"
				+ " date			DATETIME NOT NULL," + " label			TEXT	NOT NULL,"
				+ " inRevision		BOOLEAN);";

		String queryRevision = "CREATE TABLE IF NOT EXISTS Revision "
				+ "(idRevision 		INTEGER PRIMARY KEY	AUTOINCREMENT,"
				+ " date			DATETIME	NOT NULL, "
				+ " mistakes		INTEGER	NOT NULL, "
				+ " Phrase_idPhrase		INTEGER	NOT NULL,"
				+ " FOREIGN KEY(Phrase_idPhrase) REFERENCES Phrase(idPhrase)"
				+ " ON DELETE CASCADE" + " ON UPDATE CASCADE" + ");";

		Connection conn = DBUtils.getConnection();
		Statement stmt = conn.createStatement();

		if (!conn.getMetaData().getURL().contains("debug")) {
			System.err.println("not a debug db!");
		} else {
			try {
				stmt.executeUpdate("DROP TABLE IF EXISTS Phrase");
				stmt.executeUpdate(queryPhrase);

				stmt.executeUpdate("DROP TABLE IF EXISTS Revision");
				stmt.executeUpdate(queryRevision);
			} finally {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
