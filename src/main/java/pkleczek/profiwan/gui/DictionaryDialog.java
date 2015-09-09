package pkleczek.profiwan.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import pkleczek.Messages;
import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.utils.DatabaseHelper;
import pkleczek.profiwan.utils.DatabaseHelperImpl;
import pkleczek.profiwan.utils.PhrasesTransfer;

@SuppressWarnings("serial")
public class DictionaryDialog extends JDialog {

	private final JPanel contentPane;
	private final List<PhraseEntry> dictionary;
	private final DictionaryTable dictionaryTable;

	/**
	 * Create the frame.
	 */
	public DictionaryDialog() throws SQLException {
		setTitle(Messages.getString("DictionaryDialog.title")); //$NON-NLS-1$
		setModal(true);
		dictionary = DatabaseHelperImpl.getInstance().getDictionary();

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0 };
		gbl_contentPane.columnWeights = new double[] {
				1.0,
				0.0,
				Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		dictionaryTable = new DictionaryTable(dictionary);
		dictionaryTable.setName("table");
		dictionaryTable.setMinimumSize(new Dimension(500, 300));
		dictionaryTable.getColumnModel().getColumn(3).setPreferredWidth(20);
		dictionaryTable.getColumnModel().getColumn(4).setPreferredWidth(20);
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

		JButton btnAdd = new JButton("+"); //$NON-NLS-1$
		btnAdd.setName("btnAdd");
		btnAdd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dictionaryTable.addRow();
			}
		});
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 0);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 0;
		panel.add(btnAdd, gbc_btnAdd);

		JButton btnRemove = new JButton("-"); //$NON-NLS-1$
		btnRemove.setName("btnRemove");
		btnRemove.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				int retVal = JOptionPane.showConfirmDialog(
						null,
						Messages.getString("DictionaryDialog.confirmDeletionInfo"), Messages.getString("DictionaryDialog.confirmDeletionTitle"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$

				if (retVal == JOptionPane.YES_OPTION) {
					dictionaryTable.removeSelectedRow();
				}
			}
		});
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemove.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemove.gridx = 0;
		gbc_btnRemove.gridy = 1;
		panel.add(btnRemove, gbc_btnRemove);

		JButton btnImport = new JButton(
				Messages.getString("DictionaryDialog.btnImport.text")); //$NON-NLS-1$
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importPhrases();
			}
		});
		GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnImport.gridx = 0;
		gbc_btnImport.gridy = 2;
		panel.add(btnImport, gbc_btnImport);

		JButton btnSave = new JButton(
				Messages.getString("DictionaryDialog.save")); //$NON-NLS-1$
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO: warning, gdy puste / niepelne wpisy
				// dictionaryTable.save();
			}
		});
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.gridx = 0;
		gbc_btnSave.gridy = 2;
		// panel.add(btnSave, gbc_btnSave);
	}

	private void importPhrases() {
		JFileChooser fc = new JFileChooser(".");

		fc.setAcceptAllFileFilterUsed(false);
		FileFilter filter = new FileFilter() {

			@Override
			public String getDescription() {
				return "DICT files";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().endsWith(".csv");
			}
		};
		fc.setFileFilter(filter);

		int returnVal = fc.showOpenDialog(null);

		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File f = fc.getSelectedFile();

		DatabaseHelper dbHelper = DatabaseHelperImpl.getInstance();

		String[] attribs = new String[] {
				// DatabaseHelper.KEY_ID,
				// DatabaseHelper.KEY_CREATED_AT,
				DatabaseHelper.KEY_PHRASE_LANG1,
				DatabaseHelper.KEY_PHRASE_LANG2,
				DatabaseHelper.KEY_PHRASE_LANG1_TEXT,
				DatabaseHelper.KEY_PHRASE_LANG2_TEXT,
				DatabaseHelper.KEY_PHRASE_LABEL,
				DatabaseHelper.KEY_PHRASE_IN_REVISION };

		Set<String> setAttr = new HashSet<>(Arrays.asList(attribs));

		Collection<PhraseEntry> list = PhrasesTransfer.importPhrasesFromCSV(
				f.getAbsolutePath(), setAttr, dbHelper);

		PhrasesTransfer.importIntoDatabase(dbHelper, list);
		
		List<PhraseEntry> db = dbHelper.getDictionary();
		dictionaryTable.reloadDictionary(db);
	}
}
