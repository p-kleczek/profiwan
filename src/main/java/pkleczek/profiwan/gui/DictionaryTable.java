package pkleczek.profiwan.gui;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import pkleczek.Messages;
import pkleczek.profiwan.debug.Debug;
import pkleczek.profiwan.model.PhraseEntry;

@SuppressWarnings("serial")
public class DictionaryTable extends JTable {

	private static final DateFormat dateOutputFormatter = new SimpleDateFormat(
			"yyyy-MM-dd"); //$NON-NLS-1$

	private static final int RUS_COLUMN = 0;
	private static final int PL_COLUMN = 1;
	private static final int LABEL_COLUMN = 2;
	private static final int REV_COLUMN = 3;

	public static final CharSequence ACCENT_MARKER = "\\"; //$NON-NLS-1$

	private static String[] columnNames = {
			"RUS", "PL", Messages.getString("DictionaryTable.label"), Messages.getString("DictionaryTable.isRevised"), Messages.getString("DictionaryTable.created") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private final DefaultTableModel dtm = new DefaultTableModel(null,
			columnNames);

	private final List<PhraseEntry> dictionary;

	private int lastModifiedEntryId = -1;

	private DictionaryTable instance = this;

	public DictionaryTable(List<PhraseEntry> dictionary) {
		setModel(dtm);

		this.dictionary = dictionary;

		for (PhraseEntry e : dictionary) {
			addRowToDTM(e);
		}

		dtm.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				int row = e.getFirstRow();
				int col = e.getColumn();

				if (col == RUS_COLUMN) {

					String val = (String) dtm.getValueAt(row, col);

					if (val.contains(ACCENT_MARKER)) {
						val = val.replace(ACCENT_MARKER, "\u0301"); //$NON-NLS-1$
						dtm.setValueAt(val, row, col);
					}

					instance.dictionary.get(row).setRusText(val);
				}
				if (col == PL_COLUMN) {
					String cellValue = (String) dtm.getValueAt(row, col);
					instance.dictionary.get(row).setPlText(cellValue);
				}
				if (col == REV_COLUMN) {
					String cellValue = (String) dtm.getValueAt(row, col);
					boolean inRevisions = !cellValue.isEmpty();
					instance.dictionary.get(row).setInRevisions(inRevisions);
				}
				if (col == LABEL_COLUMN) {
					String cellValue = (String) dtm.getValueAt(row, col);
					instance.dictionary.get(row).setLabel(cellValue);
				}

				if (col != -1) {
					lastModifiedEntryId = instance.dictionary.get(row).getId();

					try {
						instance.dictionary.get(row).updateDBEntry();
					} catch (SQLException e1) {
						JOptionPane.showMessageDialog(
								null,
								Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					}

					Debug.printDict("update"); //$NON-NLS-1$
				}
			}
		});
	}

	private void addRowToDTM(PhraseEntry pe) {
		String isInRevisionStr = pe.isInRevisions() ? "y" : ""; //$NON-NLS-1$ //$NON-NLS-2$
		dtm.addRow(new Object[] { pe.getRusText(), pe.getPlText(),
				pe.getLabel(), isInRevisionStr,
				dateOutputFormatter.format(pe.getCreationDate()) });
	}

	public void addRow() {
		PhraseEntry entry = new PhraseEntry();
		entry.setCreationDate(Calendar.getInstance().getTime());

		addRowToDTM(entry);

		try {
			entry.insertDBEntry();
		} catch (SQLException e) {
			JOptionPane
					.showMessageDialog(
							null,
							Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		}

		dictionary.add(entry);

		Debug.printDict("add"); //$NON-NLS-1$
	}

	public void removeSelectedRow() {
		int selectedRowInx = getSelectedRow();

		if (getSelectedRow() != -1) {
			dtm.removeRow(selectedRowInx);

			try {
				dictionary.get(selectedRowInx).deleteDBEntry();
			} catch (SQLException e) {
				JOptionPane
						.showMessageDialog(
								null,
								Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			}

			dictionary.remove(selectedRowInx);

			Debug.printDict("delete"); //$NON-NLS-1$
			Debug.printRev("del"); //$NON-NLS-1$
		}
	}

	public int getLastModifiedEntryId() {
		return lastModifiedEntryId;
	}

}
