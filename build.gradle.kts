import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    alias(libs.plugins.spotless) apply false
}

defaultTasks("updateLicenses", "shadowJar")

version = "0.25.0-SNAPSHOT"

subprojects {
    version = rootProject.version
    group = "xyz.kyngs.librelogin"

    plugins.withType<JavaPlugin> {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    pluginManager.apply("com.diffplug.spotless")

    configure<SpotlessExtension> {
        java {
            targetExclude("**/generated/**")
            licenseHeaderFile(rootProject.file("HEADER.txt"))
            removeUnusedImports()
            endWithNewline()
        }
    }
}
