package pkleczek.profiwan.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhraseEntry implements Serializable {

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

}
