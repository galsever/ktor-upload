plugins {
    kotlin("jvm") version "2.2.20"
    `maven-publish`
}

group = "io.github.galsever"
version = "0.0.1"
description = "Ktor Upload to S3 Library"

dependencies {
    compileOnly("aws.sdk.kotlin:s3:1.0.50")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "com.github.galsever"
            artifactId = "ktor-upload"
            version = "0.0.1"

            pom {
                name.set("ktor-upload")
                description.set("Ktor Upload to S3 Library")
                url.set("https://github.com/galsever/ktor-upload")
            }
        }
    }
}