package com.logicway.grpcclient.lib;

// Command to generate .h file from cmd: javah com.logicway.grpcclient.lib.JNILib

public class JNILib {

    public native String getAndroidId();

    public native String getGsfId();

    public native String getFingerprint();

    public native String getBase64FromIdsHashes();
}

