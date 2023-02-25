package org.example;

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
		File dir = new File("C:\\Users\\Usuario\\IdeaProjects\\MessagingAppServer\\src\\main\\resources\\BadWordList\\");
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


		message = message.replaceAll("à","a");
		message = message.replaceAll("á","a");
		message = message.replaceAll("ä","a");
		message = message.replaceAll("â","a");


		message = message.replaceAll("è","e");
		message = message.replaceAll("é","e");
		message = message.replaceAll("ë","e");
		message = message.replaceAll("ê","e");


		message = message.replaceAll("í","i");
		message = message.replaceAll("ì","i");
		message = message.replaceAll("ï","i");
		message = message.replaceAll("î","i");


		message = message.replaceAll("ò","o");
		message = message.replaceAll("ó","o");
		message = message.replaceAll("ö","o");
		message = message.replaceAll("ô","o");


		message = message.replaceAll("ú","u");
		message = message.replaceAll("ù","u");
		message = message.replaceAll("ü","u");
		message = message.replaceAll("û","u");


		message = message.replaceAll("[^a-zA-Z]", "");

	}
}
