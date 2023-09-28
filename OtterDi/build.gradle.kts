plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.8.2")
    testImplementation("org.assertj:assertj-core:3.22.0")

    // reflection
    implementation(kotlin("reflect"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}