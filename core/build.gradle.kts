plugins {
    kotlin("jvm")
}

group = "ru.info.search"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api("io.ktor:ktor-client-core:3.4.0")
    api("io.ktor:ktor-client-content-negotiation:3.4.0")
    api("io.ktor:ktor-serialization-kotlinx-json-jvm:3.4.0")
    implementation("io.ktor:ktor-client-cio:3.4.0")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}