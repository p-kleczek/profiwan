package pkleczek.profiwan.debug;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;
import pkleczek.profiwan.utils.DBUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Debug {
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
			// TODO Auto-generated catch block
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
				Date date = rs.getDate("date");
				int mistakes = rs.getInt("mistakes");
				int phrId = rs.getInt("Phrase_idPhrase");

				System.out.println(String.format("\t%s [%d] -> %d", date.toString(), mistakes, phrId));
			}
				
			System.out.println("------------");
			System.out.println();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
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
