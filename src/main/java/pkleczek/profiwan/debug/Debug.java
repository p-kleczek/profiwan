package pkleczek.profiwan.debug;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.smartcardio.ATR;

import org.joda.time.DateTime;

import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.RevisionEntry;
import pkleczek.profiwan.model.RevisionsSession;
import pkleczek.profiwan.utils.DBUtils;
import pkleczek.profiwan.utils.DatabaseHelper;
import pkleczek.profiwan.utils.DatabaseHelperImpl;

public class Debug {

	// for tests
	public static void prepareDB() {

		DatabaseHelper dbHelper = DatabaseHelperImpl.getInstance();

		try {
			((DatabaseHelperImpl) dbHelper).recreateTables();

			PhraseEntry e = null;
			PhraseEntry e1 = null;

			e1 = new PhraseEntry();
			e1.setLangA("pl");
			e1.setLangB("rus");
			e1.setLangAText("a");
			e1.setLangBText("b");
			e1.setCreatedAt(DateTime.now());
			e1.setLabel("rand");
			e1.setInRevisions(true);
			dbHelper.createPhrase(e1);

			e = new PhraseEntry();
			e.setLangA("pl");
			e.setLangB("rus");
			e.setLangAText("ala");
			e.setLangBText("ma");
			e.setCreatedAt(DateTime.now());
			e.setLabel("rand");
			e.setInRevisions(true);
			dbHelper.createPhrase(e);

			e = new PhraseEntry();
			e.setLangA("pl");
			e.setLangB("rus");
			e.setLangAText("x");
			e.setLangBText("y");
			e.setCreatedAt(DateTime.now());
			e.setLabel("rand");
			e.setInRevisions(true);
			dbHelper.createPhrase(e);

			e.setLangBText("x"); //$NON-NLS-1$
			dbHelper.updatePhrase(e);

			RevisionEntry re = null;

			re = new RevisionEntry();
			re.setCreatedAt(new DateTime(2013, 12, 5, 10, 15, 8));
			re.setMistakes(3);
			e1.getRevisions().add(re);
			dbHelper.createRevision(re, e1.getId());

			re = new RevisionEntry();
			re.setCreatedAt(new DateTime(2014, 1, 2, 10, 15, 8));
			re.setMistakes(2);
			e1.getRevisions().add(re);
			dbHelper.createRevision(re, e1.getId());
			//
			// re = new RevisionEntry();
			// cal.set(2013, 12, 15);
			// re.date = cal.getTime();
			// re.mistakes = 2;
			// e.getRevisions().add(re);
			// re.insertDBEntry(1);
			//
			// re = new RevisionEntry();
			// cal = Calendar.getInstance();
			// re.date = cal.getTime();
			// re.mistakes = 2;
			// e.getRevisions().add(re);
			// re.insertDBEntry(1);

			Debug.printDict("init"); //$NON-NLS-1$
			Debug.printRev("init"); //$NON-NLS-1$

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void printDict(String operation) {
		DatabaseHelper dbHelper = DatabaseHelperImpl.getInstance();
		List<PhraseEntry> dict = dbHelper.getDictionary();
		System.out.println(String.format("--- %s (%d) ---", operation,
				dict.size()));
		for (PhraseEntry e : dict) {
			System.out.println(e);
		}

		System.out.println("------------");
		System.out.println();
	}

	public static void printRev(String str) {
		Connection conn = DatabaseHelperImpl.getConnection();
		Statement stmt = null;
		ResultSet rs = null;

		System.out.println(String.format("----- %s -----", str));

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM Revision");

			while (rs.next()) {
				Date date = new Date(
						(long) rs.getInt(DatabaseHelper.KEY_CREATED_AT) * 1000L);
				int mistakes = rs.getInt(DatabaseHelper.KEY_REVISION_MISTAKES);
				int phrId = rs.getInt(DatabaseHelper.KEY_REVISION_PHRASE_ID);

				System.out.println(String.format("\t%s [%d] -> %d",
						date.toString(), mistakes, phrId));
			}

			System.out.println("------------");
			System.out.println();
		} catch (SQLException e1) {
			e1.printStackTrace();
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
	}

	public static void printRevisionsNumber() {
		final int timespan = 15;
		DateTime dt = DateTime.now();

		DatabaseHelper dbHelper = DatabaseHelperImpl.getInstance();

		for (int i = 0; i < timespan; i++) {
			System.out.println(RevisionsSession.getListOfPendingPhrases(
					dbHelper, dt).size());
			dt = dt.plusDays(1);
		}
	}

	public static void testCSVExport() {
		DatabaseHelper dbhHelper = DatabaseHelperImpl.getInstance();
		List<PhraseEntry> dict = dbhHelper.getDictionary();

		String[] attribs = new String[] {
				DatabaseHelper.KEY_ID,
				DatabaseHelper.KEY_CREATED_AT,
				DatabaseHelper.KEY_PHRASE_LANG1,
				DatabaseHelper.KEY_PHRASE_LANG2,
				DatabaseHelper.KEY_PHRASE_LANG1_TEXT,
				DatabaseHelper.KEY_PHRASE_LANG2_TEXT,
				DatabaseHelper.KEY_PHRASE_LABEL,
				DatabaseHelper.KEY_PHRASE_IN_REVISION };

		DBUtils.exportPhrasesToCSV("logs/phrases.csv", dict, attribs);

		System.exit(0);
	}

	public static void testCSVImport() {
		DatabaseHelper dbHelper = DatabaseHelperImpl.getInstance();

		try {
			((DatabaseHelperImpl) dbHelper).recreateTables();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String[] attribs = new String[] {
				DatabaseHelper.KEY_ID,
//				DatabaseHelper.KEY_CREATED_AT,
				DatabaseHelper.KEY_PHRASE_LANG1,
				DatabaseHelper.KEY_PHRASE_LANG2,
				DatabaseHelper.KEY_PHRASE_LANG1_TEXT,
				DatabaseHelper.KEY_PHRASE_LANG2_TEXT,
				DatabaseHelper.KEY_PHRASE_LABEL,
				DatabaseHelper.KEY_PHRASE_IN_REVISION };

		Set<String> setAttr = new HashSet<>(Arrays.asList(attribs));

		Collection<PhraseEntry> list = DBUtils.importPhrasesFromCSV("logs/phrases.csv", setAttr, dbHelper);

		System.out.println(list);
	}
}
