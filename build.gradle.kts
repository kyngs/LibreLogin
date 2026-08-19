plugins {
    //alias(libs.plugins.licenser)
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

    //configure<LicenseExtension> {
    //    header(rootProject.file("HEADER.txt"))
    //    include("**/*.java")
    //    newLine(true)
//
    //    matching("**/protocollib/EncryptionUtil.java", delegateClosureOf<LicenseProperties> {
    //        header(rootProject.file("licenses/FASTLOGIN_LICENSE"))
    //    })
    //}
}
