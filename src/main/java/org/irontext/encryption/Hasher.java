package org.irontext.encryption;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import org.mindrot.jbcrypt.BCrypt;



public class Hasher {
    private static final char[] base64_code = new char[]{'.', '/', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private String mode = "";
    public Hasher(String m){
        this.mode = m;
    }
    private static String encode_base64(byte[] d, int len) throws IllegalArgumentException {
        int off = 0;
        StringBuffer rs = new StringBuffer();
        if (len > 0 && len <= d.length) {
            while(off < len) {
                int c1 = d[off++] & 255;
                rs.append(base64_code[c1 >> 2 & 63]);
                c1 = (c1 & 3) << 4;
                if (off >= len) {
                    rs.append(base64_code[c1 & 63]);
                    break;
                }

                int c2 = d[off++] & 255;
                c1 |= c2 >> 4 & 15;
                rs.append(base64_code[c1 & 63]);
                c1 = (c2 & 15) << 2;
                if (off >= len) {
                    rs.append(base64_code[c1 & 63]);
                    break;
                }

                c2 = d[off++] & 255;
                c1 |= c2 >> 6 & 3;
                rs.append(base64_code[c1 & 63]);
                rs.append(base64_code[c2 & 63]);
            }

            return rs.toString();
        } else {
            throw new IllegalArgumentException("Invalid len");
        }
    }


    public String hashString(String input, String saltSeed) {
        int log_rounds = 10; // the number of BCRYPT rounds to use (10 is a good default)
        Random random = new Random();
        random.setSeed(saltSeed.hashCode());
        StringBuffer rs = new StringBuffer();
        byte[] rnd = new byte[16];
        random.nextBytes(rnd);
        rs.append("$2a$");
        if (log_rounds < 10) {
            rs.append("0");
        }

        if (log_rounds > 30) {
            throw new IllegalArgumentException("log_rounds exceeds maximum (30)");
        } else {
            rs.append(Integer.toString(log_rounds));
            rs.append("$");
            rs.append(encode_base64(rnd, rnd.length));
            rs.toString();
        }
        return BCrypt.hashpw(input, rs.toString()); // hash the input string with the salt
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
