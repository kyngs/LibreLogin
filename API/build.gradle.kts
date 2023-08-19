plugins {
    id("java-library")
    id("maven-publish")
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    api("javax.annotation:javax.annotation-api:1.3.2")
    api("net.kyori:adventure-platform-bungeecord:4.1.2")
    compileOnly("com.google.guava:guava:30.0-jre")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            name = "kyngsRepo"
            url = uri(
                "https://repo.kyngs.xyz/" + (if (project.version.toString()
                        .contains("SNAPSHOT")
                ) "snapshots" else "releases") + "/"
            )
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