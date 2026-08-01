import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("maven-publish")
}

android {
    namespace = "com.strangerblocker.engine"
    compileSdk = 35

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

dependencies {
    // Core types (BlockPattern, SpamLabel, NumberRules…) are part of the public API.
    api(project(":sb-engine-core"))

    implementation("androidx.core:core-ktx:1.15.0")

    // Room DB + entities are part of the public API (AppDatabase, DAOs).
    val roomVersion = "2.6.1"
    api("androidx.room:room-runtime:$roomVersion")
    api("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.room:room-testing:$roomVersion")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.strangerblocker"
            artifactId = "sb-engine-android"
            version = "2.1.1"
            afterEvaluate {
                from(components["release"])
            }
            pom {
                name.set("Stranger Blocker Engine")
                description.set("On-device Android call/SMS spam-blocking engine — no account, no cloud, no data collection.")
                url.set("https://github.com/khrlagst/stranger-call-blocker")
                licenses {
                    license {
                        name.set("Apache-2.0 OR Commercial")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("khrlagst")
                        name.set("khrlagst")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/khrlagst/stranger-call-blocker.git")
                    developerConnection.set("scm:git:ssh://github.com/khrlagst/stranger-call-blocker.git")
                    url.set("https://github.com/khrlagst/stranger-call-blocker")
                }
            }
        }
    }
}
