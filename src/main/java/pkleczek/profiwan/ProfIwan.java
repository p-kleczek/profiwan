package pkleczek.profiwan;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import pkleczek.profiwan.gui.DictionaryFrame;
import pkleczek.profiwan.model.PhraseEntry;

public class ProfIwan {

	private JFrame frame;
	
	private List<PhraseEntry> dictionary = new ArrayList<PhraseEntry>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
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
		initializeDictionary();
		initialize();
	}

	private void initializeDictionary() {
		PhraseEntry e = new PhraseEntry();
		e.setRusText("a");
		e.setPlText("b");
		dictionary.add(e);		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new DictionaryFrame();
		frame.setTitle("ProfIwan");
	}

}
