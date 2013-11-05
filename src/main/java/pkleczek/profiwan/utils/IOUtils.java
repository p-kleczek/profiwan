package pkleczek.profiwan.utils;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import pkleczek.profiwan.model.PhraseEntry;

public class IOUtils {
	public static final String vocabularyDict = "vocabulary.dict";

	public static List<PhraseEntry> readDictionary(String dictName) {
		List<PhraseEntry> dict = new ArrayList<>();

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				dictName))) {
			while (true) {
				try {
					PhraseEntry entry = (PhraseEntry) ois.readObject();
					dict.add(entry);
				} catch (EOFException e) {
					break;
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return dict;
	}

	public static void writeDictionary(String dictName, List<PhraseEntry> dict) {
		try (ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(dictName))) {
			for (PhraseEntry e : dict) {
				oos.writeObject(e);
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
