package pkleczek.profiwan.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
	public static int MIN_REVISION_INTERVAL = 2;

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
	 * The <code>RevisionEntry</code> class stores all information about a
	 * revision relevant for generation of further revisions.
	 * 
	 * @author Pawel
	 * 
	 */
	public static class RevisionEntry {

		public Date date;

		/**
		 * How many times a mistake was made during the given revision.
		 */
		public Integer mistakes;

		public void insertDBEntry(int revisionEntryId) throws SQLException {

			PreparedStatement stmt = DBUtils.insertRevisionEntry;

			stmt.setInt(1, (int) (date.getTime()/1000));
			stmt.setInt(2, mistakes);
			stmt.setInt(3, revisionEntryId);
			stmt.executeUpdate();
		}
		
		public static void updateDBEntry(int revisionEntryId, int mistakes) throws SQLException {

			PreparedStatement stmt = DBUtils.updateRevisionEntry;

			stmt.setInt(1, mistakes);
			stmt.setInt(2, revisionEntryId);
			stmt.executeUpdate();
		}		
	}

	private int id;

	private String plText = ""; //$NON-NLS-1$
	private String rusText = ""; //$NON-NLS-1$
	private Date creationDate = null;
	private String label = ""; //$NON-NLS-1$

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
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
		int freq = getRevisionFrequency();

		Calendar calendar = Calendar.getInstance();
		Date lastRevisionDate = revisions.get(revisions.size() - 1).date;
		calendar.setTime(lastRevisionDate);
		calendar.add(Calendar.DATE, freq);
		Date nextRevisionDate = calendar.getTime();
		Date today = Calendar.getInstance().getTime();

		return today.after(nextRevisionDate);
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
		stmt.setInt(4, (int) (getCreationDate().getTime()/1000));
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
