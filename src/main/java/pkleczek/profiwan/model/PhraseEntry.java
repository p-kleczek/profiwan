package pkleczek.profiwan.model;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pkleczek.profiwan.utils.DBExecutable;
import pkleczek.profiwan.utils.DBUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * The <code>PhraseEntry</code> class stores all the information about a phrase
 * (ie. translations, revisions).
 * 
 * @author Pawel
 * 
 */
public class PhraseEntry implements Serializable, DBExecutable {

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
	 * TODO: blad => reset postepow we FREQ do podanego ulamka
	 */
	private static double MISTAKE_MULTIPLIER = 0.5;

	private static final long serialVersionUID = 5318708654536022199L;

	/**
	 * The <code>RevisionEntry</code> class stores all information about a
	 * revision relevant for generation of further revisions.
	 * 
	 * @author Pawel
	 * 
	 */
	public static class RevisionEntry implements Serializable {
		private static final long serialVersionUID = 939716563607754267L;

		public Date date;

		/**
		 * How many times a mistake was made during the given revision.
		 */
		public Integer mistakes;
	}

	private int id;

	private String plText = "";
	private String rusText = "";

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

	// TODO: testowac !!
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

		stmt.executeUpdate();

		ResultSet generatedKeys = null;
		try {
			generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
				setId(generatedKeys.getInt(1));
			} else {
				throw new SQLException(
						"PhraseEntry: no generated key obtained.");
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
		stmt.setInt(4, getId());
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
		return String.format("id=%d rus=\'%s\' pl=\'%s\'", getId(),
				getRusText(), getPlText());
	}

}
