
plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    `maven-publish`
    id("com.palantir.git-version") version "0.13.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0" apply false
    id("com.diffplug.spotless") version "5.0.0" apply false
    id("com.dorongold.task-tree") version "2.1.0"
}

group = "org.modelix"
description = "Projectional Editor"
version = computeVersion()
println("Version: $version")

fun computeVersion(): Any {
    val versionFile = file("version.txt")
    val gitVersion: groovy.lang.Closure<String> by extra
    return if (versionFile.exists()) {
        versionFile.readText().trim()
    } else {
        gitVersion().let{ if (it.endsWith("-SNAPSHOT")) it else "$it-SNAPSHOT" }.also { versionFile.writeText(it) }
    }
}

subprojects {
    apply(plugin = "maven-publish")
    version = rootProject.version
    group = rootProject.group

    repositories {
        mavenLocal()
        maven { url = uri("https://artifacts.itemis.cloud/repository/maven-mps/") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
        mavenCentral()
    }

    publishing {
        repositories {
            if (project.hasProperty("artifacts.itemis.cloud.user")) {
                maven {
                    name = "itemis"
                    url = if (version.toString().contains("SNAPSHOT"))
                        uri("https://artifacts.itemis.cloud/repository/maven-mps-snapshots/")
                    else
                        uri("https://artifacts.itemis.cloud/repository/maven-mps-releases/")
                    credentials {
                        username = project.findProperty("artifacts.itemis.cloud.user").toString()
                        password = project.findProperty("artifacts.itemis.cloud.pw").toString()
                    }
                }
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask> {
    dependsOn(":ts-model-api:npm_run_build")
}