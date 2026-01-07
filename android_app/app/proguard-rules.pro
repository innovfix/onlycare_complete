# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Retrofit
-keep class retrofit2.** { *; }
-keepclassmembers,allowobfuscation class * {
  @retrofit2.http.* <methods>;
}

# Retrofit needs generic type information at runtime.
# Without these attributes, you can get:
# java.lang.Class cannot be cast to java.lang.reflect.ParameterizedType
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep data models
-keep class com.onlycare.app.domain.model.** { *; }

# Keep API DTOs (Gson reflection / @SerializedName)
-keep class com.onlycare.app.data.remote.dto.** { *; }

# Keep Retrofit service interfaces (extra safety; methods already kept via annotations above)
-keep class com.onlycare.app.data.remote.api.** { *; }

# Keep Agora SDK
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# Keep OneSignal
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**

# Keep Socket.IO client
-keep class io.socket.** { *; }
-dontwarn io.socket.**

# Keep Truecaller SDK
-keep class com.truecaller.** { *; }
-dontwarn com.truecaller.**
