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

// Delegate assembleDebug and required lifecycle tasks to :app
tasks.register("assembleDebug") {
    dependsOn(":app:assembleDebug")
}

tasks.register("testDebugUnitTest") {
    dependsOn(":app:testDebugUnitTest")
}

tasks.register("testReleaseUnitTest") {
    dependsOn(":app:testDebugUnitTest")
}

tasks.register("lintDebug") {
    dependsOn(":app:lintDebug")
}

tasks.register("lintRelease") {
    dependsOn(":app:lintRelease")
}

tasks.register("bundleRelease") {
    dependsOn(":app:bundleRelease")
}
