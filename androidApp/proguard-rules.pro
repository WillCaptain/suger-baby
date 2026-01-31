# 糖小稳 ProGuard 规则

# Keep Kotlin Metadata
-keep class kotlin.Metadata { *; }

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Compose
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# Keep Shared Module
-keep class com.twelfth.** { *; }
-keep interface com.twelfth.** { *; }
