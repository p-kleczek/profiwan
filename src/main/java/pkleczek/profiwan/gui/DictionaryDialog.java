package pkleczek.profiwan.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.utils.DBUtils;

public class DictionaryDialog extends JDialog {

	private JPanel contentPane;
	private List<PhraseEntry> dictionary;

	/**
	 * Create the frame.
	 */
	public DictionaryDialog() {
		setTitle("Dictionary");
		setModal(true);
		dictionary = null;
		try {
			dictionary = DBUtils.getDictionary();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0,
				Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		final DictionaryTable dictionaryTable = new DictionaryTable(dictionary);
		GridBagConstraints gbc_dictionaryTable = new GridBagConstraints();
		gbc_dictionaryTable.insets = new Insets(0, 0, 0, 5);
		gbc_dictionaryTable.fill = GridBagConstraints.BOTH;
		gbc_dictionaryTable.gridx = 0;
		gbc_dictionaryTable.gridy = 1;

		JScrollPane scrollPane = new JScrollPane(dictionaryTable);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 0;
		contentPane.add(panel, gbc_panel);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JButton btnAdd = new JButton("+");
		btnAdd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dictionaryTable.addRow();
			}
		});
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.insets = new Insets(0, 0, 5, 0);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 0;
		panel.add(btnAdd, gbc_btnAdd);

		JButton btnRemove = new JButton("-");
		btnRemove.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				int retVal = JOptionPane.showConfirmDialog(null,
						"Are you sure?", "Warning", JOptionPane.YES_NO_OPTION);

				if (retVal == JOptionPane.YES_OPTION) {
					dictionaryTable.removeSelectedRow();
				}
			}
		});
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemove.gridx = 0;
		gbc_btnRemove.gridy = 1;
		panel.add(btnRemove, gbc_btnRemove);

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO: warning, gdy puste / niepelne wpisy
//				dictionaryTable.save();
//				saveDictionary();
				
				// TODO: save last edited entry
			}
		});
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.gridx = 0;
		gbc_btnSave.gridy = 2;
//		panel.add(btnSave, gbc_btnSave);
	}

	private void saveDictionary() {
		for (PhraseEntry pe : dictionary) {
			try {
				pe.insertDBEntry();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
