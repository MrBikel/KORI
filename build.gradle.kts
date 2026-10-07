plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "com.mrbikel"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.mrbikel.kori.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
