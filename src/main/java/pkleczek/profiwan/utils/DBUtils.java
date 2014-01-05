package pkleczek.profiwan.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.joda.time.DateTime;
import org.sqlite.SQLiteConfig;

import pkleczek.Messages;
import pkleczek.profiwan.ProfIwan;
import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;

public class DBUtils {

	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static Connection c = null;

	public static PreparedStatement insertPhraseEntry = null;
	public static PreparedStatement updatePhraseEntry = null;
	public static PreparedStatement deletePhraseEntry = null;
	private static PreparedStatement selectRevisionEntry = null;
	public static PreparedStatement insertRevisionEntry = null;
//	public static PreparedStatement updateRevisionEntry = null;
	public static PreparedStatement updateRevisionEntryId = null;

	public static final String prodDb = "jdbc:sqlite:profiwan.db";
	public static final String debugDb = "jdbc:sqlite:profiwan_debug.db";

	static {
		try {
			Class.forName("org.sqlite.JDBC");

			SQLiteConfig conf = new SQLiteConfig();
			conf.enforceForeignKeys(true);
			
			String modeURL = ProfIwan.inDebugMode ? debugDb : prodDb;
			
			c = DriverManager.getConnection(modeURL, conf.toProperties());
		} catch (ClassNotFoundException | SQLException e) {
			JOptionPane
					.showMessageDialog(
							null,
							Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(-1);
		}

		String insertPhraseEntryQuery = "INSERT INTO Phrase (idPhrase,lang1,lang2,inRevision,createdOn,label) "
				+ "VALUES (NULL, ?, ?, ?, ?, ?);";

		String updatePhraseEntryQuery = "UPDATE Phrase SET lang1 = ?, lang2 = ?, inRevision = ?, label = ? WHERE idPhrase = ?;";

		String deletePhraseEntryQuery = "DELETE FROM Phrase WHERE idPhrase = ?;";

		String selectRevisionEntryQuery = "SELECT * FROM Revision WHERE Phrase_idPhrase = ? ORDER BY revDate;";

		String insertRevisionEntryQuery = "INSERT INTO Revision (idRevision,revDate,mistakes,Phrase_idPhrase) "
				+ "VALUES (NULL, ?, ?, ?) ;";

//		String updateRevisionEntryQuery = "UPDATE Revision SET mistakes = ? WHERE Phrase_idPhrase = ? AND date(revDate, 'unixepoch') = date('now');";

		String updateRevisionEntryIdQuery = "UPDATE Revision SET mistakes = ? WHERE idRevision = ?;";

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

//			updateRevisionEntry = getConnection().prepareStatement(
//					updateRevisionEntryQuery);

			updateRevisionEntryId = getConnection().prepareStatement(
					updateRevisionEntryIdQuery);
		} catch (SQLException e) {
			JOptionPane
					.showMessageDialog(
							null,
							Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			logger.severe(e.toString());
			System.err.println(e);
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
				DateTime date = new DateTime((long) rs.getInt("createdOn")*1000L);
				String label = rs.getString("label");

				PhraseEntry entry = new PhraseEntry();
				entry.setId(id);
				entry.setLangBText(lang1);
				entry.setLangAText(lang2);
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
					re.date = new DateTime((long) rs.getInt("revDate")*1000L);
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
				+ " createdOn			INTEGER NOT NULL," + " label			TEXT	NOT NULL,"
				+ " inRevision		BOOLEAN);";

		String queryRevision = "CREATE TABLE IF NOT EXISTS Revision "
				+ "(idRevision 		INTEGER PRIMARY KEY	AUTOINCREMENT,"
				+ " revDate			INTEGER	NOT NULL, "
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
	
	public static int getIntFromDateTime(DateTime dt) {
		return (int) (dt.getMillis() / 1000);
	}

	public static DateTime getDateTimeFromInt(int i) {
		return new DateTime((long) i*1000L);
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
}
