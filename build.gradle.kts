plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.secrets) apply false
    kotlin("jvm") version "2.2.10"
    application
}

group = "com.mrbikel"
version = "0.1.0"

repositories {
    google()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.mrbikel.kori.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

// Delegate assembleDebug to :app:assembleDebug for AI Studio emulator build pipeline
tasks.register("assembleDebug") {
    dependsOn(":app:assembleDebug")
}
