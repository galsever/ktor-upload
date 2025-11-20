plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.3.2"
}

group = "org.srino"
version = "0.0.1"

dependencies {
    implementation("aws.sdk.kotlin:s3:1.0.50")
}
