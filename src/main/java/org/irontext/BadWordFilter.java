package org.irontext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class BadWordFilter {
	// USE https://www.freewebheaders.com/full-list-of-bad-words-banned-by-google/

	public BadWordFilter(){}

	public ArrayList<String> filter(String message) {

		// load word list
		ArrayList<String> badWords = new ArrayList<>();
		try { badWords = loadWords(); } catch (Exception exception){ exception.printStackTrace();}
		clearMessage(message);
		ArrayList<String> detectedWords = new ArrayList<>();

		for (String word : badWords){

			clearMessage(word);
			if (word == null){
				continue;
			}
 			if (message.contains(" " + word + " ") || message.startsWith(word + " ") || message.endsWith(" "+ word)){
				detectedWords.add(word);
			}


		}
		return detectedWords;
	}

	private ArrayList<String> loadWords() throws IOException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream en = classloader.getResourceAsStream("BadWordList/en.txt");
		InputStream es = classloader.getResourceAsStream("BadWordList/es.txt");
		InputStream more = classloader.getResourceAsStream("BadWordList/more.txt");

		ArrayList<String> wordList = new ArrayList<>();



		InputStreamReader streamReaderEN = new InputStreamReader(en, StandardCharsets.UTF_8);
		BufferedReader readerEN = new BufferedReader(streamReaderEN);

		InputStreamReader streamReaderES = new InputStreamReader(es, StandardCharsets.UTF_8);
		BufferedReader readerES = new BufferedReader(streamReaderES);

		InputStreamReader streamReaderMR = new InputStreamReader(more, StandardCharsets.UTF_8);
		BufferedReader readerMR = new BufferedReader(streamReaderMR);

		for (String line; (line = readerES.readLine()) != null;) {
			wordList.add(line.replace("\n", ""));
		}
		for (String line; (line = readerEN.readLine()) != null;) {
			wordList.add(line.replace("\n", ""));
		}
		for (String line; (line = readerMR.readLine()) != null;) {
			wordList.add(line.replace("\n", ""));

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
		message = message.replaceAll("\n","");


		message = message.replaceAll("[^a-zA-Z ]", "");
	}
}
