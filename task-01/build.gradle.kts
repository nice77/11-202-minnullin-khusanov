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

}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}