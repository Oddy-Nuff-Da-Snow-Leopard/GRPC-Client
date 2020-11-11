#include "com_logicway_grpcclient_lib_JNILib.h"

static jobject getGlobalContext(JNIEnv *env) {

    jclass activityThread = env->FindClass("android/app/ActivityThread");
    jmethodID currentActivityThread = env->GetStaticMethodID(activityThread, "currentActivityThread",
                                                             "()Landroid/app/ActivityThread;");
    jobject at = env->CallStaticObjectMethod(activityThread, currentActivityThread);

    jmethodID getApplication = env->GetMethodID(activityThread, "getApplication", "()Landroid/app/Application;");
    jobject context = env->CallObjectMethod(at, getApplication);

    return context;
}

JNIEXPORT jstring JNICALL Java_com_logicway_grpcclient_lib_JNILib_getAndroidId(JNIEnv *env, jobject thiz) {

    jclass c_settings_secure = env->FindClass("android/provider/Settings$Secure");
    jclass c_context = env->FindClass("android/content/Context");

    jmethodID m_get_content_resolver = env->GetMethodID(c_context, "getContentResolver",
                                                        "()Landroid/content/ContentResolver;");

    jfieldID f_android_id = env->GetStaticFieldID(c_settings_secure, "ANDROID_ID", "Ljava/lang/String;");

    jstring s_android_id = (jstring)env->GetStaticObjectField(c_settings_secure, f_android_id);

    jobject o_content_resolver = env->CallObjectMethod(getGlobalContext(env), m_get_content_resolver);

    jmethodID m_get_string = env->GetStaticMethodID(c_settings_secure, "getString",
                        "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");
    jstring android_id = (jstring)env->CallStaticObjectMethod(c_settings_secure,
                                            m_get_string, o_content_resolver, s_android_id);
    return android_id;
}

JNIEXPORT jstring JNICALL Java_com_logicway_grpcclient_lib_JNILib_getGsfId(JNIEnv *env, jobject thiz) {

    jclass c_uri = env->FindClass("android/net/Uri");
    jmethodID m_parse = env->GetStaticMethodID(c_uri, "parse", "(Ljava/lang/String;)Landroid/net/Uri;");
    jobject o_uri = env->CallStaticObjectMethod(c_uri, m_parse,
                                                env->NewStringUTF("content://com.google.android.gsf.gservices"));

    jobjectArray a_params = env->NewObjectArray(1, env->FindClass("java/lang/String"), env->NewStringUTF("android_id"));

    jclass c_context = env->FindClass("android/content/Context");
    jmethodID m_get_content_resolver = env->GetMethodID(c_context, "getContentResolver",
                                                        "()Landroid/content/ContentResolver;");
    jobject o_content_resolver = env->CallObjectMethod(getGlobalContext(env), m_get_content_resolver);

    jclass c_context_resolver = env->FindClass("android/content/ContentResolver");
    jmethodID m_query = env->GetMethodID(c_context_resolver, "query",
                                         "(Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;");
    jobject o_cursor = env->CallObjectMethod(o_content_resolver, m_query, o_uri, NULL, NULL, a_params, NULL);

    jclass c_cursor = env->FindClass("android/database/Cursor");
    jmethodID m_move_to_first = env->GetMethodID(c_cursor, "moveToFirst", "()Z");
    jboolean result = env->CallBooleanMethod(o_cursor, m_move_to_first);

    jmethodID m_get_string = env->GetMethodID(c_cursor, "getString", "(I)Ljava/lang/String;");
    jstring str = (jstring)env->CallObjectMethod(o_cursor, m_get_string, (jint)1);

    jclass c_long = env->FindClass("java/lang/Long");
    jmethodID m_parse_long = env->GetStaticMethodID(c_long, "parseLong", "(Ljava/lang/String;)J");
    jlong long_value = env->CallStaticLongMethod(c_long, m_parse_long, str);

    jmethodID m_to_hex_string = env->GetStaticMethodID(c_long, "toHexString", "(J)Ljava/lang/String;");
    jstring gsf_id = (jstring)env->CallStaticObjectMethod(c_long, m_to_hex_string, long_value);

    return gsf_id;
}

JNIEXPORT jstring JNICALL Java_com_logicway_grpcclient_lib_JNILib_getFingerprint(JNIEnv *env, jobject thiz) {

    jclass c_build = env->FindClass("android/os/Build");
    jfieldID f_fingerprint = env->GetStaticFieldID(c_build, "FINGERPRINT", "Ljava/lang/String;");
    jstring fingerprint = (jstring)env->GetStaticObjectField(c_build, f_fingerprint);
    return fingerprint;
}

JNIEXPORT jstring JNICALL Java_com_logicway_grpcclient_lib_JNILib_getBase64FromIdsHashes(JNIEnv *env, jobject thiz) {

    jobjectArray strings_to_hash = env->NewObjectArray(3, env->FindClass("java/lang/String"), env->NewStringUTF(""));

    env->SetObjectArrayElement(strings_to_hash, 0, Java_com_logicway_grpcclient_lib_JNILib_getAndroidId(env, thiz));
    env->SetObjectArrayElement(strings_to_hash, 1, Java_com_logicway_grpcclient_lib_JNILib_getGsfId(env, thiz));
    env->SetObjectArrayElement(strings_to_hash, 2, Java_com_logicway_grpcclient_lib_JNILib_getFingerprint(env, thiz));

    jclass c_message_digest = env->FindClass("java/security/MessageDigest");
    jmethodID m_get_instance = env->GetStaticMethodID(c_message_digest, "getInstance", "(Ljava/lang/String;)Ljava/security/MessageDigest;");
    jobject o_message_digest = env->CallStaticObjectMethod(c_message_digest, m_get_instance, env->NewStringUTF("MD5"));

    jclass c_string = env->FindClass("java/lang/String");
    jmethodID m_get_bytes = env->GetMethodID(c_string, "getBytes", "(Ljava/lang/String;)[B");

    jmethodID m_update = env->GetMethodID(c_message_digest, "update", "([B)V");

    for(int i = 0; i < env->GetArrayLength(strings_to_hash); i++) {
        env->CallVoidMethod(o_message_digest, m_update, env->CallObjectMethod(env->GetObjectArrayElement(strings_to_hash, i), m_get_bytes, env->NewStringUTF("UTF-8")));
    }

    jmethodID m_digest = env->GetMethodID(c_message_digest, "digest", "()[B");
    jbyteArray digest = (jbyteArray)env->CallObjectMethod(o_message_digest, m_digest);

    jclass c_base64 = env->FindClass("java/util/Base64");
    jclass c_base64_encoder = env->FindClass("java/util/Base64$Encoder");
    jmethodID m_get_encoder = env->GetStaticMethodID(c_base64, "getEncoder", "()Ljava/util/Base64$Encoder;");
    jobject o_encoder = env->CallStaticObjectMethod(c_base64, m_get_encoder);

    jmethodID m_encode_to_string = env->GetMethodID(c_base64_encoder, "encodeToString", "([B)Ljava/lang/String;");
    jstring result = (jstring)env->CallObjectMethod(o_encoder, m_encode_to_string, digest);

return result;
}