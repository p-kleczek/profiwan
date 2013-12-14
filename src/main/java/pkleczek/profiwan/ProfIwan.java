package pkleczek.profiwan;

import java.awt.EventQueue;
import java.sql.SQLException;

import javax.swing.JFrame;

import pkleczek.profiwan.debug.Debug;
import pkleczek.profiwan.gui.DictionaryFrame;
import pkleczek.profiwan.model.PhraseEntry;
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

			Debug.printDict("init");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new DictionaryFrame();
		// frame = new RevisionsFrame();
		frame.setTitle("ProfIwan");
	}

}
