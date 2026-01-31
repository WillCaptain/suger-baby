plugins {
    // Android 插件
    id("com.android.application").version("8.2.0").apply(false)
    id("com.android.library").version("8.2.0").apply(false)
    
    // Kotlin 插件
    kotlin("android").version("1.9.20").apply(false)
    kotlin("multiplatform").version("1.9.20").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
