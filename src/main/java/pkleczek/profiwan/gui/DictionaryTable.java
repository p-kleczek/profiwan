package pkleczek.profiwan.gui;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import pkleczek.profiwan.ProfIwan;
import pkleczek.profiwan.debug.Debug;
import pkleczek.profiwan.model.PhraseEntry;

public class DictionaryTable extends JTable {

	private static final DateFormat dateOutputFormatter = new SimpleDateFormat(
			"yyyy-mm-dd");

	private static final int RUS_COLUMN = 0;
	private static final int PL_COLUMN = 1;
	private static final int REV_COLUMN = 2;
	private static final int LABEL_COLUMN = 3;

	public static final CharSequence ACCENT_MARKER = "\\";

	private static String[] columnNames = { "RUS", "PL", "label", "rev", "date" };
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
						val = val.replace(ACCENT_MARKER, "\u0301");
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
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					Debug.printDict("update");
				}
			}
		});
	}

	private void addRowToDTM(PhraseEntry pe) {
		String isInRevisionStr = pe.isInRevisions() ? "y" : "";
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dictionary.add(entry);

		Debug.printDict("add");
	}

	public void removeSelectedRow() {
		int selectedRowInx = getSelectedRow();

		if (getSelectedRow() != -1) {
			dtm.removeRow(selectedRowInx);

			try {
				dictionary.get(selectedRowInx).deleteDBEntry();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			dictionary.remove(selectedRowInx);

			Debug.printDict("delete");
			Debug.printRev("del");
		}
	}

	public int getLastModifiedEntryId() {
		return lastModifiedEntryId;
	}

	public void save() {
		for (PhraseEntry e : dictionary) {
			System.out.println(String.format(e.getRusText() + " "
					+ e.getPlText()));
		}
		System.out.println("---");

		// int nRow = dtm.getRowCount();
		// int nCol = dtm.getColumnCount();
		//
		// for (int i = 0; i < nRow; i++) {
		// for (int j = 0; j < nCol; j++)
		// tableData[i][j] = dtm.getValueAt(i, j);
		// }

		// TODO: odszukaj wpisu z dana nazwa, zamien dane
		// TODO: dodaj nowe zwroty
	}
}
