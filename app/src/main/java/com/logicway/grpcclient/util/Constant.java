package com.logicway.grpcclient.util;

public final class Constant {

    public static final String HOST = "172.28.10.39";
    public static final String PORT = "1337";
    public static final long KEEP_ALIVE_TIME = 60;
    public static final long TIMEOUT = 5;

    public static final String TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    public static final String DEX_FILE_URL = "id_getter.dex";
    public static final String SO_FILE_URL = "libid_getter.so";

    public static final String ROOT_PACKAGE = "com.logicway.grpcclient";
    public static final String ID_GETTER_CLASSNAME = ROOT_PACKAGE + ".lib.IdGetter";
    public static final String GENERATE_RANDOM_COMMAND_TASK_CLASSNAME = ROOT_PACKAGE
            + ".service.asynctask.okhttp.GenerateRandomCommandTask";
    public static final String DOWNLOAD_FILE_TASK_CLASSNAME = ROOT_PACKAGE
            + ".service.asynctask.okhttp.DownloadFileTask";

    public static final String ENCRYPTED_DEX_FILE_NAME = "classes.dex";

    public static final String EXECUTE_METHOD_NAME = "execute";
    public static final String GET_METHOD_NAME = "get";

    public static final String GET_ANDROID_ID_METHOD_NAME = "getAndroidId";
    public static final String GET_GSF_ID_METHOD_NAME = "getGsfId";
    public static final String GET_FINGERPRINT_METHOD_NAME = "getFingerprint";
    public static final String GET_BASE64_FROM_IDS_HASHES_METHOD_NAME = "getBase64FromIdsHashes";

    private Constant() {

    }
}
