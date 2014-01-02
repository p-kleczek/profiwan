package pkleczek.profiwan;

import java.awt.EventQueue;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.JFrame;

import pkleczek.profiwan.debug.Debug;
import pkleczek.profiwan.gui.MainFrame;
import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;
import pkleczek.profiwan.utils.DBUtils;

public class ProfIwan {

	private static final String LOG_FILENAME = "log.txt"; //$NON-NLS-1$
	
	private JFrame frame;
	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static FileHandler fileTxt;
	
	{
		try {
			fileTxt = new FileHandler(LOG_FILENAME);
			logger.addHandler(fileTxt);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
							logger.severe(e.toString());
						}
					}
				});

				try {
					new ProfIwan();
				} catch (Exception e) {
					logger.severe(e.toString());
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ProfIwan() {
		// test
		prepareDB();
		
		frame = new MainFrame();
		frame.setVisible(true);
	}

	// for tests
	private void prepareDB() {
		try {
			DBUtils.recreateTables();
			
			PhraseEntry e = new PhraseEntry();
			e.setRusText("x"); //$NON-NLS-1$
			e.setPlText("y"); //$NON-NLS-1$
			e.setCreationDate(Calendar.getInstance().getTime());
			e.setLabel("rand"); //$NON-NLS-1$
			e.insertDBEntry();
			
			e.setRusText("x"); //$NON-NLS-1$
			e.updateDBEntry();
			
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
			re.insertDBEntry(1);

			Debug.printDict("init"); //$NON-NLS-1$
			Debug.printRev("init"); //$NON-NLS-1$

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
