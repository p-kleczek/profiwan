package pkleczek.profiwan.model;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;

import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;
import pkleczek.profiwan.utils.DBUtils;

public class PhraseEntryTest {

	@Test
	public void testIsReviseNowNotInRevision() {
		PhraseEntry pe = new PhraseEntry();
		pe.setInRevisions(false);
		assertFalse(pe.isReviseNow());
	}	
	
	@Test
	public void testIsReviseNowAfterNextInterval() {
		PhraseEntry pe = new PhraseEntry();
		pe.setInRevisions(true);
		RevisionEntry re = null;

		re = new RevisionEntry();
		re.date = DateTime.now().minusDays(PhraseEntry.MIN_REVISION_INTERVAL + 1);
		re.mistakes = 1;
		pe.getRevisions().add(re);

		assertTrue(pe.isReviseNow());
	}

	@Test
	public void testIsReviseNowBeforeNextInterval() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		re = new RevisionEntry();
		re.date = DateTime.now();
		re.mistakes = 1;
		pe.getRevisions().add(re);

		assertFalse(pe.isReviseNow());
	}

	@Test
	public void testGetRevisionsFrequencyNoStreak() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK; i++) {
			re = new RevisionEntry();
			re.date = DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK);
			re.mistakes = 0;
			pe.getRevisions().add(re);
		}

		assertEquals(pe.getRevisionFrequency(),
				PhraseEntry.MIN_REVISION_INTERVAL);
	}

	@Test
	public void testGetRevisionsFrequencyStreak() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK + 1; i++) {
			re = new RevisionEntry();
			re.date = DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK);
			re.mistakes = 0;
			pe.getRevisions().add(re);
		}

		assertEquals(PhraseEntry.MIN_REVISION_INTERVAL
				+ PhraseEntry.FREQUENCY_DECAY, pe.getRevisionFrequency());
	}

	@Test
	public void testGetRevisionsFrequencyNoStreakError() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK - 2; i++) {
			re = new RevisionEntry();
			re.date = DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK);
			re.mistakes = 0;
			pe.getRevisions().add(re);
		}

		re = new RevisionEntry();
		re.date = DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK);
		re.mistakes = 1;
		pe.getRevisions().add(re);

		assertEquals(PhraseEntry.MIN_REVISION_INTERVAL,
				pe.getRevisionFrequency());
	}

	@Test
	public void testGetRevisionsFrequencyStreakError() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK + 2; i++) {
			re = new RevisionEntry();
			re.date = DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK);
			re.mistakes = 0;
			pe.getRevisions().add(re);
		}

		re = new RevisionEntry();
		re.date = DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK);
		re.mistakes = 1;
		pe.getRevisions().add(re);

		assertEquals(PhraseEntry.MIN_REVISION_INTERVAL
				+ PhraseEntry.FREQUENCY_DECAY, pe.getRevisionFrequency());
	}
	
	@Test
	public void testInsertIntoDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		DateTime dt = DateTime.now();
		
		pe.setLangAText("pl");
		pe.setLangBText("rus");
		pe.setCreationDate(dt);
		pe.setLabel("lab");
		
		pe.insertDBEntry();
		
		List<PhraseEntry> dictionary = DBUtils.getDictionary();
		boolean found = false;
		
		for (PhraseEntry ipe : dictionary) {
			if (ipe.getId() != pe.getId()) {
				continue;				
			}
			
			found = true;
			
			assertEquals(pe.getLangAText(), ipe.getLangAText());
			assertEquals(pe.getLangBText(), ipe.getLangBText());
			
			int d1 = DBUtils.getIntFromDateTime(pe.getCreationDate());
			int d2 = DBUtils.getIntFromDateTime(ipe.getCreationDate());
			assertEquals(d1, d2);

			assertEquals(pe.getLabel(), ipe.getLabel());
		}
		
		assertTrue(found);
	}
	
	@Test
	public void testUpdateInDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		DateTime dt = DateTime.now();
		
		pe.setLangAText("pl");
		pe.setLangBText("rus");
		pe.setCreationDate(dt);
		pe.setLabel("lab");
		
		pe.insertDBEntry();
		
		pe.setLangAText("plX");
		pe.setLangBText("rusX");
		pe.setLabel("labX");
		
		pe.updateDBEntry();
		
		List<PhraseEntry> dictionary = DBUtils.getDictionary();
		boolean found = false;
		
		for (PhraseEntry ipe : dictionary) {
			if (ipe.getId() != pe.getId()) {
				continue;				
			}
			
			found = true;
			
			assertEquals(pe.getLangAText(), ipe.getLangAText());
			assertEquals(pe.getLangBText(), ipe.getLangBText());
			
			int d1 = DBUtils.getIntFromDateTime(dt);
			int d2 = DBUtils.getIntFromDateTime(ipe.getCreationDate());
			assertEquals(d1, d2);

			assertEquals(pe.getLabel(), ipe.getLabel());
		}
		
		assertTrue(found);
	}
	
	@Test
	public void testDeleteFromDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreationDate(DateTime.now());
		
		pe.insertDBEntry();
		pe.deleteDBEntry();
		
		List<PhraseEntry> dictionary = DBUtils.getDictionary();
		for (PhraseEntry ipe : dictionary) {
			assertThat(ipe.getId(), is(not(pe.getId())));
		}
	}	
}
