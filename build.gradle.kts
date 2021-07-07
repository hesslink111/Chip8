import org.jetbrains.compose.compose

plugins {
    id("org.jetbrains.compose") version "0.5.0-build229"
    kotlin("jvm") version "1.5.10"
}

group = "me.will"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}

kotlin {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
    }
}
