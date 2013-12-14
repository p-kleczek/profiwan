package pkleczek.profiwan.debug;

import java.sql.SQLException;
import java.util.List;

import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.utils.DBUtils;

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
}
