package pkleczek.profiwan.gui;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

import java.sql.SQLException;

import javax.swing.JButton;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import pkleczek.profiwan.ProfIwan;
import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.utils.DBUtils;
import utils.TestUtils;

public class DictionaryDialogTest {

	@Before
	public void setUpDB() throws SQLException {
		ProfIwan.inDebugMode = true;
		DBUtils.recreateTables();
	}
	
	@Test
	public void testAddRowEmptyDB() throws SQLException {
		DictionaryDialog dlg = new DictionaryDialog();

		DictionaryTable tbl = (DictionaryTable) TestUtils.getChildNamed(dlg,
				"table");

		JButton btn = (JButton) TestUtils.getChildNamed(dlg, "btnAdd");
		assertNotNull(btn);

		btn.doClick();
		assertEquals(1, tbl.getModel().getRowCount());
	}

	@Test
	public void testAddRowRecordsInDB() throws SQLException {
		DictionaryDialog dlg = new DictionaryDialog();

		DictionaryTable tbl = (DictionaryTable) TestUtils.getChildNamed(dlg,
				"table");

		JButton btn = (JButton) TestUtils.getChildNamed(dlg, "btnAdd");
		assertNotNull(btn);

		btn.doClick();
		btn.doClick();
		assertEquals(2, tbl.getModel().getRowCount());
	}
	
	@Test
	public void testRemoveRow() throws SQLException {
		addPhraseEntryToDB();
		DictionaryDialog dlg = new DictionaryDialog();

		DictionaryTable tbl = (DictionaryTable) TestUtils.getChildNamed(dlg,
				"table");

		JButton btn = (JButton) TestUtils.getChildNamed(dlg, "btnRemove");
		assertNotNull(btn);

		btn.doClick();
		
		assertEquals(0, DBUtils.getDictionary().size());
		assertEquals(0, tbl.getModel().getRowCount());
	}	
	
	private void addPhraseEntryToDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		DateTime dt = DateTime.now();
		
		pe.setLangAText("pl");
		pe.setLangBText("rus");
		pe.setCreationDate(dt);
		pe.setLabel("lab");
		
		pe.insertDBEntry();
	}
}
