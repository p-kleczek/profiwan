package pkleczek.profiwan.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import pkleczek.Messages;
import pkleczek.profiwan.model.RevisionsSession;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private MainFrame instance = this;

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle("ProfIwan"); //$NON-NLS-1$
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnDictionary = new JButton(
				Messages.getString("MainFrame.dictionary")); //$NON-NLS-1$
		btnDictionary.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog dlg;
				try {
					dlg = new DictionaryDialog();
					dlg.setVisible(true);
				} catch (SQLException e1) {
					logger.severe(e1.toString());
				}
			}
		});
		contentPane.add(btnDictionary);

		JButton btnRevisions = new JButton(
				Messages.getString("MainFrame.revisions")); //$NON-NLS-1$
		btnRevisions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RevisionsSession revisionSession = new RevisionsSession();
				revisionSession.prepareRevisions();

				if (revisionSession.hasRevisions()) {
					RevisionsDialog dlg = new RevisionsDialog(revisionSession);
					dlg.setVisible(true);
				} else {
					JOptionPane.showMessageDialog(instance,
							Messages.getString("MainFrame.noPendingRevisions")); //$NON-NLS-1$
					return;
				}

			}
		});
		contentPane.add(btnRevisions);

		pack();
	}

}
