package pkleczek.profiwan.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import pkleczek.Messages;
import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.RevisionsSession;
import pkleczek.profiwan.utils.TextUtils;

@SuppressWarnings("serial")
public class RevisionsDialog extends JDialog {

	enum State {
		USER_INPUT, ANSWER
	}

	private final RevisionsDialog instance = this;

	private State state = State.USER_INPUT;

	private JPanel contentPane;
	private JLabel lblPolish = new JLabel();
	private JTextField textField;
	private JLabel lblCorrect = new JLabel();
	private JLabel lblStats = new JLabel();
	private JButton btnEdit;
	private JButton btnAccept;

	private final RevisionsSession revisionSession;

	/**
	 * Create the frame.
	 */
	public RevisionsDialog(final RevisionsSession revisionSession) {

		this.revisionSession = revisionSession;

		setTitle(Messages.getString("RevisionsDialog.revisions")); //$NON-NLS-1$
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 178, 86, 0 };
		gbl_contentPane.rowHeights = new int[] { 39, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] {
				0.0,
				0.0,
				Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] {
				0.0,
				0.0,
				0.0,
				0.0,
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
				enterPhrase();
			}
		});
		textField.setColumns(10);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		panel.add(textField, gbc_textField);

		JButton btnStop = new JButton(
				Messages.getString("RevisionsDialog.stop")); //$NON-NLS-1$
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finishRevisions();
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
				editPhrase();
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
				acceptRevision();
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

		lblPolish.setText(revisionSession.getCurrentPhrase().getLangAText()); // XXX:
																		// checkme!
		lblStats.setText("0 / " + revisionSession.getWordsNumber()); //$NON-NLS-1$

		pack();
	}
	
	private void acceptRevision() {
		if (state == State.ANSWER) {
			revisionSession.acceptRevision();
			nextWord();
		}
	}

	private void editPhrase() {
		PhraseEntry currentRevision = revisionSession
				.getCurrentPhrase();

		String s = (String) JOptionPane.showInputDialog(
				instance,
				Messages.getString("RevisionsDialog.newVersion"), //$NON-NLS-1$
				Messages.getString("RevisionsDialog.editWord"), //$NON-NLS-1$
				JOptionPane.PLAIN_MESSAGE, null, null,
				currentRevision.getLangBText());

		s = s.replace(TextUtils.CUSTOM_ACCENT_MARKER, "\u0301"); //$NON-NLS-1$

		revisionSession.editPhrase(s);

		textField.requestFocus();		
	}

	private void enterPhrase() {
		if (state == State.USER_INPUT) {
			textField.setEditable(false);

			boolean wasCorrect = revisionSession.processTypedWord(textField
					.getText());

			if (wasCorrect) {
				// correct
				lblCorrect.setText(Messages.getString("ok")); //$NON-NLS-1$
				lblStats.setText(revisionSession.getWordsNumber()
						- revisionSession.getPendingRevisionsSize() + " / " //$NON-NLS-1$
						+ revisionSession.getWordsNumber());
			} else {
				// incorrect
				lblCorrect
						.setText(Messages.getString("RevisionsDialog.correct") + revisionSession.getCurrentPhrase().getLangBText()); //$NON-NLS-1$
				lblCorrect.setForeground(Color.red);
				btnEdit.setEnabled(true);
				btnAccept.setEnabled(true);
			}

			state = State.ANSWER;
		} else {
			// answer
			nextWord();
		}
	}

	private void nextWord() {
		btnEdit.setEnabled(false);
		btnAccept.setEnabled(false);

		if (!revisionSession.hasRevisions()) {
			finishRevisions();
		} else {
			revisionSession.nextRevision();

			lblPolish.setText(revisionSession.getCurrentPhrase().getLangAText());

			textField.setEditable(true);
			textField.setText(""); //$NON-NLS-1$

			lblCorrect.setText(""); //$NON-NLS-1$
			lblCorrect.setForeground(Color.BLACK);
		}

		state = State.USER_INPUT;
	}

	public void finishRevisions() {
		String msg = String
				.format("Statistics:\n   words                  = %d\n   correct words   = %d\n   total revisions   = %d",
						revisionSession.getWordsNumber(),
						revisionSession.getCorrectWordsNumber(),
						revisionSession.getRevisionsNumber());

		JOptionPane.showMessageDialog(this, msg);
		dispose();
	}
}
