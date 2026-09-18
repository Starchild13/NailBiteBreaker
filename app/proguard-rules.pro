# NailBiteBreaker custom R8/ProGuard rules.

# ── General Android ────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

# ── Room Database ──────────────────────────────────────────────────────────────
# Room uses reflection to instantiate the generated database implementation.
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ── Coroutines ────────────────────────────────────────────────────────────────
# Prevent optimization of internal coroutine classes that use reflection.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-dontwarn kotlinx.coroutines.**

# ── RevenueCat ────────────────────────────────────────────────────────────────
# RevenueCat provides its own consumer ProGuard rules, but keeping these helps
# prevent issues with JSON mapping if custom models are added later.
-keep class com.revenuecat.purchases.** { *; }

# ── AdMob ─────────────────────────────────────────────────────────────────────
# Keep the GMS classes required for Ads to function.
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# ── Application Specific ───────────────────────────────────────────────────────
# Keep our Agent and Data models as they are often serialized or used with
# reflection in tests/Room.
-keep class com.nailbitebreaker.agents.** { *; }
-keep class com.nailbitebreaker.data.** { *; }

# Keep the Application class.
-keep class com.nailbitebreaker.NailBiteBreakerApplication { *; }

# Keep Compose-related classes if using @Preview in release (optional but safe).
-keep class androidx.compose.ui.tooling.** { *; }
