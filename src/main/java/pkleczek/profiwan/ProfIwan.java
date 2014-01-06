package pkleczek.profiwan;

import java.awt.EventQueue;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.JFrame;

import pkleczek.profiwan.debug.Debug;
import pkleczek.profiwan.gui.MainFrame;
import pkleczek.profiwan.utils.DBUtils;

public class ProfIwan {

	private static final String LOG_FILENAME = "log.txt"; //$NON-NLS-1$
	
//	private static final String dbPath = "D:\Dropbox\ProfIwan\profiwan_debug.db";

	private JFrame frame;
	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static FileHandler fileTxt;

	public static boolean inDebugMode = false;

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
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ProfIwan() {
		System.setProperty("org.joda.time.DateTimeZone.Provider", "org.joda.time.tz.UTCProvider");
		
		// test
		if (inDebugMode) {
			Debug.prepareDB();
			Debug.printDict("init");
		}
		
		// TODO: transfer z dropboxa
		

		frame = new MainFrame();
		frame.setVisible(true);
	}

}
