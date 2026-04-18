# Seqaya app ProGuard rules.
# Keep Kotlinx Serialization metadata so @Serializable classes survive R8.
-keepattributes RuntimeVisible*Annotations,AnnotationDefault,InnerClasses

# Supabase + ktor rely on reflection over @Serializable DTOs.
-keep,includedescriptorclasses class com.seqaya.app.**$$serializer { *; }
-keepclassmembers class com.seqaya.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.seqaya.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit interfaces — keep generic signatures for converter.
-keepattributes Signature,Exceptions
-keep,allowobfuscation interface retrofit2.Call
-keep,allowobfuscation,allowshrinking interface * {
    @retrofit2.http.* <methods>;
}
