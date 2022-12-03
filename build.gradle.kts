plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "dev.chiroptical"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.1.2")
    implementation("cc.ekblad.konbini:konbini:0.1.3")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.4")
    testImplementation("io.kotest:kotest-property-jvm:5.5.4")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}