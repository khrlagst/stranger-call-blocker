import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Local signing credentials live in local.properties (gitignored). CI provides
// them as environment variables. No passwords are hardcoded in this file.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun signingValue(envName: String, propName: String, default: String? = null): String? =
    System.getenv(envName)?.takeIf { it.isNotBlank() }
        ?: localProps.getProperty(propName)?.takeIf { it.isNotBlank() }
        ?: default

android {
    namespace = "com.strangerblocker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.strangerblocker"
        minSdk = 29
        targetSdk = 35
        versionCode = 68
        versionName = "2.1.2"
    }

    signingConfigs {
        create("ci") {
            val storePassword = signingValue("CI_KEYSTORE_PASSWORD", "keystore.password")
            val keyPassword = signingValue("CI_KEY_PASSWORD", "keystore.password")
            if (storePassword == null || keyPassword == null) {
                throw GradleException(
                    "Signing password missing: set CI_KEYSTORE_PASSWORD/CI_KEY_PASSWORD " +
                        "or keystore.password in local.properties",
                )
            }
            storeFile = rootProject.file(
                signingValue("CI_KEYSTORE_PATH", "keystore.path", "ci.keystore")!!,
            )
            this.storePassword = storePassword
            keyAlias = signingValue("CI_KEY_ALIAS", "keystore.alias", "strangerblocker")!!
            this.keyPassword = keyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("ci")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("ci")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":sb-engine-android"))

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
