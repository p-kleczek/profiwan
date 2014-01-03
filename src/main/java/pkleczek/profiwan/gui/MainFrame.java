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

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
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
		
		JButton btnDictionary = new JButton(Messages.getString("MainFrame.dictionary")); //$NON-NLS-1$
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
		
		JButton btnRevisions = new JButton(Messages.getString("MainFrame.revisions")); //$NON-NLS-1$
		btnRevisions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RevisionsDialog dlg;
				try {
					dlg = new RevisionsDialog();
					if (dlg.hasRevisions()) {
						dlg.setVisible(true);
					} else {
						JOptionPane.showMessageDialog(dlg, Messages.getString("MainFrame.noPendingRevisions")); //$NON-NLS-1$
						dlg.dispose();
						return;
					}
				} catch (SQLException e1) {
					JOptionPane.showMessageDialog(
							null,
							Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					logger.severe(e1.toString());
				}
				
				
			}
		});
		contentPane.add(btnRevisions);
		
		pack();
	}

}
