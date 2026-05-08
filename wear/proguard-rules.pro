# GearOS proguard rules

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Wear
-keep class androidx.wear.** { *; }
-keep class com.google.android.horologist.** { *; }
-dontwarn androidx.wear.**

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-dontwarn dagger.hilt.**

# Moshi
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class *
-keepclassmembers @com.squareup.moshi.JsonClass class * {
    <init>(...);
    <fields>;
}
-dontwarn com.squareup.moshi.**

# Retrofit
-keep,allowshrinking,allowobfuscation,allowoptimization interface * {
    @retrofit2.http.* <methods>;
}
-keep class kotlin.coroutines.Continuation
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlin metadata
-keep class kotlin.Metadata { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# Tiles + Watchface — generované service classes
-keep class * extends androidx.wear.tiles.TileService { *; }
-keep class * extends androidx.wear.watchface.WatchFaceService { *; }
-keep class * extends androidx.wear.watchface.complications.datasource.ComplicationDataSourceService { *; }

# Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.**

# Timber
-dontwarn org.jetbrains.annotations.**
