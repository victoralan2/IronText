package org.irontext.encryption;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class ExtremeRandom extends Random {

	public ExtremeRandom(){
		this(generateRandomSeed());
	}

	private ExtremeRandom(long seed){
		super(seed);

	}

	private static long generateRandomSeed(){
		try {
			System.out.println(getHashValue(getFreeMemory() / getMaxMemory() * getTotalMemory() - getUsedMemory() * (System.nanoTime())));
			System.out.println(getHashValue(getFreeMemory() / getMaxMemory() * getTotalMemory() - getUsedMemory() * (System.nanoTime())));
			System.out.println(getHashValue(getFreeMemory() / getMaxMemory() * getTotalMemory() - getUsedMemory() * (System.nanoTime())));
			return getHashValue(getFreeMemory() / getMaxMemory() * getTotalMemory() - getUsedMemory() ^ (System.nanoTime()));
		} catch (Exception ignored){ignored.printStackTrace();}
		return -1;
	}
	private static long getHashValue(Long number) throws NoSuchAlgorithmException {
		Hasher hasher = new Hasher("SHA256");
		String hash = hasher.hashString(number.toString());
		StringBuilder numb = new StringBuilder();
		for (Character character : hash.toCharArray()){
			if (Character.isDigit(character) && character != '0'){
				numb.append(character);
			}
		}
		return Long.parseLong(numb.substring(0, numb.length()/3))-new Random().nextInt();
	}
	private static long getFreeMemory() {
		return Runtime.getRuntime().freeMemory();
	}
	private static long getTotalMemory() {
		return Runtime.getRuntime().totalMemory();
	}
	private static long getUsedMemory() {
		return getMaxMemory() - getFreeMemory();
	}
	private static long getMaxMemory() {
		return Runtime.getRuntime().maxMemory();
	}
}
