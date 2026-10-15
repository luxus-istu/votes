# Preserve line numbers and source file attributes for crash stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Kotlin Serialization
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class *$$serializer { *; }
-keep,allowobfuscation,allowshrinking interface kotlinx.serialization.KSerializer

# Koin Dependency Injection
-keepclassmembers class * {
    @org.koin.core.annotation.* <fields>;
    @org.koin.core.annotation.* <methods>;
}

# SQLDelight Database
-keep class app.cash.sqldelight.** { *; }
-keep class ru.luxus.votes.db.** { *; }
