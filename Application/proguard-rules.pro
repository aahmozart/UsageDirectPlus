# PrettyTime loads its translations by class name (org.ocpsoft.prettytime.i18n.Resources_xx)
-keep class org.ocpsoft.prettytime.i18n.** { *; }

# Keep line numbers for readable crash reports from F-Droid builds
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# TypedRecyclerView instantiates TypeHandler subclasses (its own and librariesDirect's)
# by reflection through their no-argument constructor
-keep class * extends godau.fynn.typedrecyclerview.TypeHandler {
    <init>();
}
