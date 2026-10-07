# The WebView calls the annotated bridge method by its JavaScript name.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
