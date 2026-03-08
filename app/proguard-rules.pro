# FocusLock ProGuard Rules
-keepattributes *Annotation*
-keep class com.focuslock.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
