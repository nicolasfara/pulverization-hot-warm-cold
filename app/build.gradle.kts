plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "it.nicolasfarabegoli.hotwarmcold"
    compileSdk = 33

    packagingOptions {
        resources.excludes += "META-INF/*.md"
    }

    defaultConfig {
        applicationId = "it.nicolasfarabegoli.hotwarmcold"
        minSdk = 31
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.coroutine.core)
    implementation(libs.coroutine.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.containtlayout)
    implementation(libs.androidx.lifecycle)
    implementation(libs.material)
    implementation(libs.pulverization.core)
    implementation(libs.pulverization.platform)
    implementation(libs.pulverization.rabbitmq)
    implementation(libs.koin.core)
    implementation(libs.beacon)
    implementation(project(":common"))
    implementation("com.github.weliem:blessed-android-coroutines:0.3.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
