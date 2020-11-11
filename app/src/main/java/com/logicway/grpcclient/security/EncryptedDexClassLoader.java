package com.logicway.grpcclient.security;

import com.logicway.grpcclient.App;

import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import dalvik.system.InMemoryDexClassLoader;

public class EncryptedDexClassLoader extends ClassLoader {

    public EncryptedDexClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class loadClass(String className, String encryptedDexFileName) {
        Class clazz = findLoadedClass(className);
        if (clazz != null) {
            return clazz;
        }
        return findClass(className, encryptedDexFileName);
    }

    private Class findClass(String name, String encryptedDexFileName) {
        byte[] fileBytes = {};
        try {
            fileBytes = loadClassData(encryptedDexFileName, DexClassEncryptor.ALGORITHM, DexClassEncryptor.KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(fileBytes.length);
        byteBuffer.put(fileBytes);
        byteBuffer.position(0);
        InMemoryDexClassLoader inMemoryDexClassLoader = new InMemoryDexClassLoader(byteBuffer, getParent());
        Class clazz = null;
        try {
            clazz = inMemoryDexClassLoader.loadClass(name);
            byteBuffer.clear();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    private byte[] loadClassData(String encryptedDexFileName, String algorithm, byte[] key) throws Exception {
        byte[] encryptedFileContent;
        try (InputStream is = App.getAppContext().getAssets().open(encryptedDexFileName)) {
            encryptedFileContent = new byte[is.available()];
            is.read(encryptedFileContent);
        }

        Cipher decryption = Cipher.getInstance(algorithm);
        decryption.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, 0, key.length, algorithm));
        byte[] decryptedFileContent = decryption.doFinal(encryptedFileContent);

        return decryptedFileContent;
    }
}
