package pkleczek.profiwan.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;

public class RevisionsFrame extends JFrame {

	enum State {
		USER_INPUT, ANSWER
	}

	private State state = State.USER_INPUT;

	private JPanel contentPane;
	private JLabel lblPolish = new JLabel();
	private JTextField textField;
	private JLabel lblCorrect = new JLabel();
	private JLabel lblStats = new JLabel();

	private List<PhraseEntry> dictionary;
	private LinkedList<PhraseEntry> pendingRevisions = new LinkedList<>(); // poprawki
	// w kolejce
	private ListIterator<PhraseEntry> revIterator = null;

	private boolean enteredCorrectly = false;
	private int initialNumberOfRevisions = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RevisionsFrame frame = new RevisionsFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void initializeDictionary() {
		dictionary = new ArrayList<PhraseEntry>();

		PhraseEntry e;

		e = new PhraseEntry();
		e.setRusText("a");
		e.setPlText("b");
		dictionary.add(e);

		e = new PhraseEntry();
		e.setRusText("x");
		e.setPlText("y");
		dictionary.add(e);
	}

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
		}

		lblPolish.setText(pendingRevisions.getFirst().getPlText());
		initialNumberOfRevisions = pendingRevisions.size();
		lblStats.setText("0 / " + initialNumberOfRevisions);
		revIterator = pendingRevisions.listIterator();
	}

	/**
	 * Create the frame.
	 */
	public RevisionsFrame() {
		initializeDictionary();
		prepareRevisions();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

					PhraseEntry pe = revIterator.next();

					enteredCorrectly = pe.getRusText().equals(
							textField.getText());
					if (enteredCorrectly) {
						// correct
						confirmRevision(pe);

						lblCorrect.setText("OK");
						lblStats.setText(initialNumberOfRevisions
								- pendingRevisions.size() + " / "
								+ initialNumberOfRevisions);
					} else {
						lblCorrect.setText("Correct: " + pe.getRusText());
						lblCorrect.setForeground(Color.red);
						// incorrect
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

		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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

		JButton btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.insets = new Insets(0, 0, 5, 0);
		gbc_btnEdit.gridx = 1;
		gbc_btnEdit.gridy = 1;
		contentPane.add(btnEdit, gbc_btnEdit);

		JButton btnAccept = new JButton("Accept");
		btnAccept.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (state == State.ANSWER && !enteredCorrectly) {
					confirmRevision(revIterator.previous());
					nextWord();
				}
			}
		});
		GridBagConstraints gbc_btnAccept = new GridBagConstraints();
		gbc_btnAccept.insets = new Insets(0, 0, 5, 0);
		gbc_btnAccept.gridx = 1;
		gbc_btnAccept.gridy = 2;
		contentPane.add(btnAccept, gbc_btnAccept);

		GridBagConstraints gbc_lblCorrect = new GridBagConstraints();
		gbc_lblCorrect.gridx = 1;
		gbc_lblCorrect.gridy = 3;
		contentPane.add(lblStats, gbc_lblCorrect);
	}

	private void confirmRevision(PhraseEntry pe) {
		RevisionEntry re = new RevisionEntry();
		re.date = Calendar.getInstance().getTime();
		re.mistakes = 0; // FIXME
		pe.getRevisions().add(re);
		revIterator.remove();
	}

	private void nextWord() {
		if (pendingRevisions.isEmpty()) {
			// TODO: koniec powtorek :)
			System.exit(0);
		} else {
			if (!revIterator.hasNext()) {
				revIterator = pendingRevisions.listIterator();
			}

			lblPolish.setText(pendingRevisions.get(revIterator.nextIndex())
					.getPlText());

			textField.setEditable(true);
			textField.setText("");

			lblCorrect.setText("");
			lblCorrect.setForeground(Color.BLACK);
		}

		state = State.USER_INPUT;
	}
}
