package pkleczek.profiwan.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import pkleczek.Messages;
import pkleczek.profiwan.debug.Debug;
import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;
import pkleczek.profiwan.utils.DBUtils;

@SuppressWarnings("serial")
public class RevisionsDialog extends JDialog {

	enum State {
		USER_INPUT, ANSWER
	}
	
	private final RevisionsDialog instance = this; 
	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private State state = State.USER_INPUT;

	private JPanel contentPane;
	private JLabel lblPolish = new JLabel();
	private JTextField textField;
	private JLabel lblCorrect = new JLabel();
	private JLabel lblStats = new JLabel();

	private List<PhraseEntry> dictionary;
	private LinkedList<PhraseEntry> pendingRevisions = new LinkedList<>(); // poprawki
	private Map<Integer, Integer> revisionMistakes = new HashMap<Integer, Integer>(); 
	// w kolejce
	private ListIterator<PhraseEntry> revIterator = null;
	PhraseEntry currentRevision = null;

	private boolean enteredCorrectly = false;
	private int initialNumberOfRevisions = 0;

	private JButton btnEdit;

	private JButton btnAccept;

	private void prepareRevisions() {
		for (PhraseEntry pe : dictionary) {
			if (!pe.getRevisions().isEmpty()) {
				RevisionEntry re = pe.getRevisions().get(
						pe.getRevisions().size() - 1);

				// TODO: kryterium oceny
				if (!(re.mistakes > 1))
					break;
			}

			pendingRevisions.add(pe);
			
			// FIXME: odczyt z bd (jesli istnieje wpis z danego dnia), np. -n = bledy, ale jeszcze niezaliczone
			revisionMistakes.put(pe.getId(), 0);
		}

		if (pendingRevisions.isEmpty()) {
			lblPolish.setText(""); //$NON-NLS-1$
			lblStats.setText("-"); //$NON-NLS-1$
		} else {
			lblPolish.setText(pendingRevisions.getFirst().getPlText());
			initialNumberOfRevisions = pendingRevisions.size();
			lblStats.setText("0 / " + initialNumberOfRevisions); //$NON-NLS-1$
			revIterator = pendingRevisions.listIterator();
		}
	}

	/**
	 * Create the frame.
	 */
	public RevisionsDialog() throws SQLException {
		dictionary = DBUtils.getDictionary();

		prepareRevisions();
		
		setTitle(Messages.getString("RevisionsDialog.revisions")); //$NON-NLS-1$
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 178, 86, 0 };
		gbl_contentPane.rowHeights = new int[] { 39, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		contentPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		GridBagConstraints gbc_lblPolish = new GridBagConstraints();
		gbc_lblPolish.anchor = GridBagConstraints.WEST;
		gbc_lblPolish.insets = new Insets(0, 0, 5, 0);
		gbc_lblPolish.gridx = 0;
		gbc_lblPolish.gridy = 0;
		panel.add(lblPolish, gbc_lblPolish);

		textField = new JTextField();
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (state == State.USER_INPUT) {
					textField.setEditable(false);

					currentRevision = revIterator.next();

					enteredCorrectly = currentRevision.getRusText().equals(
							textField.getText());
					if (enteredCorrectly) {
						// correct
						confirmRevision(currentRevision);

						lblCorrect.setText(Messages.getString("ok")); //$NON-NLS-1$
						lblStats.setText(initialNumberOfRevisions
								- pendingRevisions.size() + " / " //$NON-NLS-1$
								+ initialNumberOfRevisions);
					} else {
						// incorrect
						lblCorrect.setText(Messages.getString("RevisionsDialog.correct") + currentRevision.getRusText()); //$NON-NLS-1$
						lblCorrect.setForeground(Color.red);
						btnEdit.setEnabled(true);
						btnAccept.setEnabled(true);
						
						int previousMistakes = revisionMistakes.get(currentRevision.getId());
						revisionMistakes.put(currentRevision.getId(), previousMistakes + 1);
						// TODO: zapis do bd
					}
					state = State.ANSWER;
				} else {
					// answer
					nextWord();
				}
			}
		});
		textField.setColumns(10);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		panel.add(textField, gbc_textField);

		JButton btnStop = new JButton(Messages.getString("RevisionsDialog.stop")); //$NON-NLS-1$
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		GridBagConstraints gbc_btnStop = new GridBagConstraints();
		gbc_btnStop.insets = new Insets(0, 0, 5, 0);
		gbc_btnStop.gridx = 1;
		gbc_btnStop.gridy = 0;
		contentPane.add(btnStop, gbc_btnStop);

		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		contentPane.add(lblCorrect, gbc_lblNewLabel);

		btnEdit = new JButton(Messages.getString("RevisionsDialog.edit")); //$NON-NLS-1$
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = (String)JOptionPane.showInputDialog(
	                    instance,
	                    Messages.getString("RevisionsDialog.newVersion"), //$NON-NLS-1$
	                    Messages.getString("RevisionsDialog.editWord"), //$NON-NLS-1$
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    null,
	                    currentRevision.getRusText());
				
				s = s.replace(DictionaryTable.ACCENT_MARKER, "\u0301"); //$NON-NLS-1$
				
				currentRevision.setRusText(s);
				
				try {
					currentRevision.updateDBEntry();
				} catch (SQLException e1) {
					JOptionPane.showMessageDialog(
							null,
							Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					logger.severe(e.toString());
				}
				
				textField.requestFocus();
			}
		});
		btnEdit.setEnabled(false);
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.insets = new Insets(0, 0, 5, 0);
		gbc_btnEdit.gridx = 1;
		gbc_btnEdit.gridy = 1;
		contentPane.add(btnEdit, gbc_btnEdit);

		btnAccept = new JButton(Messages.getString("RevisionsDialog.accept")); //$NON-NLS-1$
		btnAccept.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (state == State.ANSWER && !enteredCorrectly) {
					confirmRevision(revIterator.previous());
					nextWord();
				}
			}
		});
		btnAccept.setEnabled(false);
		GridBagConstraints gbc_btnAccept = new GridBagConstraints();
		gbc_btnAccept.insets = new Insets(0, 0, 5, 0);
		gbc_btnAccept.gridx = 1;
		gbc_btnAccept.gridy = 2;
		contentPane.add(btnAccept, gbc_btnAccept);

		GridBagConstraints gbc_lblCorrect = new GridBagConstraints();
		gbc_lblCorrect.gridx = 1;
		gbc_lblCorrect.gridy = 3;
		contentPane.add(lblStats, gbc_lblCorrect);
		
		pack();
	}

	private void confirmRevision(PhraseEntry pe) {
		RevisionEntry re = new RevisionEntry();
		re.date = Calendar.getInstance().getTime();
		re.mistakes = revisionMistakes.get(pe.getId()); // FIXME
		pe.getRevisions().add(re);
		revIterator.remove();

		try {
			re.insertDBEntry(pe.getId());
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(
					null,
					Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			logger.severe(e.toString());
		}

		Debug.printRev("conf"); //$NON-NLS-1$
	}

	private void nextWord() {
		btnEdit.setEnabled(false);
		btnAccept.setEnabled(false);
		
		if (pendingRevisions.isEmpty()) {
			// TODO: statystyki sesji
			dispose();
		} else {
			if (!revIterator.hasNext()) {
				revIterator = pendingRevisions.listIterator();
			}

			lblPolish.setText(pendingRevisions.get(revIterator.nextIndex())
					.getPlText());

			textField.setEditable(true);
			textField.setText(""); //$NON-NLS-1$

			lblCorrect.setText(""); //$NON-NLS-1$
			lblCorrect.setForeground(Color.BLACK);
		}

		state = State.USER_INPUT;
	}
	
	public boolean hasRevisions() {
		return !pendingRevisions.isEmpty();
	}
}
