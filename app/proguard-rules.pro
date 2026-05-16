-keep class com.fundhelper.app.data.model.** { *; }
-keep class com.fundhelper.app.data.api.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <init>(...);
    @com.squareup.moshi.ToJson <init>(...);
}
