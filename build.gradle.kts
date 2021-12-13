/*
 * EvergreenHUD - A mod to improve your heads-up-display.
 * Copyright (c) isXander [2019 - 2021].
 *
 * This work is licensed under the CC BY-NC-SA 4.0 License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0
 */

plugins {
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("fabric-loom") version "0.10.+"
    id("com.google.devtools.ksp") version "1.6.0-1.0.1"
    id("net.kyori.blossom") version "1.3.0"
    `java-library`
    java
}

group = "dev.isxander"
version = "2.0.0-pre13"

repositories {
    maven(url = "https://maven.fabricmc.net")
    maven(url = "https://repo.sk1er.club/repository/maven-public")
}

fun DependencyHandlerScope.includeApi(dep: Any) {
    api(dep)
    include(dep)
}

fun DependencyHandlerScope.includeModApi(dep: Any) {
    modApi(dep)
    include(dep)
}

dependencies {
    includeApi(project(":annotations"))
    ksp(project(":processor"))

    includeApi("org.bundleproject:libversion:0.0.2")
    includeApi("dev.isxander:settxi:2b6bef2ded")
    includeModApi("com.github.Chocohead:Fabric-ASM:v2.3")

    includeApi("com.electronwill.night-config:core:3.6.5")
    includeApi("com.electronwill.night-config:json:3.6.5")

    includeApi("io.ktor:ktor-client-gson:1.6.7")
    includeApi("io.ktor:ktor-client-core:1.6.7")
    includeApi("io.ktor:ktor-client-apache:1.6.7")

    minecraft("com.mojang:minecraft:1.18.1")
    mappings("net.fabricmc:yarn:1.18.1+build.2:v2")
    modImplementation("net.fabricmc:fabric-loader:0.12.+")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.44.0+1.18")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.7.0+kotlin.$kotlinVersion")
    modImplementation("io.ejekta:kambrik:3.0.0-1.18")
}

blossom {
    val evergreenClass = "src/main/kotlin/dev/isxander/evergreenhud/EvergreenHUD.kt"

    replaceToken("__GRADLE_NAME__", modName, evergreenClass)
    replaceToken("__GRADLE_ID__", modId, evergreenClass)
    replaceToken("__GRADLE_VERSION__", project.version, evergreenClass)
}

tasks {
    processResources {
        inputs.property("mod_id", modId)
        inputs.property("mod_name", modName)
        inputs.property("mod_version", project.version)

        filesMatching(listOf("fabric.mod.json", "bundle.project.json")) {
            expand(
                "mod_id" to modId,
                "mod_name" to modName,
                "mod_version" to project.version
            )
        }
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }

    tasks {
        withType<JavaCompile> {
            options.release.set(17)
        }

        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
}