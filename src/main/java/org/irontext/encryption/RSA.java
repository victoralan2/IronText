package org.irontext.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public class RSA {
    private final int seed;
    private final KeyPair pair;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public RSA(int seed)  {
        this.seed = seed;
        KeyPairGenerator generator = null;
        try{
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (Exception pass){}

        generator.initialize(4096);
        this.pair = generator.generateKeyPair();
        this.publicKey = pair.getPublic();
        this.privateKey = pair.getPrivate();


    }

    public PublicKey getPublicKey(){
        return this.publicKey;
    }

    public PrivateKey getPrivateKey(){
        return this.privateKey;
    }

    public byte[] encryptBytes(byte[] message){
        byte[] secretMessageBytes = null;
        byte[] encryptedMessageBytes = null;
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            secretMessageBytes = message;
            encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
                 IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
        return encryptedMessageBytes;
    }
    public byte[] decryptBytes(byte[] encryptedMessage, PrivateKey decryptKey){
        byte[] decryptedMessageBytes = null;
        try {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey);
            decryptedMessageBytes = decryptCipher.doFinal(encryptedMessage);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
                 IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
        return decryptedMessageBytes;
    }

}
