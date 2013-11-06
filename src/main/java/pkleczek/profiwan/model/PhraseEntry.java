package pkleczek.profiwan.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PhraseEntry implements Serializable {

	public static int MIN_REVISION_FREQUENCY = 30; // 1x / 30 dni
	public static int MAX_REVISION_FREQUENCY = 2; // 1x / 2 dni
	public static int MIN_CORRECT_STREAK = 3; // 3x
	public static int FREQUENCY_DECAY = 2; // 2 dni za kazda poprawna powtorke

	/**
	 * blad => reset postepow we FREQ do podanego ulamka
	 */
	private static double MISTAKE_MULTIPLIER = 0.5;

	/**
	 * 
	 */
	private static final long serialVersionUID = 5318708654536022199L;

	public static class RevisionEntry implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 939716563607754267L;

		public Date date;

		/**
		 * How many times a mistake was made during the given revision.
		 */
		public Integer mistakes;
	}

	private String plText = "";
	private String rusText = "";
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
		int freq = MAX_REVISION_FREQUENCY;
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
					freq = Math.min(freq, MIN_REVISION_FREQUENCY);
					freq = Math.max(freq, MAX_REVISION_FREQUENCY);
				}
				correctStreak = 0;
			}
		}

		return freq;
	}
}
