package pkleczek.profiwan.model;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;
import pkleczek.profiwan.utils.DBUtils;

public class RevisionEntryTest {

	@Test
	public void testIsToContinueTodayNoMistakes() {
		RevisionEntry re = new RevisionEntry();
		re.date = DateTime.now();
		re.mistakes = 0;
		
		assertFalse(re.isToContinue());
	}
	
	@Test
	public void testIsToContinueTodayCompletedWithMistakes() {
		RevisionEntry re = new RevisionEntry();
		re.date = DateTime.now();
		re.mistakes = 1;
		
		assertFalse(re.isToContinue());
	}	
	
	@Test
	public void testIsToContinueTodayUncompletedWithMistakes() {
		RevisionEntry re = new RevisionEntry();
		re.date = DateTime.now();
		re.mistakes = -1;
		
		assertTrue(re.isToContinue());
	}
	
	@Test
	public void testIsToContinuePast() {
		RevisionEntry re = new RevisionEntry();
		re.date = DateTime.now().minusDays(1);
		re.mistakes = -1;
		
		assertFalse(re.isToContinue());
	}
	
	@Test
	public void testInsertIntoDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreationDate(DateTime.now());
		pe.insertDBEntry();
		
		RevisionEntry re = new RevisionEntry();
		re.date = new DateTime(1000L);
		re.mistakes = 1;
		re.insertDBEntry(pe.getId());
		
		boolean found = false;
		List<PhraseEntry> dictionary = DBUtils.getDictionary();
		
		for (PhraseEntry ipe : dictionary) {
			if (ipe.getId() != pe.getId()) {
				continue;				
			}
			
			found = true;
			
			List<RevisionEntry> revs = ipe.getRevisions();
			assertFalse(revs.isEmpty());
			
			RevisionEntry ire = revs.get(0);
			assertEquals(re.date.getMillis(), ire.date.getMillis());
			assertEquals(re.mistakes, ire.mistakes);
		}
		
		assertTrue(found);
	}
	
	@Test
	public void testUpdateInDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreationDate(DateTime.now());
		pe.insertDBEntry();
		
		RevisionEntry re = new RevisionEntry();
		re.date = new DateTime(1000L);
		re.mistakes = 1;
		re.insertDBEntry(pe.getId());
		
		re.mistakes = 3;
		re.updateDBEntry();
		
		boolean found = false;
		List<PhraseEntry> dictionary = DBUtils.getDictionary();
		
		for (PhraseEntry ipe : dictionary) {
			if (ipe.getId() != pe.getId()) {
				continue;				
			}
			
			found = true;
			
			List<RevisionEntry> revs = ipe.getRevisions();
			assertFalse(revs.isEmpty());
			
			RevisionEntry ire = revs.get(0);
			assertEquals(re.date.getMillis(), ire.date.getMillis());
			assertEquals(re.mistakes, ire.mistakes);
		}
		
		assertTrue(found);
	}
	
}
