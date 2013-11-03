package pkleczek.profiwan.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import pkleczek.profiwan.model.PhraseEntry;

public class DictionaryTable extends JTable {

	private static final int RUS_COLUMN = 0;
	private static final int PL_COLUMN = 1;

	private static String[] columnNames = { "RUS", "PL" };
	private static DefaultTableModel dtm = new DefaultTableModel(null,
			columnNames);

	private final List<PhraseEntry> dictionary;

	private DictionaryTable instance = this;

	public DictionaryTable(List<PhraseEntry> dictionary) {
		super(dtm);

		this.dictionary = new ArrayList<>(dictionary);

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

			}
		});
	}

	public void addRow() {
		dtm.addRow(new Object[0]);
		dictionary.add(new PhraseEntry());
	}

	public void removeSelectedRow() {
		int selectedRowInx = getSelectedRow();

		if (getSelectedRow() != -1) {
			dtm.removeRow(selectedRowInx);
			dictionary.remove(selectedRowInx);
		}
	}

	public void save() {
		for (PhraseEntry e : dictionary) {
			System.out.println(String.format(e.getRusText() + " " + e.getPlText()));
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
