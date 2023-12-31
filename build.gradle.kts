import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("com.google.protobuf") version "0.8.19"
    id("org.jetbrains.compose")
}

group = "ru.nsu.nettech"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.google.protobuf:protobuf-kotlin:3.24.3")

    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.6")

    runtimeOnly("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
}

val protobufGenPath = "build/generated/source/proto"

sourceSets {
    main {
        proto {
            srcDir("src/main/proto")
        }
        kotlin.srcDirs += File(protobufGenPath)
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "snake-game"
            packageVersion = "1.0.0"
        }
    }
}