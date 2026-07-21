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

# Capacitor가 annotation/리플렉션으로 찾는 커스텀 플러그인을 유지한다.
-keep @com.getcapacitor.annotation.CapacitorPlugin class * { *; }
-keep class com.logfit.app.TimerPlugin { *; }

# 릴리스 빌드에서는 디버그/정보 로그 호출을 제거한다. 경고와 오류는 유지한다.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
