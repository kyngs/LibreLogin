plugins {
    id("java-library")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.javax.annotation)
    compileOnly(libs.guava)
    compileOnly(libs.adventure.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            name = "kyngsRepo"
            val channel = if (project.version.toString().contains("SNAPSHOT")) "snapshots" else "releases"
            url = uri("https://repo.kyngs.xyz/$channel/")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
