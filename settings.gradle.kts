pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven { url = uri("https://artifacts.itemis.cloud/repository/maven-mps/") }
        mavenCentral()
    }
}
plugins {
    id("de.fayard.refreshVersions") version "0.51.0"
}

rootProject.name = "modelix.core"

include("authorization")
include("light-model-client")
include("metamodel-export")
include("model-api")
include("model-api-gen")
include("model-api-gen-gradle")
include("model-api-gen-runtime")
include("model-client")
include("model-server")
include("model-server-api")
include("model-server-lib")
include("model-sync-lib")
include("mps-model-adapters")
include("mps-model-server-plugin")
include("ts-model-api")
