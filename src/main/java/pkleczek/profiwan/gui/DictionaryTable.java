package pkleczek.profiwan.gui;

import java.sql.SQLException;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import pkleczek.profiwan.debug.Debug;
import pkleczek.profiwan.model.PhraseEntry;

public class DictionaryTable extends JTable {

	private static final int RUS_COLUMN = 0;
	private static final int PL_COLUMN = 1;
	private static final int REV_COLUMN = 2;

	private static String[] columnNames = { "RUS", "PL", "rev" };
	private static DefaultTableModel dtm = new DefaultTableModel(null,
			columnNames);

	private final List<PhraseEntry> dictionary;

	private int lastModifiedEntryId = -1;

	private DictionaryTable instance = this;

	public DictionaryTable(List<PhraseEntry> dictionary) {
		super(dtm);

		this.dictionary = dictionary;

		for (PhraseEntry e : dictionary) {
			dtm.addRow(new Object[] { e.getRusText(), e.getPlText() });
		}

		dtm.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				int row = e.getFirstRow();
				int col = e.getColumn();

				if (col == RUS_COLUMN) {
					instance.dictionary.get(row).setRusText(
							(String) dtm.getValueAt(row, col));
				}
				if (col == PL_COLUMN) {
					instance.dictionary.get(row).setPlText(
							(String) dtm.getValueAt(row, col));
				}
				if (col == REV_COLUMN) {
					String cellValue = (String) dtm.getValueAt(row, col);
					boolean inRevisions = !cellValue.isEmpty();
					instance.dictionary.get(row).setInRevisions(inRevisions);
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

	public void addRow() {
		dtm.addRow(new Object[0]);

		PhraseEntry entry = new PhraseEntry();

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
