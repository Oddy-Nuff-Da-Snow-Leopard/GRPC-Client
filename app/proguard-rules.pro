# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Don't show logs
-assumenosideeffects class android.util.Log {
   public static *** v(...);
  # public static *** d(...);
   public static *** i(...);
   public static *** w(...);
  # public static *** e(...);
}

# Don't proguard AdMob classes
-keep public class com.google.android.gms.ads.* {
    public *;
}

-keep public class com.google.ads.* {
    public *;
}

# Simple solution, need to go further
#-keep public class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Pretty solution, just keep all proto message fields, works fine
-keepclassmembers class com.logicway.grpcclient.filedownload.Command {
    int commandName_;
}
-keepclassmembers class com.logicway.grpcclient.filedownload.FileDownloadRequest {
    java.lang.String url_;
}
-keepclassmembers class com.logicway.grpcclient.filedownload.DataChunk {
    com.google.protobuf.ByteString data_;
}
-keepclassmembers class com.logicway.grpcclient.filedownload.CollectionElement {
    int value_;
}

# Миша, указанные ниже правила нужны только для того, чтобы вызывать классы,
# в которых используется okhttp (GenerateRandomCommandTask и FileDownloadTask из
# пакета ...asynctask.okhttp). Эти классы удаляются
# после того, как они были помещены в jar архив, из которого потом был сделан dex файл,
# помещенный в папку assets. Так как эти файлы не попадают в apk, то и пакеты из okhttp туда
# не попадают, их убирает прогуард

#-keep class okhttp3.OkHttpClient$Builder { public *; }
#-keep class okhttp3.Protocol { public *; }
#-keep class okhttp3.Request$Builder { public *; }
#-keep class okhttp3.RequestBody { public *; }
#-keep class okhttp3.OkHttpClient { public *; }
#-keep class okhttp3.Call { public *; }
#-keep class okhttp3.Response { public *; }
#-keep class okhttp3.ResponseBody { public *; }
#
#-keepclassmembers class com.logicway.grpcclient.App {
#   public static getAppContext();
#}
#
#-keep class com.logicway.grpcclient.filedownload.FileDownloadRequest$Builder { public *; }
#-keep class com.google.protobuf.GeneratedMessageLite { public *; }
#-keep class com.logicway.grpcclient.filedownload.FileDownloadRequest { public static newBuilder(); }
#
#-keepclassmembers class com.logicway.grpcclient.filedownload.Command { public *; }
#-keepclassmembers class com.logicway.grpcclient.filedownload.CommandName { public *; }
#-keep class okio.BufferedSource { public *; }
#-keepclassmembers class com.logicway.grpcclient.filedownload.DataChunk { public *; }
#-keep class com.google.protobuf.ByteString { public *; }

