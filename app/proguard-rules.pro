# FocusLock ProGuard Rules

# Keep all application classes
-keepattributes *Annotation*
-keep class com.focuslock.** { *; }

# Gson specific rules
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-keepattributes Signature
-keepattributes *Annotation*

# Keep generic signature of Gson's TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Application classes that will be serialized/deserialized over Gson
-keep class com.focuslock.model.** { *; }
-keep class com.focuslock.data.** { *; }

# AndroidX and Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**

-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep setters in Views so that animations can still work
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# Keep Activity constructors
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimize and obfuscate
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
