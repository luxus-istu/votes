# ============================================================================
# Udmurt language app — R8 / ProGuard configuration
# ============================================================================

# Keep readable crash stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# ============================================================================
# Kotlinx Serialization
# Used by type-safe Compose Navigation routes (@Serializable route classes).
# R8 must not remove/rename the generated serializers, because navigation
# resolves them reflectively via `serializer<T>()`.
# ============================================================================

# Keep `Companion` object fields of serializable classes.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named).
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Generated serializers and the serializer interface itself
-keep,allowobfuscation,allowshrinking interface kotlinx.serialization.KSerializer
-keep,allowobfuscation,allowshrinking class *$$serializer { *; }
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.**$$serializer { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Navigation route classes: keep their names/serial names stable
-keep class ru.luxus.votes.navigation.** { *; }

# ============================================================================
# Koin (core + annotations + compiler-plugin generated hints)
# ============================================================================

-keep class org.koin.** { *; }
-keep class org.koin.plugin.hints.** { *; }
-keep class * extends org.koin.core.module.Module { *; }
-keep class * implements org.koin.core.component.KoinComponent { *; }

# Koin annotations used by the compiler plugin
-keepclassmembers class * {
    @org.koin.core.annotation.* <fields>;
    @org.koin.core.annotation.* <methods>;
}

# Application DI modules declared with @Module
-keep class ru.luxus.votes.core.di.** { *; }
-keep class ru.luxus.votes.root.RootModule { *; }

# ============================================================================
# Room 3 (KMP)
# The database implementation and its constructor are wired through the
# @ConstructedBy annotation; keep the generated implementations reachable.
# ============================================================================

-keep class * extends androidx.room3.RoomDatabase { *; }
-keep class * implements androidx.room3.RoomDatabaseConstructor { *; }
-keep class * extends androidx.room3.RoomOpenDelegate { *; }
-keepclassmembers @androidx.room3.Entity class * {
    <fields>;
}

# ============================================================================
# Kotlin / Compose
# Compose, lifecycle, navigation and coroutines ship their own consumer rules;
# only the Kotlin metadata needs to survive for libraries that read it.
# ============================================================================

-keep class kotlin.Metadata { *; }

# Keep annotated Kotlin classes used by Koin/navigation at runtime
-keep,allowobfuscation @kotlinx.serialization.Serializable class * { *; }

# ============================================================================
# Optional platform classes referenced by transitive dependencies
# ============================================================================

-dontwarn org.jetbrains.annotations.**
-dontwarn kotlinx.coroutines.debug.**
