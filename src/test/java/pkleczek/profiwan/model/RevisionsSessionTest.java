package pkleczek.profiwan.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import pkleczek.profiwan.ProfIwan;
import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;
import pkleczek.profiwan.utils.DBUtils;

public class RevisionsSessionTest {

	private RevisionsSession rs;

	@Before
	public void prepareDB() throws SQLException {
		ProfIwan.inDebugMode = true;
		DBUtils.recreateTables();

		rs = new RevisionsSession();
	}

	@Test
	public void testPrepareRevisions() throws Exception {

		PhraseEntry pe = new PhraseEntry();
		pe.setInRevisions(true);
		pe.setCreationDate(DateTime.now());
		pe.insertDBEntry();

		RevisionEntry re = new RevisionEntry();
		re.date = new DateTime(0L);
		re.mistakes = 0;
		re.insertDBEntry(pe.getId());
		
		rs.prepareRevisions();
		
		assertTrue(rs.hasRevisions());
		assertEquals(1, rs.getPendingRevisionsSize());

		 Field field = RevisionsSession.class.getDeclaredField("revisionEntries");
		 field.setAccessible(true);

		 Map<Integer, RevisionEntry> map = (Map<Integer, RevisionEntry>) field.get(rs);
		 assertEquals(1, map.size());
	}

	@Test
	public void testPrepareRevisionEntryBrandNew() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreationDate(DateTime.now());

		RevisionEntry re = new RevisionEntry();
		re.id = 1;
		re.date = new DateTime(1000L);
		re.mistakes = -1;
		pe.getRevisions().add(re);

		Method method = RevisionsSession.class
				.getDeclaredMethod("prepareRevisionEntry", PhraseEntry.class);
		method.setAccessible(true);
		RevisionEntry ret = (RevisionEntry) method.invoke(rs, pe);
		
		assertNull(ret.id);
		assertEquals(0, ret.mistakes);
	}
	
	@Test
	public void testPrepareRevisionEntryNoContinue() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreationDate(DateTime.now());

		RevisionEntry re = new RevisionEntry();
		re.id = 1;
		re.date = DateTime.now();
		re.mistakes = -1;
		pe.getRevisions().add(re);

		Method method = RevisionsSession.class
				.getDeclaredMethod("prepareRevisionEntry", PhraseEntry.class);
		method.setAccessible(true);
		RevisionEntry ret = (RevisionEntry) method.invoke(rs, pe);
		
		assertSame(ret, re);
	}	
}
