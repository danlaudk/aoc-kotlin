plugins {
    kotlin("jvm") version "1.7.21"
    application
    id("com.google.devtools.ksp") version "1.7.21-1.0.8"
    idea
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

    implementation(platform("io.arrow-kt:arrow-stack:1.1.3"))
    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-optics")
    ksp("io.arrow-kt:arrow-optics-ksp-plugin:1.1.3")

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

// Took this from https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code
idea {
   module {
      // Not using += due to https://github.com/gradle/gradle/issues/8749
      sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin") // or tasks["kspKotlin"].destination
      testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
      generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
   }
}