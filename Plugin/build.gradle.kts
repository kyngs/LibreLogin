import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java`
    alias(libs.plugins.shadow)
    alias(libs.plugins.blossom)
    alias(libs.plugins.librarian)
    alias(libs.plugins.mcupload)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/")
    maven("https://repo.kyngs.xyz/public/")
    maven("https://mvn.exceptionflug.de/repository/exceptionflug-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.alessiodp.com/releases/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io/")
}

dependencies {
    // API
    implementation(project(":API"))

    // Velocity
    annotationProcessor(libs.velocity.api)
    compileOnly(libs.velocity.api)
    compileOnly(libs.velocity.proxy)

    // MySQL
    librarian(libs.mariadb)
    librarian(libs.hikari)

    // SQLite
    librarian(libs.sqlite)

    // PostgreSQL
    librarian(libs.postgresql)

    // ACF
    librarian(libs.bundles.acf)

    // Utils
    librarian(libs.caffeine)
    librarian(libs.configurate.hocon)
    librarian(libs.bcrypt)
    librarian(libs.totp)
    compileOnly(libs.protocolize.api)
    librarian(libs.bouncycastle)
    librarian(libs.commons.email)
    librarian(libs.adventure.minimessage)
    librarian(libs.legacymessage)

    // Geyser
    compileOnly(libs.floodgate.api)
    // LuckPerms
    compileOnly(libs.luckperms.api)

    // BungeeCord
    compileOnly(libs.redisbungee)

    // bStats
    librarian(libs.bundles.bstats)

    // Paper
    compileOnly(libs.paper.api)
    implementation(libs.packetevents.spigot)
    compileOnly(libs.netty.transport)
    compileOnly(libs.datafixerupper) // I hate this so much
    compileOnly(libs.log4j.core)

    // Libby
    implementation(libs.bundles.librarian)

    // NanoLimboPlugin
    compileOnly(libs.nanolimbo.api)
}

tasks.withType<ProcessResources> {
    outputs.upToDateWhen { false }
    filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
        expand(mapOf("version" to version))
    }
}

blossom {
    replaceToken("@version@", version)
}


// Dependencies pulled in transitively that we never want shaded or downloaded at runtime.
val excludedLibs = listOf(
    "org.slf4j:.*:.*",
    "org.checkerframework:.*:.*",
    "com.google.errorprone:.*:.*",
    "com.google.protobuf:.*:.*",
)

tasks.withType<ShadowJar> {
    archiveFileName.set("LibreLogin.jar")

    dependencies {
        excludedLibs.forEach { exclude(dependency(it)) }
    }

    relocate("co.aikar.acf", "xyz.kyngs.librelogin.lib.acf")
    relocate("com.github.benmanes.caffeine", "xyz.kyngs.librelogin.lib.caffeine")
    relocate("com.typesafe.config", "xyz.kyngs.librelogin.lib.hocon")
    relocate("com.zaxxer.hikari", "xyz.kyngs.librelogin.lib.hikari")
    relocate("org.mariadb", "xyz.kyngs.librelogin.lib.mariadb")
    relocate("org.bstats", "xyz.kyngs.librelogin.lib.metrics")
    relocate("org.intellij", "xyz.kyngs.librelogin.lib.intellij")
    relocate("org.jetbrains", "xyz.kyngs.librelogin.lib.jetbrains")
    relocate("io.leangen.geantyref", "xyz.kyngs.librelogin.lib.reflect")
    relocate("org.spongepowered.configurate", "xyz.kyngs.librelogin.lib.configurate")
    relocate("xyz.kyngs.librarian", "xyz.kyngs.librelogin.lib.librarian")
    relocate("org.postgresql", "xyz.kyngs.librelogin.lib.postgresql")
    relocate("com.github.retrooper.packetevents", "xyz.kyngs.librelogin.lib.packetevents.api")
    relocate("io.github.retrooper.packetevents", "xyz.kyngs.librelogin.lib.packetevents.platform")
}

tasks.withType<Jar> {
    from("../LICENSE.txt")
}

librarian {
    excludedLibs.forEach { excludeDependency(it) }

    // Often redeploys the same version, so calculating checksum causes false flags
    noChecksumDependency("com.github.retrooper.packetevents:.*:.*")
}


mcupload {
    file = tasks.shadowJar
    swallowErrors = true
    platforms {
        modrinth {
            loaders = listOf("paper", "purpur", "bungeecord", "waterfall", "velocity")
            projectId = "tL0SCXYq"
            gameVersions = listOf(
                "1.21.8", "1.21.7", "1.21.6", "1.21.5", "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21",
                "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20",
                "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19",
                "1.18.2", "1.18.1", "1.18",
                "1.17.1", "1.17",
                "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1", "1.16",
                "1.15.2", "1.15.1", "1.15",
                "1.14.4", "1.14.3", "1.14.2", "1.14.1", "1.14",
                "1.13.2", "1.13.1", "1.13",
            )
            token = System.getenv("MODRINTH_TOKEN")
        }
        /*polymart {
            apiKey = System.getenv("POLYMART_TOKEN")
            resourceId = "2179"
        }*/
        github {
            token = System.getenv("GITHUB_TOKEN")
            repository = "kyngs/LibreLogin"
        }
        discord {
            webhookUrl = System.getenv("DISCORD_WEBHOOK_URL")
            configureEmbed {
                setColor(0x0398FC)
            }
        }
    }
    datasource {
        file {
            readmeFile = "README.md"
            changelogFile = "CHANGELOG.md"
        }
    }
}
