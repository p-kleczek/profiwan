package pkleczek.profiwan.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import pkleczek.profiwan.debug.Debug;
import pkleczek.profiwan.utils.DBExecutable;
import pkleczek.profiwan.utils.DBUtils;

/**
 * The <code>PhraseEntry</code> class stores all the information about a phrase
 * (ie. translations, revisions).
 * 
 * @author Pawel
 * 
 */
public class PhraseEntry implements DBExecutable {

	/**
	 * Maximum number of days between two consecutive revisions.
	 */
	public static int MAX_REVISION_INTERVAL = 30;

	/**
	 * Minimum number of days between two consecutive revisions.
	 */
	public static int MIN_REVISION_INTERVAL = 1;

	/**
	 * Numbers of initial consecutive correct revisions before its frequency
	 * starts to fall.
	 */
	public static int MIN_CORRECT_STREAK = 3;

	/**
	 * Change in revisions' frequency with each correct revision.
	 */
	public static int FREQUENCY_DECAY = 2;

	/**
	 * blad => reset postepow we FREQ do podanego ulamka
	 */
	private static double MISTAKE_MULTIPLIER = 0.5;

	/**
	 * Makes revision frequency vary +/- n% from its initial value to prevent
	 * the stacking effect of revisions made on the same day.
	 */
	private static double COUNTER_STACKING_FACTOR = 0.1;

	/**
	 * The <code>RevisionEntry</code> class stores all information about a
	 * revision relevant for generation of further revisions.
	 * 
	 * @author Pawel
	 * 
	 */
	public static class RevisionEntry {

		public Integer id = null;

		public DateTime date = null;

		/**
		 * How many times a mistake was made during the given revision.
		 */
		public int mistakes;

		public boolean isToContinue() {
			return (date.isAfter(DateTime.now().withTimeAtStartOfDay()) && mistakes < 0);
		}

		public void insertDBEntry(int revisionEntryId) throws SQLException {

			PreparedStatement stmt = DBUtils.insertRevisionEntry;

			stmt.setInt(1, (int) (date.getMillis() / 1000));
			stmt.setInt(2, mistakes);
			stmt.setInt(3, revisionEntryId);
			stmt.executeUpdate();

			ResultSet generatedKeys = null;
			try {
				generatedKeys = stmt.getGeneratedKeys();
				if (generatedKeys.next()) {
					id = generatedKeys.getInt(1);
				} else {
					throw new SQLException(
							"PhraseEntry: no generated key obtained."); //$NON-NLS-1$
				}
			} finally {
				if (generatedKeys != null)
					try {
						generatedKeys.close();
					} catch (SQLException logOrIgnore) {
					}
			}

			Debug.printDict("insert RE");
		}

		// public static void updateDBEntry(int revisionEntryId, int mistakes)
		// throws SQLException {
		//
		// PreparedStatement stmt = DBUtils.updateRevisionEntry;
		//
		// stmt.setInt(1, mistakes);
		// stmt.setInt(2, revisionEntryId);
		// stmt.executeUpdate();
		// }

		public void updateDBEntry() throws SQLException {

			PreparedStatement stmt = DBUtils.updateRevisionEntryId;

			stmt.setInt(1, mistakes);
			stmt.setInt(2, id);
			stmt.executeUpdate();

			Debug.printDict("update RE");
		}
	}

	private int id;

	private String plText = ""; //$NON-NLS-1$
	private String rusText = ""; //$NON-NLS-1$
	private DateTime creationDate = null;
	private String label = ""; //$NON-NLS-1$

	public DateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(DateTime creationDate) {
		this.creationDate = creationDate;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * <code>true</code> if the phrase is currently revised
	 */
	private boolean inRevisions = false;

	private List<RevisionEntry> revisions = new ArrayList<>();

	public String getPlText() {
		return plText;
	}

	public void setPlText(String plText) {
		this.plText = plText;
	}

	public String getRusText() {
		return rusText;
	}

	public void setRusText(String rusText) {
		this.rusText = rusText;
	}

	public List<RevisionEntry> getRevisions() {
		return revisions;
	}

	public void setRevisions(List<RevisionEntry> revisions) {
		this.revisions = revisions;
	}

	public boolean isInRevisions() {
		return inRevisions;
	}

	public void setInRevisions(boolean inRevisions) {
		this.inRevisions = inRevisions;
	}

	public boolean isReviseNow() {

		if (!isInRevisions()) {
			return false;
		}

		if (revisions.isEmpty()) {
			return true;
		}

		RevisionEntry lastRevision = revisions.get(revisions.size() - 1);
		if (lastRevision.isToContinue()) {
			return true;
		}

		int freq = getRevisionFrequency();

		// Modify frequency to prevent stacking of revisions made on the same
		// day.
		freq *= (1.0 - COUNTER_STACKING_FACTOR) + Math.random()
				* (COUNTER_STACKING_FACTOR / 2.0);
		freq = Math.max(freq, MIN_REVISION_INTERVAL);
		freq = Math.min(freq, MAX_REVISION_INTERVAL);

		DateTime nextRevisionDate = lastRevision.date.plusDays(freq)
				.withTimeAtStartOfDay();
		DateTime todayMidnight = DateTime.now().withTimeAtStartOfDay();

		return !nextRevisionDate.isAfter(todayMidnight);
	}

	public int getRevisionFrequency() {
		int freq = MIN_REVISION_INTERVAL;
		int correctStreak = 0;
		boolean isInitialStreak = false;

		for (int i = 0; i < revisions.size(); i++) {
			RevisionEntry e = revisions.get(i);

			if (e.mistakes == 0) {
				if (isInitialStreak) {
					freq += FREQUENCY_DECAY;
				}

				correctStreak++;

				if (!isInitialStreak && correctStreak == MIN_CORRECT_STREAK) {
					isInitialStreak = true;
					correctStreak = 0;
				}
			} else {
				if (isInitialStreak) {
					freq -= correctStreak * FREQUENCY_DECAY
							* MISTAKE_MULTIPLIER;

					// clamp
					freq = Math.min(freq, MAX_REVISION_INTERVAL);
					freq = Math.max(freq, MIN_REVISION_INTERVAL);
				}
				correctStreak = 0;
			}
		}

		return freq;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void insertDBEntry() throws SQLException {

		PreparedStatement stmt = DBUtils.insertPhraseEntry;

		int ir = isInRevisions() ? 1 : 0;

		stmt.setString(1, getRusText());
		stmt.setString(2, getPlText());
		stmt.setInt(3, ir);
		stmt.setInt(4, (int) (getCreationDate().getMillis() / 1000));
		stmt.setString(5, getLabel());

		stmt.executeUpdate();

		ResultSet generatedKeys = null;
		try {
			generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
				setId(generatedKeys.getInt(1));
			} else {
				throw new SQLException(
						"PhraseEntry: no generated key obtained."); //$NON-NLS-1$
			}
		} finally {
			if (generatedKeys != null)
				try {
					generatedKeys.close();
				} catch (SQLException logOrIgnore) {
				}
		}
	}

	@Override
	public void updateDBEntry() throws SQLException {
		PreparedStatement stmt = DBUtils.updatePhraseEntry;

		stmt.setString(1, getRusText());
		stmt.setString(2, getPlText());
		stmt.setBoolean(3, isInRevisions());
		stmt.setString(4, getLabel());
		stmt.setInt(5, getId());
		stmt.executeUpdate();
	}

	@Override
	public void deleteDBEntry() throws SQLException {
		PreparedStatement stmt = DBUtils.deletePhraseEntry;

		stmt.setInt(1, getId());
		stmt.executeUpdate();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("id=%d rus=\'%s\' pl=\'%s\' [%s]\n", getId(), //$NON-NLS-1$
				getRusText(), getPlText(), getCreationDate()));

		for (RevisionEntry re : revisions) {
			sb.append(String.format("\t%s [%d]\n", re.date.toString(), //$NON-NLS-1$
					re.mistakes));
		}

		return sb.toString();
	}

}
