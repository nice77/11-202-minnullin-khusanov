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
    implementation(project(":core"))
    implementation("org.jsoup:jsoup:1.18.1")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}
