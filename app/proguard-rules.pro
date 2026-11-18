# ==============================================================================
# HydroBuddy ProGuard / R8 Rules for Signed Release Build
# ==============================================================================

# 1. Filament (Google) - Native Rendering Engine & JNI
-keep class com.google.android.filament.** { *; }
-keepclassmembers class com.google.android.filament.** { *; }
-dontwarn com.google.android.filament.**

-keep class com.google.android.filament.gltfio.** { *; }
-keepclassmembers class com.google.android.filament.gltfio.** { *; }
-dontwarn com.google.android.filament.gltfio.**

-keep class com.google.android.filament.utils.** { *; }
-keepclassmembers class com.google.android.filament.utils.** { *; }
-dontwarn com.google.android.filament.utils.**

# 2. SceneView 2.2.1 - 3D SceneGraph, ModelNode, Camera, Lights, Loaders
-keep class io.github.sceneview.** { *; }
-keepclassmembers class io.github.sceneview.** { *; }
-dontwarn io.github.sceneview.**

# 3. Native JNI Methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# 4. Critical Type Attributes & Enums
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 5. HydroBuddy Data Models, Entities, Database & DataStore Preferences
-keep class com.hydrobuddy.app.data.model.** { *; }
-keep class com.hydrobuddy.app.data.database.** { *; }
-keep class com.hydrobuddy.app.data.config.** { *; }
-keep class com.hydrobuddy.app.data.datastore.** { *; }

# 6. KotlinX Coroutines & Android Lifecycle
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
