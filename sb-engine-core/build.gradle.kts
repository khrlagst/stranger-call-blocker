import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

// Compile to JVM 17 bytecode using whatever JDK runs Gradle (no toolchain lookup).
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.strangerblocker"
            artifactId = "sb-engine-core"
            version = "2.1.1"
            from(components["java"])
            pom {
                name.set("Stranger Blocker Engine — Core")
                description.set("On-device spam-pattern learning and number rules for Android (no network).")
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
