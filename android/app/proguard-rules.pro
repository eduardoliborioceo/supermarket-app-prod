# Add project specific ProGuard rules here.
# Kotlinx Serialization keeps its generated serializers via annotations already
# understood by R8's default Kotlin rules bundled with the Kotlin Gradle plugin.

-keepattributes *Annotation*, InnerClasses
-keepclassmembers class **$Companion {
    *;
}
