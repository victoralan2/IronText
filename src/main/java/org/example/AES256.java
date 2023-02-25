package org.example;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class AES256 {
    public static String encryptAES256(String key, String message, String seed) throws Exception {
        // Instance of the hasher for hashing the key 
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        // Conver the key / password to a sha256 hash as bytes.
        byte[] keyBytes = Arrays.copyOf(sha256.digest(key.getBytes(StandardCharsets.UTF_8)), 32);
        // Generate the iv using the seed
        byte[] iv = new byte[16];
        // convert seed to a integer
        int intSeed = getHashCode(seed);

        Random random = new Random(intSeed);

        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        byte[] encryptedWithIv = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }
    public static int getHashCode(String string){
        try {
            Hasher hasher = new Hasher("SHA-256");
            StringBuilder code = new StringBuilder();
            for (Character character : hasher.hashString(string).toCharArray()){
                if (Character.isDigit(character)){
                    if (character == '0') continue;
                    code.append(character);
                }
            }
            return Integer.parseInt(code.substring(0, 8));
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return 0;
    }

}
