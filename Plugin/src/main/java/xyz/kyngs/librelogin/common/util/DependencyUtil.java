/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.util;

import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import xyz.kyngs.librelogin.api.Logger;

import java.util.ArrayList;
import java.util.Collection;

public class DependencyUtil {

    public static void downloadDependencies(Logger logger, LibraryManager libraryManager, Collection<String> customRepositories, Collection<Library> customDependencies) {
        logger.info("Loading libraries...");

        //libraryManager.addMavenLocal();
        libraryManager.addMavenCentral();

        var repos = new ArrayList<>(customRepositories);

        repos.add("https://jitpack.io/");
        repos.add("https://mvn.exceptionflug.de/repository/exceptionflug-public/");
        repos.add("https://repo.kyngs.xyz/repository/maven-libraries/");

        repos.forEach(libraryManager::addRepository);

        var dependencies = new ArrayList<>(customDependencies);

        dependencies.add(Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version("5.0.1")
                .relocate("com{}zaxxer{}hikari", "xyz{}kyngs{}librelogin{}lib{}hikari")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}mariadb{}jdbc")
                .artifactId("mariadb-java-client")
                .version("3.1.4")
                .relocate("org{}mariadb", "xyz{}kyngs{}librelogin{}lib{}mariadb")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("com{}github{}ben-manes{}caffeine")
                .artifactId("caffeine")
                .version("3.1.1")
                .relocate("com{}github{}benmanes{}caffeine", "xyz{}kyngs{}librelogin{}lib{}caffeine")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}spongepowered")
                .artifactId("configurate-hocon")
                .version("4.1.2")
                .relocate("org{}spongepowered{}configurate", "xyz{}kyngs{}librelogin{}lib{}configurate")
                .relocate("io{}leangen{}geantyref", "xyz{}kyngs{}librelogin{}lib{}reflect")
                .relocate("com{}typesafe{}config", "xyz{}kyngs{}librelogin{}lib{}hocon")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}spongepowered")
                .artifactId("configurate-core")
                .version("4.1.2")
                .relocate("org{}spongepowered{}configurate", "xyz{}kyngs{}librelogin{}lib{}configurate")
                .relocate("io{}leangen{}geantyref", "xyz{}kyngs{}librelogin{}lib{}reflect")
                .relocate("com{}typesafe{}config", "xyz{}kyngs{}librelogin{}lib{}hocon")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("io{}leangen{}geantyref")
                .artifactId("geantyref")
                .relocate("io{}leangen{}geantyref", "xyz{}kyngs{}librelogin{}lib{}reflect")
                .version("1.3.13")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("com{}typesafe")
                .artifactId("config")
                .version("1.4.2")
                .relocate("com{}typesafe{}config", "xyz{}kyngs{}librelogin{}lib{}hocon")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("at{}favre{}lib")
                .artifactId("bcrypt")
                .version("0.9.0")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("dev{}samstevens{}totp")
                .artifactId("totp")
                .version("1.7.1")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("at{}favre{}lib")
                .artifactId("bytes")
                .version("1.5.0")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.40.1.0")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("commons-codec")
                .artifactId("commons-codec")
                .version("1.13")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("com{}google{}zxing")
                .artifactId("core")
                .version("3.4.0")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("com{}google{}zxing")
                .artifactId("javase")
                .version("3.4.0")
                .build()
        );
        dependencies.add(Library.builder()
                .groupId("org{}bouncycastle")
                .artifactId("bcprov-jdk18on")
                .version("1.73")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}postgresql")
                .artifactId("postgresql")
                .version("42.6.0")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("javax{}activation")
                .artifactId("activation")
                .version("1.1")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("com{}sun{}mail")
                .artifactId("javax.mail")
                .version("1.5.6")
                .build()
        );

        dependencies.add(Library.builder()
                .groupId("org{}apache{}commons")
                .artifactId("commons-email")
                .version("1.5")
                .build()
        );

        dependencies.forEach(libraryManager::loadLibrary);
    }

}
