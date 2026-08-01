pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StrangerCallBlocker"
include(":app")
include(":sb-engine-core")
include(":sb-engine-android")
include(":sample")
