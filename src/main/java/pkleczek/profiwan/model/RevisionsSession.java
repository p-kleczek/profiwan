package pkleczek.profiwan.model;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.joda.time.DateTime;

import pkleczek.Messages;
import pkleczek.profiwan.model.PhraseEntry.RevisionEntry;
import pkleczek.profiwan.utils.DBUtils;

public class RevisionsSession {
	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	/**
	 * List of pending revisions.
	 */
	private LinkedList<PhraseEntry> pendingRevisions = new LinkedList<>();

	/**
	 * Maps phrase's ID on a corresponding revision entry.
	 */
	private Map<Integer, RevisionEntry> revisionEntries = new HashMap<Integer, RevisionEntry>();

	private ListIterator<PhraseEntry> pendingRevisionsIterator = null;
	private PhraseEntry currentRevision = null;

	private boolean enteredCorrectly = false;
	private int wordsNumber = 0;
	private int correctWordsNumber = 0;
	private int revisionsNumber = 1;

	public RevisionsSession() {
	}

	public void prepareRevisions() {
		List<PhraseEntry> dictionary = null;
		try {
			dictionary = DBUtils.getDictionary();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (PhraseEntry pe : dictionary) {
			if (pe.isReviseNow()) {
				pendingRevisions.add(pe);
				revisionEntries.put(pe.getId(), prepareRevisionEntry(pe));
			}
		}

		Collections.shuffle(pendingRevisions);

		wordsNumber = pendingRevisions.size();
		pendingRevisionsIterator = pendingRevisions.listIterator();
	}

	private RevisionEntry prepareRevisionEntry(PhraseEntry phrase) {
		RevisionEntry revision = new RevisionEntry();
		revision.date = DateTime.now();

		List<RevisionEntry> revisions = phrase.getRevisions();
		if (!revisions.isEmpty()) {
			RevisionEntry re = revisions.get(revisions.size() - 1);

			if (re.isToContinue()) {
				revision = re;
			}
		}

		return revision;
	}

	public boolean hasRevisions() {
		return !pendingRevisions.isEmpty();
	}

	public boolean processTypedWord(String input) {
		currentRevision = pendingRevisionsIterator.next();
		enteredCorrectly = currentRevision.getLangBText().equals(input);

		RevisionEntry re = revisionEntries.get(currentRevision.getId());
		if (re.mistakes == 0) {
			try {
				re.insertDBEntry(currentRevision.getId());
				currentRevision.getRevisions().add(re);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (enteredCorrectly) {
			re.mistakes = -re.mistakes;
			confirmRevision(currentRevision);
		} else {
			re.mistakes--;
		}

		try {
			re.updateDBEntry();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return enteredCorrectly;
	}

	public PhraseEntry getCurrentPhrase() {
		return currentRevision;
	}

	private void confirmRevision(PhraseEntry pe) {
		RevisionEntry re = new RevisionEntry();
		pe.getRevisions().add(re);
		pendingRevisionsIterator.remove();
		revisionEntries.remove(pe.getId());
		correctWordsNumber++;
	}

	public void acceptRevision() {
		if (!enteredCorrectly) {
			confirmRevision(pendingRevisionsIterator.previous());
		}
	}

	public void editPhrase(String newText) {
		currentRevision.setLangBText(newText);

		try {
			currentRevision.updateDBEntry();
		} catch (SQLException e1) {
			JOptionPane
					.showMessageDialog(
							null,
							Messages.getString("dbError"), Messages.getString("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			logger.severe(e1.toString());
		}
	}

	public int getWordsNumber() {
		return wordsNumber;
	}

	public int getCorrectWordsNumber() {
		return correctWordsNumber;
	}

	public int getRevisionsNumber() {
		return revisionsNumber;
	}

	public int getPendingRevisionsSize() {
		return pendingRevisions.size();
	}

	public void nextWord() {
		if (!hasRevisions()) {
			return;
		}

		if (!pendingRevisionsIterator.hasNext()) {
			pendingRevisionsIterator = pendingRevisions.listIterator();
		}

		revisionsNumber++;
	}

	public PhraseEntry getNextWord() {
		return pendingRevisions.get(pendingRevisionsIterator.nextIndex());
	}
}
