# Kushagra ReconX ProGuard / R8 rules
# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Keep entity/model classes used for reflection-based Room mapping
-keep class com.kushagra.reconx.database.entity.** { *; }
-keep class com.kushagra.reconx.models.** { *; }

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**

# Compose keeps handled automatically by the Android Gradle Plugin's
# built-in Compose rules; nothing extra required here.
