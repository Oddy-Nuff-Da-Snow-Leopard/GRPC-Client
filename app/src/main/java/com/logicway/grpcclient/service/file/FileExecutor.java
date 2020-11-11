package com.logicway.grpcclient.service.file;

import android.content.Context;
import android.util.Log;

import com.logicway.grpcclient.App;
import com.logicway.grpcclient.lib.JNILib;
import com.logicway.grpcclient.util.Constant;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import dalvik.system.DexClassLoader;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class FileExecutor {

    private static final String TAG = FileExecutor.class.getName();

    private final Context context = App.getAppContext();

    private final AtomicBoolean isLoaded = new AtomicBoolean(false);

    public void executeDexFile(String fileName, String className, String methodName) {
        File dexFile = new File(context.getCacheDir() + File.separator + fileName);
        DexClassLoader classLoader = new DexClassLoader(dexFile.getAbsolutePath(),
                context.getCacheDir().getPath(),
                null, context.getClassLoader());
        try {
            Class clazz = classLoader.loadClass(className);
            Method m = clazz.getMethod(methodName, Context.class);
            String result = (String) m.invoke(clazz.newInstance(), App.getAppContext());
            Log.d(TAG, methodName + ": " + result);
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void executeSoFile(String url) {
        if (!isLoaded.get()) {
            System.load(context.getCacheDir() + File.separator + url);
            isLoaded.compareAndSet(false, true);
        }
        JNILib jniLib = new JNILib();
        Log.d(TAG, Constant.GET_ANDROID_ID_METHOD_NAME + ": " + jniLib.getAndroidId());
        Log.d(TAG, Constant.GET_GSF_ID_METHOD_NAME + ": " + jniLib.getGsfId());
        Log.d(TAG, Constant.GET_FINGERPRINT_METHOD_NAME + ": " + jniLib.getFingerprint());
        Log.d(TAG, Constant.GET_BASE64_FROM_IDS_HASHES_METHOD_NAME + ": " + jniLib.getBase64FromIdsHashes());
    }
}
