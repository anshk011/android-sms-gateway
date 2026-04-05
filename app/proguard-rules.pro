# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * { @androidx.room.* <methods>; }

# App models
-keep class com.example.smsgateway.net.local.** { *; }
-keep class com.example.smsgateway.webhook.** { *; }
-keep class com.example.smsgateway.net.relay.** { *; }
