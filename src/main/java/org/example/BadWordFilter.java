package org.irontext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class BadWordFilter {
	// USE https://www.freewebheaders.com/full-list-of-bad-words-banned-by-google/

	public BadWordFilter(){}

	public ArrayList<String> filter(String message) {

		// load word list
		ArrayList<String> badWords;
		try { badWords = loadWords(); } catch (Exception exception){ throw new RuntimeException(new FileNotFoundException()); }
		clearMessage(message);
		ArrayList<String> detectedWords = new ArrayList<>();

		for (String word : badWords){

			clearMessage(word);

			if (message.contains(" " + word+ " ")){
				System.out.println(word);
				detectedWords.add(word);
			}


		}
		return detectedWords;
	}

	private ArrayList<String> loadWords() throws FileNotFoundException {
		File dir = new File(getClass().getResource("BadWordList/").getFile());
		ArrayList<String> wordList = new ArrayList<>();

		for (File file :  dir.listFiles()){
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()){
				wordList.add(scanner.nextLine());
			}
		}
		return wordList;
	}
	private void clearMessage(String message){
		message = message.toLowerCase();

		message = message.replaceAll("1","i");
		message = message.replaceAll("!","i");
		message = message.replaceAll("3","e");
		message = message.replaceAll("4","a");
		message = message.replaceAll("@","a");
		message = message.replaceAll("5","s");
		message = message.replaceAll("7","t");
		message = message.replaceAll("0","o");
		message = message.replaceAll("9","g");


		message = message.replaceAll("[^a-zA-Z]", "");

	}
}
