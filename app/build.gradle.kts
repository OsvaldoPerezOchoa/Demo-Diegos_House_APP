plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.ayzconsultores.diegoshouse"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ayzconsultores.diegoshouse"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    buildFeatures {
        dataBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase BOM para gestionar versiones de Firebase de manera centralizada
    implementation(platform(libs.firebase.bom.v3330))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.messaging)


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.core.splashscreen)
    implementation(libs.circleimageview)
    implementation(libs.play.services.auth)
    implementation(libs.shapeofview)
    implementation(libs.sdp.android)
    implementation(libs.lottie)
    implementation(libs.picasso)
    implementation(libs.dotsindicator)
    implementation(libs.core)
    implementation(libs.firebase.ui.firestore)
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation ("com.github.Foysalofficial:NafisBottomNav:5.0")
    implementation ("androidx.work:work-runtime-ktx:2.8.0")
    implementation ("com.google.guava:guava:30.1-android")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

}
