import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")
}

val sbVersion = providers.gradleProperty("sbVersion").get()

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

tasks.register<Jar>("sourcesJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.named("javadoc"))
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.strangerblocker"
            artifactId = "sb-engine-core"
            version = sbVersion
            from(components["java"])
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
            pom {
                name.set("Stranger Blocker Engine — Core")
                description.set("On-device spam-pattern learning and number rules for Android (no network).")
                url.set("https://github.com/khrlagst/stranger-call-blocker")
                licenses {
                    license {
                        name.set("Apache-2.0")
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
    repositories {
        // Set sbPublishUrl (+ credentials env) to publish; publishToMavenLocal works without it.
        providers.gradleProperty("sbPublishUrl").orNull?.let { publishUrl ->
            maven {
                name = "releases"
                url = uri(publishUrl)
            }
        }
    }
}

signing {
    isRequired = false
    if (providers.gradleProperty("signing.keyId").isPresent) {
        sign(publishing.publications["maven"])
    }
}
