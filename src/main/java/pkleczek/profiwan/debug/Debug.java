package pkleczek.profiwan.debug;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;
import pkleczek.profiwan.utils.DBUtils;

public class Debug {
	
	// for tests
	public static void prepareDB() {

		try {
			DBUtils.recreateTables();
			
			PhraseEntry e = null;

			e = new PhraseEntry();
			e.setLangA("pl");
			e.setLangB("rus");
			e.setLangAText("a");
			e.setLangBText("b");
			e.setCreationDate(DateTime.now());
			e.setLabel("rand");
			e.setInRevisions(true);
			e.insertDBEntry();
			
			e = new PhraseEntry();
			e.setLangA("pl");
			e.setLangB("rus");
			e.setLangAText("ala");
			e.setLangBText("ma");
			e.setCreationDate(DateTime.now());
			e.setLabel("rand");
			e.setInRevisions(true);
			e.insertDBEntry();
			
			e = new PhraseEntry();
			e.setLangA("pl");
			e.setLangB("rus");
			e.setLangAText("x");
			e.setLangBText("y");
			e.setCreationDate(DateTime.now());
			e.setLabel("rand");
			e.setInRevisions(true);
			e.insertDBEntry();

			e.setLangBText("x"); //$NON-NLS-1$
			e.updateDBEntry();

			RevisionEntry re = null;

			re = new RevisionEntry();
			re.date = new DateTime(2013, 12, 5, 10, 15, 8);
			re.mistakes = 3;
			e.getRevisions().add(re);
			re.insertDBEntry(1);

			re = new RevisionEntry();
			re.date = new DateTime(2014, 1, 2, 10, 15, 8);
			re.mistakes = 2;
			e.getRevisions().add(re);
			re.insertDBEntry(1);
//
//			re = new RevisionEntry();
//			cal.set(2013, 12, 15);
//			re.date = cal.getTime();
//			re.mistakes = 2;
//			e.getRevisions().add(re);
//			re.insertDBEntry(1);
//
//			re = new RevisionEntry();
//			cal = Calendar.getInstance();
//			re.date = cal.getTime();
//			re.mistakes = 2;
//			e.getRevisions().add(re);
//			re.insertDBEntry(1);

			Debug.printDict("init"); //$NON-NLS-1$
			Debug.printRev("init"); //$NON-NLS-1$

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void printDict(String operation) {
		List<PhraseEntry> dict;
		try {
			dict = DBUtils.getDictionary();
			System.out.println(String.format("--- %s (%d) ---", operation,
					dict.size()));
			for (PhraseEntry e : dict) {
				System.out.println(e);
			}

			System.out.println("------------");
			System.out.println();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void printRev(String str) {
		Connection conn = DBUtils.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		
		System.out.println(String.format("----- %s -----", str));

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM Revision");
			
			while (rs.next()) {
				Date date = new Date((long) rs.getInt("revDate")*1000L);
				int mistakes = rs.getInt("mistakes");
				int phrId = rs.getInt("Phrase_idPhrase");

				System.out.println(String.format("\t%s [%d] -> %d", date.toString(), mistakes, phrId));
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
}
