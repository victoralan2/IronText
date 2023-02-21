package org.example;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class Hasher {
    private String mode = "";
    public Hasher(String m){
        this.mode = m;
    }

    public String hashString(String input, String saltSeed) {
        int rounds = 10; // the number of BCRYPT rounds to use (10 is a good default)
        String saltEncrypt = BCrypt.gensalt(rounds, new SecureRandom(saltSeed.getBytes())); // generate a random salt
        return BCrypt.hashpw(input, saltEncrypt); // hash the input string with the salt
    }
    public String hashString(String input) throws NoSuchAlgorithmException {
        if (Objects.equals(mode, "bcrypt")){
            return null;

        } else {
            MessageDigest messageDigest = MessageDigest.getInstance(mode);
            byte[] hash = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        }

    }
    private String bytesToHex(byte[] hash){
        StringBuilder hexString = new StringBuilder(2*hash.length);
        for (int i = 0; i<hash.length; i++){
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1){
                hexString.append("0");
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String randomString(int length){
        char[] characters = "abcdefghijklmnoñpqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        String randomString = "";
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            randomString+= characters[random.nextInt(characters.length)];
        }
        return randomString;
    }
}