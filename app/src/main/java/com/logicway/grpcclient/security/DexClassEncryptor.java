package com.logicway.grpcclient.security;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DexClassEncryptor {

    private static final String STRING_KEY = "w9z$C&F)J@NcRfUj";

    public static final String ALGORITHM = "AES";

    public static final byte[] KEY = STRING_KEY.getBytes();

    public static void main(String[] args) throws Exception {
        encrypt(args[1], args[2], ALGORITHM, KEY);
    }

    public static void encrypt(String path, String dexFileName, String algorithm, byte[] key) throws Exception {

        Path file = Paths.get(path + File.separator + dexFileName);
        byte[] content = Files.readAllBytes(file);
        Cipher encryption = Cipher.getInstance(algorithm);
        encryption.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, 0, key.length, algorithm));
        byte[] encryptedContent = encryption.doFinal(content);
        writeToFile(path, dexFileName, encryptedContent);
    }

    public static void writeToFile(String path, String dexFileName, byte[] content) throws Exception {
        FileOutputStream fos = new FileOutputStream(path + File.separator + dexFileName);
        fos.write(content);
        fos.close();
    }
}
