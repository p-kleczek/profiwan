package pkleczek.profiwan.model;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;

import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;

public class PhraseEntryTest {

	@Test
	public void testIsReviseNow() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;
		Calendar cal = null;

		re = new RevisionEntry();
		cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -PhraseEntry.MIN_CORRECT_STREAK);
		re.date = cal.getTime();
		re.mistakes = 1;
		pe.getRevisions().add(re);

		assertTrue(pe.isReviseNow());
	}

	@Test
	public void testIsReviseNowInitial() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;
		Calendar cal = null;

		re = new RevisionEntry();
		cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -PhraseEntry.FREQUENCY_DECAY);
		re.date = cal.getTime();
		re.mistakes = 1;
		pe.getRevisions().add(re);

		assertFalse(pe.isReviseNow());
	}

	@Test
	public void testGetRevisionsFrequencyNoStreak() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;
		Calendar cal = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK; i++) {
			re = new RevisionEntry();
			cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -PhraseEntry.MIN_CORRECT_STREAK);
			re.date = cal.getTime();
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
		Calendar cal = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK + 1; i++) {
			re = new RevisionEntry();
			cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -PhraseEntry.MIN_CORRECT_STREAK);
			re.date = cal.getTime();
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
		Calendar cal = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK - 2; i++) {
			re = new RevisionEntry();
			cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -PhraseEntry.MIN_CORRECT_STREAK);
			re.date = cal.getTime();
			re.mistakes = 0;
			pe.getRevisions().add(re);
		}

		re = new RevisionEntry();
		cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -PhraseEntry.MIN_CORRECT_STREAK);
		re.date = cal.getTime();
		re.mistakes = 1;
		pe.getRevisions().add(re);

		assertEquals(PhraseEntry.MIN_REVISION_INTERVAL,
				pe.getRevisionFrequency());
	}

	@Test
	public void testGetRevisionsFrequencyStreakError() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;
		Calendar cal = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK + 2; i++) {
			re = new RevisionEntry();
			cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -PhraseEntry.MIN_CORRECT_STREAK);
			re.date = cal.getTime();
			re.mistakes = 0;
			pe.getRevisions().add(re);
		}

		re = new RevisionEntry();
		cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -PhraseEntry.MIN_CORRECT_STREAK);
		re.date = cal.getTime();
		re.mistakes = 1;
		pe.getRevisions().add(re);

		assertEquals(PhraseEntry.MIN_REVISION_INTERVAL
				+ PhraseEntry.FREQUENCY_DECAY, pe.getRevisionFrequency());
	}
}
