package pkleczek.profiwan;

import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFrame;

import pkleczek.profiwan.debug.Debug;
import pkleczek.profiwan.gui.DictionaryFrame;
import pkleczek.profiwan.gui.RevisionsFrame;
import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;
import pkleczek.profiwan.utils.DBUtils;

public class ProfIwan {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {

				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							DBUtils.getConnection().close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

				try {
					ProfIwan window = new ProfIwan();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ProfIwan() {
		prepareDB();
		initialize();
	}

	// for tests
	private void prepareDB() {
		try {
			DBUtils.recreateTables();
			PhraseEntry e = new PhraseEntry();
			e.setRusText("x");
			e.setPlText("y");
			e.insertDBEntry();

			Calendar cal = Calendar.getInstance();
			RevisionEntry re = null;

			re = new RevisionEntry();
			cal.set(2013, 12, 10);
			re.date = cal.getTime();
			re.mistakes = 3;
			e.getRevisions().add(re);
			re.insertDBEntry(1);

			re = new RevisionEntry();
			cal.set(2013, 12, 12);
			re.date = cal.getTime();
			re.mistakes = 2;
			e.getRevisions().add(re);
			re.insertDBEntry(2);

			Debug.printDict("init");
			Debug.printRev("");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		// frame = new DictionaryFrame();
		frame = new RevisionsFrame();
		frame.setTitle("ProfIwan");
	}

}
